/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

import android.util.Log
import androidx.annotation.CallSuper
import androidx.test.core.app.ApplicationProvider
import de.grobox.transportr.locations.WrapLocation
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.LocationType.STATION
import org.junit.Before
import org.junit.ClassRule
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import tools.fastlane.screengrab.locale.LocaleUtil.getTestLocale
import java.util.*


abstract class ScreengrabTest {

    companion object {
        @JvmField
        @ClassRule
        val localeTestRule = LocaleTestRule()
    }

    private val app = ApplicationProvider.getApplicationContext<TestApplication>()
    val component = app.component as TestComponent

    @Before
    @CallSuper
    open fun setUp() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
    }

    val networkId: NetworkId = when(Locale.forLanguageTag(getTestLocale())) {
        Locale.FRANCE -> NetworkId.PARIS
        Locale.US -> NetworkId.TLEM
        Locale.forLanguageTag("pt-BR") -> NetworkId.BRAZIL
        else -> NetworkId.DB
    }

    val departureStation = when(networkId) {
        NetworkId.PARIS -> "Gare De Lyon"
        NetworkId.TLEM -> "Waterloo Station"
        NetworkId.BRAZIL -> "Republica"
        else -> "Berlin Hbf"
    }

    protected fun getFrom(i: Int): WrapLocation = when(networkId) {
        NetworkId.PARIS -> when(i) {
            0 -> WrapLocation(STATION, "stop_area:OIF:SA:8739305", 48857298, 2293270, "Paris", "Champ De Mars Tour Eiffel", null)
            1 -> WrapLocation(STATION, "stop_area:OIF:SA:8739100", 48842481, 2321783, "Paris", "Gare Montparnasse", null)
            2 -> WrapLocation(STATION, "stop_area:OIF:SA:8727100", 48880372, 2356597, null, "Gare Du Nord", null)
            else -> throw RuntimeException()
        }
        NetworkId.TLEM -> when(i) {
            0 -> WrapLocation(STATION, "1000119", 51503449, -152036, "London", "Hyde Park Corner", null)
            1 -> getLocation("Blackfriars Pier")
            2 -> getLocation("Moorgate")
            else -> throw RuntimeException()
        }
        NetworkId.BRAZIL -> when(i) {
            0 -> WrapLocation(STATION, "stop_point:OSA:SP:2600672", -23555071, -46662131, "São Paulo", "Paulista", null)
            1 -> getLocation("Pinheiros")
            2 -> getLocation("Vila Madalena")
            else -> throw RuntimeException()
        }
        else -> when(i) {
            0 -> WrapLocation(STATION, "8011155", 52521481, 13410962, null, "Berlin Alexanderplatz", null)
            1 -> getLocation("Zoologischer Garten")
            2 -> getLocation("Kottbusser Tor")
            else -> throw RuntimeException()
        }
    }

    protected fun getTo(i: Int): WrapLocation = when(networkId) {
        NetworkId.PARIS -> when(i) {
            0 -> WrapLocation(STATION, "stop_area:OIF:SA:8711300", 48876241, 2358326, "Paris", "Gare De L'est", null)
            1 -> WrapLocation(STATION, "stop_area:OIF:SA:8754730", 48860751, 2325874, "Paris", "Musée D'orsay", null)
            2 -> WrapLocation(STATION, "stop_area:OIF:SA:59290", 48866800, 2334338, "Paris", "Pyramides", null)
            else -> throw RuntimeException()
        }
        NetworkId.TLEM -> when(i) {
            0 -> WrapLocation(STATION, "1000238", 51509829, -76797, "London", "Tower Hill", null)
            1 -> getLocation("Westminster")
            2 -> getLocation("Temple")
            else -> throw RuntimeException()
        }
        NetworkId.BRAZIL -> when(i) {
            0 -> WrapLocation(STATION, "stop_point:OSA:SP:18876", -23543118, -46589599, "São Paulo", "Belem", null)
            1 -> getLocation("Trianon Masp")
            2 -> getLocation("Anhangabaú")
            else -> throw RuntimeException()
        }
        else -> when(i) {
            0 -> WrapLocation(STATION, "730874", 52507278, 13331992, null, "Checkpoint Charlie", null)
            1 -> getLocation("Bundestag")
            2 -> getLocation("Friedrichstraße")
            else -> throw RuntimeException()
        }
    }

    private fun getLocation(name: String): WrapLocation {
        return WrapLocation(STATION, getRandomId(), 0, 0, null, name, null)
    }

    private fun getRandomId(): String {
        return Random().nextInt().toString()
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
