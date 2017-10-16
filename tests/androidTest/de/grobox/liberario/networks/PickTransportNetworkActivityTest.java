package de.grobox.liberario.networks;


import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.grobox.liberario.R;
import de.grobox.liberario.map.MapActivity;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class PickTransportNetworkActivityTest {

	@Rule
	// when app data is cleared, this should open PickTransportNetworkActivity
	public ActivityTestRule<MapActivity> mActivityTestRule = new ActivityTestRule<>(MapActivity.class);

	@ClassRule
	public static final LocaleTestRule localeTestRule = new LocaleTestRule();

	@Test
	public void firstRunTest() {
		onView(withId(R.id.firstRunTextView)).check(matches(withText(R.string.pick_network_first_run)));

		Screengrab.screenshot("PickTransportNetwork");

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
