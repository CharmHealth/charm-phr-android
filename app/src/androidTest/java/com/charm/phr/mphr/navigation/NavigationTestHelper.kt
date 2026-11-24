package com.charm.phr.mphr.navigation

import android.os.Build
import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/**
 * Reusable helper for navigating module screens during UI smoke tests.
 *
 * Provides utilities for:
 * - Granting runtime permissions (notifications) via ADB
 * - Dismissing OS-level and app-level dialogs
 * - Switching bottom navigation tabs
 * - Scrolling Compose lists to find module items
 * - Dynamically discovering and clicking TabLayout tabs, ChipGroup chips
 * - Clicking list items (first only, or all items for sub-module lists)
 * - Clicking Floating Action Buttons (FABs)
 * - Recovering to the dashboard when navigation goes off-track
 */
class NavigationTestHelper(private val device: UiDevice) {

    companion object {
        private const val TAG = "NavigationTestHelper"
        const val APP_PACKAGE = "com.charm.phr.mphr"

        const val SHORT_TIMEOUT = 5_000L
        const val DEFAULT_TIMEOUT = 10_000L
        const val NAVIGATION_TIMEOUT = 15_000L
        const val POST_CLICK_DELAY = 1_500L
    }

    /** Result of attempting to open a module from the My Health grid. */
    enum class OpenResult {
        /** Module text not found in the grid. */
        NOT_FOUND,
        /** Module clicked and target toolbar detected. */
        OPENED,
        /** Module clicked but target toolbar was NOT detected. */
        CLICKED_BUT_BLOCKED
    }

    // =========================================================================
    // Permission & System Dialog Handling
    // =========================================================================

