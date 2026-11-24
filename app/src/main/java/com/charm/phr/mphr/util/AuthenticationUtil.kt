package com.charm.phr.mphr.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.charm.phr.mphr.BuildConfig
import com.charmhealth.auth.AuthState
import com.charmhealth.auth.CharmAuthAndroid
import com.charmhealth.auth.CharmAuthClient
import com.charmhealth.auth.CharmAuthConfig
import com.charmhealth.auth.CharmAuthException
import com.charmhealth.auth.CharmAuthInterceptor
import com.charmhealth.auth.CharmAuthLogger
import com.charmhealth.auth.CustomTabsAuthBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

/**
 * The only place the app uses the CharmHealth sign-in SDK (charm-mobile-auth-sdk,
 * `com.charmhealth.auth`). Everything else calls these functions and gets app-level results
 * ([SignInResult], [AuthFailure]), never SDK types.
 *
 * Settings come from BuildConfig (CHARM_AUTH_*, set in secure.properties). The redirect URI is
 * `<applicationId>:/oauth2/callback`; app/build.gradle registers the same scheme in the manifest.
 */
object AuthenticationUtil {

    private const val TAG = "AuthenticationUtil"

    /**
     * Header a request carries to be sent without the access token; [apiInterceptor] removes it.
     */
    const val HEADER_SKIP_AUTH = "X-Charm-Skip-Auth"

    /** Scope for sign-in work that outlives a screen (token fetches, sign-out). */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @Volatile
    private var client: CharmAuthClient? = null

    @Volatile
    private var configError: CharmAuthException.Misconfigured? = null

    private var restored: Deferred<AuthState>? = null

    // region Setup

    /** Creates the client and loads the stored sign-in. Call at app start; safe to call again. */
    @Synchronized
    fun init(context: Context) {
        if (client != null || configError != null) return
        val appContext = context.applicationContext
        try {
            val redirectScheme = appContext.packageName
            val config = CharmAuthConfig(
                // CHIS is the authorization server: PAR, PKCE and DPoP are always on.
                issuer = BuildConfig.CHARM_AUTH_ISSUER,
                clientId = BuildConfig.CHARM_AUTH_CLIENT_ID,
                redirectUri = "$redirectScheme:/oauth2/callback",
                postLogoutRedirectUri = "$redirectScheme:/oauth2/logout",
                audience = BuildConfig.CHARM_AUTH_AUDIENCE.ifBlank { null },
                additionalScopes = BuildConfig.CHARM_AUTH_ADDITIONAL_SCOPES.split(' ').filter { it.isNotBlank() },
                apiOrigins = BuildConfig.CHARM_AUTH_API_ORIGINS.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
            )
            val created = CharmAuthClient(
                config,
                CharmAuthAndroid.platform(appContext),
                logger = { level, message ->
                    if (level == CharmAuthLogger.Level.WARN) Log.w(TAG, message) else Log.d(TAG, message)
                },
            )
            client = created
            restored = scope.async { created.restore() }
        } catch (e: CharmAuthException.Misconfigured) {
            configError = e
            Log.e(TAG, e.message ?: "CharmAuth misconfigured")
        }
    }

    /** The client; throws [CharmAuthException.Misconfigured] if the build's settings are wrong. */
    private fun requireClient(): CharmAuthClient =
        client ?: throw (configError ?: IllegalStateException("AuthenticationUtil.init() not called"))

    // endregion

    // region Session

    /** True once the stored sign-in has loaded and the user is signed in. */
    suspend fun awaitSignedIn(): Boolean {
        if (configError != null) return false
        val state = restored?.await() ?: client?.state?.value
        return state is AuthState.SignedIn
    }

    /**
     * Calls [onSessionEnded] each time the server ends the sign-in (revoked, user deactivated or
     * blocked). Not called for the app's own sign-out.
     */
    fun observeSessionEnded(onSessionEnded: () -> Unit) {
        val c = client ?: return
        scope.launch {
            c.state.collect { state ->
                if (state is AuthState.SignedOut && state.sessionEnded) onSessionEnded()
            }
        }
    }

