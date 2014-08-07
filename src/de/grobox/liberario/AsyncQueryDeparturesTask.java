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

import java.io.IOException;

import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.QueryDeparturesResult;

import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

public class AsyncQueryDeparturesTask extends AsyncTask<Void, Void, QueryDeparturesResult> {
	private StationsListActivity activity;
	private View view;
	private String station;
	private String error = null;
	private int max_departures;

	public AsyncQueryDeparturesTask(StationsListActivity activity, View view, String station, int max_departures) {
		this.activity = activity;
		this.view = view;
		this.station = station;
		this.max_departures = max_departures;
	}

	@Override
	protected QueryDeparturesResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(activity));

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(activity)) {
				return np.queryDepartures(station, max_departures, true);
			}
			else {
				error = activity.getResources().getString(R.string.error_no_internet);
				return null;
			}
		} catch (IOException e) {
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
	protected void onPostExecute(QueryDeparturesResult result) {
		if(result == null || result.status != QueryDeparturesResult.Status.OK) {
			if(error == null) {
				Toast.makeText(activity, activity.getResources().getString(R.string.error_no_departures_found), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
			}
			return;
		}

		activity.addDepartures(view, result);
	}

}
