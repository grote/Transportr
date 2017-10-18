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

package de.grobox.transportr.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.BaseDrawerItem;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import java.util.Collections;

import de.grobox.transportr.BuildConfig;
import de.grobox.transportr.R;
import de.grobox.transportr.about.AboutMainFragment;
import de.grobox.transportr.fragments.DeparturesFragment;
import de.grobox.transportr.fragments.DirectionsFragment;
import de.grobox.transportr.fragments.NearbyStationsFragment;
import de.grobox.transportr.fragments.RecentTripsFragment;
import de.grobox.transportr.networks.NetworkProviderFactory;
import de.grobox.transportr.networks.PickTransportNetworkActivity;
import de.grobox.transportr.networks.TransportNetwork;
import de.grobox.transportr.settings.Preferences;
import de.grobox.transportr.settings.SettingsFragment;
import de.grobox.transportr.ui.TransportrChangeLog;
import de.grobox.transportr.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.PromptStateChangeListener;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static de.grobox.transportr.networks.PickTransportNetworkActivity.FORCE_NETWORK_SELECTION;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.Builder;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FINISHED;

@Deprecated
public class MainActivity extends TransportrActivity implements FragmentManager.OnBackStackChangedListener {

	public static final String TAG = MainActivity.class.toString();

	static final public int PR_ACCESS_FINE_LOCATION_NEARBY_STATIONS = 0;
	static final public int PR_ACCESS_FINE_LOCATION_DIRECTIONS = 1;
	static final public int PR_ACCESS_FINE_LOCATION_MAPS = 2;
	static final public int PR_WRITE_EXTERNAL_STORAGE = 3;

	private final static String ONBOARDING_DRAWER = "onboardingDrawer";

	private Drawer drawer;
	private AccountHeader accountHeader;
	private Toolbar toolbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		enableStrictMode();
		setContentView(R.layout.activity_main);

		PreferenceManager.setDefaultValues(MainActivity.this, R.xml.preferences, false);

		final TransportNetwork network = Preferences.getTransportNetwork(this);

