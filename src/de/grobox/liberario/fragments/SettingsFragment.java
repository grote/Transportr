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

package de.grobox.liberario.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.PickNetworkProviderActivity;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener, HomePickerDialogFragment.OnHomeChangedListener {

	public static final String TAG = "de.grobox.liberario.settings";

	Preference network_pref;
	Preference home;

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String s) {
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		TransportNetwork network = Preferences.getTransportNetwork(getActivity());

		// Fill in current home location if available
		network_pref = findPreference("pref_key_network");
		if(network != null) network_pref.setSummary(network.getName());

		network_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
			    Intent intent = new Intent(getActivity(), PickNetworkProviderActivity.class);

//				View view = preference.getView(null, null);
				View view = getView();
				if(view != null) view = view.findFocus();

				ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getX(), (int) view.getY(), 0, 0);
				ActivityCompat.startActivityForResult(getActivity(), intent, MainActivity.CHANGED_NETWORK_PROVIDER, options.toBundle());

				return true;
			}
		});

		home = findPreference("pref_key_home");
		home.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// show home picker dialog
				HomePickerDialogFragment setHomeFragment = HomePickerDialogFragment.newInstance();
				setHomeFragment.setOnHomeChangedListener(SettingsFragment.this);
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				setHomeFragment.show(ft, HomePickerDialogFragment.TAG);

				return true;
			}
		});

		// Fill in current home location if available
		if(network != null) setHome(null);
	}

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		Fragment homePicker = getActivity().getSupportFragmentManager().findFragmentByTag(HomePickerDialogFragment.TAG);
		if(homePicker != null && homePicker.isAdded()) {
			((HomePickerDialogFragment) homePicker).setOnHomeChangedListener(this);
		}
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
	public void onHomeChanged(Location home) {
		setHome(home);
	}

	private void setHome(Location home) {
		if(home == null) {
			home = RecentsDB.getHome(getActivity());
		}
		if(home != null) {
			this.home.setSummary(TransportrUtils.getFullLocName(home));
		} else {
			this.home.setSummary(getResources().getString(R.string.location_home));
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if(requestCode == MainActivity.CHANGED_NETWORK_PROVIDER && resultCode == Activity.RESULT_OK) {
			((MainActivity) getActivity()).onNetworkProviderChanged();
		}
	}

	private void reload() {
		getActivity().recreate();
	}
}
