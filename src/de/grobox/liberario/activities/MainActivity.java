/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.model.interfaces.Nameable;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import de.cketti.library.changelog.ChangeLog;
import de.grobox.liberario.TransportrApplication;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.fragments.AboutMainFragment;
import de.grobox.liberario.fragments.DeparturesFragment;
import de.grobox.liberario.fragments.DirectionsFragment;
import de.grobox.liberario.fragments.FavTripsFragment;
import de.grobox.liberario.fragments.NearbyStationsFragment;
import de.grobox.liberario.fragments.PrefsFragment;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;

public class MainActivity extends AppCompatActivity implements TransportNetwork.Handler {
	static final public int CHANGED_NETWORK_PROVIDER = 1;
	static final public int CHANGED_HOME = 2;

	static final public String ACTION_DIRECTIONS = "de.grobox.liberario.directions";
	static final public String ACTION_DIRECTIONS_PRESET = "de.grobox.liberario.directions.preset";
	static final public String ACTION_DEPARTURES = "de.grobox.liberario.departures";
	static final public String ACTION_NEARBY_LOCATIONS = "de.grobox.liberario.nearby_locations";
	static final public String ACTION_SETTINGS = "de.grobox.liberario.settings";

	private Drawer drawer;
	private AccountHeader accountHeader;
	private Toolbar toolbar;

	private int selectedItem;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize Application Context with all Transport Networks
		((TransportrApplication) getApplicationContext()).initilize(this);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		final TransportNetwork network = Preferences.getTransportNetwork(this);

