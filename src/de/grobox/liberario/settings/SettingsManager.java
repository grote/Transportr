package de.grobox.liberario.settings;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;

import de.schildbach.pte.NetworkId;

public class SettingsManager {

	private final static String PREFS = "LiberarioPrefs";

	private final Context context;
	private final SharedPreferences sharedPreferences;

	@Inject
	public SettingsManager(Context context) {
		this.context = context;
		this.sharedPreferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}

	public NetworkId getNetworkId(int i) {
		String str = "";
		if(i == 2) str = "2";
		else if (i == 3) str= "3";

		String networkId = sharedPreferences.getString("NetworkId" + str, null);
		if (networkId == null) return null;
		return NetworkId.valueOf(networkId);
	}

}
