/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

package de.grobox.transportr.networks


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.map.MapActivity
import de.schildbach.pte.NetworkId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@LargeTest
@RunWith(AndroidJUnit4::class)
class PickTransportNetworkActivityTest : ScreengrabTest() {

    @Rule  // when app data is cleared, this should open PickTransportNetworkActivity
    @JvmField
    val activityRule = ActivityTestRule(MapActivity::class.java, true, false)

    @Inject
    lateinit var manager: TransportNetworkManager

    @Before
    override fun setUp() {
        super.setUp()

        activityRule.runOnUiThread {
            component.inject(this)
            manager.clearTransportNetwork()
        }
        activityRule.launchActivity(null)
    }

    @Test
    fun firstRunTest() {
        sleep(500)

        onView(withId(R.id.firstRunTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.pick_network_first_run)))

        // find network position in list
        val context = InstrumentationRegistry.getTargetContext()
        val (continentIndex, countryIndex, networkIndex) = getTransportNetworkPositions(context, getTransportNetwork(NetworkId.DB)!!)
        val countryPos = continentIndex + countryIndex + 1
        val networkPos = countryPos + networkIndex + 1

        onView(withId(R.id.list))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(continentIndex))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(continentIndex, click()))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(countryPos))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(countryPos, click()))

        sleep(500)
        makeScreenshot("1_FirstStart")

        onView(withId(R.id.list))
            .perform(scrollToPosition<RecyclerView.ViewHolder>(networkPos + 5))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(networkPos, click()))
    }

}
