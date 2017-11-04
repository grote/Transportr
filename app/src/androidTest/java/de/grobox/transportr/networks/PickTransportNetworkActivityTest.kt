package de.grobox.transportr.networks


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.actionOnItem
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import android.support.test.espresso.matcher.RootMatchers.isPlatformPopup
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.rule.ActivityTestRule
import android.support.test.rule.GrantPermissionRule
import android.support.test.rule.GrantPermissionRule.grant
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import android.test.suitebuilder.annotation.LargeTest
import de.grobox.transportr.R
import de.grobox.transportr.ScreengrabTest
import de.grobox.transportr.map.MapActivity
import org.hamcrest.CoreMatchers.anything
import org.junit.Before
import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.*

@LargeTest
@RunWith(AndroidJUnit4::class)
class PickTransportNetworkActivityTest : ScreengrabTest() {

    @Rule  // when app data is cleared, this should open PickTransportNetworkActivity
    @JvmField
    val activityRule = ActivityTestRule(MapActivity::class.java, false, false)

    @Rule
    @JvmField
    val permissionRule: GrantPermissionRule = grant(ACCESS_FINE_LOCATION)

    companion object {
        @JvmField
        @ClassRule
        val localeTestRule = LocaleTestRule()
    }

    @Before
    fun setUp() {
        Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        resetSharedPreferences()
        activityRule.launchActivity(null)
    }

    @Test
    fun firstRunTest() {
        onView(withId(R.id.firstRunTextView)).check(matches(withText(R.string.pick_network_first_run)))
        makeScreenshot("PickTransportNetwork")

        // hack to find region position in list
        val regionList = ArrayList(EnumSet.allOf(Region::class.java))
        val context = InstrumentationRegistry.getTargetContext()
        Collections.sort(regionList) { r1, r2 -> context.getString(r1.getName()).compareTo(context.getString(r2.getName())) }

        // select DB network provider
        onView(withId(R.id.list))
                .perform(scrollToPosition<RecyclerView.ViewHolder>(regionList.indexOf(Region.GERMANY) + 5))
                .perform(actionOnItem<RecyclerView.ViewHolder>(withChild(withText(R.string.np_region_germany)), click()))
                .perform(actionOnItem<RecyclerView.ViewHolder>(withChild(withText(R.string.np_name_db)), click()))

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

        sleep(1500)
        makeScreenshot("MapActivity-selectedStation")
    }

}
