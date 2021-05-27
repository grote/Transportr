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

package de.grobox.transportr.map

import android.Manifest
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.data.DbTest
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.TO
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.waitForId
import org.hamcrest.CoreMatchers.anything
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import javax.inject.Inject

@LargeTest
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MapActivityTest : ScreengrabTest() {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(MapActivity::class.java)

    @Rule
    @JvmField
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    @Inject
    lateinit var manager: TransportNetworkManager
    @Inject
    lateinit var locationRepository: LocationRepository
    @Inject
    lateinit var searchesRepository: SearchesRepository

    @Before
    override fun setUp() {
        super.setUp()

        activityRule.runOnUiThread {
            component.inject(this)
            val transportNetwork: TransportNetwork = manager.getTransportNetworkByNetworkId(networkId) ?: throw RuntimeException()
            manager.setTransportNetwork(transportNetwork)
            // ensure networkId got updated before continuing
            assertEquals(networkId, DbTest.getValue(manager.networkId))
        }
    }

    @Test
    @Ignore("currently broken") // TODO fix
    fun favoritesTest() {
        locationRepository.setHomeLocation(getFrom(0))
        locationRepository.setWorkLocation(getTo(0))

        locationRepository.addFavoriteLocation(getFrom(1), FROM)
        locationRepository.addFavoriteLocation(getTo(1), TO)
        locationRepository.addFavoriteLocation(getFrom(2), FROM)
        locationRepository.addFavoriteLocation(getTo(2), TO)

        onView(isRoot()).perform(waitForId(R.id.title))
        locationRepository.favoriteLocations.observe(activityRule.activity, Observer { this.addSavedSearches(it) })

        onView(isRoot()).perform(waitForId(R.id.fromIcon))
        sleep(2500)

        makeScreenshot("2_SavedSearches")
    }

    @Test
    fun searchLocationShowDeparturesTest() {
        // search for station
        onView(withId(R.id.location))
            .perform(typeText(departureStation))

        // click station
        onData(anything())
            .inRoot(isPlatformPopup())
            .atPosition(0)
            .perform(click())

        // assert bottom sheet is shown
        onView(withId(R.id.bottomSheet))
            .check(matches(isDisplayed()))
        onView(withId(R.id.locationName))
            .check(matches(withText(departureStation)))
        onView(withId(R.id.locationIcon))
            .check(matches(isDisplayed()))

        // expand bottom sheet
        onView(withId(R.id.locationName))
            .perform(click())
        onView(withId(R.id.departuresButton))
            .check(matches(isDisplayed()))

        // wait for departures to load and then make screenshot
        onView(isRoot()).perform(waitForId(R.id.linesLayout))
        makeScreenshot("5_Station")

        // click departure button
        onView(withId(R.id.departuresButton))
            .perform(click())

        onView(isRoot()).perform(waitForId(R.id.line))
        makeScreenshot("6_Departures")
    }

    private fun addSavedSearches(list: List<FavoriteLocation>?) {
        if (list == null || list.size < 4) return
        Thread {
            // copy the list to avoid it changing mid-flight
            val locations = list.toList()
            for (i in 1 until locations.size step 2) {
                val uid = searchesRepository.storeSearch(locations[i - 1], null, locations[i])
                if (i == 1) {
                    val item = FavoriteTripItem(uid, locations[i - 1], null, locations[i])
                    item.favorite = true
                    searchesRepository.updateFavoriteState(item)
                }
            }
        }.start()
    }

}
