package de.grobox.transportr.map

import android.Manifest
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
import de.grobox.transportr.networks.TransportNetwork
import org.hamcrest.CoreMatchers.anything
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MapActivityTest : ScreengrabTest() {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(MapActivity::class.java)

    @Rule
    @JvmField
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun setUp() {
        super.setUp()

        val manager = activityRule.activity.manager
        val transportNetwork: TransportNetwork = manager.getTransportNetworkByNetworkId(getNetworkId()) ?: throw RuntimeException()
        manager.setTransportNetwork(transportNetwork)
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

        // wait for departures to load and then make screenshot
        sleep(1500)
        makeScreenshot("Station")

        // expand bottom sheet
        onView(withId(R.id.locationName))
                .perform(click())
        onView(withId(R.id.departuresButton))
                .check(matches(isDisplayed()))

        onView(withId(R.id.departuresButton))
                .perform(click())

        sleep(1500)
        makeScreenshot("Departures")
    }

}
