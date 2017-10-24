package de.grobox.transportr.map;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerViewClickListener;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.NearbyLocationsLoader;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.map.GpsController.FabState;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.graphics.PorterDuff.Mode.SRC_IN;
import static com.mapbox.mapboxsdk.constants.MyLocationTracking.TRACKING_FOLLOW;
import static de.grobox.transportr.map.GpsController.FabState.FIX;
import static de.grobox.transportr.map.GpsController.FabState.FOLLOW;
import static de.grobox.transportr.utils.Constants.LOADER_NEARBY_STATIONS;
import static de.grobox.transportr.utils.Constants.REQUEST_LOCATION_PERMISSION;
import static de.grobox.transportr.utils.TransportrUtils.getLatLng;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;
import static de.grobox.transportr.utils.TransportrUtils.getMarkerForProduct;
import static de.schildbach.pte.dto.LocationType.STATION;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

public class MapFragment extends BaseMapFragment implements LoaderCallbacks<NearbyLocationsResult>, OnMarkerClickListener, OnMarkerViewClickListener {

	private final static int LOCATION_ZOOM = 14;

	@Inject ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;
	private GpsController gpsController;

	private FloatingActionButton gpsFab;
	private @Nullable Marker selectedLocationMarker;
	private Map<Marker, Location> nearbyLocations = new HashMap<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		assert v != null;

		getComponent().inject(this);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(MapViewModel.class);
		viewModel.getTransportNetwork().observe(this, transportNetwork -> requestPermission());
		gpsController = viewModel.getGpsController();

		gpsFab = v.findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(view -> onGpsFabClick());

		if (savedInstanceState == null && viewModel.getTransportNetwork().getValue() != null) {
			requestPermission();
		}

		return v;
	}

	@Override
	@LayoutRes
	protected int getLayout() {
		return R.layout.fragment_map;
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		super.onMapReady(mapboxMap);

		Location location = new Location(STATION, "fake");
		Bundle args = NearbyLocationsLoader.getBundle(location, 0);
		getActivity().getSupportLoaderManager().initLoader(LOADER_NEARBY_STATIONS, args, this);

		map.setOnMapClickListener(point -> viewModel.mapClicked.call());
		map.setOnMapLongClickListener(point -> viewModel.selectLocation(new WrapLocation(point)));
		map.setOnMarkerClickListener(this);
		map.getMarkerViewManager().setOnMarkerViewClickListener(this);

		if (map.getMyLocation() != null) {
			gpsController.setLocation(map.getMyLocation());
			if (viewModel.getSelectedLocation().getValue() == null) {
				zoomToMyLocation();
			}
		}
		map.setOnMyLocationChangeListener(newLocation -> gpsController.setLocation(newLocation));
		map.setOnMyLocationTrackingModeChangeListener(myLocationTrackingMode -> gpsController.setTrackingMode(myLocationTrackingMode));
		gpsController.getFabState().observe(this, this::onNewFabState);

		// observe map related data
		viewModel.getZoomTo().observe(this, this::zoomTo);
		viewModel.getSelectedLocation().observe(this, this::onLocationSelected);
		viewModel.getFindNearbyStations().observe(this, this::findNearbyStations);
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker) {
		if (marker.equals(selectedLocationMarker)) {
			viewModel.markerClicked.call();
			return true;
		}
		return false;
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker, @NonNull View view, @NonNull MapboxMap.MarkerViewAdapter adapter) {
		// https://github.com/mapbox/mapbox-gl-native/issues/8236
		if (nearbyLocations.containsKey(marker)) {
			WrapLocation wrapLocation = new WrapLocation(nearbyLocations.get(marker));
			viewModel.selectLocation(wrapLocation);
			return true;
		}
		return false;
	}

	private void onLocationSelected(WrapLocation location) {
		LatLng latLng = getLatLng(location.getLocation());
		addMarker(latLng);
		zoomTo(latLng);
	}

	private void addMarker(LatLng latLng) {
		if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);
		selectedLocationMarker = map.addMarker(new MarkerOptions().position(latLng));
	}

	private void onGpsFabClick() {
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
		CameraUpdate update = CameraUpdateFactory.newLatLng(coords);
		map.easeCamera(update, 750, new MapboxMap.CancelableCallback() {
			@Override
			public void onCancel() {

			}

			@Override
			public void onFinish() {
				map.getTrackingSettings().setMyLocationTrackingMode(TRACKING_FOLLOW);
			}
		});
	}

	private void onNewFabState(@Nullable FabState fabState) {
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

	public void zoomTo(LatLng latLng) {
		CameraUpdate update = map.getCameraPosition().zoom < LOCATION_ZOOM ?
				CameraUpdateFactory.newLatLngZoom(latLng, LOCATION_ZOOM) : CameraUpdateFactory.newLatLng(latLng);
		map.easeCamera(update, 1500);
	}

	private void zoomToMyLocation() {
		if (map.getMyLocation() == null) return;
		LatLng coords = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coords, LOCATION_ZOOM);
		map.moveCamera(update);
	}

	public void findNearbyStations(WrapLocation location) {
		// TODO limit maxDistance to visible area at least, some providers return a lot of stations
		Bundle args = NearbyLocationsLoader.getBundle(location.getLocation(), 0);
		getActivity().getSupportLoaderManager().restartLoader(LOADER_NEARBY_STATIONS, args, this).forceLoad();
	}

	private void requestPermission() {
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
				zoomToMyLocation();
			}
		} else if (map != null) {
			map.setMyLocationEnabled(false);
		}
	}

	/* Nearby Stations Loader */

	@Override
	public Loader<NearbyLocationsResult> onCreateLoader(int id, Bundle args) {
		return new NearbyLocationsLoader(getContext(), args);
	}

	@Override
	public void onLoadFinished(Loader<NearbyLocationsResult> loader, @Nullable NearbyLocationsResult result) {
		if (result != null && result.status == OK && result.locations != null && result.locations.size() > 0) {
			for (Location location : result.locations) {
				if (!location.hasLocation()) continue;
				Marker marker = map.addMarker(new MarkerViewOptions()
						.position(getLatLng(location))
						.title(getLocationName(location))
						.icon(getNearbyLocationsIcon(getMarkerForProduct(location.products)))
				);
				nearbyLocations.put(marker, location);
			}
		} else {
			// TODO
			Log.e("TEST", "ERROR loading nearby stations.");
		}
		viewModel.setNearbyStationsFound(true);
	}

	@Override
	public void onLoaderReset(Loader<NearbyLocationsResult> loader) {
		nearbyLocations.clear();
	}

	private Icon getNearbyLocationsIcon(@DrawableRes int res) {
		IconFactory iconFactory = IconFactory.getInstance(getContext());
		Drawable drawable = ContextCompat.getDrawable(getContext(), res);
		return iconFactory.fromBitmap(getBitmap(drawable));
	}

	private Bitmap getBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

}
