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

package de.grobox.liberario.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v4.content.IntentCompat;

import com.github.machinarius.preferencefragment.PreferenceFragment;

import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.activities.PickNetworkProviderActivity;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.data.FavDB;
import de.schildbach.pte.dto.Location;

public class PrefsFragment extends PreferenceFragment implements TransportNetwork.Handler, SharedPreferences.OnSharedPreferenceChangeListener {

	Preference network;
	Preference home;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// Fill in current home location if available
		network = findPreference("pref_key_network");
		network.setSummary(Preferences.getTransportNetwork(getActivity()).getName());

		network.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
				startActivityForResult(new Intent(getActivity(), PickNetworkProviderActivity.class), MainActivity.CHANGED_NETWORK_PROVIDER);

				return true;
			}
		});


		home = findPreference("pref_key_home");
		home.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Intent intent = new Intent(getActivity(), SetHomeActivity.class);

				if(FavDB.getHome(getActivity()) != null) {
					intent.putExtra("new", false);
				} else {
					intent.putExtra("new", true);
				}

				startActivityForResult(intent, MainActivity.CHANGED_HOME);

				return true;
			}
		});

		// Fill in current home location if available
		setHome();
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
		if (key.equals(Preferences.THEME)) {
			ListPreference themePref = (ListPreference) findPreference(key);
			themePref.setSummary(themePref.getEntry());

			getActivity().finish();
			final Intent intent = getActivity().getIntent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
			getActivity().startActivity(intent);

			// TODO switch back to this fragment
		}
	}

	private void setHome() {
		Location home_loc = FavDB.getHome(getActivity());
		if(home_loc != null) {
			home.setSummary(home_loc.uniqueShortName());
		} else {
			home.setSummary(getResources().getString(R.string.location_home));
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);

		if(requestCode == MainActivity.CHANGED_NETWORK_PROVIDER && resultCode == Activity.RESULT_OK) {
			((MainActivity) getActivity()).onNetworkProviderChanged(Preferences.getTransportNetwork(getActivity()));
		}
		else if(requestCode == MainActivity.CHANGED_HOME && resultCode == Activity.RESULT_OK) {
			// set new home
			setHome();
		}
	}

	public void onNetworkProviderChanged(TransportNetwork network) {
		if(getActivity() != null) {
			// set new network name
			this.network.setSummary(network.getName());

			setHome();
		}
	}
}
