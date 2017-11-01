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
import static com.mapbox.services.android.telemetry.location.LocationEnginePriority.HIGH_ACCURACY;

@ParametersAreNonnullByDefault
public class LocationLiveData extends LiveData<WrapLocation> implements LocationEngineListener, ReverseGeocoderCallback {

	private final Context context;
	private final LocationSource locationSource = Mapbox.getLocationSource();

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
		locationSource.setPriority(HIGH_ACCURACY);
		locationSource.setInterval(5000);
		locationSource.addLocationEngineListener(this);
		locationSource.requestLocationUpdates();
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
