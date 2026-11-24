package com.charm.phr.mphr.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import androidx.work.Configuration
import coil.ImageLoader
import coil.disk.DiskCache
import com.charm.phr.mphr.BuildConfig
import com.charm.phr.mphr.MPHRApplication
import com.charm.phr.mphr.model.database.mPHRDB
import com.charm.phr.mphr.service.MDBaseWorkerFactory
import com.charm.phr.mphr.util.AuthManager
import com.charm.phr.mphr.util.AuthenticationUtil
import com.charm.phr.mphr.util.MDAccountsRelatedUtil
import com.charm.phr.mphr.util.ModelUtil
import com.charm.phr.mphr.util.WebService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    companion object {
        private const val NAME_BASE_URL = "NAME_BASE_URL"
        private const val OK_HTTP_CLIENT_AUTHENTICATED = "OK_HTTP_CLIENT_AUTHENTICATED"
    }

    @Provides
    @Singleton
    fun provideContext(app: MPHRApplication): Context = app.applicationContext

    @Provides
    @Singleton
    fun provideSharedPreferences(app: MPHRApplication): SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    @Provides
    fun provideExecutor(): Executor = Executors.newCachedThreadPool()

    //region Remote

    @Provides
    @Named(NAME_BASE_URL)
    fun provideBaseUrlString() = MDAccountsRelatedUtil.baseUrl + "/"

    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    @Provides
    @Singleton
    fun provideImageLoader(
        @Named(OK_HTTP_CLIENT_AUTHENTICATED) okHttpClient: OkHttpClient,
        context: Context
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 2% of the app's available disk space
                    .build()
            }
            .build()
    }

    @Provides
    @Named(OK_HTTP_CLIENT_AUTHENTICATED)
    fun provideAuthenticatedOkHttpClient(authManager: AuthManager): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(ModelUtil.connectionTimeOut, TimeUnit.SECONDS)
            .readTimeout(ModelUtil.readTimeOut, TimeUnit.SECONDS)
            .writeTimeout(ModelUtil.writeTimeOut, TimeUnit.SECONDS)
            .addInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val requestBuilder = chain.request().newBuilder()
                    requestBuilder.addHeader("app_user_agent", "Android_mPHR_${BuildConfig.VERSION_CODE}")
                    val skipAuth = when {
                        authManager.isBasic -> { authManager.isBasic = false; true }
                        authManager.skipAuth -> { authManager.skipAuth = false; true }
                        else -> false
                    }
                    if (skipAuth) requestBuilder.header(AuthenticationUtil.HEADER_SKIP_AUTH, "1")
                    return chain.proceed(requestBuilder.build())
                }
            })
            .addInterceptor(AuthenticationUtil.apiInterceptor())
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, @Named(OK_HTTP_CLIENT_AUTHENTICATED) okHttpClient: OkHttpClient, @Named(NAME_BASE_URL) baseUrl: String): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideWebService(retrofit: Retrofit): WebService = retrofit.create(WebService::class.java)

    @Provides
    @Singleton
    fun providePicasso(@Named(OK_HTTP_CLIENT_AUTHENTICATED) okHttpClient: OkHttpClient, appContext: Context): Picasso {
        val builder = Picasso.Builder(appContext)
        builder.downloader(OkHttp3Downloader(okHttpClient))
        return builder.build()
    }

    //endregion

    @Singleton
    @Provides
    fun provideDatabase(context: Context): mPHRDB {
        return mPHRDB.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideWorkManagerConfiguration(
        mdBaseWorkerFactory: MDBaseWorkerFactory
    ): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(mdBaseWorkerFactory)
            .build()
    }

}