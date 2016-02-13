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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.List;

import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;

public class LocationInputGPSView extends LocationInputView implements LocationListener {

	private LocationManager locationManager;
	private boolean searching = false;
	private Location gps_location = null;
	private int caller;
	private boolean request_permission = false;

	public LocationInputGPSView(FragmentActivity context, LocationInputViewHolder ui, int loaderId, int caller) {
		super(context, ui, loaderId, false);

		this.caller = caller;
		this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		getAdapter().setGPS(true);
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
		if(isSearching()) return;

		// check permissions
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// we don't have a permission, so store the information that we are requesting it
			request_permission = true;

			// Should we show an explanation?
			if(ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
				Toast.makeText(context, R.string.permission_denied_gps, Toast.LENGTH_LONG).show();
			} else {
				// No explanation needed, we can request the permission
				ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, caller);
			}

			return;
		}

		// we arrive here only once we have got the permission and are not longer requesting it
		request_permission = false;

		List<String> providers = locationManager.getProviders(true);

		for(String provider : providers) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestSingleUpdate(provider, this, null);

			Log.d(getClass().getSimpleName(), "Register provider for location updates: " + provider);
		}

		// check if there is a non-passive provider available
		if(providers.size() == 0 || (providers.size() == 1 && providers.get(0).equals(LocationManager.PASSIVE_PROVIDER))) {

			locationManager.removeUpdates(this);
			Toast.makeText(context, context.getResources().getString(R.string.error_no_location_provider), Toast.LENGTH_LONG).show();

			return;
		}

		// clear input
		//noinspection deprecation
		setLocation(null, context.getResources().getDrawable(R.drawable.ic_gps));
		ui.clear.setVisibility(View.VISIBLE);

		// clear current GPS location, because we are looking to find a new one
		gps_location = null;

		// show GPS button blinking
		final Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(500);
		animation.setInterpolator(new LinearInterpolator());
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		ui.status.startAnimation(animation);

		ui.location.setHint(R.string.stations_searching_position);
		ui.location.clearFocus();

		searching = true;
	}

	public void deactivateGPS() {
		searching = false;

		if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			locationManager.removeUpdates(this);
		}

		// deactivate button
		ui.status.clearAnimation();
		ui.location.setHint(hint);
	}

	public boolean isSearching() {
		return searching;
	}

	public boolean isRequestingPermission() {
		return request_permission;
	}

	public void onLocationChanged(Location location) {
		//noinspection deprecation
		setLocation(location, context.getResources().getDrawable(R.drawable.ic_gps));
	}

	// Called when a new location is found by the network location provider.
	public void onLocationChanged(android.location.Location location) {
		// no more updates to prevent this method from being called more than once
		if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			locationManager.removeUpdates(this);
		}

		// only execute if we still do not have a location to make super sure this is not run twice
		if(gps_location == null) {
			Log.d(getClass().getSimpleName(), "Found location: " + location.toString());

			int lat = (int) Math.round(location.getLatitude() * 1E6);
			int lon = (int) Math.round(location.getLongitude() * 1E6);

			// create location based on GPS coordinates
			gps_location = Location.coord(lat, lon);

			onLocationChanged(gps_location);
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}
}
