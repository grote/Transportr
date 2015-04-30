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

import java.util.List;

import de.grobox.liberario.tasks.AsyncQueryNearbyStationsTask;
import de.grobox.liberario.FavLocation;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.ui.DelayAutoCompleteTextView;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NetworkProvider.Capability;
import de.schildbach.pte.dto.Location;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class StationsFragment extends LiberarioFragment implements LocationListener {
	private View mView;
	private LocationManager locationManager;
	private boolean loc_found = false;
	public ProgressDialog pd;
	LocationInputView.LocationInputViewHolder holder;
	LocationInputView loc;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_stations, container, false);

		NetworkId networkId = Preferences.getNetworkId(getActivity());
		NetworkProvider np = null;

		if(networkId != null) {
			np = NetworkProviderFactory.provider(networkId);
		}

		if(np != null && np.hasCapabilities(Capability.DEPARTURES)) {
			setDeparturesView();
		} else {
			LinearLayout departuresLayout = (LinearLayout) mView.findViewById(R.id.departuresLayout);
			departuresLayout.setVisibility(View.GONE);
		}

		if(np != null && np.hasCapabilities(Capability.NEARBY_LOCATIONS)) {
			setNearbyStationsView();
		} else {
			LinearLayout nearbyStationsLayout = (LinearLayout) mView.findViewById(R.id.nearbyStationsLayout);
			nearbyStationsLayout.setVisibility(View.GONE);
		}

		return mView;
	}

	@Override
	public void onNetworkProviderChanged(NetworkProvider np) {
		if(mView == null) return;

		LinearLayout departuresLayout = (LinearLayout) mView.findViewById(R.id.departuresLayout);

		if(np.hasCapabilities(Capability.DEPARTURES)) {
			departuresLayout.setVisibility(View.VISIBLE);

			// clear favorites for auto-complete
			if(holder.location.getAdapter() != null) {
				((LocationAdapter) holder.location.getAdapter()).resetList();
			}

			// clear text view
			loc.clearLocation();
		} else {
			departuresLayout.setVisibility(View.GONE);
		}

		LinearLayout nearbyStationsLayout = (LinearLayout) mView.findViewById(R.id.nearbyStationsLayout);

		if(np.hasCapabilities(Capability.NEARBY_LOCATIONS)) {
			nearbyStationsLayout.setVisibility(View.VISIBLE);
		} else {
			nearbyStationsLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after new home location was selected, put it right into the input field
		if(resultCode == AppCompatActivity.RESULT_OK && requestCode == MainActivity.CHANGED_HOME) {
			queryForStations(FavDB.getHome(getActivity()));
		}
	}

	private void setDeparturesView() {
		holder = new LocationInputView.LocationInputViewHolder();
		holder.location = (DelayAutoCompleteTextView) mView.findViewById(R.id.location);
		holder.clear = (ImageButton) mView.findViewById(R.id.clearButton);
		holder.progress = (ProgressBar) mView.findViewById(R.id.progress);
		holder.status = (ImageView) mView.findViewById(R.id.statusButton);

		loc = new LocationInputView(getActivity(), holder, true);
		loc.setFavs(true);

		// home station button
		ImageButton stationHomeButton = (ImageButton) mView.findViewById(R.id.stationHomeButton);
		stationHomeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Location home = FavDB.getHome(getActivity());

				if(home != null) {
					queryForStations(home);
				} else {
					Intent intent = new Intent(getActivity(), SetHomeActivity.class);
					intent.putExtra("new", true);

					startActivityForResult(intent, MainActivity.CHANGED_HOME);
				}
			}
		});
		// Home Button Long Click
		stationHomeButton.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				Intent intent = new Intent(getActivity(), SetHomeActivity.class);
				intent.putExtra("new", false);

				startActivityForResult(intent, MainActivity.CHANGED_HOME);

				return true;
			}
		});

		// Find Departures Search Button
		Button stationButton = (Button) mView.findViewById(R.id.stationButton);
		stationButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(loc.getLocation() != null) {
					// use location to query departures

					if(!loc.getLocation().hasId()) {
						Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
						return;
					}
					queryForStations(loc.getLocation());
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void queryForStations(Location location) {
		// Location is valid, so make it a favorite or increase counter
		FavDB.updateFavLocation(getActivity(), location, FavLocation.LOC_TYPE.FROM);

		LiberarioUtils.findDepartures(getActivity(), location);
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
			query_stations.setFragment(this);
			query_stations.setGPS(true);
			query_stations.execute();
		}
		loc_found = true;
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}

}

