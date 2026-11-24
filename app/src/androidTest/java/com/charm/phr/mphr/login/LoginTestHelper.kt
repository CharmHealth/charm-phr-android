package com.charm.phr.mphr.login

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.File

/**
 * Reusable helper for automating the CharmAuth (CHIS) sign-in screen.
 *
 * ## How It Works
 *
 * The CharmAuth SDK opens a **Chrome Custom Tab** for authentication.
 * This is a separate process (`com.android.chrome`), so Espresso cannot interact with it.
 * We use **UI Automator** which works cross-process to find and interact with elements
 * inside the Chrome Custom Tab.
 *
 * ## Login Page Structure (accounts.charmtracker.com)
 *
 * The CHIS sign-in page has two steps:
 *
 * **Step 1 — Username:**
 * ```
 * ┌─────────────────────────────────┐
 * │  Sign in                        │  ← Header text (NOT clickable)
 * │  to access Charm mPHR           │
 * │                                 │
 * │  ┌───────────────────────────┐  │
 * │  │ username@email.com        │  │  ← EditText (By.clazz)
 * │  └───────────────────────────┘  │
 * │                                 │
 * │  ┌─────────┐                    │
 * │  │  Next    │                   │  ← Button (By.text("Next"))
 * │  └─────────┘                    │
 * └─────────────────────────────────┘
 * ```
 *
 * **Step 2 — Password:**
 * ```
 * ┌─────────────────────────────────┐
 * │  Sign in                        │  ← Header text (same text as button!)
 * │  to access Charm mPHR           │
 * │  phrdemo          Change        │
 * │                                 │
 * │  ┌───────────────────────────┐  │
 * │  │ ••••••••                  │  │  ← EditText (password=true)
 * │  └───────────────────────────┘  │
 * │                                 │
 * │  ┌─────────────────────────────┐│
 * │  │         Sign in             ││  ← Button (res=nextbtn)
 * │  └─────────────────────────────┘│
 * └─────────────────────────────────┘
 * ```
 *
 * **Important:** The "Sign in" header text and "Sign in" button have the same text.
 * We target the button by its resource-id `nextbtn` to avoid clicking the header.
 *
 * ## Usage
 *
 * ```kotlin
 * val helper = LoginTestHelper(device)
 * helper.performLogin("username", "password")
 * ```
 */
class LoginTestHelper(private val device: UiDevice) {

    companion object {
        const val FIELD_TIMEOUT = 15_000L
        const val POST_CLICK_DELAY = 3_000L
        private const val TAG = "LoginTestHelper"
    }

    /**
     * Performs the full SSO login: enter username → Next → enter password → Sign in.
     */
    fun performLogin(username: String, password: String) {
        enterUsername(username)
        enterPassword(password)
    }

    /**
     * Step 1: Enter the username and tap "Next".
     *
     * Finds the first EditText on the Chrome Custom Tab page (the email input field),
     * types the username, then taps the "Next" button to proceed to the password step.
     */
    private fun enterUsername(username: String) {
        val usernameField = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            FIELD_TIMEOUT
        ) ?: throw AssertionError("Username field not found within ${FIELD_TIMEOUT}ms")

        usernameField.clear()
        usernameField.text = username

        val nextButton = findButtonByText("Next")
            ?: throw AssertionError("'Next' button not found. ${dumpHierarchyInfo()}")

        nextButton.click()
        // Wait for the password page to load in the Chrome Custom Tab
        Thread.sleep(POST_CLICK_DELAY)
    }

    /**
     * Step 2: Enter the password and tap "Sign in".
     *
     * After the username step, the page transitions to show the password field.
     * The "Sign in" button shares its text with the page header, so we target
     * it by resource-id (res=nextbtn) to avoid ambiguity.
     */
    private fun enterPassword(password: String) {
        val passwordField = device.wait(
            Until.findObject(By.clazz("android.widget.EditText")),
            FIELD_TIMEOUT
        ) ?: throw AssertionError("Password field not found within ${FIELD_TIMEOUT}ms")

        passwordField.clear()
        passwordField.text = password

        // Target the button by resource-id to avoid clicking the "Sign in" header text.
        // Fallback uses clickable(true) to exclude the non-interactive header which shares
        // the same text. By.res("nextbtn") has no package prefix intentionally — Chrome's
        // accessibility bridge exposes the HTML id attribute without a package qualifier.
        val signInButton = device.wait(
            Until.findObject(By.res("nextbtn")),
            FIELD_TIMEOUT
        ) ?: device.wait(
            Until.findObject(By.clickable(true).text("Sign in")),
            FIELD_TIMEOUT
        ) ?: throw AssertionError("'Sign In' button not found. ${dumpHierarchyInfo()}")

        signInButton.click()
    }

    /**
     * Finds a button by trying multiple text variations.
     *
     * Chrome Custom Tabs render web content as native Android views, but the
     * text casing may vary. This method tries exact matches first, then falls
     * back to partial (prefix) matching.
     */
    private fun findButtonByText(vararg textVariations: String): UiObject2? {
        // Try exact text match
        for (text in textVariations) {
            val obj = device.wait(Until.findObject(By.text(text)), 3_000L)
            if (obj != null) {
                Log.d(TAG, "Found button with text: '$text'")
                return obj
            }
        }
        // Fallback: partial match using first 4 characters
        for (text in textVariations) {
            val obj = device.wait(Until.findObject(By.textContains(text.take(4))), 3_000L)
            if (obj != null) {
                Log.d(TAG, "Found button via textContains: '${text.take(4)}'")
                return obj
            }
        }
        return null
    }

    /**
     * Dumps the current UI hierarchy to the Downloads folder for debugging.
     *
     * When an element is not found, call this to capture what's actually on screen.
     * Pull the dump with: `adb pull /sdcard/Download/test_ui_dump.xml`
     * Parse with: `python3 -c "import xml.etree.ElementTree as ET; ..."`
     */
    fun dumpHierarchyInfo(): String {
        return try {
            val dumpFile = File(
                android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_DOWNLOADS
                ),
                "test_ui_dump.xml"
            )
            device.dumpWindowHierarchy(dumpFile)
            "UI hierarchy dumped to ${dumpFile.absolutePath}"
        } catch (e: Exception) {
            "Failed to dump UI hierarchy: ${e.message}"
        }
    }
}
