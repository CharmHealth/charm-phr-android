package com.charm.phr.mphr.base

import android.graphics.Bitmap
import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestName
import java.io.File
import java.io.FileOutputStream

open class BaseUITest {

    protected lateinit var device: UiDevice
    protected val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    protected val testContext get() = InstrumentationRegistry.getInstrumentation().context

    @get:Rule
    val testName = TestName()

    companion object {
        const val DEFAULT_TIMEOUT = 10_000L
        const val LOGIN_TIMEOUT = 30_000L
        const val LONG_TIMEOUT = 60_000L
    }

    @Before
    open fun setUp() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wakeUp()
    }

    @After
    open fun tearDown() {
        // Take screenshot if test failed — called manually from subclasses or via TestWatcher
    }

    protected fun takeScreenshot(tag: String = testName.methodName) {
        // Use app-scoped external files dir — no WRITE_EXTERNAL_STORAGE permission needed (API 29+)
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "test_screenshots")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "${tag}_${System.currentTimeMillis()}.png")
        device.takeScreenshot(file)
    }
}
