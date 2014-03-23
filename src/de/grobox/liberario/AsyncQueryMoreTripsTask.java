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
import de.schildbach.pte.dto.QueryTripsContext;
import de.schildbach.pte.dto.QueryTripsResult;

import android.os.AsyncTask;
import android.widget.Toast;

public class AsyncQueryMoreTripsTask extends AsyncTask<Void, Void, QueryTripsResult> {
	private TripsActivity activity;
	private QueryTripsContext qtcontext;
	private Boolean later;
	private String error = null;

	public AsyncQueryMoreTripsTask(TripsActivity activity, QueryTripsContext qtcontext, Boolean later) {
		this.activity = activity;
		this.qtcontext = qtcontext;
		this.later = later;
	}

	@Override
	protected QueryTripsResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(activity.getBaseContext()));

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(activity.getBaseContext())) {
				return np.queryMoreTrips(qtcontext, later);
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
	protected void onPostExecute(QueryTripsResult result) {
		if(result == null || result.trips == null) {
			if(error == null) {
				Toast.makeText(activity, activity.getResources().getString(R.string.error_no_trips_found), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
			}
		}
		else {
			activity.addMoreTrips(result, later);
		}
		activity.onRefreshComplete();
	}

}
