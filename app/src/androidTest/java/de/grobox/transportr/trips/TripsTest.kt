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

package de.grobox.transportr.trips

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.isRoot
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.trips.search.DirectionsActivity
import de.grobox.transportr.trips.search.DirectionsActivity.Companion.ACTION_SEARCH
import de.grobox.transportr.utils.Constants
import de.grobox.transportr.waitForId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject


@LargeTest
@RunWith(AndroidJUnit4::class)
class TripsTest : ScreengrabTest() {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(DirectionsActivity::class.java, true, false)

    @Inject
    lateinit var manager: TransportNetworkManager

    @Before
    override fun setUp() {
        super.setUp()

        activityRule.runOnUiThread {
            component.inject(this)
            val transportNetwork: TransportNetwork = manager.getTransportNetworkByNetworkId(networkId) ?: throw RuntimeException()
            manager.setTransportNetwork(transportNetwork)
        }
    }

    @Test
    fun searchTest() {
        val intent = Intent()
        intent.action = ACTION_SEARCH
        intent.putExtra(Constants.FROM, getFrom(0))
        intent.putExtra(Constants.TO, getTo(0))
        activityRule.launchActivity(intent)

        // wait for trips to be found
        onView(isRoot()).perform(waitForId(R.id.lines))
        sleep(1000)
        makeScreenshot("3_Trips")

        onView(withId(R.id.list))
            .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        // wait for map to appear and give extra time for it to load
        onView(isRoot()).perform(waitForId(R.id.map))
        sleep(2500)
        makeScreenshot("4_TripDetails")
    }

}
