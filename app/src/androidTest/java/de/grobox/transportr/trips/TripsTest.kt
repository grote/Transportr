package de.grobox.transportr.trips

import android.content.Intent
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.trips.search.DirectionsActivity
import de.grobox.transportr.utils.Constants
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType.STATION
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
            val transportNetwork: TransportNetwork = manager.getTransportNetworkByNetworkId(getNetworkId()) ?: throw RuntimeException()
            manager.setTransportNetwork(transportNetwork)
        }
    }

    @Test
    fun searchTest() {
        val intent = Intent()
        intent.putExtra(Constants.FROM, getFrom())
        intent.putExtra(Constants.TO, getTo())
        intent.putExtra(Constants.SEARCH, true)
        activityRule.launchActivity(intent)

        sleep(2500)
        makeScreenshot("Trips")

        onView(withId(R.id.list))
                .perform(actionOnItemAtPosition<RecyclerView.ViewHolder>(0, click()))

        sleep(2500)
        makeScreenshot("TripDetails")
    }

    private fun getFrom(): WrapLocation {
        return WrapLocation(Location(STATION, "8011155", 52521481, 13410962, null, "Berlin Alexanderplatz"))
    }

    private fun getTo(): WrapLocation {
        return WrapLocation(Location(STATION, "730874", 52507278, 13331992, null, "Checkpoint Charlie"))
    }

}
