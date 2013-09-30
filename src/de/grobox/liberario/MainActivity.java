/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

package de.grobox.liberario;

import java.util.ArrayList;
import java.util.List;

import de.cketti.library.changelog.ChangeLog;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	MainPagerAdapter mainPagerAdapter;
	ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mViewPager = (ViewPager) findViewById(R.id.pager);

		final ActionBar actionBar = getActionBar();

		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				// show the given tab
				mViewPager.setCurrentItem(tab.getPosition());
			}
			@Override
			public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
				// hide the given tab
			}
			@Override
			public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
				// probably ignore this event
			}
		};

		List<String> fragments = new ArrayList<String>();

		fragments.add(DirectionsFragment.class.getName());
		actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.tab_directions)).setTabListener(tabListener));

		actionBar.addTab(actionBar.newTab().setText(getResources().getString(R.string.tab_fav_trips)).setTabListener(tabListener));
		fragments.add(FavTripsFragment.class.getName());

		mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager(), this, fragments);

		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// When swiping between pages, select the corresponding tab.
				getActionBar().setSelectedNavigationItem(position);
			}
		});

		mViewPager.setAdapter(mainPagerAdapter);

		// show Changelog
		ChangeLog cl = new ChangeLog(this, "body { color:white; font-size: 0.9em; background-color: #282828; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }");
		if(cl.isFirstRun() && !cl.isFirstRunEver()) {
			cl.getLogDialog().show();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_settings:
				startActivityForResult(new Intent(this, PickNetworkProviderActivity.class), DirectionsFragment.CHANGED_NETWORK_PROVIDER);

				return true;
			case R.id.action_about:
				startActivity(new Intent(this, AboutActivity.class));

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == DirectionsFragment.CHANGED_NETWORK_PROVIDER) {
			// call the DirectionsFragment's activity to handle the request there
			Fragment fragment = getSupportFragmentManager().getFragments().get(0);
			fragment.onActivityResult(requestCode, resultCode, intent);
		}
	}

}
