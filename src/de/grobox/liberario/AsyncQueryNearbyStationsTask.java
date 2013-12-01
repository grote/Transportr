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

import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.NearbyStationsResult;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

public class AsyncQueryNearbyStationsTask extends AsyncTask<Void, Void, NearbyStationsResult> {
	private StationsFragment fragment;
	private Context context;
	private Location loc;
	private String error = null;
	int maxDistance;
	int maxStations;

	public AsyncQueryNearbyStationsTask(StationsFragment fragment, Location loc, int maxDistance, int maxStations) {
		this.fragment = fragment;
		this.context = fragment.getActivity();
		this.loc = loc;
		this.maxDistance = maxDistance;
		this.maxStations = maxStations;
	}

	@Override
	protected NearbyStationsResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(context));

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(context)) {
				return np.queryNearbyStations(loc, maxDistance, maxStations);
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
	protected void onPostExecute(NearbyStationsResult result) {
		if(fragment.pd != null) {
			fragment.pd.dismiss();
		}

		if(result == null || result.status != NearbyStationsResult.Status.OK) {
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
		context.startActivity(intent);
	}

}
