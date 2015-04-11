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

import java.util.List;

import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.ui.DelayAutoCompleteTextView;
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
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class StationsFragment extends LiberarioFragment implements LocationListener {
	private View mView;
	private LocationManager locationManager;
	private boolean loc_found = false;
	public ProgressDialog pd;

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
			DelayAutoCompleteTextView stationView = ((DelayAutoCompleteTextView) mView.findViewById(R.id.stationView));
			if(stationView.getAdapter() != null) {
				((LocationAdapter) stationView.getAdapter()).resetList();
			}

			// clear text view
			setStation(null);

			// hide text clear button
			ImageButton stationClearButton = (ImageButton) mView.findViewById(R.id.stationClearButton);
			stationClearButton.setVisibility(View.GONE);
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
		if(resultCode == FragmentActivity.RESULT_OK && requestCode == MainActivity.CHANGED_HOME) {
			queryForStations(FavDB.getHome(getActivity()));
		}
	}

	private void setDeparturesView() {
		// station name TextView
		final DelayAutoCompleteTextView stationView = (DelayAutoCompleteTextView) mView.findViewById(R.id.stationView);
		LocationAdapter locAdapter = new LocationAdapter(getActivity(), FavLocation.LOC_TYPE.FROM, true);
		locAdapter.setFavs(true);
		stationView.setAdapter(locAdapter);
		stationView.setLoadingIndicator((android.widget.ProgressBar) mView.findViewById(R.id.stationProgress));
		stationView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				Location loc = (Location) parent.getItemAtPosition(position);
				setStation(loc);

				if(!loc.hasId()) {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
					return;
				}
				queryForStations(loc);
			}
		});

		// clear from text button
		final ImageButton stationClearButton = (ImageButton) mView.findViewById(R.id.stationClearButton);
		stationClearButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				setStation(null);
				stationView.requestFocus();
			}
		});

		// When text changed
		stationView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear saved station
				stationView.setTag(null);

				// show clear button
				if(s.length() > 0) {
					stationClearButton.setVisibility(View.VISIBLE);
				} else {
					stationClearButton.setVisibility(View.GONE);
				}
			}
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});

		// TODO adapt like in DirectionsFragment
		// station name favorites button
		OnClickListener stationViewListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int size = ((LocationAdapter) stationView.getAdapter()).addFavs();

				if(size > 0) {
					stationView.showDropDown();
				}
				else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_no_favs), Toast.LENGTH_SHORT).show();
				}
			}
		};
		mView.findViewById(R.id.stationFavButton).setOnClickListener(stationViewListener);
		stationView.setOnClickListener(stationViewListener);

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
				if(stationView.getTag() != null && stationView.getTag() instanceof Location) {
					// use location to query departures
					Location location = (Location) stationView.getTag();

					if(!location.hasId()) {
						Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
						return;
					}
					queryForStations(location);
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
		btn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getLocation();
			}
		});
	}

	private void setStation(Location station) {
		DelayAutoCompleteTextView stationView = (DelayAutoCompleteTextView) mView.findViewById(R.id.stationView);
		ImageButton stationClearButton = (ImageButton) mView.findViewById(R.id.stationClearButton);

		if(station != null) {
			stationView.setText(station.uniqueShortName());
			stationView.setTag(station);
			stationClearButton.setVisibility(View.VISIBLE);
		} else {
			stationView.setText("");
			stationView.setTag(null);
			stationClearButton.setVisibility(View.GONE);
		}
		stationView.dismissDropDown();
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