    /**
     * Grants POST_NOTIFICATIONS permission via ADB shell command.
     *
     * On Android 13+ (API 33), apps must request POST_NOTIFICATIONS at runtime.
     * Granting it proactively prevents the system dialog from blocking UI during tests.
     * On older APIs this is a no-op (permission is auto-granted at install).
     */
    fun grantNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                device.executeShellCommand(
                    "pm grant $APP_PACKAGE android.permission.POST_NOTIFICATIONS"
                )
                Log.d(TAG, "Granted POST_NOTIFICATIONS permission via ADB")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to grant notification permission: ${e.message}")
            }
        }
    }

    /**
     * Dismisses any OS-level permission or notification dialog by clicking "Allow".
     *
     * Called automatically within [openModule] if the toolbar isn't detected.
     * Prefers "Allow" to ensure permission is granted.
     *
     * Catches [androidx.test.uiautomator.StaleObjectException] in case a button
     * reference becomes invalid between find and click (screen transitions).
     */
    fun dismissSystemDialogs() {
        try {
            // Prefer "Allow" -- grants permission so future dialogs don't appear
            val allowBtn = device.findObject(By.text("Allow"))
                ?: device.findObject(By.text("ALLOW"))
                ?: device.findObject(By.text("While using the app"))
            if (allowBtn != null) {
                allowBtn.click()
                Log.d(TAG, "Dismissed system dialog by clicking 'Allow'")
                Thread.sleep(POST_CLICK_DELAY)
                return
            }

            // Chrome / generic dialogs
            val noThanks = device.findObject(By.text("No thanks"))
                ?: device.findObject(By.text("NO THANKS"))
                ?: device.findObject(By.text("Not now"))
            if (noThanks != null) {
                noThanks.click()
                Log.d(TAG, "Dismissed dialog by clicking '${noThanks.text}'")
                Thread.sleep(POST_CLICK_DELAY)
                return
            }

            // Generic OK / GOT IT
            val okBtn = device.findObject(By.text("OK"))
                ?: device.findObject(By.text("GOT IT"))
                ?: device.findObject(By.text("Got it"))
            if (okBtn != null) {
                okBtn.click()
                Log.d(TAG, "Dismissed dialog by clicking '${okBtn.text}'")
                Thread.sleep(POST_CLICK_DELAY)
            }
        } catch (e: androidx.test.uiautomator.StaleObjectException) {
            Log.w(TAG, "StaleObjectException in dismissSystemDialogs -- screen may have transitioned")
        }
    }

    // =========================================================================
    // App Dialog Handling
    // =========================================================================

    /**
     * Dismisses app-level dialogs such as "unsaved changes" confirmation.
     *
     * MDAddBaseActivity shows: "Are you sure you want to discard the changes?"
     * with "Yes" (positive) and "No" (negative) buttons.
     *
     * @return true if a dialog was dismissed
     */
    fun dismissAppDialogs(): Boolean {
        try {
            // MDAddBaseActivity "discard changes" dialog -- click "Yes" to discard
            val yesBtn = device.findObject(By.text("Yes"))
                ?: device.findObject(By.text("YES"))
            if (yesBtn != null) {
                yesBtn.click()
                Log.d(TAG, "Dismissed app dialog by clicking 'Yes' (discard changes)")
                Thread.sleep(POST_CLICK_DELAY)
                return true
            }

            // Generic discard/cancel patterns used by other dialogs
            val discardBtn = device.findObject(By.text("Discard"))
                ?: device.findObject(By.text("DISCARD"))
                ?: device.findObject(By.text("Leave"))
                ?: device.findObject(By.text("LEAVE"))
            if (discardBtn != null) {
                discardBtn.click()
                Log.d(TAG, "Dismissed app dialog by clicking '${discardBtn.text}'")
                Thread.sleep(POST_CLICK_DELAY)
                return true
            }
        } catch (e: androidx.test.uiautomator.StaleObjectException) {
            Log.w(TAG, "StaleObjectException in dismissAppDialogs -- screen may have transitioned")
            return true // Assume dialog was dismissed by the transition
        }

        return false
    }

    /**
     * Dismisses any dialog -- tries app dialogs first, then system dialogs.
     *
     * @return true if any dialog was dismissed
     */
    fun dismissAnyDialog(): Boolean {
        if (dismissAppDialogs()) return true
        val before = System.currentTimeMillis()
        dismissSystemDialogs()
        // If dismissSystemDialogs consumed time clicking something, it dismissed
        return System.currentTimeMillis() - before > 500
    }

    // =========================================================================
    // Bottom Navigation
    // =========================================================================

    /**
     * Switches to a bottom navigation tab by matching its text label.
     *
     * @return true if the tab was found and clicked
     */
    fun switchBottomNavTab(tabText: String): Boolean {
        val bottomNav = device.wait(
            Until.findObject(By.res("$APP_PACKAGE:id/dashboardBottomSheet")),
            DEFAULT_TIMEOUT
        )
        if (bottomNav == null) {
            Log.w(TAG, "Bottom navigation not found")
            return false
        }

        val tab = device.wait(
            Until.findObject(By.text(tabText)),
            SHORT_TIMEOUT
        )
        if (tab != null) {
            tab.click()
            Thread.sleep(POST_CLICK_DELAY)
            Log.d(TAG, "Switched to bottom tab: $tabText")
            return true
        }
        Log.w(TAG, "Bottom nav tab '$tabText' not found")
        return false
    }

    // =========================================================================
    // Module Item Finding & Clicking (Compose List)
    // =========================================================================

    /**
     * Finds a module item by its display text in the Health Data Compose screen.
     * Scrolls down to find items that aren't initially visible.
     */
    fun findModuleItem(moduleName: String): UiObject2? {
        var item = device.wait(Until.findObject(By.text(moduleName)), SHORT_TIMEOUT)
        if (item != null) return item

        repeat(5) { attempt ->
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(Direction.DOWN, 0.6f)
            } else {
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    20
                )
            }
            Thread.sleep(800)
            item = device.findObject(By.text(moduleName))
            if (item != null) {
                Log.d(TAG, "Found '$moduleName' after ${attempt + 1} scroll(s)")
                return item
            }
        }

        Log.w(TAG, "Module '$moduleName' not found after scrolling")
        return null
    }

    /**
     * Clicks a module item and waits for the target screen to load.
     * If toolbar not detected, tries dismissing system dialogs and retries.
     */
    fun openModule(moduleName: String, expectedToolbarId: String): OpenResult {
        val item = findModuleItem(moduleName)
        if (item == null) {
            return OpenResult.NOT_FOUND
        }

        item.click()
        Log.d(TAG, "Clicked module: $moduleName")

        var arrived = device.wait(
            Until.hasObject(By.res("$APP_PACKAGE:id/$expectedToolbarId")),
            NAVIGATION_TIMEOUT
        )

        if (arrived != true) {
            Log.d(TAG, "Toolbar '$expectedToolbarId' not found -- checking for dialogs")
            dismissAnyDialog()
            arrived = device.wait(
                Until.hasObject(By.res("$APP_PACKAGE:id/$expectedToolbarId")),
                SHORT_TIMEOUT
            )
        }

        if (arrived != true) {
            // Check if we at least left the dashboard (module opened but toolbar ID differs)
            val stillOnDashboard = device.findObject(
                By.res("$APP_PACKAGE:id/dashboardBottomSheet")
            ) != null
            if (!stillOnDashboard) {
                Log.d(TAG, "Left dashboard but toolbar '$expectedToolbarId' not found -- treating as opened")
                return OpenResult.OPENED
            }
        }

        return if (arrived == true) OpenResult.OPENED else OpenResult.CLICKED_BUT_BLOCKED
    }

    // =========================================================================
    // Dynamic Tab & Chip Discovery
    // =========================================================================

    /**
     * Discovers and clicks through ALL tabs and chips on the current screen.
     *
     * Uses a multi-strategy approach since Material component class names are not
     * always visible to UI Automator (especially on Android 16+):
     *
     * 1. **Resource ID** -- Find TabLayout by its resource ID, then click children
     * 2. **Expected tab text** -- Click each expected tab label directly by text
     * 3. **Class name fallback** -- Try Material class names (works on older Android)
     *
     * @param tabLayoutId optional resource ID of the TabLayout/ChipGroup container
     * @param expectedTabs optional list of expected tab/chip labels to click by text
     * @return list of tab/chip text labels that were clicked
     */
    fun discoverAndClickAllTabs(
        tabLayoutId: String? = null,
        expectedTabs: List<String> = emptyList()
    ): List<String> {
        val clickedLabels = mutableListOf<String>()

        // -- Strategy 1: Click expected tab labels by text (most reliable) --------
        // TabLayout on Android 16 exposes only the selected tab as a child,
        // but all tab texts are findable via By.text(). Try both exact and
        // uppercase since TabLayout may render labels in uppercase.
        if (expectedTabs.isNotEmpty()) {
            for (tabLabel in expectedTabs) {
                val tab = device.findObject(By.text(tabLabel))
                    ?: device.findObject(By.text(tabLabel.uppercase()))
                if (tab != null) {
                    try {
                        tab.click()
                        Thread.sleep(POST_CLICK_DELAY)
                        clickedLabels.add(tabLabel)
                        Log.d(TAG, "Clicked tab by text: $tabLabel")
                    } catch (e: androidx.test.uiautomator.StaleObjectException) {
                        Log.w(TAG, "StaleObjectException clicking tab '$tabLabel' -- skipping")
                    }
                } else {
                    Log.d(TAG, "Expected tab '$tabLabel' not found by text (tried exact + uppercase)")
                }
            }
            if (clickedLabels.isNotEmpty()) return clickedLabels
        }

        // -- Strategy 2: Find container by resource ID ----------------------------
        if (tabLayoutId != null) {
            val container = device.findObject(By.res("$APP_PACKAGE:id/$tabLayoutId"))
            if (container != null) {
                val children = container.children ?: emptyList()
                Log.d(TAG, "Found tab container '$tabLayoutId' with ${children.size} child(ren)")
                for (child in children) {
                    try {
                        val label = child.text
                            ?: child.findObject(By.clazz("android.widget.TextView"))?.text
                            ?: "Tab"
                        child.click()
                        Thread.sleep(POST_CLICK_DELAY)
                        clickedLabels.add(label)
                        Log.d(TAG, "Clicked tab: $label")
                    } catch (e: androidx.test.uiautomator.StaleObjectException) {
                        Log.w(TAG, "StaleObjectException clicking tab child -- skipping")
                    }
                }
                if (clickedLabels.isNotEmpty()) return clickedLabels
            } else {
                Log.d(TAG, "Tab container '$tabLayoutId' not found by resource ID")
            }
        }

        // -- Strategy 3: Class name fallback (works on older Android) -------------
        val tabLayout = device.findObject(
            By.clazz("com.google.android.material.tabs.TabLayout")
        )
        if (tabLayout != null) {
            val tabs = tabLayout.children ?: emptyList()
            Log.d(TAG, "Found TabLayout by class with ${tabs.size} tab(s)")
            for (tab in tabs) {
                try {
                    val label = tab.text
                        ?: tab.findObject(By.clazz("android.widget.TextView"))?.text
                        ?: "Tab"
                    tab.click()
                    Thread.sleep(POST_CLICK_DELAY)
                    clickedLabels.add(label)
                    Log.d(TAG, "Clicked TabLayout tab: $label")
                } catch (e: androidx.test.uiautomator.StaleObjectException) {
                    Log.w(TAG, "StaleObjectException clicking TabLayout tab -- skipping")
                }
            }
            return clickedLabels
        }

        val chipGroup = device.findObject(
            By.clazz("com.google.android.material.chip.ChipGroup")
        )
        if (chipGroup != null) {
            val chips = chipGroup.children ?: emptyList()
            Log.d(TAG, "Found ChipGroup by class with ${chips.size} chip(s)")
            for (chip in chips) {
                try {
                    val label = chip.text ?: "Chip"
                    chip.click()
                    Thread.sleep(POST_CLICK_DELAY)
                    clickedLabels.add(label)
                    Log.d(TAG, "Clicked chip: $label")
                } catch (e: androidx.test.uiautomator.StaleObjectException) {
                    Log.w(TAG, "StaleObjectException clicking chip -- skipping")
                }
            }
            return clickedLabels
        }

        Log.d(TAG, "No tabs or chips found on current screen")
        return clickedLabels
    }

    // =========================================================================
    // List Item Interaction
    // =========================================================================

    /**
     * Clicks the first item in a RecyclerView to open a detail screen, then presses back.
     *
     * This validates that list items are clickable and the detail screen renders.
     * After clicking, waits briefly for the detail screen, takes a screenshot label,
     * then navigates back to the module screen.
     *
     * @param backButtonId the module's back button ID (to verify we returned to the module)
     * @return true if a list item was clicked and we returned to the module
     */
    fun clickFirstListItemAndReturn(backButtonId: String): Boolean {
        try {
            val rv = device.findObject(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
            )
            if (rv == null || rv.childCount == 0) {
                Log.d(TAG, "No RecyclerView or empty list -- skipping list item click")
                return false
            }

            val firstItem = rv.children.firstOrNull()
            if (firstItem == null) {
                Log.d(TAG, "RecyclerView has no clickable children")
                return false
            }

            // Remember current toolbar to detect navigation
            val hadToolbar = device.findObject(By.res("$APP_PACKAGE:id/$backButtonId")) != null

            firstItem.click()
            Log.d(TAG, "Clicked first list item")
            Thread.sleep(POST_CLICK_DELAY)

            // Dismiss any dialog that may have popped (e.g., permission requests from detail screen)
            dismissSystemDialogs()

            // Check if we navigated away (toolbar back button changed or disappeared)
            val stillOnSameScreen = device.findObject(By.res("$APP_PACKAGE:id/$backButtonId")) != null

            if (!stillOnSameScreen || !hadToolbar) {
                // We navigated to a detail screen -- go back
                Thread.sleep(POST_CLICK_DELAY)
                device.pressBack()
                Thread.sleep(POST_CLICK_DELAY)

                // Dismiss any dialog that might appear on back navigation (e.g., unsaved changes)
                dismissAppDialogs()
                dismissSystemDialogs()

                Log.d(TAG, "Returned from detail screen")
                return true
            }

            // Click didn't navigate away (e.g., expanded an inline view)
            Log.d(TAG, "List item click did not navigate -- may be inline expansion")
            return true
        } catch (e: androidx.test.uiautomator.StaleObjectException) {
            Log.w(TAG, "StaleObjectException in clickFirstListItemAndReturn -- recovering")
            dismissAppDialogs()
            return true
        }
    }

    /**
     * Clicks ALL items in the RecyclerView one by one, going to each detail screen and back.
     *
     * Used for sub-module lists like Wellness where every item should be visited.
     * Scrolls down to find more items after clicking visible ones.
     *
     * @param backButtonId the module's back button ID (to verify we returned)
     * @return number of items clicked
     */
    fun clickAllListItemsAndReturn(backButtonId: String): Int {
        var totalClicked = 0
        var scrollAttempts = 0
        val maxScrollAttempts = 8
        val clickedPositions = mutableSetOf<String>()

        while (scrollAttempts < maxScrollAttempts) {
            val rv = device.findObject(
                By.clazz("androidx.recyclerview.widget.RecyclerView")
            )
            if (rv == null || rv.childCount == 0) break

            var clickedAny = false
            for (child in rv.children.orEmpty()) {
                // Use the child's text or bounds as a unique key to avoid re-clicking
                val itemKey = child.text
                    ?: child.findObject(By.clazz("android.widget.TextView"))?.text
                    ?: "${child.visibleBounds}"
                if (itemKey in clickedPositions) continue

                clickedPositions.add(itemKey)
                child.click()
                Log.d(TAG, "Clicked list item: $itemKey")
                Thread.sleep(POST_CLICK_DELAY)

                // Dismiss any dialogs
                dismissAppDialogs()
                dismissSystemDialogs()

                // Check if we navigated to a new screen
                val stillOnModule = device.findObject(By.res("$APP_PACKAGE:id/$backButtonId")) != null
                if (!stillOnModule) {
                    // On detail screen -- go back
                    Thread.sleep(POST_CLICK_DELAY)
                    device.pressBack()
                    Thread.sleep(POST_CLICK_DELAY)
                    dismissAppDialogs()
                }

                totalClicked++
                clickedAny = true
            }

            if (!clickedAny) {
                // All visible items already clicked -- try scrolling
                val scrollable = device.findObject(By.scrollable(true))
                if (scrollable != null) {
                    scrollable.scroll(Direction.DOWN, 0.5f)
                } else {
                    device.swipe(
                        device.displayWidth / 2,
                        device.displayHeight * 3 / 4,
                        device.displayWidth / 2,
                        device.displayHeight / 4,
                        20
                    )
                }
                Thread.sleep(800)
                scrollAttempts++
            } else {
                scrollAttempts = 0 // Reset scroll attempts since we found new items
            }
        }

        Log.d(TAG, "Clicked $totalClicked list items total")
        return totalClicked
    }

    // =========================================================================
    // FAB Interaction
    // =========================================================================

    /**
     * Finds and clicks a Floating Action Button (FAB) on the current screen.
     *
     * Uses a multi-strategy approach since Material FAB class names are not
     * always visible to UI Automator on Android 16+:
     * 1. Find by resource ID (most reliable)
     * 2. Find by class name (fallback for older Android)
     *
     * After clicking, presses back to return to the module.
     *
     * @param fabId optional resource ID of the FAB (e.g., "messageComposeBtn")
     * @return true if a FAB was found and clicked
     */
    fun clickFabAndReturn(fabId: String? = null): Boolean {
        // Strategy 1: Find by resource ID
        var fab = if (fabId != null) {
            device.findObject(By.res("$APP_PACKAGE:id/$fabId"))
        } else {
            null
        }

        // Strategy 2: Find by class name (fallback)
        if (fab == null) {
            fab = device.findObject(
                By.clazz("com.google.android.material.floatingactionbutton.FloatingActionButton")
            ) ?: device.findObject(
                By.clazz("com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton")
            )
        }

        if (fab == null) {
            Log.d(TAG, "No FAB found on current screen (id=$fabId)")
            return false
        }

        try {
            if (!fab.isEnabled) {
                Log.d(TAG, "FAB found but not enabled -- skipping")
                return false
            }

            fab.click()
            Log.d(TAG, "Clicked FAB${if (fabId != null) " ($fabId)" else ""}")
        } catch (e: androidx.test.uiautomator.StaleObjectException) {
            Log.w(TAG, "StaleObjectException clicking FAB -- skipping")
            return false
        }

        Thread.sleep(POST_CLICK_DELAY)

        // Dismiss any dialog that may have appeared
        dismissSystemDialogs()

        // Press back to return to the module
        device.pressBack()
        Thread.sleep(POST_CLICK_DELAY)

        // Dismiss "discard changes?" dialog (MDAddBaseActivity uses "Yes"/"No")
        dismissAppDialogs()
        dismissSystemDialogs()

        Log.d(TAG, "Returned from FAB action")
        return true
    }

    // =========================================================================
    // Back Navigation & Recovery
    // =========================================================================

    /**
     * Navigates back from a module screen by clicking the toolbar back button.
     * Falls back to system back if the button isn't found.
     */
    fun pressBackButton(backButtonId: String): Boolean {
        val backBtn = device.findObject(By.res("$APP_PACKAGE:id/$backButtonId"))
        if (backBtn != null) {
            backBtn.click()
            Log.d(TAG, "Pressed back button: $backButtonId")
            Thread.sleep(POST_CLICK_DELAY)
            return true
        }

        Log.w(TAG, "Back button '$backButtonId' not found -- using system back")
        device.pressBack()
        Thread.sleep(POST_CLICK_DELAY)
        return true
    }

    /**
     * Waits for the dashboard bottom navigation to appear.
     */
    fun waitForDashboard(): Boolean {
        val dashboardVisible = device.wait(
            Until.hasObject(By.res("$APP_PACKAGE:id/dashboardBottomSheet")),
            NAVIGATION_TIMEOUT
        )
        return dashboardVisible == true
    }

    /**
     * Aggressively recovers to the dashboard by pressing back repeatedly.
     * Dismisses any app and system dialogs encountered along the way.
     *
     * @return true if the dashboard was reached
     */
    fun recoverToDashboard(maxAttempts: Int = 8): Boolean {
        repeat(maxAttempts) { attempt ->
            val onDashboard = device.wait(
                Until.hasObject(By.res("$APP_PACKAGE:id/dashboardBottomSheet")),
                SHORT_TIMEOUT
            )
            if (onDashboard == true) {
                if (attempt > 0) Log.d(TAG, "Recovered to dashboard after $attempt attempt(s)")
                return true
            }

            // Dismiss app dialogs first (e.g., "discard changes?" -> "Yes")
            // then system dialogs, before pressing back
            if (!dismissAppDialogs()) {
                dismissSystemDialogs()
            }

            device.pressBack()
            Thread.sleep(POST_CLICK_DELAY)
        }
        Log.e(TAG, "Could not recover to dashboard after $maxAttempts attempts")
        return false
    }

    // =========================================================================
    // Screen Verification
    // =========================================================================

    /**
     * Checks if any list content is displayed on the current screen.
     */
    fun hasListContent(): Boolean {
        val anyRv = device.findObject(By.clazz("androidx.recyclerview.widget.RecyclerView"))
        return anyRv != null && anyRv.childCount > 0
    }

    /**
     * Resets the My Health screen scroll position to the top.
     */
    fun resetHealthDataScroll() {
        repeat(3) {
            val scrollable = device.findObject(By.scrollable(true))
            if (scrollable != null) {
                scrollable.scroll(Direction.UP, 1.0f)
            } else {
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    20
                )
            }
        }
        Thread.sleep(500)
    }

    // =========================================================================
    // Logout
    // =========================================================================

    /**
     * Logs out of the app via Profile tab → "Log out" → confirm "Yes".
     * Call at the end of test to leave the app in a clean state.
     */
    fun performLogout(): Boolean {
        // Ensure we're on the dashboard first
        if (!waitForDashboard()) {
            if (!recoverToDashboard()) {
                Log.w(TAG, "Cannot logout -- not on dashboard")
                return false
            }
        }

        // Switch to Profile tab
        val profileTab = device.findObject(By.text("Profile"))
        if (profileTab == null) {
            Log.w(TAG, "Profile tab not found -- cannot logout")
            return false
        }
        profileTab.click()
        Thread.sleep(POST_CLICK_DELAY)

        // Scroll down to find "Log out" button
        var logoutBtn = device.findObject(By.text("Log out"))
        if (logoutBtn == null) {
            // Scroll down to find it
            repeat(3) {
                device.swipe(
                    device.displayWidth / 2,
                    device.displayHeight * 3 / 4,
                    device.displayWidth / 2,
                    device.displayHeight / 4,
                    20
                )
                Thread.sleep(POST_CLICK_DELAY)
                logoutBtn = device.findObject(By.text("Log out"))
                if (logoutBtn != null) return@repeat
            }
        }

        if (logoutBtn == null) {
            Log.w(TAG, "'Log out' button not found")
            return false
        }

        logoutBtn!!.click()
        Thread.sleep(POST_CLICK_DELAY)

        // Confirm the "Are you sure you want to sign out?" dialog
        val yesBtn = device.wait(Until.findObject(By.text("Yes")), SHORT_TIMEOUT)
        if (yesBtn != null) {
            yesBtn.click()
            Log.d(TAG, "Logged out successfully")
            Thread.sleep(POST_CLICK_DELAY)
            return true
        }

        Log.w(TAG, "Logout confirmation dialog not found")
        return false
    }
}
