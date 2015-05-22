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

package de.grobox.liberario.tasks;

import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.StationsListActivity;
import de.grobox.liberario.fragments.DeparturesFragment;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.EnumSet;

public class AsyncQueryNearbyStationsTask extends AsyncTask<Void, Void, NearbyLocationsResult> {
	private Context context;
	private Location loc;
	private boolean gps = false;
	private String error = null;
	int maxDistance;
	int maxStations;

	public AsyncQueryNearbyStationsTask(Context context, Location loc, int maxDistance, int maxStations) {
		this.context = context;
		this.loc = loc;
		this.maxDistance = maxDistance;
		this.maxStations = maxStations;
	}

	@Override
	protected NearbyLocationsResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(context));
		// TODO: allow user to specify location types
		EnumSet<LocationType> types = EnumSet.of(LocationType.STATION);

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(context)) {
				return np.queryNearbyLocations(types, loc, maxDistance, maxStations);
			}
			else {
				error = context.getResources().getString(R.string.error_no_internet);
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
				Toast.makeText(context, context.getResources().getString(R.string.error_no_trips_found), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, error, Toast.LENGTH_LONG).show();
			}
			return;
		}

		Intent intent = new Intent(context, StationsListActivity.class);
		intent.setAction("de.grobox.liberario.LIST_NEARBY_STATIONS");
		intent.putExtra("de.schildbach.pte.dto.NearbyStationsResult", result);
		intent.putExtra("de.schildbach.pte.dto.Location", loc);
		intent.putExtra("de.grobox.liberario.activities.StationsListActivity.gps", gps);
		context.startActivity(intent);
	}

	public void setFragment(DeparturesFragment fragment) {

	}

	public void setGPS(boolean gps) {
		this.gps = gps;
	}
}
