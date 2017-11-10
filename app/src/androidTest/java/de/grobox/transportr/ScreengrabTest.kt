/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr

import android.support.annotation.CallSuper
import android.support.test.InstrumentationRegistry
import android.util.Log
import de.grobox.transportr.locations.WrapLocation
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.LocationType.STATION
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

    protected fun getFrom(i: Int): WrapLocation = when(i) {
        0 -> WrapLocation(STATION, "8011155", 52521481, 13410962, null, "Berlin Alexanderplatz", null)
        1 -> WrapLocation(STATION, "idFrom1", 0, 0, null, "Zoologischer Garten", null)
        2 -> WrapLocation(STATION, "idFrom2", 0, 0, null, "Kottbusser Tor", null)
        else -> throw RuntimeException()
    }

    protected fun getTo(i: Int): WrapLocation = when(i) {
        0 -> WrapLocation(STATION, "730874", 52507278, 13331992, null, "Checkpoint Charlie", null)
        1 -> WrapLocation(STATION, "idTo1", 0, 0, null, "Bundestag", null)
        2 -> WrapLocation(STATION, "idTo2", 0, 0, null, "Friedrichstraße", null)
        else -> throw RuntimeException()
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
