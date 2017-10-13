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

import java.util.Date;
import java.util.EnumSet;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NetworkProvider.Optimize;
import de.schildbach.pte.NetworkProvider.WalkSpeed;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryTripsResult;

@ParametersAreNonnullByDefault
class TripsLoader extends AsyncTaskLoader<QueryTripsResult> {

	private final String TAG = getClass().getName();

	private final TransportNetworkManager manager;
	private final Location from, to;
	@Nullable
	private final Location via;
	private final Date date;
	private final boolean departure;

	TripsLoader(Context context, TransportNetworkManager manager, Location from, @Nullable Location via, Location to, Date date, boolean departure) {
		super(context);
		this.manager = manager;
		this.from = from;
		this.to = to;
		this.via = via;
		this.date = date;
		this.departure = departure;
	}

	@Nullable
	@Override
	public QueryTripsResult loadInBackground() {
		TransportNetwork network = manager.getTransportNetwork().getValue();
		if (network == null) return null;

		// TODO expose via TransportNetworkManager
		Optimize optimize = TransportrUtils.getOptimize(getContext());
		WalkSpeed walkSpeed = TransportrUtils.getWalkSpeed(getContext());
		EnumSet<Product> products = EnumSet.allOf(Product.class);

		Log.i(TAG, "From: " + from);
		Log.i(TAG, "Via: " + via);
		Log.i(TAG, "To: " + to);
		Log.i(TAG, "Date: " + date);
		Log.i(TAG, "Departure: " + departure);
		Log.i(TAG, "Products: " + products);
		Log.i(TAG, "Optimize for: " + optimize);
		Log.i(TAG, "Walk Speed: " + walkSpeed);

		try {
			NetworkProvider np = network.getNetworkProvider();
			return np.queryTrips(from, via, to, date, departure, products, optimize, walkSpeed, null, null);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
