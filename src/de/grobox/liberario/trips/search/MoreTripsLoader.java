/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.liberario.trips.search;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.QueryTripsContext;
import de.schildbach.pte.dto.QueryTripsResult;

@ParametersAreNonnullByDefault
class MoreTripsLoader extends AsyncTaskLoader<QueryTripsResult> {

	private final String TAG = getClass().getName();

	private final TransportNetworkManager manager;
	private final @Nullable QueryTripsContext queryTripsContext;
	private final boolean later;

	MoreTripsLoader(Context context, TransportNetworkManager manager, @Nullable QueryTripsContext queryTripsContext, boolean later) {
		super(context);
		this.manager = manager;
		this.queryTripsContext = queryTripsContext;
		this.later = later;
	}

	@Nullable
	@Override
	public QueryTripsResult loadInBackground() {
		TransportNetwork network = manager.getTransportNetwork().getValue();
		if (network == null) return null;

		if (queryTripsContext == null) return null;
		if (later && !queryTripsContext.canQueryLater()) return null;
		if (!later && !queryTripsContext.canQueryEarlier()) return null;

		Log.i(TAG, "QueryTripsContext: " + queryTripsContext.toString());
		Log.i(TAG, "Later: " + later);

		try {
			NetworkProvider np = network.getNetworkProvider();
			return np.queryMoreTrips(queryTripsContext, later);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
