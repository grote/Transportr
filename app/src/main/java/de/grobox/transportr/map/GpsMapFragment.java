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

package de.grobox.transportr.map;

import android.content.res.ColorStateList;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.location.LostLocationEngine;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.graphics.PorterDuff.Mode.SRC_IN;
import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLng;
import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom;
import static de.grobox.transportr.map.GpsController.FabState.FIX;
import static de.grobox.transportr.map.GpsController.FabState.FOLLOW;
import static de.grobox.transportr.utils.Constants.REQUEST_LOCATION_PERMISSION;

@ParametersAreNonnullByDefault
abstract public class GpsMapFragment extends BaseMapFragment implements LocationEngineListener {

	protected final static int LOCATION_ZOOM = 14;

	private LocationLayerPlugin locationPlugin;
	private @Nullable LostLocationEngine locationEngine;
	protected GpsController gpsController;
	protected FloatingActionButton gpsFab;
	protected int mapPadding;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		assert v != null;

		mapPadding = getResources().getDimensionPixelSize(R.dimen.mapPadding);

		gpsFab = v.findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(view -> onGpsFabClick());
		return v;
	}

	@Override
	@CallSuper
	@SuppressWarnings({ "MissingPermission" })
	public void onStart() {
		super.onStart();
		if (locationEngine != null) {
			locationEngine.requestLocationUpdates();
			locationEngine.addLocationEngineListener(this);
		}
	}

	@Override
	@CallSuper
	public void onStop() {
		super.onStop();
		if (locationEngine != null) {
			locationEngine.removeLocationEngineListener(this);
			locationEngine.removeLocationUpdates();
		}
	}

	@Override
	@CallSuper
	public void onMapReady(MapboxMap mapboxMap) {
		super.onMapReady(mapboxMap);
		enableLocationPlugin();

		if (locationEngine != null && ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
			gpsController.setLocation(locationEngine.getLastLocation());
		}

		// TODO
//		map.setOnMyLocationTrackingModeChangeListener(myLocationTrackingMode -> gpsController.setTrackingMode(myLocationTrackingMode));
		gpsController.getFabState().observe(this, this::onNewFabState);
	}

	private void enableLocationPlugin() {
		// Check if permissions are enabled and if not request
		if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
			// Create an instance of LOST location engine
			initializeLocationEngine();

			if (locationPlugin == null) {
				locationPlugin = new LocationLayerPlugin(mapView, map, locationEngine);
				locationPlugin.setLocationLayerEnabled(LocationLayerMode.TRACKING);
			}
		} else {
			requestPermission();
		}
	}

	@RequiresPermission(ACCESS_FINE_LOCATION)
	private void initializeLocationEngine() {
		locationEngine = new LostLocationEngine(getContext());
		locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
		locationEngine.activate();

		locationEngine.addLocationEngineListener(this);
	}

	protected void animateTo(@Nullable LatLng latLng, int zoom) {
		if (latLng == null) return;
		CameraUpdate update = map.getCameraPosition().zoom < zoom ? newLatLngZoom(latLng, zoom) : newLatLng(latLng);
		map.easeCamera(update, 1500);
	}

	private void zoomToBounds(@Nullable LatLngBounds latLngBounds, boolean animate) {
		if (latLngBounds == null) return;
		CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, mapPadding);
		if (animate) {
			map.easeCamera(update);
		} else {
			map.moveCamera(update);
		}
	}

	protected void zoomToBounds(@Nullable LatLngBounds latLngBounds) {
		zoomToBounds(latLngBounds, false);
	}

	protected void animateToBounds(@Nullable LatLngBounds latLngBounds) {
		zoomToBounds(latLngBounds, true);
	}

	protected void onGpsFabClick() {
		if (map == null) return;
		if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
			Toast.makeText(getContext(), R.string.permission_denied_gps, Toast.LENGTH_SHORT).show();
			return;
		}
		if (locationPlugin.getLastKnownLocation() == null) {
			Toast.makeText(getContext(), R.string.warning_no_gps_fix, Toast.LENGTH_SHORT).show();
			return;
		}
		LatLng coords = new LatLng(locationPlugin.getLastKnownLocation().getLatitude(), locationPlugin.getLastKnownLocation().getLongitude());
		CameraUpdate update = map.getCameraPosition().zoom < LOCATION_ZOOM ? newLatLngZoom(coords, LOCATION_ZOOM) : newLatLng(coords);
		map.easeCamera(update, 750, new MapboxMap.CancelableCallback() {
			@Override
			public void onCancel() {

			}

			@Override
			public void onFinish() {
				// TODO manual tracking
//				mapView.post(() -> map.getTrackingSettings().setMyLocationTrackingMode(TRACKING_FOLLOW));
			}
		});
	}

	protected void onNewFabState(@Nullable GpsController.FabState fabState) {
		int iconColor = ContextCompat.getColor(getContext(), R.color.fabForegroundInitial);
		ColorStateList backgroundColor = ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.fabBackground));
		if (fabState == FIX) {
			iconColor = ContextCompat.getColor(getContext(), R.color.fabForegroundMoved);
			backgroundColor = ColorStateList.valueOf(ContextCompat.getColor(getContext(), R.color.fabBackgroundMoved));
		} else if (fabState == FOLLOW) {
			iconColor = ContextCompat.getColor(getContext(), R.color.fabForegroundFollow);
		}
		gpsFab.getDrawable().setColorFilter(iconColor, SRC_IN);
		gpsFab.setBackgroundTintList(backgroundColor);
	}

	protected void requestPermission() {
		if (ContextCompat.checkSelfPermission(getContext(), ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) return;

//		if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
		// TODO Show an explanation to the user *asynchronously*
		// After the user sees the explanation, try again to request the permission.
//		}
		requestPermissions(new String[] { ACCESS_FINE_LOCATION }, REQUEST_LOCATION_PERMISSION);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode != REQUEST_LOCATION_PERMISSION) return;
		if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
			enableLocationPlugin();
		}
	}

	@Override
	@SuppressWarnings({ "MissingPermission" })
	public void onConnected() {
		if (locationEngine != null) {
			locationEngine.requestLocationUpdates();
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		Timber.e("NEW LOCATION: %s", location);
		// TODO add manual tracking here
		gpsController.setLocation(location);
	}

}
