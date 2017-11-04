package de.grobox.transportr

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.util.Log
import tools.fastlane.screengrab.Screengrab
import java.io.File

abstract class ScreengrabTest {

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

    protected fun resetSharedPreferences() {
        val root = InstrumentationRegistry.getTargetContext().filesDir.parentFile
        val sharedPreferencesFileNames = File(root, "shared_prefs").list()
        for (fileName in sharedPreferencesFileNames) {
            InstrumentationRegistry.getTargetContext().getSharedPreferences(fileName.replace(".xml", ""), Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .commit()
        }
    }

}
