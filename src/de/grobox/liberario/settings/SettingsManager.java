package de.grobox.liberario.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.schildbach.pte.NetworkId;

@ParametersAreNonnullByDefault
public class SettingsManager {

	private final static String PREFS = "LiberarioPrefs";
	private final static String NETWORK_ID_1 = "NetworkId";
	private final static String NETWORK_ID_2 = "NetworkId2";
	private final static String NETWORK_ID_3 = "NetworkId3";

	private final Context context;
	private final SharedPreferences sharedPreferences;

	@Inject
	public SettingsManager(Context context) {
		this.context = context;
		this.sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}

	@Nullable
	public NetworkId getNetworkId(int i) {
		String networkSettingsStr = NETWORK_ID_1;
		if (i == 2) networkSettingsStr = NETWORK_ID_2;
		else if (i == 3) networkSettingsStr = NETWORK_ID_3;

		String networkStr = sharedPreferences.getString(networkSettingsStr, null);
		if (networkStr == null) return null;

		NetworkId networkId;
		try {
			networkId = NetworkId.valueOf(networkStr);
		} catch (IllegalArgumentException e) {
			return null;
		}
		return networkId;
	}

	public void setNetworkId(NetworkId newNetworkId) {
		String networkId1 = sharedPreferences.getString(NETWORK_ID_1, "");
		if (networkId1.equals(newNetworkId.name())) {
			return; // same network selected
		}
		String networkId2 = sharedPreferences.getString(NETWORK_ID_2, "");
		Editor editor = sharedPreferences.edit();
		if (!networkId2.equals(newNetworkId.name())) {
			editor.putString(NETWORK_ID_3, networkId2);
		}
		editor.putString(NETWORK_ID_2, networkId1);
		editor.putString(NETWORK_ID_1, newNetworkId.name());
		editor.apply();
	}

}