		// Handle Toolbar
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		// Accounts aka TransportNetworks
		accountHeader = new AccountHeaderBuilder()
             .withActivity(this)
             .withHeaderBackground(R.drawable.account_header_background)
             .withSelectionListEnabled(false)
			 .withThreeSmallProfileImages(true)
             .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
	             @Override
	             public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
		             if(!currentProfile && profile != null && profile instanceof ProfileDrawerItem) {
			             TransportNetwork network = (TransportNetwork) ((ProfileDrawerItem) profile).getTag();
			             if(network != null) {
				             // save new network
				             Preferences.setNetworkId(getContext(), network.getId());

				             // notify everybody of this change
				             onNetworkProviderChanged(network);
			             }
		             }
		             return false;
	             }
             })
			.withOnAccountHeaderSelectionViewClickListener(new AccountHeader.OnAccountHeaderSelectionViewClickListener() {
				@Override
				public boolean onClick(View view, IProfile profile) {
					Intent intent = new Intent(getContext(), PickNetworkProviderActivity.class);
					ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(getCurrentFocus(), 0, 0, 0, 0);
					ActivityCompat.startActivityForResult(MainActivity.this, intent, CHANGED_NETWORK_PROVIDER, options.toBundle());

					return true;
				}
			})
             .build();

		// Drawer
		drawer = new DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(accountHeader)
            .addDrawerItems(
		           new PrimaryDrawerItem().withName(R.string.tab_directions).withIdentifier(R.string.tab_directions).withIcon(TransportrUtils.getTintedDrawable(getContext(), android.R.drawable.ic_menu_directions)),
		           new PrimaryDrawerItem().withName(R.string.tab_fav_trips).withIdentifier(R.string.tab_fav_trips).withIcon(TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_star)),
		           new PrimaryDrawerItem().withName(R.string.tab_departures).withIdentifier(R.string.tab_departures).withIcon(TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_departures)),
		           new PrimaryDrawerItem().withName(R.string.tab_nearby_stations).withIdentifier(R.string.tab_nearby_stations).withIcon(TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_tab_stations)),
		           new DividerDrawerItem(),
		           new PrimaryDrawerItem().withName(R.string.action_settings).withIdentifier(R.string.action_settings).withIcon(TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_settings)),
		           new PrimaryDrawerItem().withName(R.string.action_changelog).withIcon(TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_changelog)),
		           new PrimaryDrawerItem().withName(R.string.action_about).withIdentifier(R.string.action_about).withIcon(TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_about))
            )
            .withOnDrawerListener(new Drawer.OnDrawerListener() {
	                                  @Override
	                                  public void onDrawerOpened(View drawerView) {
		                                  KeyboardUtil.hideKeyboard(MainActivity.this);
	                                  }

	                                  @Override
	                                  public void onDrawerClosed(View drawerView) {}

	                                  @Override
	                                  public void onDrawerSlide(View drawerView, float slideOffset) {}
                                  })
            .withFireOnInitialOnClick(false)
            .withSavedInstance(savedInstanceState)
            .withShowDrawerOnFirstLaunch(true)
            .build();

		drawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
			@Override
			public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
				if(position == -1) {
					// adjust position to first item when -1 for some reason
					position = 0;
				}

				if(drawerItem != null && drawerItem instanceof Nameable) {
					int res = ((Nameable) drawerItem).getName().getTextRes();

					if(res == R.string.action_changelog) {
						// don't select changelog item
						drawer.setSelection(selectedItem, false);

						new HoloChangeLog(getContext()).getFullLogDialog().show();
					} else {
						switchFragment(res);
					}
				}

				return false;
			}
		});

		// Fragments
		if(savedInstanceState == null) {
			final Fragment directionsFragment = new DirectionsFragment();
			final Fragment favTripsFragment = new FavTripsFragment();
			final Fragment departuresFragment = new DeparturesFragment();
			final Fragment nearbyStationsFragment = new NearbyStationsFragment();
			final Fragment prefsFragment = new PrefsFragment();
			final Fragment aboutFragment = new AboutMainFragment();

			// add initial fragment
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.fragment_container, directionsFragment, getString(R.string.tab_directions))
			                           .addToBackStack(getString(R.string.tab_directions))
			                           .commit();

			// add all other fragments hidden
			getSupportFragmentManager().beginTransaction()
			                           .add(R.id.fragment_container, favTripsFragment, getString(R.string.tab_fav_trips))
			                           .hide(favTripsFragment)
			                           .add(R.id.fragment_container, departuresFragment, getString(R.string.tab_departures))
			                           .hide(departuresFragment)
			                           .add(R.id.fragment_container, nearbyStationsFragment, getString(R.string.tab_nearby_stations))
			                           .hide(nearbyStationsFragment)
			                           .add(R.id.fragment_container, prefsFragment, getString(R.string.action_settings))
			                           .hide(prefsFragment)
			                           .add(R.id.fragment_container, aboutFragment, getString(R.string.action_about))
			                           .hide(aboutFragment)
			                           .commit();
		} else {
			// find currently active fragment
			String fragment_tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
			Fragment fragment_old = getSupportFragmentManager().findFragmentByTag(fragment_tag);

			// hide inactive fragments when restoring state
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			for(Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment != fragment_old) {
					transaction.hide(fragment);
				}
			}
			transaction.commit();

			// restore selected drawer item
			selectedItem = savedInstanceState.getInt("selectedItem");
			drawer.setSelection(selectedItem, false);
		}

		if(network != null) {
			toolbar.setSubtitle(network.getName());
			updateDrawerItems(network);
		}

		checkFirstRun(network);

		addAccounts(network);

		if(savedInstanceState != null) {
			processIntent();
		}

		// show Changelog
		HoloChangeLog cl = new HoloChangeLog(this);
		if(cl.isFirstRun() && !cl.isFirstRunEver()) {
			cl.getLogDialog().show();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// remember selected drawer item
		outState.putInt("selectedItem", selectedItem);
	}

	@Override
	public void onBackPressed() {
		String fragment_tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
		Fragment fragment_current = getSupportFragmentManager().findFragmentByTag(fragment_tag);

		// close the drawer first
		if(drawer != null && drawer.isDrawerOpen()) {
			drawer.closeDrawer();
		} else if(fragment_current.getTag().equals(getString(R.string.tab_directions))) {
			finish();
		} else {
			switchFragment(R.string.tab_directions);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		processIntent();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if(requestCode == CHANGED_NETWORK_PROVIDER && resultCode == RESULT_OK) {
			onNetworkProviderChanged(Preferences.getTransportNetwork(this));
		}

		// bounce home location change back to PrefsFragment since it uses animation to call the activity
		// and can't get the result itself at the moment
		if(requestCode == CHANGED_HOME && resultCode == RESULT_OK) {
			for(Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment instanceof PrefsFragment) {
					fragment.onActivityResult(requestCode, resultCode, intent);
				}
			}
		}

	}

	public void onNetworkProviderChanged(TransportNetwork network) {
		// get and set new network name for app bar
		toolbar.setSubtitle(network.getName());

		// update accounts the lazy way
		accountHeader.clear();
		addAccounts(network);

		// update drawer items based on network capabilities
		updateDrawerItems(network);

		// notify the others of change, so call this method for each fragment
		if(getSupportFragmentManager().getFragments() != null) {
			for(Fragment fragment : getSupportFragmentManager().getFragments()) {
				if(fragment instanceof TransportNetwork.Handler) {
					((TransportNetwork.Handler) fragment).onNetworkProviderChanged(network);
				}
			}
		}
	}

	private void updateDrawerItems(TransportNetwork network) {
		// disable sections not supported by new network
		for(IDrawerItem i : drawer.getDrawerItems()) {
			if(i instanceof BaseDrawerItem) {
				BaseDrawerItem item = (BaseDrawerItem) i;

				switch(item.getName().getTextRes()) {
					case R.string.tab_directions:
					case R.string.tab_fav_trips:
						item.withEnabled(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.TRIPS));
						break;
					case R.string.tab_departures:
						item.withEnabled(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.DEPARTURES));
						break;
					case R.string.tab_nearby_stations:
						item.withEnabled(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.NEARBY_LOCATIONS));
						break;
				}
				drawer.updateItem(item);
			}
		}

		if(drawer.getCurrentSelectedPosition() <= drawer.getDrawerItems().size() && drawer.getDrawerItem(drawer.getCurrentSelection()) != null && !drawer.getDrawerItem(drawer.getCurrentSelection()).isEnabled()) {
			// select last section if this one is not supported by current network
			drawer.setSelectionAtPosition(drawer.getDrawerItems().size());
		}
	}

	private void switchFragment(int res) {
		String f = getString(res);

		// remember selected drawer item
		selectedItem = drawer.getCurrentSelection();

		// set fragment name in toolbar
		toolbar.setTitle(f);

		// set network name in toolbar
		if(res != R.string.action_settings && res != R.string.action_about) {
			TransportNetwork network = Preferences.getTransportNetwork(getContext());
			if(network != null) {
				toolbar.setSubtitle(network.getName());
			}
		} else {
			toolbar.setSubtitle(null);
		}

		// switch to fragment
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(f);


		if(fragment != null) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
			                           .show(fragment)
			                           .addToBackStack(f);

			if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
				String fragment_tag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
				Fragment fragment_old = getSupportFragmentManager().findFragmentByTag(fragment_tag);

				if(fragment_old != null && fragment_old != fragment) {
					transaction.hide(fragment_old);
				}

				// Only relevant when CoordinatorLayout is used
				//showToolbar();
			}
			transaction.commit();

			// select the proper drawer item in the drawer
			drawer.setSelection(res, false);
		}
	}

	private void checkFirstRun(TransportNetwork network) {
		// return if no network is set
		if(network == null) {
			Intent intent = new Intent(this, PickNetworkProviderActivity.class);

			// force choosing a network provider
			intent.putExtra("FirstRun", true);

			startActivityForResult(intent, CHANGED_NETWORK_PROVIDER);
		}
		else if(toolbar != null) {
			toolbar.setSubtitle(network.getName());
		}
	}

