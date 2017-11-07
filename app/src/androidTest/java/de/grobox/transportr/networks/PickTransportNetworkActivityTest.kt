package de.grobox.transportr.networks


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItem
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.map.MapActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
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
        makeScreenshot("1_FirstStart")

        // hack to find region position in list
        val regionList = ArrayList(EnumSet.allOf(Region::class.java))
        val context = InstrumentationRegistry.getTargetContext()
        Collections.sort(regionList) { r1, r2 -> context.getString(r1.getName()).compareTo(context.getString(r2.getName())) }

        // select DB network provider
        onView(withId(R.id.list))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(regionList.indexOf(Region.GERMANY) + 5))
                .perform(actionOnItem<RecyclerView.ViewHolder>(withChild(withText(R.string.np_region_germany)), click()))
                .perform(actionOnItem<RecyclerView.ViewHolder>(withChild(withText(R.string.np_name_db)), click()))
    }

}
