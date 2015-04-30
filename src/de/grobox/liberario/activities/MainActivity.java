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

package de.grobox.liberario.activities;

import de.cketti.library.changelog.ChangeLog;
import de.grobox.liberario.fragments.DirectionsFragment;
import de.grobox.liberario.fragments.FavTripsFragment;
import de.grobox.liberario.fragments.LiberarioFragment;
import de.grobox.liberario.fragments.LiberarioListFragment;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.fragments.StationsFragment;
import de.grobox.liberario.ui.SlidingTabLayout;
import de.schildbach.pte.NetworkProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends AppCompatActivity {
	MainPagerAdapter mPagerAdapter;

	static final public int CHANGED_NETWORK_PROVIDER = 1;
	static final public int CHANGED_HOME = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) toolbar.setLogo(R.drawable.ic_launcher);
		toolbar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityForResult(new Intent(view.getContext(), PickNetworkProviderActivity.class), CHANGED_NETWORK_PROVIDER);
			}
		});
		setSupportActionBar(toolbar);

		ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);

		// don't recreate the fragments when changing tabs
		mViewPager.setOffscreenPageLimit(3);

		mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);

		SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		mSlidingTabLayout.setCustomTabView(R.layout.tab, R.id.tabimage);
		mSlidingTabLayout.setViewPager(mViewPager);

		// show about screen and make sure a transport network is selected
		checkFirstRun();

		// show Changelog
		HoloChangeLog cl = new HoloChangeLog(this);
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
				startActivity(new Intent(this, PrefsActivity.class));

				return true;
			case R.id.action_changelog:
				new HoloChangeLog(this).getFullLogDialog().show();

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

		if(requestCode == CHANGED_NETWORK_PROVIDER && resultCode == RESULT_OK) {
			NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(this));
			onNetworkProviderChanged(np);
		}
	}

	public void onNetworkProviderChanged(NetworkProvider np) {
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			// get and set new network name for app bar
			toolbar.setSubtitle(Preferences.getNetwork(this));
		}

		if(getSupportFragmentManager().getFragments() != null) {
			// call this method for each fragment
			for(final Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment instanceof LiberarioFragment) {
					((LiberarioFragment) fragment).onNetworkProviderChanged(np);
				} else if(fragment instanceof LiberarioListFragment) {
					((LiberarioListFragment) fragment).onNetworkProviderChanged(np);
				}
			}
		}
	}

	private void checkFirstRun() {
		SharedPreferences settings = getSharedPreferences(Preferences.PREFS, Context.MODE_PRIVATE);
		boolean firstRun = settings.getBoolean("FirstRun", true);

		// show about page at first run
		if(firstRun) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("FirstRun", false);
			editor.apply();

			startActivity(new Intent(this, AboutActivity.class));
		}

		String network = settings.getString("NetworkId", null);

		// return if no network is set
		if(network == null) {
			Intent intent = new Intent(this, PickNetworkProviderActivity.class);
			intent.putExtra("FirstRun", true);
			startActivityForResult(intent, CHANGED_NETWORK_PROVIDER);
		}
		else {
			Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
			if(toolbar != null) toolbar.setSubtitle(network);
		}

	}


	public static class HoloChangeLog extends ChangeLog {
		public static final String DARK_THEME_CSS =
				"body { color: #f3f3f3; font-size: 0.9em; background-color: #282828; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";

		public HoloChangeLog(Context context) {
			super(new ContextThemeWrapper(context, R.style.AppTheme), DARK_THEME_CSS);
		}
	}


	class MainPagerAdapter extends FragmentStatePagerAdapter {
		// Since this is an object collection, use a FragmentStatePagerAdapter,
		// and NOT a FragmentPagerAdapter.
		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			if(i == 1) {
				return new FavTripsFragment();
			} else if(i == 2) {
				return new StationsFragment();
			}
			return new DirectionsFragment();
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int i) {
			if(i == 1) {
				return String.valueOf(R.drawable.ic_action_star);
			} else if(i == 2) {
				return String.valueOf(R.drawable.ic_tab_stations);
			}
			return String.valueOf(android.R.drawable.ic_menu_directions);
		}
	}
}
