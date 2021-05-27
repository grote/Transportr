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

package de.grobox.transportr.departures;

import android.content.Context;
import android.os.Bundle;
import androidx.loader.content.AsyncTaskLoader;
import android.util.Log;

import java.util.Date;

import de.grobox.transportr.networks.TransportNetwork;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.QueryDeparturesResult;

import static de.grobox.transportr.utils.Constants.DATE;

public class DeparturesLoader extends AsyncTaskLoader<QueryDeparturesResult> {

	private final static String TAG = DeparturesLoader.class.getName();
	private final static String STATION_ID = "stationId";
	private final static String MAX_DEPARTURES = "maxDepartures";

	private final TransportNetwork network;
	private final String stationId;
	private final Date date;
	private final int maxDepartures;

	public DeparturesLoader(Context context, TransportNetwork network, Bundle args) {
		super(context);

		this.network = network;
		this.stationId = args.getString(STATION_ID);
		this.date = (Date) args.getSerializable(DATE);
		this.maxDepartures = args.getInt(MAX_DEPARTURES);
	}

	@Override
	public QueryDeparturesResult loadInBackground() {
		NetworkProvider np = network.getNetworkProvider();

		Log.d(TAG, "Departures (" + maxDepartures + "): " + stationId);
		Log.d(TAG, "Date: " + date.toString());

		try {
			return np.queryDepartures(stationId, date, maxDepartures, false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Bundle getBundle(String stationId, Date date, int maxDepartures) {
		Bundle args = new Bundle();
		args.putString(STATION_ID, stationId);
		args.putSerializable(DATE, date);
		args.putInt(MAX_DEPARTURES, maxDepartures);
		return args;
	}

}
