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
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerViewClickListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.NearbyLocationsLoader;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.map.GpsController.FabState;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.NearbyLocationsResult;
import de.schildbach.pte.dto.Product;

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
public class MapFragment extends BaseMapFragment implements LoaderCallbacks<NearbyLocationsResult>, OnMarkerClickListener, OnMarkerViewClickListener {

	private final static int LOCATION_ZOOM = 14;

	@Inject ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;
	private GpsController gpsController;

	private FloatingActionButton gpsFab;
	private @Nullable Marker selectedLocationMarker;
	private final Map<Marker, Location> nearbyLocations = new HashMap<>();

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
		}

		if (isFreshStart) {
			// zoom to favorite locations or only current location, if no favorites exist
			viewModel.liveBounds.observe(this, bounds -> {
				if (bounds != null) {
					zoomToBounds(bounds);
					viewModel.liveBounds.removeObservers(this);
				} else if (map.getMyLocation() != null) {
					zoomToMyLocation();
				}
			});
		}

		map.setOnMyLocationChangeListener(newLocation -> gpsController.setLocation(newLocation));
		map.setOnMyLocationTrackingModeChangeListener(myLocationTrackingMode -> gpsController.setTrackingMode(myLocationTrackingMode));
		gpsController.getFabState().observe(this, this::onNewFabState);

		// observe map related data
		viewModel.getSelectedLocation().observe(this, this::onLocationSelected);
		viewModel.getSelectedLocationClicked().observe(this, this::onSelectedLocationClicked);
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
		map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));
	}

	private void findNearbyStations(WrapLocation location) {
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
		return new NearbyLocationsLoader(getContext(), viewModel.getTransportNetwork().getValue(), args);
	}

	@Override
	public void onLoadFinished(Loader<NearbyLocationsResult> loader, @Nullable NearbyLocationsResult result) {
		if (result != null && result.status == OK && result.locations != null && result.locations.size() > 0) {
			for (Location location : result.locations) {
				if (!location.hasLocation()) continue;
				WrapLocation wrapLocation = new WrapLocation(location);
				Marker marker = map.addMarker(new MarkerViewOptions()
						.position(wrapLocation.getLatLng())
						.title(wrapLocation.getName())
						.icon(getIconForProduct(location.products))
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

	private Icon getIconForProduct(@Nullable Set<Product> p) {
		@DrawableRes
		int image_res = R.drawable.product_bus_marker;

		if (p != null && p.size() > 0) {
			switch (p.iterator().next()) {
				case HIGH_SPEED_TRAIN:
					image_res = R.drawable.product_high_speed_train_marker;
					break;
				case REGIONAL_TRAIN:
					image_res = R.drawable.product_regional_train_marker;
					break;
				case SUBURBAN_TRAIN:
					image_res = R.drawable.product_suburban_train_marker;
					break;
				case SUBWAY:
					image_res = R.drawable.product_subway_marker;
					break;
				case TRAM:
					image_res = R.drawable.product_tram_marker;
					break;
				case BUS:
					image_res = R.drawable.product_bus_marker;
					break;
				case FERRY:
					image_res = R.drawable.product_ferry_marker;
					break;
				case CABLECAR:
					image_res = R.drawable.product_cablecar_marker;
					break;
				case ON_DEMAND:
					image_res = R.drawable.product_on_demand_marker;
					break;
			}
		}
		return getNearbyLocationsIcon(image_res);
	}

	private Icon getNearbyLocationsIcon(@DrawableRes int res) {
		IconFactory iconFactory = IconFactory.getInstance(getContext());
		Drawable drawable = ContextCompat.getDrawable(getContext(), res);
		return iconFactory.fromBitmap(getBitmap(drawable));
	}

	private Bitmap getBitmap(@Nullable Drawable drawable) {
		if (drawable == null) throw new IllegalArgumentException();
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

}
