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
import de.grobox.liberario.LiberarioApplication;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.fragments.AboutMainFragment;
import de.grobox.liberario.fragments.DirectionsFragment;
import de.grobox.liberario.fragments.FavTripsFragment;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.fragments.NearbyStationsFragment;
import de.grobox.liberario.fragments.PrefsFragment;
import de.grobox.liberario.fragments.DeparturesFragment;

import de.schildbach.pte.NetworkProvider;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;
import it.neokree.materialnavigationdrawer.elements.MaterialAccount;
import it.neokree.materialnavigationdrawer.elements.MaterialSection;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialAccountListener;
import it.neokree.materialnavigationdrawer.elements.listeners.MaterialSectionListener;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;

public class MainActivity extends MaterialNavigationDrawer implements TransportNetwork.Handler {
	static final public int CHANGED_NETWORK_PROVIDER = 1;
	static final public int CHANGED_HOME = 2;

	private TransportNetwork anet1;
	private TransportNetwork anet2;
	private TransportNetwork anet3;

	private boolean clicked_account = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Initialize Application Context with all Transport Networks
		((LiberarioApplication) getApplicationContext()).initilize(this);

		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.NavigationDrawerTheme);
		} else {
			setTheme(R.style.NavigationDrawerTheme_Light);
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public void init(Bundle savedInstanceState) {
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
		TransportNetwork network = Preferences.getTransportNetwork(this);

		checkFirstRun(network);

		addAccounts(network);

		// TODO make this work on first run when network == null

		if(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.TRIPS)) {
			//noinspection deprecation
			addSection(newSection(getString(R.string.tab_directions), getResources().getDrawable(android.R.drawable.ic_menu_directions), new DirectionsFragment()));
			//noinspection deprecation
			addSection(newSection(getString(R.string.tab_fav_trips), getResources().getDrawable(R.drawable.ic_action_star), new FavTripsFragment()));
		}

		if(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.DEPARTURES)) {
			//noinspection deprecation
			addSection(newSection(getString(R.string.tab_departures), getResources().getDrawable(R.drawable.ic_action_departures), new DeparturesFragment()));
		}

		if(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.NEARBY_LOCATIONS)) {
			//noinspection deprecation
			addSection(newSection(getString(R.string.nearby_stations), getResources().getDrawable(R.drawable.ic_tab_stations), new NearbyStationsFragment()));
		}

		//noinspection deprecation
		addBottomSection(newSection(getString(R.string.action_settings), getResources().getDrawable(R.drawable.ic_action_settings), new PrefsFragment()));
		//noinspection deprecation
		addBottomSection(newSection(getResources().getString(R.string.action_about) + " " + getResources().getString(R.string.app_name), getResources().getDrawable(R.drawable.ic_action_about), new AboutMainFragment()));
		addBottomSection(newSection(getString(R.string.action_changelog), getResources().getDrawable(R.drawable.ic_action_changelog), new MaterialSectionListener() {
			@Override
			public void onClick(MaterialSection materialSection) {
				new HoloChangeLog(getContext()).getFullLogDialog().show();
				materialSection.unSelect();
			}
		}));

		setAccountListener(new MaterialAccountListener() {
			@Override
			public void onAccountOpening(MaterialAccount materialAccount) {
				Intent intent = new Intent(getContext(), PickNetworkProviderActivity.class);
				ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(getCurrentFocus(), 0, 0, 0, 0);
				ActivityCompat.startActivityForResult(MainActivity.this, intent, CHANGED_NETWORK_PROVIDER, options.toBundle());
			}

			@Override
			public void onChangeAccount(MaterialAccount materialAccount) {
				// remember that this change came from the drawer
				clicked_account = true;

				// find out to which network we just switched
				TransportNetwork network;
				if(materialAccount.getTitle().equals(anet1.getName())) {
					network = anet1;
				} else if(materialAccount.getTitle().equals(anet2.getName())) {
					network = anet2;
				} else {
					network = anet3;
				}

				// save new network
				Preferences.setNetworkId(getContext(), network.getId());

				// notify everybody of this change
				onNetworkProviderChanged(network);
			}
		});

		// show Changelog
		HoloChangeLog cl = new HoloChangeLog(this);
		if(cl.isFirstRun() && !cl.isFirstRunEver()) {
			cl.getLogDialog().show();
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if(requestCode == CHANGED_NETWORK_PROVIDER && resultCode == RESULT_OK) {
			onNetworkProviderChanged(Preferences.getTransportNetwork(this));
		}

		// bounce home location change back to PrefsFragment since it uses animation to call the activity
		// and can't get the result itself at the moment
		if(requestCode == CHANGED_HOME && resultCode == RESULT_OK) {
			for(Object section : getSectionList()) {
				Object obj = ((MaterialSection) section).getTargetFragment();

				if(obj instanceof PrefsFragment) {
					((PrefsFragment) obj).onActivityResult(requestCode, resultCode, intent);
				}
			}
		}

	}

	public void onNetworkProviderChanged(TransportNetwork network) {
		Toolbar toolbar = getToolbar();
		if(toolbar != null) {
			// get and set new network name for app bar
			toolbar.setSubtitle(network.getName());
		}

		// nothing changed, so bail out
		if(network.equals(anet1)) return;

		// switch around the current networks
		if(anet2 != network) {
			anet3 = anet2;
		}
		if(anet1 != network) {
			anet2 = anet1;
		}
		anet1 = network;

		// TODO remove sections not supported by new network

		// switching accounts ourselves is not possible, so do this nasty workaround
		if(!clicked_account) {
			// the material drawer does not return null, but an exception...
			MaterialAccount account3;
			try {
				account3 = getAccountAtCurrentPosition(2);
			} catch(RuntimeException e) {
				account3 = null;
			}
			// the material drawer does not return null, but an exception...
			MaterialAccount account2;
			try {
				account2 = getAccountAtCurrentPosition(1);
			} catch(RuntimeException e) {
				account2 = null;
			}
			MaterialAccount account1 = getAccountAtCurrentPosition(0);

			// move second account to last
			if(account2 != null && !account2.getTitle().equals(network.getName())) {
				if(account3 != null) {
					account3.setTitle(anet3.getName());
					account3.setSubTitle(anet3.getRegion());
					account3.setPhoto(anet3.getLogo());
					account3.setBackground(anet3.getBackground());
				} else if(anet3 != null) {
					addAccount(new MaterialAccount(this.getResources(), anet3.getName(), anet3.getRegion(), anet3.getLogo(), anet3.getBackground()));
				}
			}

			// move former first account to second
			if(!account1.getTitle().equals(network.getName())) {
				if(account2 != null) {
					account2.setTitle(anet2.getName());
					account2.setSubTitle(anet2.getRegion());
					account2.setPhoto(anet2.getLogo());
					account2.setBackground(anet2.getBackground());
				} else if(anet2 != null) {
					addAccount(new MaterialAccount(this.getResources(), anet2.getName(), anet2.getRegion(), anet2.getLogo(), anet2.getBackground()));
				}
			}

			// set data for new first account
			account1.setTitle(anet1.getName());
			account1.setSubTitle(anet1.getRegion());
			account1.setPhoto(anet1.getLogo());
			account1.setBackground(anet1.getBackground());

			// notify the drawer that accounts changed
			notifyAccountDataChanged();
		}

		// reset state, so we know how accounts will be changed next time
		clicked_account = false;

		// notify the others of change, so call this method for each fragment
		for(Object section : getSectionList()) {
			Object obj = ((MaterialSection) section).getTargetFragment();

			if(obj instanceof TransportNetwork.Handler) {
				((TransportNetwork.Handler) obj).onNetworkProviderChanged(network);
			}
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
		else {
			Toolbar toolbar = getToolbar();
			if(toolbar != null) toolbar.setSubtitle(network.getName());

			disableLearningPattern();
		}
	}

	private void addAccounts(TransportNetwork network) {
		if(network == null) {
			// add fake account, so there's something to be changed later. Otherwise crashes
			MaterialAccount account = new MaterialAccount(this.getResources(), "null", "null", R.drawable.ic_placeholder, R.drawable.background_default);
			addAccount(account);
		} else {
			// add current network as first account
			MaterialAccount account = new MaterialAccount(this.getResources(), network.getName(), network.getRegion(), network.getLogo(), network.getBackground());
			addAccount(account);
			anet1 = network;

			TransportNetwork network2 = Preferences.getTransportNetwork(getContext(), 2);
			if(network2 != null) {
				MaterialAccount account2 = new MaterialAccount(this.getResources(), network2.getName(), network2.getRegion(), network2.getLogo(), network2.getBackground());
				addAccount(account2);
				anet2 = network2;
			}

			TransportNetwork network3 = Preferences.getTransportNetwork(getContext(), 3);
			if(network3 != null) {
				MaterialAccount account3 = new MaterialAccount(this.getResources(), network3.getName(), network3.getRegion(), network3.getLogo(), network3.getBackground());
				addAccount(account3);
				anet3 = network3;
			}
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
