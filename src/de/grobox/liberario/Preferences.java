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

package de.grobox.liberario;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import de.schildbach.pte.NetworkId;

public class Preferences {

	public static final String PREFS = "LiberarioPrefs";

	public static String getNetwork(Context context) {
		SharedPreferences settings = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

		return settings.getString("NetworkId", null);
	}

	public static NetworkId getNetworkId(Context context) {
		String network = getNetwork(context);

		// construct NetworkId object from network string
		NetworkId network_id = null;
		try {
			network_id = NetworkId.valueOf(network);
		}
		catch (IllegalArgumentException e) {
			Log.d(context.getClass().getSimpleName(), "Invalid NetworkId in Settings.");
		}

		return network_id;
	}

}