		// Handle Toolbar
		toolbar = findViewById(R.id.toolbar);
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
		             if(currentProfile) {
			             return true;
		             } else if(profile != null && profile instanceof ProfileDrawerItem) {
			             TransportNetwork network = (TransportNetwork) ((ProfileDrawerItem) profile).getTag();
			             if(network != null) {
				             // save new network
				             Preferences.setNetworkId(getContext(), network.getId());

				             // notify everybody of this change
				             onNetworkProviderChanged();
			             }
		             }
		             return false;
	             }
             })
             .build();

		// Drawer
		drawer = new DrawerBuilder()
            .withActivity(this)
            .withToolbar(toolbar)
            .withAccountHeader(accountHeader)
            .addDrawerItems(
		            getDrawerItem(DirectionsFragment.TAG, R.drawable.ic_menu_directions),
		            getDrawerItem(RecentTripsFragment.TAG, R.drawable.ic_tab_recents),
		            getDrawerItem(DeparturesFragment.TAG, R.drawable.ic_action_departures),
		            getDrawerItem(NearbyStationsFragment.TAG, R.drawable.ic_tab_stations),
		            new DividerDrawerItem(),
		            getDrawerItem(SettingsFragment.TAG, R.drawable.ic_action_settings),
		            getDrawerItem(TransportrChangeLog.TAG, R.drawable.ic_action_changelog),
		            getDrawerItem(AboutMainFragment.TAG, R.drawable.ic_action_about)
            )
            .withOnDrawerListener(new Drawer.OnDrawerListener() {
	            @Override
	            public void onDrawerOpened(View drawerView) {
		            KeyboardUtil.hideKeyboard(MainActivity.this);
	            }

	            @Override
	            public void onDrawerClosed(View drawerView) {
	            }

	            @Override
	            public void onDrawerSlide(View drawerView, float slideOffset) {
	            }
            })
            .withFireOnInitialOnClick(false)
            .withSavedInstance(savedInstanceState)
            .build();

		if(savedInstanceState == null) {
			// make the user select a transport network, if none is selected
			ensureTransportNetworkAvailable(network);

			// update drawer items to reflect network capabilities
			updateDrawerItems(network);

			// show network name in toolbar subtitle
			if(network != null) {
				toolbar.setSubtitle(network.getName(this));
			}

			// add initial fragment
			getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.fragment_container, new DirectionsFragment(), DirectionsFragment.TAG)
					.commit();
		} else {
			// restore toolbar title
			String tag = getCurrentFragmentTag();
			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setTitle(getFragmentName(tag));

			if(network != null && !tag.equals(AboutMainFragment.TAG) && !tag.equals(SettingsFragment.TAG)) {
				toolbar.setSubtitle(network.getName(this));
			}

			processIntent();
		}

		getSupportFragmentManager().addOnBackStackChangedListener(this);

		// add transport networks to header
		addAccounts(network);

		// show Changelog if something is new
		TransportrChangeLog cl = new TransportrChangeLog(this, Preferences.darkThemeEnabled(this));
		if(cl.isFirstRun() && !cl.isFirstRunEver()) {
			cl.getLogDialog().show();
		}

		// create Android 7.1 shortcut
		registerNougatShortcuts();
	}

	@TargetApi(Build.VERSION_CODES.N_MR1)
	private void registerNougatShortcuts() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
			ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

			ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "quickhome")
					.setShortLabel(getString(R.string.widget_name_quickhome))
					.setIcon(Icon.createWithResource(getContext(), R.drawable.ic_quickhome_widget))
					.setIntent(TransportrUtils.getShortcutIntent(getContext()))
					.build();
			shortcutManager.setDynamicShortcuts(Collections.singletonList(shortcut));
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		showOnboarding();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// remember drawer state such as selected drawer item
		drawer.saveInstanceState(outState);
	}

	@Override
	public void onBackPressed() {
		// close the drawer first if it is open
		if(drawer != null && drawer.isDrawerOpen()) {
			drawer.closeDrawer();
			return;
		}

		// check if next is start screen or use default action
		if(getCurrentFragmentTag().equals(DirectionsFragment.TAG) && Preferences.exitOnBack(this)) {
			// do not go back further if we are at the start screen
			finish();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		processIntent();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch(requestCode) {
				case PR_ACCESS_FINE_LOCATION_NEARBY_STATIONS: {
					NearbyStationsFragment f = (NearbyStationsFragment) getFragment(NearbyStationsFragment.TAG);
					if(f != null) f.activateGPS();
					break;
				}
				case PR_ACCESS_FINE_LOCATION_DIRECTIONS: {
					DirectionsFragment f = (DirectionsFragment) getFragment(DirectionsFragment.TAG);
					if(f != null) f.activateGPS();
					break;
				}
				default:
					Toast.makeText(this, R.string.warning_permission_granted_action, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public void onNetworkProviderChanged() {
		// create an intent for restarting this activity
		Intent intent = new Intent(this, MainActivity.class);
		intent.setAction(getCurrentFragmentTag());
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		finish();
		startActivity(intent);
	}

	private PrimaryDrawerItem getDrawerItem(final String tag, final int icon) {
		Drawer.OnDrawerItemClickListener onClick;
		String name;

		if(tag.equals(TransportrChangeLog.TAG)) {
			onClick = new Drawer.OnDrawerItemClickListener() {
				@Override
				public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
					new TransportrChangeLog(getContext(), Preferences.darkThemeEnabled(getContext())).getFullLogDialog().show();
					return true;
				}
			};
			name = getString(R.string.drawer_changelog);
		}
		else {
			onClick = new Drawer.OnDrawerItemClickListener() {
				@Override
				public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
					drawer.closeDrawer();
					switchFragment(tag);
					return true;
				}
			};
			name = getFragmentName(tag);
		}
		return new PrimaryDrawerItem()
				.withName(name)
				.withTag(tag)
				.withIcon(TransportrUtils.getTintedDrawable(getContext(), icon))
				.withSelectable(false)
				.withOnDrawerItemClickListener(onClick);
	}

	private void updateDrawerItems(TransportNetwork network) {
		if(network == null) return;

		NetworkProvider provider = network.getNetworkProvider();

		// disable sections not supported by new network
		for(IDrawerItem i : drawer.getDrawerItems()) {
			if(i instanceof BaseDrawerItem) {
				BaseDrawerItem item = (BaseDrawerItem) i;
				String tag = (String) item.getTag();

				if(tag.equals(DirectionsFragment.TAG) || tag.equals(RecentTripsFragment.TAG)) {
					item.withEnabled(provider.hasCapabilities(NetworkProvider.Capability.TRIPS));
				}
				else if(tag.equals(DeparturesFragment.TAG)) {
					item.withEnabled(provider.hasCapabilities(NetworkProvider.Capability.DEPARTURES));
				}
				else if(tag.equals(NearbyStationsFragment.TAG)) {
					item.withEnabled(provider.hasCapabilities(NetworkProvider.Capability.NEARBY_LOCATIONS));
				}
				drawer.updateItem(item);
			}
		}

		if(drawer.getCurrentSelectedPosition() <= drawer.getDrawerItems().size() && drawer.getDrawerItem(drawer.getCurrentSelection()) != null && !drawer.getDrawerItem(drawer.getCurrentSelection()).isEnabled()) {
			// select last section if this one is not supported by current network
			drawer.setSelectionAtPosition(drawer.getDrawerItems().size());
		}
	}

	@Override
	public void onBackStackChanged() {
		String tag = getCurrentFragmentTag();

		// select proper drawer item (even when switch was initiated by intent)
		drawer.setSelection(drawer.getDrawerItem(tag), false);

		// set fragment name as toolbar title
		toolbar.setTitle(getFragmentName(tag));

		// set network name in toolbar
		TransportNetwork network = Preferences.getTransportNetwork(getContext());
		if(network != null && !tag.equals(AboutMainFragment.TAG) && !tag.equals(SettingsFragment.TAG)) {
			toolbar.setSubtitle(network.getName(this));
		} else {
			toolbar.setSubtitle(null);
		}
	}

	private void switchFragment(String tag) {
		// get the fragment to switch to
		Fragment fragment = getFragment(tag);

		// switch the fragment
		@SuppressLint("CommitTransaction")
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
				.replace(R.id.fragment_container, fragment, tag);
		if(!getCurrentFragmentTag().equals(tag)) {
			// don't add the same fragment to the back stack twice in a row
			transaction.addToBackStack(tag);
		}
		transaction.commit();
	}

	private String getCurrentFragmentTag() {
		if(getSupportFragmentManager().getBackStackEntryCount() == 0) return DirectionsFragment.TAG;
		return getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
	}

	private Fragment getFragment(String tag) {
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
		if(fragment != null) return fragment;

		if(tag.equals(DirectionsFragment.TAG)) return new DirectionsFragment();
		if(tag.equals(RecentTripsFragment.TAG)) return new RecentTripsFragment();
		if(tag.equals(DeparturesFragment.TAG)) return new DeparturesFragment();
		if(tag.equals(NearbyStationsFragment.TAG)) return new NearbyStationsFragment();
		if(tag.equals(SettingsFragment.TAG)) return new SettingsFragment();
		if(tag.equals(AboutMainFragment.TAG)) return new AboutMainFragment();

		Log.w(TAG, "Could not find fragment: " + tag);
		return null;
	}

	private String getFragmentName(String tag) {
		if(tag.equals(DirectionsFragment.TAG)) return getString(R.string.drawer_directions);
		if(tag.equals(RecentTripsFragment.TAG)) return getString(R.string.drawer_recent_trips);
		if(tag.equals(DeparturesFragment.TAG)) return getString(R.string.drawer_departures);
		if(tag.equals(NearbyStationsFragment.TAG)) return getString(R.string.drawer_nearby_stations);
		if(tag.equals(SettingsFragment.TAG)) return getString(R.string.drawer_settings);
		if(tag.equals(AboutMainFragment.TAG)) return getString(R.string.drawer_about);

		Log.w(TAG, "Could not find string for fragment: " + tag);
		return "";
	}

	private void ensureTransportNetworkAvailable(TransportNetwork network) {
		// return if a network is set
		if(network == null) {
			Intent intent = new Intent(this, PickTransportNetworkActivity.class);

			// force choosing a network provider
			intent.putExtra(FORCE_NETWORK_SELECTION, true);

			startActivity(intent);
		}
	}

	private void addAccounts(TransportNetwork network) {
		if(network != null) {
			ProfileDrawerItem item1 = new ProfileDrawerItem()
					                          .withName(network.getName(this))
					                          .withEmail(network.getDescription(this))
					                          .withIcon(ContextCompat.getDrawable(this, network.getLogo()));
			item1.withTag(network);
			accountHeader.addProfile(item1, accountHeader.getProfiles().size());
		}

		TransportNetwork network2 = Preferences.getTransportNetwork(getContext(), 2);
		if(network2 != null) {
			ProfileDrawerItem item2 = new ProfileDrawerItem()
					                          .withName(network2.getName(this))
					                          .withEmail(network2.getDescription(this))
					                          .withIcon(ContextCompat.getDrawable(this, network2.getLogo()));
			item2.withTag(network2);
			accountHeader.addProfile(item2, accountHeader.getProfiles().size());
		}

		TransportNetwork network3 = Preferences.getTransportNetwork(getContext(), 3);
		if(network3 != null) {
			ProfileDrawerItem item3 = new ProfileDrawerItem()
					                          .withName(network3.getName(this))
					                          .withEmail(network3.getDescription(this))
					                          .withIcon(ContextCompat.getDrawable(this, network3.getLogo()));
			item3.withTag(network3);
			accountHeader.addProfile(item3, accountHeader.getProfiles().size());
		}
	}

	private void processIntent() {
		final Intent intent = getIntent();

		if(intent != null) {
			final String action = intent.getAction();
			if(action == null) return;

			switch(action) {
				case DirectionsFragment.TAG:
					findDirections();
					break;
				case DeparturesFragment.TAG:
					findDepartures();
					break;
				case NearbyStationsFragment.TAG:
					findNearbyStations();
					break;
				case RecentTripsFragment.TAG:
//				case SettingsFragment.TAG:
				case AboutMainFragment.TAG:
					// these fragments do not have special intent actions, so just switch to them
					switchFragment(action);

					// remove the intent (and clear its action) since it was already processed
					// and should not be processed again
					if(getIntent() != null) getIntent().setAction(null);
					setIntent(null);
			}
		}
	}

	private void findDirections() {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

		if(!np.hasCapabilities(NetworkProvider.Capability.TRIPS)) {
			Toast.makeText(getContext(), getString(R.string.error_no_trips_capability), Toast.LENGTH_SHORT).show();
		}
		switchFragment(DirectionsFragment.TAG);
	}

	private void findDepartures() {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

		if(!np.hasCapabilities(NetworkProvider.Capability.DEPARTURES)) {
			Toast.makeText(getContext(), getString(R.string.error_no_departures_capability), Toast.LENGTH_SHORT).show();
		}
		switchFragment(DeparturesFragment.TAG);
	}

	private void findNearbyStations() {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

		if(!np.hasCapabilities(NetworkProvider.Capability.NEARBY_LOCATIONS)) {
			Toast.makeText(getContext(), getString(R.string.error_no_nearby_locations_capability), Toast.LENGTH_SHORT).show();
		}
		switchFragment(NearbyStationsFragment.TAG);
	}

	private void showOnboarding() {
		if(getDefaultSharedPreferences(getContext()).getBoolean(ONBOARDING_DRAWER, true)) {
			View target = null;
			for(int i = 0; i < toolbar.getChildCount(); i++) {
				if(toolbar.getChildAt(i) instanceof AppCompatImageButton) {
					target = toolbar.getChildAt(i);
				}
			}
			if(target == null) return;

			new Builder(this)
					.setTarget(target)
					.setPrimaryText(R.string.onboarding_drawer_title)
					.setSecondaryText(R.string.onboarding_drawer_text)
					.setBackgroundColour(ContextCompat.getColor(MainActivity.this, R.color.primary))
					.setIcon(R.drawable.ic_menu)
					.setIconDrawableColourFilter(ContextCompat.getColor(MainActivity.this, R.color.primary))
					.setPromptStateChangeListener(new PromptStateChangeListener() {
						@Override
						public void onPromptStateChanged(MaterialTapTargetPrompt materialTapTargetPrompt, int i) {
							if (i == STATE_DISMISSED || i == STATE_FINISHED) {
								getDefaultSharedPreferences(getContext())
										.edit()
										.putBoolean(ONBOARDING_DRAWER, false)
										.apply();

							}
						}
					})
					.show();
		}
	}

	private Context getContext() {
		return this;
	}

	private void enableStrictMode() {
		if(!BuildConfig.DEBUG) return;

		StrictMode.ThreadPolicy.Builder threadPolicy = new StrictMode.ThreadPolicy.Builder();
		threadPolicy.detectAll();
		threadPolicy.penaltyLog();
		StrictMode.setThreadPolicy(threadPolicy.build());
		StrictMode.VmPolicy.Builder vmPolicy = new StrictMode.VmPolicy.Builder();
		vmPolicy.detectAll();
		vmPolicy.penaltyLog();
		StrictMode.setVmPolicy(vmPolicy.build());
	}

}
