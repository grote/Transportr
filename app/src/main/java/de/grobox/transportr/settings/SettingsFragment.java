/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

package de.grobox.transportr.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.map.MapActivity;
import de.grobox.transportr.networks.PickTransportNetworkActivity;
import de.grobox.transportr.networks.TransportNetwork;
import de.grobox.transportr.networks.TransportNetworkManager;

@ParametersAreNonnullByDefault
public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

	public static final String TAG = SettingsFragment.class.getSimpleName();

	@Inject TransportNetworkManager manager;
	private Preference network_pref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		((TransportrApplication) getActivity().getApplication()).getComponent().inject(this);
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String s) {
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Fill in current transport network if available
		network_pref = findPreference("pref_key_network");
		manager.getTransportNetwork().observe(this, this::onTransportNetworkChanged);

		network_pref.setOnPreferenceClickListener(preference -> {
			Intent intent = new Intent(getActivity(), PickTransportNetworkActivity.class);
			View view = getView();
			if (view != null) view = view.findFocus();

			ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getX(), (int) view.getY(), 0, 0);
			ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
			return true;
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(SettingsManager.THEME)) {
			ListPreference themePref = (ListPreference) findPreference(key);
			themePref.setSummary(themePref.getEntry());

			reload();
		} else if (key.equals(SettingsManager.LANGUAGE)) {
			ListPreference langPref = (ListPreference) findPreference(key);
			langPref.setSummary(langPref.getEntry());

			reload();
		}
	}

	private void onTransportNetworkChanged(TransportNetwork network) {
		network_pref.setSummary(network.getName(getContext()));
	}

	private void reload() {
		// getActivity().recreate() does only recreate SettingActivity

		Intent intent = new Intent(getContext(), MapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		getActivity().startActivity(intent);

		getActivity().finish();
	}

}
