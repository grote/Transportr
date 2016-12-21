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

import java.util.Date;

import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.fragments.DeparturesFragment;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.QueryDeparturesResult;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class AsyncQueryDeparturesTask extends AsyncTask<Void, Void, QueryDeparturesResult> {
	private DeparturesFragment fragment;
	private String stationId;
	private Date date;
	private boolean later;
	private boolean more;
	private int max_departures;
	private String error = null;

	private static final String TAG = DeparturesFragment.class.toString();

	public AsyncQueryDeparturesTask(DeparturesFragment fragment, String stationId, Date date, boolean later, int max_departures, boolean more) {
		this.fragment = fragment;
		this.stationId = stationId;
		this.date = date;
		this.later = later;
		this.max_departures = max_departures;
		this.more = more;
	}

	public AsyncQueryDeparturesTask(DeparturesFragment fragment, String stationId, Date date, boolean later, int max_departures) {
		this(fragment, stationId, date, later, max_departures, false);
	}

	@Override
	protected QueryDeparturesResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(fragment.getActivity()));

		Log.i(getClass().getSimpleName(), "Departures (" + String.valueOf(max_departures) + "): " + stationId);
		Log.i(getClass().getSimpleName(), "Date: " + date.toString());

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(fragment.getActivity())) {
				return np.queryDepartures(stationId, date, max_departures, false);
			}
			else {
				error = fragment.getResources().getString(R.string.error_no_internet);
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
	protected void onPostExecute(QueryDeparturesResult result) {
		if(result == null || result.status != QueryDeparturesResult.Status.OK || result.stationDepartures.size() == 0) {
			if(error == null) {
				Toast.makeText(fragment.getActivity(), fragment.getResources().getString(R.string.error_no_departures_found), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(fragment.getActivity(), error, Toast.LENGTH_LONG).show();
			}
			// although not successful, we are still done
			fragment.onNoResults(later, more);

			return;
		}

		fragment.addDepartures(result, later, more);
	}

}
