package de.grobox.transportr.map;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.NearbyLocationsLoader;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.map.GpsController.FabState;
import de.grobox.transportr.networks.TransportNetwork;
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
import static de.schildbach.pte.dto.LocationType.STATION;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

@ParametersAreNonnullByDefault
public class MapFragment extends BaseMapFragment implements LoaderCallbacks<NearbyLocationsResult>, OnMarkerClickListener {

	private final static int LOCATION_ZOOM = 14;

	@Inject ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;
	private GpsController gpsController;

	private FloatingActionButton gpsFab;
	private @Nullable Marker selectedLocationMarker;
	private NearbyStationsDrawer nearbyStationsDrawer;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		assert v != null;

		getComponent().inject(this);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(MapViewModel.class);
		viewModel.getTransportNetwork().observe(this, this::onTransportNetworkChanged);
		gpsController = viewModel.getGpsController();

		gpsFab = v.findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(view -> onGpsFabClick());

		nearbyStationsDrawer = new NearbyStationsDrawer(getContext());

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

		if (map.getMyLocation() != null) {
			gpsController.setLocation(map.getMyLocation());
		}

		map.setOnMyLocationChangeListener(newLocation -> gpsController.setLocation(newLocation));
		map.setOnMyLocationTrackingModeChangeListener(myLocationTrackingMode -> gpsController.setTrackingMode(myLocationTrackingMode));
		gpsController.getFabState().observe(this, this::onNewFabState);

		// observe map related data
		viewModel.isFreshStart.observe(this, this::onFreshStart);
		viewModel.getSelectedLocation().observe(this, this::onLocationSelected);
		viewModel.getSelectedLocationClicked().observe(this, this::onSelectedLocationClicked);
		viewModel.getFindNearbyStations().observe(this, this::findNearbyStations);
	}

	private void onFreshStart(@Nullable Boolean isFreshStart) {
		if (isFreshStart != null && !isFreshStart) return;
		// zoom to favorite locations or only current location, if no favorites exist
		viewModel.liveBounds.observe(this, bounds -> {
			if (bounds != null) {
				zoomToBounds(bounds);
				viewModel.liveBounds.removeObservers(this);
			} else if (map.getMyLocation() != null) {
				zoomToMyLocation();
			}
		});
		viewModel.isFreshStart.setValue(false);
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker) {
		if (marker.equals(selectedLocationMarker)) {
			viewModel.markerClicked.call();
			return true;
		}
		WrapLocation wrapLocation = nearbyStationsDrawer.getClickedNearbyStation(marker);
		if (wrapLocation != null) {
			viewModel.selectLocation(wrapLocation);
			return true;
		}
		return false;
	}

	private void onTransportNetworkChanged(@Nullable TransportNetwork network) {
		requestPermission();
		if (network != null && map != null) {
			// stop observing fresh start, so we get only updated when activity was recreated
			viewModel.isFreshStart.removeObservers(this);
			viewModel.isFreshStart.setValue(true);
			// prevent loader from re-adding nearby stations
			getActivity().getSupportLoaderManager().destroyLoader(LOADER_NEARBY_STATIONS);
		}
	}

	private void onLocationSelected(@Nullable WrapLocation location) {
		if (location == null) return;
		LatLng latLng = location.getLatLng();
		addMarker(latLng);
		zoomTo(latLng);
	}

	private void onSelectedLocationClicked(@Nullable LatLng latLng) {
		if (latLng == null) return;
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
				mapView.post(() -> map.getTrackingSettings().setMyLocationTrackingMode(TRACKING_FOLLOW));
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

	private void zoomTo(LatLng latLng) {
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

	private void zoomToBounds(LatLngBounds latLngBounds) {
		int padding = getResources().getDimensionPixelSize(R.dimen.mapPadding);
		CameraUpdate update = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding);
		map.moveCamera(update);
	}

	private void findNearbyStations(WrapLocation location) {
		Bundle args = NearbyLocationsLoader.getBundle(location.getLocation(), 1000);
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
		return new NearbyLocationsLoader(getContext(), viewModel.getTransportNetwork().getValue(), args);
	}

	@Override
	public void onLoadFinished(Loader<NearbyLocationsResult> loader, @Nullable NearbyLocationsResult result) {
		if (result != null && result.status == OK && result.locations != null && result.locations.size() > 0) {
			nearbyStationsDrawer.draw(map, result.locations);
		} else {
			Toast.makeText(getContext(), R.string.error_find_nearby_stations, Toast.LENGTH_SHORT).show();
		}
		viewModel.setNearbyStationsFound(true);
	}

	@Override
	public void onLoaderReset(Loader<NearbyLocationsResult> loader) {
		nearbyStationsDrawer.reset();
	}

}
