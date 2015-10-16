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

package de.grobox.liberario.ui;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.List;

import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

public class LocationInputGPSView extends LocationInputView implements LocationListener {

	private LocationManager locationManager;
	private boolean searching = false;
	private Location gps_location = null;

	public LocationInputGPSView(Context context, LocationInputViewHolder ui) {
		super(context, ui, false);

		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		locAdapter.setGPS(true);
	}

	@Override
	public void onLocationItemClick(Location loc, View view) {
		if(loc.id != null && loc.id.equals("Transportr.GPS")) {
			activateGPS();
		} else {
			super.onLocationItemClick(loc, view);
		}
	}

	@Override
	public void handleTextChanged(CharSequence s) {
		if(isSearching()) {
			deactivateGPS();
		}
		super.handleTextChanged(s);
	}

	public void activateGPS() {
		List<String> providers = locationManager.getProviders(true);

		for(String provider : providers) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestSingleUpdate(provider, this, null);

			Log.d(getClass().getSimpleName(), "Register provider for location updates: " + provider);
		}

		// check if there is a non-passive provider available
		if(providers.size() == 0 || (providers.size() == 1 && providers.get(0).equals(LocationManager.PASSIVE_PROVIDER)) ) {
			locationManager.removeUpdates(this);
			Toast.makeText(context, context.getResources().getString(R.string.error_no_location_provider), Toast.LENGTH_LONG).show();

			return;
		}

		// clear input
		//noinspection deprecation
		setLocation(null, context.getResources().getDrawable(R.drawable.ic_gps));
		holder.clear.setVisibility(View.VISIBLE);

		// show GPS button blinking
		final Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(500);
		animation.setInterpolator(new LinearInterpolator());
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		holder.status.setAnimation(animation);

		holder.location.setHint(R.string.stations_searching_position);
		holder.location.clearFocus();

		searching = true;
	}

	public void deactivateGPS() {
		searching = false;

		locationManager.removeUpdates(this);

		// deactivate button
		holder.status.clearAnimation();
		holder.location.setHint(hint);
	}

	public boolean isSearching() {
		return searching;
	}

	public void onLocationChanged(Location location) {
		//noinspection deprecation
		setLocation(location, context.getResources().getDrawable(R.drawable.ic_gps));
	}

	// Called when a new location is found by the network location provider.
	public void onLocationChanged(android.location.Location location) {
		// no more updates to prevent this method from being called more than once
		locationManager.removeUpdates(this);

		// only execute if we still do not have a location to make super sure this is not run twice
		if(gps_location == null) {
			Log.d(getClass().getSimpleName(), "Found location: " + location.toString());

			int lat = (int) Math.round(location.getLatitude() * 1E6);
			int lon = (int) Math.round(location.getLongitude() * 1E6);

			String lat_str = String.valueOf(location.getLatitude());
			if(lat_str.length() > 9) lat_str = lat_str.substring(0, 8);
			String lon_str = String.valueOf(location.getLongitude());
			if(lon_str.length() > 9) lon_str = lon_str.substring(0, 8);

			// create location based on GPS coordinates
			gps_location = new Location(LocationType.ADDRESS, null, lat, lon, "GPS", lat_str + "/" + lon_str);

			onLocationChanged(gps_location);
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}
}
