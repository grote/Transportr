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

package de.grobox.liberario.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.util.EnumSet;

import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.fragments.NearbyStationsFragment;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

public class AsyncQueryNearbyStationsTask extends AsyncTask<Void, Void, NearbyLocationsResult> {
	private NearbyStationsFragment fragment;
	private Location loc;
	private String error = null;
	EnumSet<LocationType> types;
	int maxDistance;
	int maxStations;

	private final static String TAG = AsyncQueryNearbyStationsTask.class.getSimpleName();

	public AsyncQueryNearbyStationsTask(NearbyStationsFragment fragment, EnumSet<LocationType> types, Location loc, int maxDistance, int maxStations) {
		this.fragment = fragment;
		this.types = types;
		this.loc = loc;
		this.maxDistance = maxDistance;
		this.maxStations = maxStations;
	}

	@Override
	protected NearbyLocationsResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(fragment.getActivity()));

		Log.d(TAG, "NearbyStation from (" + String.valueOf(maxDistance) + "m #" + maxStations + "): " + loc.toString());

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(fragment.getActivity())) {
				return np.queryNearbyLocations(types, loc, maxDistance, maxStations);
			}
			else {
				error = fragment.getActivity().getResources().getString(R.string.error_no_internet);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();

			if(e.getCause() != null) {
				error = e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
			} else {
				error = e.getClass().getSimpleName() + ": " + e.getMessage();
			}
			return null;
		}
	}

	@Override
	protected void onPostExecute(NearbyLocationsResult result) {
		if(result == null || result.status != NearbyLocationsResult.Status.OK) {
			if(error == null) {
				Toast.makeText(fragment.getActivity(), fragment.getActivity().getResources().getString(R.string.error_no_stations_found), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(fragment.getActivity(), error, Toast.LENGTH_LONG).show();
			}
			fragment.onRefreshError();
			return;
		}

		fragment.addStations(result);
	}

}
