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
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.TransportrApplication;
import de.grobox.liberario.networks.PickTransportNetworkActivity;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.utils.TransportrUtils;

@ParametersAreNonnullByDefault
public class SettingsFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

	public static final String TAG = "de.grobox.liberario.settings";

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

		network_pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference preference) {
			    Intent intent = new Intent(getActivity(), PickTransportNetworkActivity.class);
				View view = getView();
				if(view != null) view = view.findFocus();

				ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getX(), (int) view.getY(), 0, 0);
				ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
				return true;
			}
		});

		Preference quickhome = findPreference("pref_key_create_quickhome_shortcut");
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

	private void onTransportNetworkChanged(TransportNetwork network) {
		network_pref.setSummary(network.getName(getContext()));
	}

	private void reload() {
		getActivity().recreate();
	}

}
