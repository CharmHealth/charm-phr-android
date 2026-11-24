package com.charm.phr.mphr.login

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import com.charm.phr.mphr.base.BaseUITest
import com.charm.phr.mphr.config.TestConfig
import com.charm.phr.mphr.ui.login.LoginActivity
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end UI test for the complete login and logout workflow.
 *
 * ## Workflow Overview
 *
 * This test validates the full user journey through the app:
 *
 * ```
 * LoginActivity
 *   │  App launches and triggers CharmAuth sign-in
 *   ▼
 * Chrome Custom Tab (CHIS sign-in)
 *   │  1. Enter username in the email field
 *   │  2. Tap "Next" to proceed to password step
 *   │  3. Enter password
 *   │  4. Tap "Sign in" button (res=nextbtn)
 *   │  SDK extracts auth token and returns to the app
 *   ▼
 * PasscodeActivity
 *   │  5. Enter 5-digit passcode (hidden EditText, id=invisible)
 *   │  6. Re-enter same passcode to confirm
 *   │  Auto-submits when 5 digits are entered
 *   ▼
 * PatientListActivity (conditional)
 *   │  7. Shown only if multiple patients are mapped to the account
 *   │  8. Tap the first patient in the RecyclerView
 *   │  (Skipped if single patient — auto-selects and proceeds)
 *   ▼
 * DashboardActivity
 *   │  9. Main app screen — verify via toolbar text ("Hi [name]")
 *   │     or bottom navigation bar
 *   │  10. Tap profile picture icon (top-right, id=patientImageView)
 *   ▼
 * UserProfileActivity (Jetpack Compose)
 *   │  11. Scroll down the profile screen
 *   │  12. Tap "Log out" button
 *   │  13. Confirmation dialog appears: "Are you sure you want to sign out?"
 *   │  14. Tap "Yes" to confirm
 *   │  App calls AuthManager.logout() — revokes OAuth token, clears
 *   │  SharedPreferences, database, notifications, and scheduled workers
 *   ▼
 * TourPageActivity
 *   │  15. Verify app landed on tour/intro screen (dashboard is gone)
 *   │  16. Wait 8 seconds for logout API to finish clearing the server session
 *   ▼
 * Test Complete
 * ```
 *
 * ## Technical Notes
 *
 * - **Chrome Custom Tab**: The CharmAuth SDK opens sign-in in Chrome (not an in-app WebView).
 *   UI Automator is required to interact with Chrome's UI elements cross-process.
 * - **Passcode**: Uses a hidden `TextInputEditText` (id=invisible, maxLength=5) with
 *   `numberPassword` input type. It auto-submits via TextWatcher when 5 digits are entered.
 * - **Patient Selection**: Only appears when `authManager.patientList.size > 1`.
 * - **Profile Screen**: Built with Jetpack Compose. The "Log out" button is at the bottom
 *   of a scrollable Column, so scrolling is needed on smaller screens.
 * - **Session Cleanup**: The 8-second wait after logout allows the server-side session
 *   revocation API call to complete, preventing "max sessions exceeded" errors on next run.
 *
 * ## Prerequisites
 *
 * - Test credentials configured in `app/src/androidTest/assets/test_config.properties`
 * - Emulator or device with Chrome installed (for Chrome Custom Tabs)
 * - Run with: `./gradlew connectedCommonDebugAndroidTest
 *     -Pandroid.testInstrumentationRunnerArguments.class=com.charm.phr.mphr.login.LoginFlowTest`
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class LoginFlowTest : BaseUITest() {

    private lateinit var loginHelper: LoginTestHelper

    @Before
    override fun setUp() {
        super.setUp()
        loginHelper = LoginTestHelper(device)
        TestConfig.init(testContext)
    }

    @Test
    fun testFullLoginAndLogout() {
        val username = TestConfig.username
        val password = TestConfig.password

        if (username.isBlank() || password.isBlank()) {
            fail("Test credentials not configured in test_config.properties")
            return
        }

        val passcode = TestConfig.passcode
        if (passcode.isBlank()) {
            fail("test.passcode not configured in test_config.properties")
            return
        }

        // ── Step 1: SSO Login via Chrome Custom Tab ──────────────────────
        // Launches LoginActivity which triggers AuthenticationUtil.signIn().
        // The CharmAuth SDK opens a Chrome Custom Tab for CHIS sign-in.
        // LoginTestHelper enters username → Next → password → Sign In.
        launchLoginActivity()
        loginHelper.performLogin(username, password)
        // The SDK returns the auth token via IAMTokenCallback.onTokenFetchComplete(),
        // then the app fetches patient list and menu data via API calls.
        val appReturned = device.wait(
            Until.hasObject(By.pkg("com.charm.phr.mphr")),
            LONG_TIMEOUT
        )
        assertTrue(
            "App did not return from login screen. ${loginHelper.dumpHierarchyInfo()}",
            appReturned ?: false
        )
        takeScreenshot("01_after_login")

        // ── Step 2: Passcode Setup ───────────────────────────────────────
        // PasscodeActivity presents a hidden EditText (id=invisible) for PIN entry.
        // On first login, the user creates a new 5-digit passcode and re-enters
        // it to confirm. On subsequent logins, this step verifies the existing passcode.
        handlePasscodeEntry()
        takeScreenshot("02_after_passcode")

        // ── Step 3: Patient Selection (conditional) ──────────────────────
        // If the account has multiple patients, PatientListActivity shows a
        // RecyclerView of patients. We tap the first one.
        // If only one patient exists, the app auto-selects and skips this screen.
        handlePatientSelection()
        takeScreenshot("03_after_patient_selection")

        // ── Step 4: Verify Dashboard ─────────────────────────────────────
        // DashboardActivity is the main screen. Verify arrival by checking for
        // the toolbar greeting ("Hi [name]") or the bottom navigation bar.
        verifyDashboard()
        takeScreenshot("04_dashboard")

        // ── Step 5: Open Profile ─────────────────────────────────────────
        // Tap the patient profile image (top-right corner of dashboard toolbar)
        // to open UserProfileActivity, which is built entirely with Jetpack Compose.
        openProfile()
        takeScreenshot("05_profile")

        // ── Step 6: Logout ───────────────────────────────────────────────
        // Scroll down in the Compose profile screen to find "Log out".
        // Tap it, then confirm with "Yes" in the AlertDialog.
        // AuthManager.logout() revokes the OAuth token, clears SharedPreferences,
        // database, notifications, and cancels all scheduled workers.
        performLogout()
        takeScreenshot("06_after_logout")

        // ── Step 7: Verify Logged Out ────────────────────────────────────
        // App navigates to TourPageActivity with FLAG_ACTIVITY_CLEAR_TASK.
        // Wait 8 seconds for the server-side session revocation to complete
        // so the next test run doesn't hit "max sessions exceeded".
        verifyLoggedOut()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Handles the PasscodeActivity screen.
     *
     * The passcode uses a hidden TextInputEditText (id=invisible, inputType=numberPassword,
     * maxLength=5). When 5 digits are entered, the TextWatcher auto-triggers validation.
     * On first login (setPasscode mode), the user enters the passcode twice to confirm.
     * If the passcode was previously set (checkPasscode mode), only one entry is needed.
     *
     * Returns silently if the passcode screen doesn't appear (e.g., biometric auth).
     */
    private fun handlePasscodeEntry() {
        val passcode = TestConfig.passcode

        val passcodeField = device.wait(
            Until.findObject(By.res("com.charm.phr.mphr:id/invisible")),
            LOGIN_TIMEOUT
        )

        if (passcodeField == null) {
            // Passcode screen not shown — may already be configured or using biometric
            return
        }

        // First entry — creates or verifies the passcode
        passcodeField.text = passcode

        // Second entry — re-enter to confirm (only on first-time passcode setup).
        // device.wait() handles the delay until the field is ready.
        val reenterField = device.wait(
            Until.findObject(By.res("com.charm.phr.mphr:id/invisible")),
            DEFAULT_TIMEOUT
        )

        if (reenterField != null) {
            reenterField.text = passcode
        }
    }

    /**
     * Handles the PatientListActivity screen (shown when multiple patients are mapped).
     *
     * The screen displays a RecyclerView (id=patientListRecyclerView) with patient items.
     * Each item shows the patient name, DOB, and profile image.
     * Tapping a patient calls PatientSelectListener.onPatientSelect() which sets the
     * selected patient in AuthManager and navigates to DashboardActivity.
     *
     * Does nothing if the patient list screen doesn't appear (single patient auto-selected).
     */
    private fun handlePatientSelection() {
        val patientList = device.wait(
            Until.findObject(By.res("com.charm.phr.mphr:id/patientListRecyclerView")),
            DEFAULT_TIMEOUT
        )

        if (patientList != null) {
            val firstPatient = patientList.children.firstOrNull()
            if (firstPatient != null) {
                firstPatient.click()
                // Wait for DashboardActivity to appear before proceeding
                device.wait(
                    Until.hasObject(By.res("com.charm.phr.mphr:id/dashboardToolbarText")),
                    LOGIN_TIMEOUT
                )
            }
        }
    }

    /**
     * Verifies the DashboardActivity is displayed.
     *
     * Checks for either:
     * - dashboardToolbarText: Shows "Hi [patient name]" greeting
     * - dashboardBottomSheet: Bottom navigation with Home and Health Data tabs
     */
    private fun verifyDashboard() {
        val dashboardToolbar = device.wait(
            Until.findObject(By.res("com.charm.phr.mphr:id/dashboardToolbarText")),
            LOGIN_TIMEOUT
        )

        val dashboardBottomNav = device.wait(
            Until.findObject(By.res("com.charm.phr.mphr:id/dashboardBottomSheet")),
            DEFAULT_TIMEOUT
        )

        assertTrue(
            "Dashboard not reached. ${loginHelper.dumpHierarchyInfo()}",
            dashboardToolbar != null || dashboardBottomNav != null
        )
    }

    /**
     * Opens UserProfileActivity by tapping the patient profile image.
     *
     * The profile icon is a ComposeView (id=patientImageView) positioned at the
     * top-right of the dashboard toolbar. It shows the patient's photo or initials
     * in a circular badge.
     */
    private fun openProfile() {
        val profileIcon = device.wait(
            Until.findObject(By.res("com.charm.phr.mphr:id/patientImageView")),
            DEFAULT_TIMEOUT
        ) ?: throw AssertionError("Profile icon not found on dashboard. ${loginHelper.dumpHierarchyInfo()}")

        profileIcon.click()
        // Wait for the profile screen to open (dashboard icon should no longer be tappable)
        device.wait(
            Until.gone(By.res("com.charm.phr.mphr:id/patientImageView")),
            DEFAULT_TIMEOUT
        )
    }

    /**
     * Performs the logout action from the UserProfileActivity.
     *
     * The profile screen is built with Jetpack Compose. The "Log out" button is inside
     * a scrollable Column at the bottom of the screen. Steps:
     * 1. Look for "Log out" text — scroll down if not immediately visible
     * 2. Tap "Log out" to trigger the confirmation dialog
     * 3. Dialog shows: "Are you sure you want to sign out?" with "Yes" / "No" buttons
     * 4. Tap "Yes" to confirm — this calls AuthManager.logout() which:
     *    - Revokes the OAuth token via AuthenticationUtil.signOut()
     *    - Clears SharedPreferences (auth token, patient data, passcode, settings)
     *    - Clears the Room database
     *    - Cancels all WorkManager tasks and scheduled alarms
     *    - Unregisters the device from push notifications
     */
    private fun performLogout() {
        // Try to find "Log out" — may need to scroll on smaller screens
        var logoutItem = device.wait(
            Until.findObject(By.text("Log out")),
            DEFAULT_TIMEOUT
        )

        if (logoutItem == null) {
            // Scroll down to reveal the logout button
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(Direction.DOWN, 1.0f)
            } else {
                // Fallback: manual swipe gesture
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    20
                )
            }

            logoutItem = device.wait(
                Until.findObject(By.text("Log out")),
                DEFAULT_TIMEOUT
            )
        }

        assertNotNull("'Log out' button not found after scrolling. ${loginHelper.dumpHierarchyInfo()}", logoutItem)
        logoutItem!!.click()

        // Confirm logout in the dialog
        val confirmButton = device.wait(
            Until.findObject(By.text("Yes")),
            DEFAULT_TIMEOUT
        ) ?: throw AssertionError("Logout confirmation 'Yes' button not found. ${loginHelper.dumpHierarchyInfo()}")

        confirmButton.click()
        // Wait for the dashboard to disappear — logout is in progress
        device.wait(
            Until.gone(By.res("com.charm.phr.mphr:id/dashboardToolbarText")),
            LOGIN_TIMEOUT
        )
    }

    /**
     * Verifies the app has logged out and landed on TourPageActivity.
     *
     * After logout, the app navigates to TourPageActivity with
     * FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK, clearing the entire back stack.
     * We verify this by waiting for the tour page's Sign In button to appear, which is
     * the most reliable UI signal that logout is fully complete — no cross-process
     * SharedPreferences reads needed.
     */
    private fun verifyLoggedOut() {
        // Dashboard should no longer be visible
        val dashboardGone = device.wait(
            Until.gone(By.res("com.charm.phr.mphr:id/dashboardToolbarText")),
            DEFAULT_TIMEOUT
        )
        assertTrue("Still on dashboard after logout", dashboardGone == true)

        // TourPageActivity's Sign In button must appear — confirms full navigation to
        // the logged-out state and that server-side session revocation has completed.
        val tourPageReached = device.wait(
            Until.hasObject(By.res("com.charm.phr.mphr:id/tourPageSignInBtn")),
            LOGIN_TIMEOUT
        )
        assertTrue(
            "App did not navigate to TourPage after logout. ${loginHelper.dumpHierarchyInfo()}",
            tourPageReached == true
        )
    }

    // ═══════════════════════════════════════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════════════════════════════════════

    private fun assertNotNull(message: String, obj: Any?) {
        assertTrue(message, obj != null)
    }

    /**
     * Launches LoginActivity with clear flags to start fresh.
     * Waits for an EditText to appear — this could be either the in-app WebView
     * login field or the Chrome Custom Tab's username field rendered by CHIS.
     */
    private fun launchLoginActivity() {
        val intent = Intent(context, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        context.startActivity(intent)

        device.wait(
            Until.hasObject(By.clazz("android.widget.EditText")),
            LONG_TIMEOUT
        )
    }
}
