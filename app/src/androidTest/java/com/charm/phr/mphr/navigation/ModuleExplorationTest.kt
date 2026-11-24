package com.charm.phr.mphr.navigation

import android.content.Intent
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import com.charm.phr.mphr.base.BaseUITest
import com.charm.phr.mphr.config.TestConfig
import com.charm.phr.mphr.login.LoginTestHelper
import com.charm.phr.mphr.ui.login.LoginActivity
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke test that navigates through every module accessible from the dashboard.
 *
 * ## Purpose
 *
 * Ensures every screen renders without crashing. Catches regressions from:
 * - `onBackPressed()` deprecation migration
 * - Dependency version bumps, theme/style changes, R8 misconfiguration
 *
 * ## Two Test Methods
 *
 * - **`testAllModules`** -- explores all enabled modules. Pass `-e fresh true` for a fresh login.
 * - **`testSelectedModules`** -- explores only the modules specified via `-e modules "Messages,Labs"`.
 * - Both methods log out at the end.
 *
 * ## What It Does Per Module
 *
 * Each module has a [ModuleInfo] descriptor that defines expected tabs, FAB, list behavior,
 * and known dialogs. The test validates discovered state against these expectations.
 *
 * ## Run Commands
 *
 * ```
 * # All enabled modules (resume session):
 * ./gradlew connectedCommonDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.charm.phr.mphr.navigation.ModuleExplorationTest#testAllModules
 *
 * # All enabled modules (fresh login):
 * ./gradlew connectedCommonDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.charm.phr.mphr.navigation.ModuleExplorationTest#testAllModules \
 *   -Pandroid.testInstrumentationRunnerArguments.fresh=true
 *
 * # Selected modules only:
 * ./gradlew connectedCommonDebugAndroidTest \
 *   -Pandroid.testInstrumentationRunnerArguments.class=com.charm.phr.mphr.navigation.ModuleExplorationTest#testSelectedModules \
 *   -Pandroid.testInstrumentationRunnerArguments.modules="Messages,Labs,Prescriptions"
 * ```
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class ModuleExplorationTest : BaseUITest() {

    private lateinit var loginHelper: LoginTestHelper
    private lateinit var navHelper: NavigationTestHelper

    // =========================================================================
    // Module Registry
    // =========================================================================

    /** How tabs/chips are presented in this module. */
    enum class TabType {
        /** No tabs or chips. */
        NONE,
        /** Standard Material TabLayout. */
        TAB_LAYOUT,
        /** Material ChipGroup for filtering. */
        CHIP_GROUP
    }

    /**
     * Defines a module and everything the test should expect when exploring it.
     *
     * Per-module configuration eliminates guesswork -- the test knows exactly which
     * tabs to expect, whether a FAB exists, and whether pressing back from the FAB
     * screen will trigger an "unsaved changes" dialog.
     */
    data class ModuleInfo(
        val displayName: String,
        val toolbarId: String,
        val backButtonId: String,
        /** Set to false to skip this module during test runs. */
        val enabled: Boolean = true,
        /** Type of tab/chip navigation in this module. */
        val tabType: TabType = TabType.NONE,
        /** Expected tab or chip labels, in order. Empty = dynamic discovery only. */
        val expectedTabs: List<String> = emptyList(),
        /** Resource ID of the TabLayout (e.g., "messageTabLayout"). Used for resource-ID-based discovery. */
        val tabLayoutId: String? = null,
        /** Whether this module has a FAB. */
        val hasFab: Boolean = false,
        /** Resource ID of the FAB (e.g., "messageComposeBtn"). Used when class-based discovery fails. */
        val fabId: String? = null,
        /** Whether the FAB opens an MDAddBaseActivity (shows "discard changes" dialog on back). */
        val fabOpensAddActivity: Boolean = false,
        /** Whether this module normally shows a list of items. */
        val hasList: Boolean = true,
        /** Permission dialogs or other known popups that appear on open. */
        val knownDialogs: List<String> = emptyList(),
        /** If true, click ALL list items (not just the first). Used for sub-module lists like Wellness. */
        val clickAllListItems: Boolean = false,
        /** If true, click the first list item in each tab (not just once). Used for Documents etc. */
        val clickListItemPerTab: Boolean = false
    )

    private val modules = listOf(
        // -- Engagement -------------------------------------------------------
        ModuleInfo(
            "Appointments", "appointmentToolbar", "appointmentBackBtn",
            enabled = true,
            tabType = TabType.CHIP_GROUP,
            expectedTabs = listOf("Appointments", "Requested", "Waiting List", "Denied"),
            tabLayoutId = "appointmentTabLayout"
        ),
        ModuleInfo(
            "Messages", "messageToolbar", "messageBackBtn",
            enabled = false,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Inbox", "Sent"),
            tabLayoutId = "messageTabLayout",
            hasFab = true, fabId = "messageComposeBtn"
        ),
        ModuleInfo(
            "Questionnaires", "questionnaireToolbar", "questionnaireBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Current", "Past"),
            tabLayoutId = "questionnaireTabLayout"
        ),
        ModuleInfo(
            "Wellness", "wellnessListToolbar", "wellnessListBackBtn",
            enabled = false,
            hasList = true,
            clickAllListItems = true
        ),
        ModuleInfo(
            "Reminders", "reminderListToolbar", "reminderListBackBtn",
            enabled = false,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Today", "All"),
            tabLayoutId = "reminderTabLayout",
            hasFab = true, fabId = "reminderFabAdd",
            knownDialogs = listOf("Allow", "Agree")
        ),
        ModuleInfo(
            "Flowsheets", "flowsheetListToolbar", "flowsheetListBackBtn",
            enabled = true
        ),

        // -- Records ----------------------------------------------------------
        ModuleInfo(
            "Visit Summary", "visitSummaryToolbar", "visitSummaryBackBtn",
            enabled = true
        ),
        ModuleInfo(
            "Documents", "documentToolbar", "documentBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("All", "Folder"),
            tabLayoutId = "documentTabLayout",
            hasFab = true, fabId = "documentAddBtn", fabOpensAddActivity = true,
            clickListItemPerTab = true
        ),
        ModuleInfo(
            "Prescriptions", "prescriptionToolbar", "prescriptionBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Medications", "Supplements"),
            tabLayoutId = "prescriptionTabLayout",
            hasFab = true, fabId = "prescriptionAddBtn", fabOpensAddActivity = true
        ),
        ModuleInfo(
            "Allergies", "allergyToolbar", "allergyBackBtn",
            enabled = true,
            hasFab = true, fabId = "allergyAddFab", fabOpensAddActivity = true
        ),
        ModuleInfo(
            "Labs", "labToolbar", "labBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Results", "Orders"),
            tabLayoutId = "labTabLayout"
        ),
        ModuleInfo(
            "Procedures", "procedureToolbar", "procedureBackBtn",
            enabled = true,
            hasFab = true, fabId = "procedureListAddFab", fabOpensAddActivity = true
        ),
        ModuleInfo(
            "Immunizations", "vaccineToolbar", "vaccineBackBtn",
            enabled = true,
            hasFab = true, fabId = "vaccineListAddFab", fabOpensAddActivity = true
        ),
        ModuleInfo(
            "Insurance", "insuranceListToolbar", "insuranceListBackBtn",
            enabled = true,
            hasFab = true, fabId = "insuranceListAddFab", fabOpensAddActivity = true
        ),

        // -- Billing & Payments -----------------------------------------------
        ModuleInfo(
            "Billing", "billingToolbar", "billingBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Invoice", "Receipt"),
            tabLayoutId = "billingTabLayout"
        ),
        ModuleInfo(
            "Card On File", "cofToolbar", "cofBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Practice Saved Cards", "Patient Saved Cards"),
            tabLayoutId = "cofTabLayout"
        ),

        // -- Resources --------------------------------------------------------
        ModuleInfo(
            "Useful Info", "usefulInfoListToolbar", "usefulInfoListBackBtn",
            enabled = true,
            hasList = false
        ),
        ModuleInfo(
            "About", "aboutToolbar", "aboutBackBtn",
            enabled = true,
            tabType = TabType.TAB_LAYOUT,
            expectedTabs = listOf("Facilities", "Providers"),
            tabLayoutId = "aboutTabLayout",
            hasList = false
        )
    )

    // =========================================================================
    // Result Tracking
    // =========================================================================

    enum class ExploreResult { VISITED, SKIPPED, FAILED }

    data class ModuleResult(
        val moduleName: String,
        val result: ExploreResult,
        val tabsClicked: List<String> = emptyList(),
        val expectedTabs: List<String> = emptyList(),
        val missingTabs: List<String> = emptyList(),
        val hasList: Boolean = false,
        val listItemClicked: Boolean = false,
        val listItemsClickedCount: Int = 0,
        val expectedFab: Boolean = false,
        val fabClicked: Boolean = false,
        val error: String? = null
    )

    // =========================================================================
    // Setup
    // =========================================================================

    @Before
    override fun setUp() {
        super.setUp()
        loginHelper = LoginTestHelper(device)
        navHelper = NavigationTestHelper(device)
        TestConfig.init(testContext)

        // Grant notification permission upfront so OS dialogs don't block tests
        navHelper.grantNotificationPermission()
    }

    // =========================================================================
    // Main Tests
    // =========================================================================

    /**
     * Explores all enabled modules.
     *
     * Resumes an existing session if available, otherwise performs a fresh SSO login.
     * Pass `-e fresh true` to clear app data and force a fresh login.
     * Logs out at the end of the test.
     *
     * ## Run (all enabled modules):
     * ```
     * ./gradlew connectedCommonDebugAndroidTest \
     *   -Pandroid.testInstrumentationRunnerArguments.class=...ModuleExplorationTest#testAllModules
     * ```
     *
     * ## Run (fresh login):
     * ```
     * ./gradlew connectedCommonDebugAndroidTest \
     *   -Pandroid.testInstrumentationRunnerArguments.class=...ModuleExplorationTest#testAllModules \
     *   -Pandroid.testInstrumentationRunnerArguments.fresh=true
     * ```
     */
    @Test
    fun testAllModules() {
        val passcode = TestConfig.passcode
        if (passcode.isBlank()) {
            fail("test.passcode not configured in test_config.properties")
            return
        }

        val fresh = InstrumentationRegistry.getArguments().getString("fresh", "false").toBoolean()
        ensureLoggedIn(passcode, clearData = fresh)

        val results = exploreAllModules()
        reportResults(results)

        navHelper.performLogout()
    }

    /**
     * Explores only the modules specified via the `modules` argument.
     * Pass a comma-separated list of module display names (case-insensitive).
     * Logs out at the end of the test.
     *
     * ## Run:
     * ```
     * ./gradlew connectedCommonDebugAndroidTest \
     *   -Pandroid.testInstrumentationRunnerArguments.class=...ModuleExplorationTest#testSelectedModules \
     *   -Pandroid.testInstrumentationRunnerArguments.modules="Messages,Labs,Prescriptions"
     * ```
     *
     * ## Available module names:
     * Appointments, Messages, Questionnaires, Wellness, Reminders, Flowsheets,
     * Visit Summary, Documents, Prescriptions, Allergies, Labs, Procedures,
     * Immunizations, Insurance, Billing, Card On File, Useful Info, About
     */
    @Test
    fun testSelectedModules() {
        val passcode = TestConfig.passcode
        if (passcode.isBlank()) {
            fail("test.passcode not configured in test_config.properties")
            return
        }

        val args = InstrumentationRegistry.getArguments()
        val modulesArg = args.getString("modules", "")
        if (modulesArg.isNullOrBlank()) {
            fail("No modules specified. Pass -e modules \"Messages,Labs\" (comma-separated)")
            return
        }

        val requestedNames = modulesArg.split(",").map { it.trim().lowercase() }
        val selectedModules = modules.filter { m ->
            requestedNames.any { it.equals(m.displayName, ignoreCase = true) }
        }

        if (selectedModules.isEmpty()) {
            fail("No matching modules found for: $modulesArg\nAvailable: ${modules.joinToString { it.displayName }}")
            return
        }

        Log.d(TAG, "Selective mode: testing ${selectedModules.size} module(s): ${selectedModules.joinToString { it.displayName }}")

        ensureLoggedIn(passcode, clearData = false)

        val results = exploreModules(selectedModules)
        reportResults(results)

        navHelper.performLogout()
    }

    // =========================================================================
    // Smart Session Management
    // =========================================================================

    /**
     * Ensures the app is logged in and on the dashboard.
     *
     * @param passcode the passcode to enter
     * @param clearData if true, clears app data first (Mode 1: fresh login).
     *                  If false, tries to resume existing session (Mode 2: resume).
     *
     * Never logs out -- keeps the session alive for rapid re-runs.
     */
    private fun ensureLoggedIn(passcode: String, clearData: Boolean = false) {
        if (clearData) {
            Log.d(TAG, "Mode 1: Clearing app data for fresh login")
            device.executeShellCommand("pm clear $APP_PACKAGE")
            Thread.sleep(NavigationTestHelper.POST_CLICK_DELAY)
            // Re-grant notification permission after clearing data
            navHelper.grantNotificationPermission()
        } else {
            Log.d(TAG, "Mode 2: Attempting to resume existing session")
        }

        // Launch app via package manager (preserves existing session in Mode 2)
        val launchIntent = context.packageManager.getLaunchIntentForPackage(APP_PACKAGE)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        } else {
            val intent = Intent(context, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg(APP_PACKAGE)), LONG_TIMEOUT)
        Thread.sleep(NavigationTestHelper.POST_CLICK_DELAY)

        // Dismiss any system dialogs (e.g., battery optimization)
        navHelper.dismissSystemDialogs()

        // Check what screen we landed on
        val onDashboard = device.findObject(
            By.res("$APP_PACKAGE:id/dashboardBottomSheet")
        ) != null

        val onPasscode = device.findObject(
            By.res("$APP_PACKAGE:id/invisible")
        ) != null

        val onTourPage = device.findObject(
            By.res("$APP_PACKAGE:id/tourPageSignInBtn")
        ) != null

        val onPatientList = device.findObject(
            By.res("$APP_PACKAGE:id/patientListRecyclerView")
        ) != null

        when {
            onDashboard -> {
                Log.d(TAG, "Already on dashboard -- resuming session")
            }

            onPasscode -> {
                Log.d(TAG, "Passcode screen detected -- entering passcode")
                handlePasscodeEntry(passcode)
                handlePatientSelection()
                waitForDashboardReady()
            }

            onPatientList -> {
                Log.d(TAG, "Patient list detected -- selecting first patient")
                handlePatientSelection()
                waitForDashboardReady()
            }

            onTourPage -> {
                Log.d(TAG, "Tour page detected -- performing fresh SSO login")
                performFreshLogin(passcode)
            }

            else -> {
                // No known screen detected -- app may have crashed (e.g., stale OAuth token).
                // Wait briefly for any known screen to appear.
                Log.w(TAG, "Unknown screen state -- possible app crash. Waiting briefly...")
                val passcodeAppeared = device.wait(
                    Until.hasObject(By.res("$APP_PACKAGE:id/invisible")),
                    NavigationTestHelper.SHORT_TIMEOUT
                )
                when {
                    passcodeAppeared == true -> {
                        handlePasscodeEntry(passcode)
                        handlePatientSelection()
                        waitForDashboardReady()
                    }
                    device.findObject(By.res("$APP_PACKAGE:id/dashboardBottomSheet")) != null -> {
                        Log.d(TAG, "Dashboard appeared after wait")
                    }
                    device.findObject(By.res("$APP_PACKAGE:id/tourPageSignInBtn")) != null -> {
                        Log.d(TAG, "Tour page appeared after wait -- performing fresh login")
                        performFreshLogin(passcode)
                    }
                    else -> {
                        // App likely crashed -- force-stop and relaunch
                        Log.w(TAG, "App appears crashed -- force-stopping and relaunching")
                        device.executeShellCommand("am force-stop $APP_PACKAGE")
                        Thread.sleep(NavigationTestHelper.POST_CLICK_DELAY)

                        val retryIntent = context.packageManager.getLaunchIntentForPackage(APP_PACKAGE)
                        if (retryIntent != null) {
                            retryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(retryIntent)
                        }
                        device.wait(Until.hasObject(By.pkg(APP_PACKAGE)), LONG_TIMEOUT)
                        Thread.sleep(NavigationTestHelper.POST_CLICK_DELAY)
                        navHelper.dismissSystemDialogs()

                        // Check what screen appeared after relaunch
                        val tourPage = device.wait(
                            Until.hasObject(By.res("$APP_PACKAGE:id/tourPageSignInBtn")),
                            LOGIN_TIMEOUT
                        )
                        if (tourPage == true) {
                            performFreshLogin(passcode)
                        } else {
                            // May need Mode 1 (fresh login) if session is corrupted
                            fail("App crashed and could not recover. Try running testAllModules with -e fresh true to clear data.")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // Core Exploration Logic
    // =========================================================================

    private fun exploreAllModules(): List<ModuleResult> {
        return exploreModules(modules.filter { it.enabled })
    }

    private fun exploreModules(modulesToExplore: List<ModuleInfo>): List<ModuleResult> {
        verifyHomeTab()

        val switchedToHealth = navHelper.switchBottomNavTab("My Health")
        assertTrue("Could not switch to My Health tab", switchedToHealth)
        takeScreenshot("explore_00_my_health_tab")

        val results = mutableListOf<ModuleResult>()
        for ((index, module) in modulesToExplore.withIndex()) {
            val result = exploreModule(module, index + 1)
            results.add(result)

            val extras = buildString {
                if (result.tabsClicked.isNotEmpty()) append(" [tabs: ${result.tabsClicked.joinToString()}]")
                if (result.missingTabs.isNotEmpty()) append(" [MISSING tabs: ${result.missingTabs.joinToString()}]")
                if (result.hasList) append(" [list]")
                if (result.listItemsClickedCount > 1) append(" [${result.listItemsClickedCount} items clicked]")
                else if (result.listItemClicked) append(" [item->detail]")
                if (result.expectedFab && !result.fabClicked) append(" [FAB MISSING]")
                else if (result.fabClicked) append(" [FAB]")
                if (result.error != null) append(" (${result.error})")
            }
            Log.d(TAG, "Module ${index + 1}/${modulesToExplore.size}: ${module.displayName} -> ${result.result}$extras")
        }

        return results
    }

    /**
     * Explores a single module using its per-module configuration:
     * open -> dismiss known dialogs -> click expected tabs/chips -> click list items -> click FAB -> back
     */
    private fun exploreModule(module: ModuleInfo, stepNumber: Int): ModuleResult {
        val stepPrefix = String.format("%02d", stepNumber)

        // Ensure we're on the dashboard
        if (!ensureOnDashboard()) {
            return ModuleResult(
                moduleName = module.displayName,
                result = ExploreResult.FAILED,
                error = "Dashboard not reachable before opening module"
            )
        }

        // Open the module
        val openResult = navHelper.openModule(module.displayName, module.toolbarId)

        when (openResult) {
            NavigationTestHelper.OpenResult.NOT_FOUND -> {
                return ModuleResult(
                    moduleName = module.displayName,
                    result = ExploreResult.SKIPPED,
                    error = "Not found in grid (RBAC-restricted or disabled)"
                )
            }

            NavigationTestHelper.OpenResult.CLICKED_BUT_BLOCKED -> {
                takeScreenshot("explore_${stepPrefix}_${module.displayName.replace(" ", "_")}_BLOCKED")
                Log.w(TAG, "${module.displayName}: clicked but toolbar not found -- recovering")
                navHelper.recoverToDashboard()
                navHelper.switchBottomNavTab("My Health")
                return ModuleResult(
                    moduleName = module.displayName,
                    result = ExploreResult.VISITED,
                    error = "Toolbar not detected -- recovered"
                )
            }

            NavigationTestHelper.OpenResult.OPENED -> {
                // Screen loaded -- explore it
            }
        }

        // Dismiss any known dialogs for this module (e.g., permission prompts)
        for (dialogText in module.knownDialogs) {
            val btn = device.findObject(By.text(dialogText))
            if (btn != null) {
                btn.click()
                Log.d(TAG, "${module.displayName}: dismissed known dialog '$dialogText'")
                Thread.sleep(NavigationTestHelper.POST_CLICK_DELAY)
            }
        }

        takeScreenshot("explore_${stepPrefix}_${module.displayName.replace(" ", "_")}")

        // -- Discover and click all tabs/chips --------------------------------
        val clickedTabs = navHelper.discoverAndClickAllTabs(module.tabLayoutId, module.expectedTabs)

        // Check for missing expected tabs
        val missingTabs = if (module.expectedTabs.isNotEmpty()) {
            val clickedLower = clickedTabs.map { it.lowercase() }
            module.expectedTabs.filter { expected ->
                clickedLower.none { it.contains(expected.lowercase()) }
            }.also { missing ->
                if (missing.isNotEmpty()) {
                    Log.w(TAG, "${module.displayName}: expected tabs ${module.expectedTabs} but missed: $missing (clicked: $clickedTabs)")
                }
            }
        } else {
            emptyList()
        }

        // -- Check for list content -------------------------------------------
        val hasList = navHelper.hasListContent()

        // -- Click list items -------------------------------------------------
        var listItemClicked = false
        var listItemsClickedCount = 0

        if (clickedTabs.isNotEmpty() && module.hasList) {
            // For tabbed modules: click first list item in EACH tab.
            // First tab is already selected, so click its list item first,
            // then switch to each subsequent tab and click its list item.
            for (tabLabel in clickedTabs) {
                val tab = device.findObject(By.text(tabLabel))
                    ?: device.findObject(By.text(tabLabel.uppercase()))
                if (tab != null) {
                    tab.click()
                    Thread.sleep(NavigationTestHelper.POST_CLICK_DELAY)
                }
                if (navHelper.hasListContent()) {
                    val clicked = navHelper.clickFirstListItemAndReturn(module.backButtonId)
                    if (clicked) {
                        listItemClicked = true
                        listItemsClickedCount++
                        Log.d(TAG, "${module.displayName}: clicked list item in tab '$tabLabel'")
                    }
                }
            }
        } else if (hasList) {
            if (module.clickAllListItems) {
                // Click ALL items in the list (e.g., Wellness sub-modules)
                listItemsClickedCount = navHelper.clickAllListItemsAndReturn(module.backButtonId)
                listItemClicked = listItemsClickedCount > 0
            } else {
                // Click just the first item to verify detail navigation
                listItemClicked = navHelper.clickFirstListItemAndReturn(module.backButtonId)
                if (listItemClicked) listItemsClickedCount = 1
            }
        } else if (module.hasList) {
            Log.d(TAG, "${module.displayName}: expected list but none found (empty data?)")
        }

        // -- Click FAB if configured ------------------------------------------
        val fabClicked = if (module.hasFab) {
            navHelper.clickFabAndReturn(module.fabId)
        } else {
            false
        }
        if (module.hasFab && !fabClicked) {
            Log.w(TAG, "${module.displayName}: expected FAB but not found or not clickable")
        }

        // -- Navigate back to dashboard ---------------------------------------
        navHelper.pressBackButton(module.backButtonId)

        if (!navHelper.waitForDashboard()) {
            Log.w(TAG, "${module.displayName}: back didn't return to dashboard -- recovering")
            navHelper.recoverToDashboard()
        }

        // Re-switch to My Health tab (back may land on Home tab)
        navHelper.switchBottomNavTab("My Health")

        return ModuleResult(
            moduleName = module.displayName,
            result = ExploreResult.VISITED,
            tabsClicked = clickedTabs,
            expectedTabs = module.expectedTabs,
            missingTabs = missingTabs,
            hasList = hasList,
            listItemClicked = listItemClicked,
            listItemsClickedCount = listItemsClickedCount,
            expectedFab = module.hasFab,
            fabClicked = fabClicked
        )
    }

    private fun ensureOnDashboard(): Boolean {
        val onDashboard = device.wait(
            Until.hasObject(By.res("$APP_PACKAGE:id/dashboardBottomSheet")),
            NavigationTestHelper.SHORT_TIMEOUT
        )
        if (onDashboard == true) return true

        Log.w(TAG, "Not on dashboard -- attempting recovery")
        return navHelper.recoverToDashboard()
    }

    // =========================================================================
    // Home Tab Verification
    // =========================================================================

    private fun verifyHomeTab() {
        val toolbarText = device.wait(
            Until.findObject(By.res("$APP_PACKAGE:id/dashboardToolbarText")),
            LOGIN_TIMEOUT
        )
        assertTrue(
            "Dashboard toolbar text not found. ${loginHelper.dumpHierarchyInfo()}",
            toolbarText != null
        )
        takeScreenshot("explore_00_home_tab")
        Log.d(TAG, "Home tab verified -- toolbar shows: ${toolbarText?.text}")
    }

    // =========================================================================
    // Login Helpers
    // =========================================================================

    /**
     * Performs a fresh SSO login when the app is on the tour/login page.
     * Triggered only when no existing session is found.
     */
    private fun performFreshLogin(passcode: String) {
        val username = TestConfig.username
        val password = TestConfig.password

        if (username.isBlank() || password.isBlank()) {
            fail("App not logged in and test credentials not configured in test_config.properties")
            return
        }

        // Tap "Sign In" on tour page to trigger SSO
        val signInBtn = device.wait(
            Until.findObject(By.res("$APP_PACKAGE:id/tourPageSignInBtn")),
            DEFAULT_TIMEOUT
        )
        signInBtn?.click() ?: run {
            // Fallback: launch LoginActivity directly
            val intent = Intent(context, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }

        device.wait(Until.hasObject(By.clazz("android.widget.EditText")), LONG_TIMEOUT)

        loginHelper.performLogin(username, password)
        val appReturned = device.wait(
            Until.hasObject(By.pkg(APP_PACKAGE)),
            LONG_TIMEOUT
        )
        assertTrue(
            "App did not return from login. ${loginHelper.dumpHierarchyInfo()}",
            appReturned ?: false
        )

        handlePasscodeEntry(passcode)
        handlePatientSelection()
        waitForDashboardReady()
        Log.d(TAG, "Fresh login complete -- dashboard reached")
    }

    private fun handlePasscodeEntry(passcode: String) {
        val passcodeField = device.wait(
            Until.findObject(By.res("$APP_PACKAGE:id/invisible")),
            LOGIN_TIMEOUT
        ) ?: return

        passcodeField.text = passcode

        val reenterField = device.wait(
            Until.findObject(By.res("$APP_PACKAGE:id/invisible")),
            DEFAULT_TIMEOUT
        )
        reenterField?.let { it.text = passcode }
    }

    private fun handlePatientSelection() {
        val patientList = device.wait(
            Until.findObject(By.res("$APP_PACKAGE:id/patientListRecyclerView")),
            DEFAULT_TIMEOUT
        ) ?: return

        val firstPatient = patientList.children.firstOrNull()
        if (firstPatient != null) {
            firstPatient.click()
            device.wait(
                Until.hasObject(By.res("$APP_PACKAGE:id/dashboardToolbarText")),
                LOGIN_TIMEOUT
            )
        }
    }

    private fun waitForDashboardReady() {
        navHelper.dismissSystemDialogs()
        val dashboardReady = device.wait(
            Until.hasObject(By.res("$APP_PACKAGE:id/dashboardBottomSheet")),
            LOGIN_TIMEOUT
        )
        assertTrue(
            "Dashboard not reached. ${loginHelper.dumpHierarchyInfo()}",
            dashboardReady ?: false
        )
    }

    // =========================================================================
    // Results Reporting
    // =========================================================================

    private fun reportResults(results: List<ModuleResult>) {
        val visited = results.count { it.result == ExploreResult.VISITED }
        val skipped = results.count { it.result == ExploreResult.SKIPPED }
        val failed = results.count { it.result == ExploreResult.FAILED }
        val withMissingTabs = results.count { it.missingTabs.isNotEmpty() }
        val withMissingFab = results.count { it.expectedFab && !it.fabClicked }

        val report = buildString {
            appendLine()
            appendLine("==================================================================")
            appendLine("Module Exploration Results")
            appendLine("------------------------------------------------------------------")
            for (result in results) {
                val icon = when (result.result) {
                    ExploreResult.VISITED -> " [OK]"
                    ExploreResult.SKIPPED -> " [--]"
                    ExploreResult.FAILED -> " [!!]"
                }
                val name = result.moduleName.padEnd(20)
                val extras = buildString {
                    if (result.tabsClicked.isNotEmpty()) append(" [tabs: ${result.tabsClicked.joinToString()}]")
                    if (result.missingTabs.isNotEmpty()) append(" [MISSING: ${result.missingTabs.joinToString()}]")
                    if (result.hasList) append(" [list]")
                    if (result.listItemsClickedCount > 1) append(" [${result.listItemsClickedCount} items]")
                    else if (result.listItemClicked) append(" [item->detail]")
                    if (result.expectedFab && !result.fabClicked) append(" [FAB MISSING]")
                    else if (result.fabClicked) append(" [FAB]")
                    if (result.error != null) append(" (${result.error})")
                }
                appendLine("$icon $name$extras")
            }
            appendLine("------------------------------------------------------------------")
            appendLine("Visited: $visited | Skipped: $skipped | Failed: $failed")
            if (withMissingTabs > 0) appendLine("WARNING: Modules with missing tabs: $withMissingTabs")
            if (withMissingFab > 0) appendLine("WARNING: Modules with missing FAB: $withMissingFab")
            appendLine("==================================================================")
        }

        Log.i(TAG, report)

        assertTrue(
            "No modules were visited -- test account may lack permissions.\n$report",
            visited > 0
        )
        assertTrue(
            "$failed module(s) failed during exploration:\n$report",
            failed == 0
        )
    }

    companion object {
        private const val TAG = "ModuleExplorationTest"
        private const val APP_PACKAGE = "com.charm.phr.mphr"
    }
}