    // endregion

    // region Sign-in / sign-out

    /** Signs in through CHIS's page in a Custom Tab; suspends until the user finishes. */
    suspend fun signIn(activity: Activity): SignInResult = try {
        requireClient().signIn(CustomTabsAuthBrowser(activity))
        SignInResult.Success
    } catch (e: CharmAuthException.Cancelled) {
        SignInResult.Cancelled
    } catch (e: CharmAuthException.SignInInProgress) {
        SignInResult.AlreadyInProgress
    } catch (e: Exception) {
        Log.d(TAG, "sign-in failed -> ${e.javaClass.simpleName}: ${e.message}")
        SignInResult.Failed(failureOf(e), e.message)
    }

    /**
     * Revokes the refresh token at the server, then deletes the local session (even offline).
     * Sign-in tabs are ephemeral where the browser supports it, so no browser sign-out is needed.
     * Returns false if revoking failed (the local session is deleted anyway).
     */
    suspend fun signOut(): Boolean = try {
        client?.signOut()
        true
    } catch (e: Exception) {
        Log.d(TAG, "sign-out failed: ${e.javaClass.simpleName}")
        false
    }

    // endregion

    // region Tokens and API calls

    /**
     * Checks that a valid access token is available (refreshing it if needed). Returns null when
     * it is, otherwise why not.
     */
    suspend fun ensureAccessToken(): AuthFailure? = try {
        requireClient().accessToken()
        null
    } catch (e: Exception) {
        Log.d(TAG, "token unavailable: ${e.javaClass.simpleName}")
        failureOf(e)
    }

    /**
     * OkHttp interceptor for the app's API client: adds the access token (bearer header, one
     * refresh and retry on 401) to calls to the configured API origins only. Image and
     * third-party URLs (e.g. payment gateways) are sent without it, as are requests marked with
     * [HEADER_SKIP_AUTH].
     */
    fun apiInterceptor(): Interceptor = ApiInterceptor()

    private class ApiInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (request.header(HEADER_SKIP_AUTH) != null) {
                return chain.proceed(request.newBuilder().removeHeader(HEADER_SKIP_AUTH).build())
            }
            val c = client
            if (c == null || !c.isApiUrl(request.url.toString())) {
                return chain.proceed(request)
            }
            return CharmAuthInterceptor(c).intercept(chain)
        }
    }

    // endregion

    // region Results

    /** Why signing in or getting a token failed, in terms the app acts on. */
    enum class AuthFailure {
        /** The session is gone (ended on the server, not signed in, refused by the API): sign in again. */
        SESSION_ENDED,
        /** The account is blocked or not allowed to sign in. */
        ACCOUNT_BLOCKED,
        /** Network problem; the session is kept. */
        NETWORK,
        /** CharmAuth settings missing or wrong in this build. */
        MISCONFIGURED,
        /** Anything else (server error, expired sign-in page, rate limit, security check). */
        OTHER;

        /** True when the user has to sign in again. */
        val requiresSignIn: Boolean get() = this == SESSION_ENDED || this == MISCONFIGURED
    }

    sealed class SignInResult {
        object Success : SignInResult()
        object Cancelled : SignInResult()
        object AlreadyInProgress : SignInResult()
        data class Failed(val failure: AuthFailure, val message: String?) : SignInResult()
    }

    private fun failureOf(e: Exception): AuthFailure = when (e) {
        is CharmAuthException.SessionEnded,
        is CharmAuthException.NotSignedIn,
        is CharmAuthException.AccessDenied -> AuthFailure.SESSION_ENDED
        is CharmAuthException.AuthorizationDenied ->
            if (e.error == "access_denied" || e.error == "unauthorized") AuthFailure.ACCOUNT_BLOCKED else AuthFailure.OTHER
        is CharmAuthException.Network -> AuthFailure.NETWORK
        is CharmAuthException.Misconfigured, is IllegalStateException -> AuthFailure.MISCONFIGURED
        else -> AuthFailure.OTHER
    }

    // endregion
}
