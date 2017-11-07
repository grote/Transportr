package de.grobox.transportr.map

import android.Manifest
import android.arch.lifecycle.Observer
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.RootMatchers.isPlatformPopup
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import org.hamcrest.CoreMatchers.anything
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
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

    @Inject lateinit var manager: TransportNetworkManager
    @Inject lateinit var locationRepository: LocationRepository
    @Inject lateinit var searchesRepository: SearchesRepository

    @Before
    override fun setUp() {
        super.setUp()

        activityRule.runOnUiThread {
            component.inject(this)
            val transportNetwork: TransportNetwork = manager.getTransportNetworkByNetworkId(getNetworkId()) ?: throw RuntimeException()
            manager.setTransportNetwork(transportNetwork)
        }

        locationRepository.setHomeLocation(getFrom(0))
        locationRepository.setWorkLocation(getTo(0))

        locationRepository.addFavoriteLocation(getFrom(1), FROM)
        locationRepository.addFavoriteLocation(getTo(1), FROM)
        locationRepository.addFavoriteLocation(getFrom(2), FROM)
        locationRepository.addFavoriteLocation(getTo(2), FROM)
        locationRepository.favoriteLocations.observe(activityRule.activity, Observer { this.addSavedSearches(it) })
    }

    @Test
    fun favoritesTest() {
        sleep(1500)
        makeScreenshot("2_SavedSearches")
    }

    @Test
    fun searchLocationShowDeparturesTest() {
        // search for station
        onView(withId(R.id.location))
                .perform(typeText("Berlin Hbf"))

        // click station
        onData(anything())
                .inRoot(isPlatformPopup())
                .atPosition(0)
                .perform(click())

        // assert bottom sheet is shown
        onView(withId(R.id.bottomSheet))
                .check(matches(isDisplayed()))
        onView(withId(R.id.locationName))
                .check(matches(withText("Berlin Hbf")))
        onView(withId(R.id.locationIcon))
                .check(matches(isDisplayed()))

        // expand bottom sheet
        onView(withId(R.id.locationName))
                .perform(click())
        onView(withId(R.id.departuresButton))
                .check(matches(isDisplayed()))

        // wait for departures to load and then make screenshot
        sleep(1500)
        makeScreenshot("5_Station")

        // click departure button
        onView(withId(R.id.departuresButton))
                .perform(click())

        sleep(1500)
        makeScreenshot("6_Departures")
    }

    private fun addSavedSearches(list: List<FavoriteLocation>?) {
        if (list == null) return
        else Thread {
            for (i in 1 until list.size step 2) {
                searchesRepository.storeSearch(0, list[i - 1], null, list[i])
                if (i == 1) {
                    val item = FavoriteTripItem(1, list[i - 1], null, list[i])
                    item.favorite = true
                    searchesRepository.updateFavoriteState(item)
                }
            }
        }.start()
    }

}
