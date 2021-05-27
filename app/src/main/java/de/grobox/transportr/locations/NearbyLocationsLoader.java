/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.locations;

import android.content.Context;
import android.os.Bundle;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import java.util.EnumSet;

import de.grobox.transportr.networks.TransportNetwork;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static de.grobox.transportr.utils.Constants.LOCATION;

public class NearbyLocationsLoader extends AsyncTaskLoader<NearbyLocationsResult> {

	private final String TAG = getClass().getName();
	private final static String MAX_DISTANCE = "maxDistance";

	private final TransportNetwork network;
	private final Location location;
	private final static EnumSet<LocationType> types = EnumSet.of(LocationType.STATION);
	private final int maxDistance;

	public NearbyLocationsLoader(Context context, TransportNetwork network, Bundle args) {
		super(context);
		this.network = network;

		this.location = (Location) args.getSerializable(LOCATION);
		this.maxDistance = args.getInt(MAX_DISTANCE);
	}

	@Override
	public NearbyLocationsResult loadInBackground() {
		NetworkProvider np = network.getNetworkProvider();

		Log.i(TAG, "NearbyStation from (" + String.valueOf(maxDistance) + "): " + location.toString());

		try {
			return np.queryNearbyLocations(types, location, maxDistance, 0);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Bundle getBundle(Location location, int maxDistance) {
		Bundle args = new Bundle();
		args.putSerializable(LOCATION, location);
		args.putInt(MAX_DISTANCE, maxDistance);
		return args;
	}

}
