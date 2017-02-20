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

package de.grobox.liberario.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;
import de.grobox.liberario.data.SpecialLocationDb;
import de.grobox.liberario.favorites.trips.HomePickerDialogFragment;
import de.grobox.liberario.networks.PickTransportNetworkActivity;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager.TransportNetworkChangedListener;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

import static android.app.Activity.RESULT_OK;
import static de.grobox.liberario.utils.Constants.REQUEST_NETWORK_PROVIDER_CHANGE;

@ParametersAreNonnullByDefault
public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener, TransportNetworkChangedListener {

	public static final String TAG = "de.grobox.liberario.settings";

	Preference network_pref;
	Preference home;
	Preference quickhome;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String s) {
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		TransportNetwork network = Preferences.getTransportNetwork(getActivity());

		// Fill in current home location if available
		network_pref = findPreference("pref_key_network");
		if(network != null) network_pref.setSummary(network.getName(getContext()));

		network_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
			    Intent intent = new Intent(getActivity(), PickTransportNetworkActivity.class);
				View view = getView();
				if(view != null) view = view.findFocus();

				ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getX(), (int) view.getY(), 0, 0);
				ActivityCompat.startActivityForResult(getActivity(), intent, REQUEST_NETWORK_PROVIDER_CHANGE, options.toBundle());
				return true;
			}
		});

		// TODO remove from here
		home = findPreference("pref_key_home");
		home.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// show home picker dialog
				HomePickerDialogFragment setHomeFragment = HomePickerDialogFragment.newInstance();
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				setHomeFragment.show(ft, HomePickerDialogFragment.TAG);

				return true;
			}
		});
		// Fill in current home location if available
		if(network != null) setHome(null);

		quickhome = findPreference("pref_key_create_quickhome_shortcut");
		quickhome.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// create launcher shortcut
				Intent addIntent = new Intent();
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, TransportrUtils.getShortcutIntent(getContext()));
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.widget_name_quickhome));
				addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getContext(), R.drawable.ic_quickhome_widget));
				addIntent.putExtra("duplicate", false);
				addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
				getContext().sendBroadcast(addIntent);

				// switch to home-screen to let the user see the new shortcut
				Intent startMain = new Intent(Intent.ACTION_MAIN);
				startMain.addCategory(Intent.CATEGORY_HOME);
				startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(startMain);

				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if(key.equals(Preferences.THEME)) {
			ListPreference themePref = (ListPreference) findPreference(key);
			themePref.setSummary(themePref.getEntry());

			reload();
		}
		else if(key.equals(Preferences.LANGUAGE)) {
			ListPreference langPref = (ListPreference) findPreference(key);
			langPref.setSummary(langPref.getEntry());

			reload();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if (requestCode == REQUEST_NETWORK_PROVIDER_CHANGE && resultCode == RESULT_OK) {
//			TransportNetwork network = manager.getTransportNetwork();
//			if (network != null) onTransportNetworkChanged(network);
		}
	}

	@Override
	public void onTransportNetworkChanged(TransportNetwork network) {
		// TODO
	}

	private void setHome(Location home) {
		if(home == null) {
			home = SpecialLocationDb.getHome(getActivity());
		}
		if(home != null) {
			this.home.setSummary(TransportrUtils.getFullLocName(home));
		} else {
			this.home.setSummary(getResources().getString(R.string.location_home));
		}
	}

	private void reload() {
		getActivity().recreate();
	}

}
