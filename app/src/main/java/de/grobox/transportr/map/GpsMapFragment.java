package de.grobox.transportr.map;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.graphics.PorterDuff.Mode.SRC_IN;
import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLng;
import static com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom;
import static com.mapbox.mapboxsdk.constants.MyLocationTracking.TRACKING_FOLLOW;
import static de.grobox.transportr.map.GpsController.FabState.FIX;
import static de.grobox.transportr.map.GpsController.FabState.FOLLOW;
import static de.grobox.transportr.utils.Constants.REQUEST_LOCATION_PERMISSION;

@ParametersAreNonnullByDefault
abstract public class GpsMapFragment extends BaseMapFragment {

	protected final static int LOCATION_ZOOM = 14;

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
	public void onMapReady(MapboxMap mapboxMap) {
		super.onMapReady(mapboxMap);

		if (map.getMyLocation() != null) {
			gpsController.setLocation(map.getMyLocation());
		}

		map.setOnMyLocationChangeListener(newLocation -> gpsController.setLocation(newLocation));
		map.setOnMyLocationTrackingModeChangeListener(myLocationTrackingMode -> gpsController.setTrackingMode(myLocationTrackingMode));
		gpsController.getFabState().observe(this, this::onNewFabState);
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
		if (map.getMyLocation() == null) {
			Toast.makeText(getContext(), R.string.warning_no_gps_fix, Toast.LENGTH_SHORT).show();
			return;
		}
		LatLng coords = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
		CameraUpdate update = map.getCameraPosition().zoom < LOCATION_ZOOM ? newLatLngZoom(coords, LOCATION_ZOOM) : newLatLng(coords);
		map.easeCamera(update, 750, new MapboxMap.CancelableCallback() {
			@Override
			public void onCancel() {

			}

			@Override
			public void onFinish() {
				mapView.post(() -> map.getTrackingSettings().setMyLocationTrackingMode(TRACKING_FOLLOW));
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
			if (map != null) {
				map.setMyLocationEnabled(true);
			}
		} else if (map != null) {
			map.setMyLocationEnabled(false);
		}
	}

}