/*	private void showToolbar() {
		CoordinatorLayout coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
		AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appbar);
		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
		AppBarLayout.Behavior behavior = (AppBarLayout.Behavior) params.getBehavior();

		if(behavior != null && behavior.getTopAndBottomOffset() < 0) {
			behavior.setTopAndBottomOffset(0);
			behavior.onNestedPreScroll(coordinator, appbar, null, 0, 1, new int[2]);
		}
	}
*/
	private void addAccounts(TransportNetwork network) {
		if(network != null) {
			//noinspection deprecation
			ProfileDrawerItem item1 = new ProfileDrawerItem()
					                          .withName(network.getName())
					                          .withEmail(network.getDescription())
					                          .withIcon(getResources().getDrawable(network.getLogo()));
			item1.withTag(network);
			accountHeader.addProfile(item1, accountHeader.getProfiles().size());
		}

		TransportNetwork network2 = Preferences.getTransportNetwork(getContext(), 2);
		if(network2 != null) {
			//noinspection deprecation
			ProfileDrawerItem item2 = new ProfileDrawerItem()
					                          .withName(network2.getName())
					                          .withEmail(network2.getDescription())
					                          .withIcon(getResources().getDrawable(network2.getLogo()));
			item2.withTag(network2);
			accountHeader.addProfile(item2, accountHeader.getProfiles().size());
		}

		TransportNetwork network3 = Preferences.getTransportNetwork(getContext(), 3);
		if(network3 != null) {
			//noinspection deprecation
			ProfileDrawerItem item3 = new ProfileDrawerItem()
					                          .withName(network3.getName())
					                          .withEmail(network3.getDescription())
					                          .withIcon(getResources().getDrawable(network3.getLogo()));
			item3.withTag(network3);
			accountHeader.addProfile(item3, accountHeader.getProfiles().size());
		}
	}

	private void processIntent() {
		final Intent intent = getIntent();

		if(intent != null) {
			final String action = intent.getAction();

			switch(action) {
				case ACTION_DIRECTIONS:
					findDirections((Location) intent.getSerializableExtra("from"), (Location) intent.getSerializableExtra("to"), false);
					break;
				case ACTION_DIRECTIONS_PRESET:
					findDirections((Location) intent.getSerializableExtra("from"), (Location) intent.getSerializableExtra("to"), true);
					break;
				case ACTION_DEPARTURES:
					findDepartures((Location) intent.getSerializableExtra("location"));
					break;
				case ACTION_NEARBY_LOCATIONS:
					findNearbyStations((Location) intent.getSerializableExtra("location"));
					break;
				case ACTION_SETTINGS:
					switchFragment(R.string.action_settings);
					break;
			}
		}
	}

	private void findDirections(Location from, Location to, boolean preset) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

		if(!np.hasCapabilities(NetworkProvider.Capability.TRIPS)) {
			Toast.makeText(getContext(), getString(R.string.error_no_trips_capability), Toast.LENGTH_SHORT).show();
		}

		DirectionsFragment f = (DirectionsFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.tab_directions));

		if(f != null) {
			if(preset) {
				f.presetFromTo(from, to);
			} else {
				f.searchFromTo(from, to);
			}
			switchFragment(R.string.tab_directions);
		}
		else {
			Toast.makeText(getContext(), R.string.error_please_file_ticket, Toast.LENGTH_LONG).show();
		}
	}

	private void findDepartures(Location loc) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

		if(!np.hasCapabilities(NetworkProvider.Capability.DEPARTURES)) {
			Toast.makeText(getContext(), getString(R.string.error_no_departures_capability), Toast.LENGTH_SHORT).show();
		}

		DeparturesFragment f = (DeparturesFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.tab_departures));

		if(f != null) {
			f.searchByLocation(loc);
			switchFragment(R.string.tab_departures);
		}
		else {
			Toast.makeText(getContext(), R.string.error_please_file_ticket, Toast.LENGTH_LONG).show();
		}
	}

	private void findNearbyStations(Location loc) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

		if(!np.hasCapabilities(NetworkProvider.Capability.NEARBY_LOCATIONS)) {
			Toast.makeText(getContext(), getString(R.string.error_no_nearby_locations_capability), Toast.LENGTH_SHORT).show();
		}

		NearbyStationsFragment f = (NearbyStationsFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.tab_nearby_stations));

		if(f != null) {
			f.searchByLocation(loc);
			switchFragment(R.string.tab_nearby_stations);
		}
		else {
			Toast.makeText(getContext(), R.string.error_please_file_ticket, Toast.LENGTH_LONG).show();
		}
	}

	private Context getContext() {
		return this;
	}


	public static class HoloChangeLog extends ChangeLog {
		public static final String DARK_THEME_CSS =
				"body { color: #f3f3f3; font-size: 0.9em; background-color: #282828; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";

		public static final String MATERIAL_THEME_CSS =
				"body { color: #f3f3f3; font-size: 0.9em; background-color: #424242; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";

		public HoloChangeLog(Context context) {
			super(new ContextThemeWrapper(context, R.style.DialogTheme), theme());
		}

		private static String theme() {
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				return MATERIAL_THEME_CSS;
			} else {
				return DARK_THEME_CSS;
			}
		}
	}
}
