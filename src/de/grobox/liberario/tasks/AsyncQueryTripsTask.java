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

import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.AmbiguousLocationActivity;
import de.grobox.liberario.activities.TripsActivity;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryTripsResult;

public class AsyncQueryTripsTask extends AsyncTask<Void, Void, QueryTripsResult> {

	private Context context;
	private ProgressDialog pd;
	private Location from;
	private Location to;
	private Date date = new Date();
	private boolean departure = true;
	private Set<Product> mProducts = EnumSet.allOf(Product.class);
	private String error = null;

	public AsyncQueryTripsTask(Context context) {
		this.context = context;
	}

	public void setFrom(Location from) {
		this.from = from;
	}

	public void setTo(Location to) {
		this.to = to;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDeparture(boolean b) {
		this.departure = b;
	}

	public void setProducts(Set<Product> products) {
		this.mProducts = products;
	}

	@Override
	protected void onPreExecute() {
		pd = new ProgressDialog(context);
		pd.setMessage(context.getResources().getString(R.string.trip_searching));
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		pd.show();
	}

	@Override
	protected QueryTripsResult doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(context));

		Log.d(getClass().getSimpleName(), "From: " + from.toString());
		Log.d(getClass().getSimpleName(), "To: " + to.toString());
		Log.d(getClass().getSimpleName(), "Date: " + date.toString());

		try {
			if(isNetworkAvailable(context)) {
				return np.queryTrips(from, null, to, date, departure, mProducts, NetworkProvider.Optimize.LEAST_DURATION, NetworkProvider.WalkSpeed.NORMAL, null, null);
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
	protected void onPostExecute(QueryTripsResult result) {
		if(pd != null) {
			pd.dismiss();
		}

		if(result == null) {
			if(error == null) {
				Toast.makeText(context, context.getResources().getString(R.string.error_no_trips_found), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, error, Toast.LENGTH_LONG).show();
			}
			return;
		}

		if(result.status == QueryTripsResult.Status.OK && result.trips.size() > 0) {
			Log.d(getClass().getSimpleName(), result.toString());

			Intent intent = new Intent(context, TripsActivity.class);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult", result);
			intent.putExtra("de.schildbach.pte.dto.Trip.from", from);
			intent.putExtra("de.schildbach.pte.dto.Trip.to", to);
			context.startActivity(intent);
		}
		else if(result.status == QueryTripsResult.Status.AMBIGUOUS) {
			Log.d(getClass().getSimpleName(), "QueryTripsResult is AMBIGUOUS");

			Intent intent = new Intent(context, AmbiguousLocationActivity.class);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult", result);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult.from", from);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult.to", to);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult.date", date);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult.departure", departure);
			context.startActivity(intent);
		}
		else {
			Toast.makeText(context, context.getResources().getString(R.string.error_no_trips_found), Toast.LENGTH_LONG).show();
		}
	}

	static public boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
