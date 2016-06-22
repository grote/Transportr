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

package de.grobox.liberario;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Product;

public class Preferences {

	public final static String PREFS = "LiberarioPrefs";
	public final static String SHOW_ADV_DIRECTIONS = "ShowAdvDirections";
	public final static String SORT_RECENT_TRIPS_COUNT = "SortRecentTripsCount";
	public final static String THEME = "pref_key_theme";
	public final static String LANGUAGE = "pref_key_language";
	public final static String WALK_SPEED = "pref_key_walk_speed";
	public final static String OPTIMIZE = "pref_key_optimize";
	public final static String EXIT_ON_BACK = "pref_key_exit_app_on_back_press";

	private final static String SELECTED_PRODUCTS = "_selected_products";

	public static String getNetwork(Context context, int i) {
		SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

		String str = "";
		if(i == 2) str = "2";
		else if (i == 3) str= "3";

		return settings.getString("NetworkId" + str, null);
	}

	public static String getNetwork(Context context) {
		return getNetwork(context, 0);
	}

	public static @Nullable TransportNetwork getTransportNetwork(Context context) {
		return getTransportNetwork(context, 0);
	}

	public static @Nullable TransportNetwork getTransportNetwork(Context context, int i) {
		String id = getNetwork(context, i);

		if(id != null && context.getApplicationContext() != null) {
			return ((TransportrApplication) context.getApplicationContext()).getTransportNetworks(context).getTransportNetwork(id);
		} else {
			return null;
		}
	}

	public static NetworkId getNetworkId(Context context) {
		String network = getNetwork(context);

		if(network == null) {
			return null;
		}

		// construct NetworkId object from network string
		NetworkId network_id = null;
		try {
			network_id = NetworkId.valueOf(network);
		}
		catch (IllegalArgumentException e) {
			Log.e(context.getClass().getSimpleName(), "Invalid NetworkId in Settings.");
		}

		return network_id;
	}


	public static boolean getPref(Context context, String pref, boolean defValue) {
		SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

		return settings.getBoolean(pref, defValue);
	}

	public static void setPref(Context context, String pref, boolean value) {
		SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		editor.putBoolean(pref, value);
		editor.commit();
	}

	public static EnumSet<Product> getProducts(Context context) {
		// check if there's a preference for selected products for the current network
		TransportNetwork current_network = getTransportNetwork(context);
		String network_pref = current_network.getId().toString() + SELECTED_PRODUCTS;
		SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		// get the selected products from the pref
		Set<String> string_set = sp.getStringSet(network_pref, null);
		// if there is no pref for the current network or all products are selected return all
		if(string_set == null || string_set.size() == 0 || string_set.size() == Product.values().length){
			return EnumSet.allOf(Product.class);
		}
		// if there is a pref, read the selected products and return them
		else {
			EnumSet<Product> products = EnumSet.noneOf(Product.class);
			for(String s : string_set) {
				products.add(Product.valueOf(s));
			}
			return products;
		}
	}

	public static void setProducts(Context context, EnumSet<Product> products) {
		if(products.size() == Product.values().length) return;
		// get the current network
		TransportNetwork current_network = getTransportNetwork(context);
		String network_id = current_network.getId().toString();
		SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		String network_pref = network_id + SELECTED_PRODUCTS;
		// store the selected products in a string set
		Set<String> product_set = new HashSet<String>();
		for(Product p : products) {
			product_set.add(p.toString());
		}
		// set the pref
		editor.putStringSet(network_pref, product_set);
		editor.apply();
	}

	public static void setNetworkId(Context context, NetworkId id) {
		SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();

		String id1 = settings.getString("NetworkId", "");
		String id2 = settings.getString("NetworkId2", "");

		if(id1.equals(id.name())) {
			// same network selected
			return;
		}

		if(!id2.equals(id.name())) {
			editor.putString("NetworkId3", id2);
		}

		editor.putString("NetworkId2", id1);
		editor.putString("NetworkId", id.name());

		editor.commit();
	}

	public static boolean darkThemeEnabled(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getString(THEME, context.getString(R.string.pref_theme_value_light)).equals(context.getString(R.string.pref_theme_value_dark));
	}

	public static String getLanguage(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getString(LANGUAGE, context.getString(R.string.pref_language_value_default));
	}

	public static String getWalkSpeed(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getString(WALK_SPEED, context.getString(R.string.pref_walk_speed_value_default));
	}

	public static String getOptimize(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getString(OPTIMIZE, context.getString(R.string.pref_optimize_value_default));
	}

	public static boolean exitOnBack(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

		return settings.getBoolean(EXIT_ON_BACK, false);
	}

}
