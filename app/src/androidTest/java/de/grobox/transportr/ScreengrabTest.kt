package de.grobox.transportr

import android.support.annotation.CallSuper
import android.support.test.InstrumentationRegistry
import android.util.Log
import de.schildbach.pte.NetworkId
import org.junit.Before
import org.junit.ClassRule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule

abstract class ScreengrabTest {

    companion object {
        @JvmField
        @ClassRule
        val localeTestRule = LocaleTestRule()
    }

    private val app = InstrumentationRegistry.getTargetContext().applicationContext as TestApplication
    val component = app.component as TestComponent

    @Before
    @CallSuper
    open fun setUp() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
    }

    protected fun getNetworkId(): NetworkId {
        return NetworkId.DB;
    }

    protected fun makeScreenshot(filename: String) {
        try {
            Screengrab.screenshot(filename)
        } catch (e: RuntimeException) {
            if (e.message != "Unable to capture screenshot.") throw e
            Log.w("Screengrab", "Permission to write screenshot is missing.")
        }

    }

    protected fun sleep(millis: Int) {
        try {
            Thread.sleep(millis.toLong())
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

}
