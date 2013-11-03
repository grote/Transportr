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

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

public class StationsFragment extends Fragment implements LocationListener {
	private LocationManager locationManager;
	public ProgressDialog pd;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		return inflater.inflate(R.layout.fragment_stations, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

		ImageButton btn = (ImageButton) getView().findViewById(R.id.findNearbyStationsButton);
		btn.setOnClickListener(new OnClickListener(){
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
		pd.show();

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		//TODO also use other providers

		// FIXME only for testing
		//android.location.Location l = new android.location.Location("");
		//l.setLatitude(52.4544);
		//l.setLongitude(13.2516);
		//onLocationChanged(l);
	}

	public void onLocationChanged(android.location.Location location) {
		// Called when a new location is found by the network location provider.
		pd.setMessage(getResources().getString(R.string.stations_searching_stations));

		Location loc = new Location(LocationType.ANY, (int) Math.round(location.getLatitude() * 1E6), (int) Math.round(location.getLongitude() * 1E6));
		AsyncQueryNearbyStationsTask query_stations = new AsyncQueryNearbyStationsTask(this, loc);
		query_stations.execute();

		locationManager.removeUpdates(this);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}

}

