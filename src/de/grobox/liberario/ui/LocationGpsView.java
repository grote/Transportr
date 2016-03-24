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
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import java.util.List;

import de.grobox.liberario.R;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

public class LocationGpsView extends LocationView implements LocationListener {

	private final String SEARCHING = "searching";

	private LocationManager locationManager;
	private LocationGpsListener gpsListener;
	private volatile boolean searching = false;
	private Location gps_location = null;
	protected int caller = 0;

	public LocationGpsView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if(!isInEditMode()) {
			this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		}

		getAdapter().setGPS(true);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = (Bundle) super.onSaveInstanceState();
		bundle.putBoolean(SEARCHING, searching);
		if(searching) {
			deactivateGPS();
		}
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) {
			Bundle bundle = (Bundle) state;
			if(bundle.getBoolean(SEARCHING)) {
				activateGPS();
				// this pass over the LocationView's restoreInstanceState
				state = bundle.getParcelable(SUPER_STATE);
			}
		}
		super.onRestoreInstanceState(state);
	}

	@Override
	public void onLocationItemClick(Location loc, View view) {
		if(loc.id != null && loc.id.equals(LocationAdapter.GPS)) {
			// prevent GPS fake name from being shown in the TextView
			ui.location.setText("");

			activateGPS();
			if(clickListener != null) clickListener.onLocationItemClick(view, loc);
		} else {
			super.onLocationItemClick(loc, view);
		}
	}

	@Override
	protected void onFocusChange(View v, boolean hasFocus) {
		if(!searching) super.onFocusChange(v, hasFocus);
	}

	@Override
	public void handleTextChanged(CharSequence s) {
		if(searching) {
			deactivateGPS();
		}
		super.handleTextChanged(s);
	}

	@Override
	protected void clearLocationAndShowDropDown() {
		deactivateGPS();
		super.clearLocationAndShowDropDown();
	}

	@Override
	 protected void onDetachedFromWindow() {
		deactivateGPS();
		super.onDetachedFromWindow();
	}

	public void setCaller(int caller) {
		this.caller = caller;
	}

	public void activateGPS() {
		if(searching) return;

		// check permissions
		if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// Should we show an explanation?
			if(ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
				Toast.makeText(getContext(), R.string.permission_denied_gps, Toast.LENGTH_LONG).show();
			} else {
				// No explanation needed, we can request the permission
				ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, caller);
			}

			return;
		}

		searching = true;

		List<String> providers = locationManager.getProviders(true);

		for(String provider : providers) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestSingleUpdate(provider, this, null);

			Log.d(getClass().getSimpleName(), "Register provider for location updates: " + provider);
		}

		// check if there is a non-passive provider available
		if(providers.size() == 0 || (providers.size() == 1 && providers.get(0).equals(LocationManager.PASSIVE_PROVIDER))) {
			locationManager.removeUpdates(this);
			Toast.makeText(getContext(), getContext().getString(R.string.error_no_location_provider), Toast.LENGTH_LONG).show();

			return;
		}

		// clear input
		setLocation(null, TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_gps));
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

		if(gpsListener != null) gpsListener.activateGPS();
	}

	public void deactivateGPS() {
		searching = false;

		if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			locationManager.removeUpdates(this);
		}

		// deactivate button
		ui.status.clearAnimation();
		ui.location.setHint(hint);

		if(gpsListener != null) gpsListener.deactivateGPS();
	}

	public boolean isSearching() {
		return searching;
	}

	public void onLocationChanged(Location location) {
		setLocation(location, TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_gps));
		if(gpsListener != null) gpsListener.onLocationChanged(location);
		deactivateGPS();
	}

	// Called when a new location is found by the network location provider.
	public void onLocationChanged(android.location.Location location) {
		// no more updates to prevent this method from being called more than once
		if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	public void setLocationGpsListener(LocationGpsListener listener) {
		this.gpsListener = listener;
	}

	public interface LocationGpsListener {
		void activateGPS();
		void deactivateGPS();
		void onLocationChanged(Location location);
	}

}
