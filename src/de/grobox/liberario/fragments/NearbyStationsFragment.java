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

package de.grobox.liberario.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.tasks.AsyncQueryNearbyStationsTask;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NetworkProvider.Capability;
import de.schildbach.pte.dto.Location;

public class NearbyStationsFragment extends LiberarioFragment implements LocationListener {
	private View mView;
	private LocationManager locationManager;
	private boolean loc_found = false;
	public ProgressDialog pd;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_nearbystations, container, false);

		setNearbyStationsView();

		return mView;
	}

	@Override
	public void onNetworkProviderChanged(TransportNetwork network) {
		if(mView == null) return;

		NetworkProvider np = network.getNetworkProvider();

		LinearLayout nearbyStationsLayout = (LinearLayout) mView.findViewById(R.id.nearbyStationsLayout);

		if(np.hasCapabilities(Capability.NEARBY_LOCATIONS)) {
			nearbyStationsLayout.setVisibility(View.VISIBLE);
		} else {
			nearbyStationsLayout.setVisibility(View.GONE);
		}
	}

	private void setNearbyStationsView() {
		// Find Nearby Stations Search Button
		ImageButton btn = (ImageButton) mView.findViewById(R.id.findNearbyStationsButton);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getLocation();
			}
		});
	}

	private void getLocation() {
		pd = new ProgressDialog(getActivity());
		pd.setMessage(getResources().getString(R.string.stations_searching_position));
		pd.setCancelable(false);
		pd.setIndeterminate(true);
		pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				removeUpdates();
				dialog.dismiss();
			}
		});
		pd.show();

		List<String> providers = locationManager.getProviders(true);

		for(String provider : providers) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestSingleUpdate(provider, this, null);

			Log.d(getClass().getSimpleName(), "Register provider for location updates: " + provider);
		}

		// check if there is a non-passive provider available
		if(providers.size() == 0 || (providers.size() == 1 && providers.get(0).equals(LocationManager.PASSIVE_PROVIDER)) ) {
			removeUpdates();
			pd.dismiss();
			Toast.makeText(getActivity(), getResources().getString(R.string.error_no_location_provider), Toast.LENGTH_LONG).show();
		}

		loc_found = false;
	}

	private void removeUpdates() {
		locationManager.removeUpdates(this);
	}

	// Called when a new location is found by the network location provider.
	public void onLocationChanged(android.location.Location location) {
		// no more updates to prevent this method from being called more than once
		removeUpdates();

		if(!loc_found) {
			Log.d(getClass().getSimpleName(), "Found location: " + location.toString());

			// Change progress dialog, because we will now be looking for nearby stations
			pd.setMessage(getResources().getString(R.string.stations_searching_stations));
			pd.getButton(ProgressDialog.BUTTON_NEGATIVE).setEnabled(false);

			int maxDistance;
			int maxStations;

			TextView distanceView = (TextView) mView.findViewById(R.id.distanceView);
			TextView numStationsView = (TextView) mView.findViewById(R.id.numStationsView);

			// Get values from form
			if(distanceView.getText().length() > 0) {
				maxDistance = Integer.valueOf(distanceView.getText().toString());
			} else {
				maxDistance = 1000;
			}
			if(numStationsView.getText().length() > 0) {
				maxStations = Integer.valueOf(numStationsView.getText().toString());
			} else{
				maxStations = 3;
			}

			// Query for nearby stations
			Location loc = Location.coord((int) Math.round(location.getLatitude() * 1E6), (int) Math.round(location.getLongitude() * 1E6));
			AsyncQueryNearbyStationsTask query_stations = new AsyncQueryNearbyStationsTask(getActivity(), loc, maxDistance, maxStations);
			query_stations.setGPS(true);
			query_stations.execute();
		}
		loc_found = true;
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}

}

