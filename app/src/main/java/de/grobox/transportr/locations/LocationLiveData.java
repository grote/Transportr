/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.locations;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.WorkerThread;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.locations.ReverseGeocoder.ReverseGeocoderCallback;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.mapbox.services.android.telemetry.location.LocationEnginePriority.BALANCED_POWER_ACCURACY;

@ParametersAreNonnullByDefault
public class LocationLiveData extends LiveData<WrapLocation> implements LocationEngineListener, ReverseGeocoderCallback {

	private final Context context;
	private final LocationSource locationSource = Mapbox.getLocationSource();

	// TODO often the GPS request is dropped after some time, WHY?
	// ActivityManager: Launch timeout has expired, giving up wake lock!
	public LocationLiveData(Context context) {
		super();
		this.context = context;
	}

	@Override
	@RequiresPermission(ACCESS_FINE_LOCATION)
	public void observe(LifecycleOwner owner, Observer<WrapLocation> observer) {
		super.observe(owner, observer);
	}

	@Override
	@SuppressLint("MissingPermission")
	protected void onActive() {
		super.onActive();
		locationSource.activate();
		locationSource.setPriority(BALANCED_POWER_ACCURACY);
		locationSource.setInterval(5000);
		locationSource.addLocationEngineListener(this);
		locationSource.requestLocationUpdates();
		// TODO what happens where when GPS is turned off?
	}

	@Override
	protected void onInactive() {
		super.onInactive();
		locationSource.removeLocationUpdates();
		locationSource.removeLocationEngineListener(this);
		locationSource.deactivate();
	}

	@Override
	public void onConnected() {
		// no-op
	}

	@Override
	public void onLocationChanged(Location location) {
		locationSource.removeLocationUpdates();
		new Thread(() -> {
			ReverseGeocoder geocoder = new ReverseGeocoder(context, this);
			geocoder.findLocation(location);
		}).start();
	}

	@Override
	@WorkerThread
	public void onLocationRetrieved(@NonNull WrapLocation loc) {
		postValue(loc);
	}

}
