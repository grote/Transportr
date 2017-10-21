/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerViewClickListener;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.BuildConfig;
import de.grobox.transportr.R;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.locations.LocationFragment;
import de.grobox.transportr.locations.LocationView;
import de.grobox.transportr.locations.LocationView.LocationViewListener;
import de.grobox.transportr.locations.NearbyLocationsLoader;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.PickTransportNetworkActivity;
import de.grobox.transportr.networks.TransportNetwork;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.graphics.PorterDuff.Mode.SRC_IN;
import static android.support.design.widget.BottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static com.mapbox.mapboxsdk.constants.MyLocationTracking.TRACKING_FOLLOW;
import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.transportr.locations.WrapLocation.WrapType.GPS;
import static de.grobox.transportr.networks.PickTransportNetworkActivity.FORCE_NETWORK_SELECTION;
import static de.grobox.transportr.utils.Constants.LOADER_NEARBY_STATIONS;
import static de.grobox.transportr.utils.TransportrUtils.convert;
import static de.grobox.transportr.utils.TransportrUtils.findDirections;
import static de.grobox.transportr.utils.TransportrUtils.getLatLng;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;
import static de.grobox.transportr.utils.TransportrUtils.getMarkerForProduct;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

@ParametersAreNonnullByDefault
public class MapActivity extends DrawerActivity
		implements LocationViewListener, OnMapReadyCallback, LoaderCallbacks<NearbyLocationsResult>, OnMarkerClickListener, OnMarkerViewClickListener {

	private final static int LOCATION_ZOOM = 14;

	@Inject	ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;
	private MapView mapView;
	private MapboxMap map;
	private LocationView search;
	private BottomSheetBehavior bottomSheetBehavior;
	private FloatingActionButton gpsFab;

	private @Nullable LocationFragment locationFragment;
	private @Nullable Marker selectedLocationMarker;
	private Map<Marker, Location> nearbyLocations = new HashMap<>();
	private boolean transportNetworkInitialized = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) enableStrictMode();
		getComponent().inject(this);
		ensureTransportNetworkSelected();
		setContentView(R.layout.activity_map);
		super.onCreate(savedInstanceState);

		mapView = findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);

		View menu = findViewById(R.id.menu);
		menu.setOnClickListener(view -> openDrawer());

		search = findViewById(R.id.search);
		search.setLocationViewListener(this);

		View bottomSheet = findViewById(R.id.bottomSheet);
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == STATE_HIDDEN) {
					search.clearLocation();
					search.reset();
					bottomSheetBehavior.setPeekHeight(0);
				}
			}
			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
		});

		// get view model and observe data
		viewModel = ViewModelProviders.of(this, viewModelFactory).get(MapViewModel.class);
		viewModel.getTransportNetwork().observe(this, this::onTransportNetworkChanged);
		viewModel.getHome().observe(this, homeLocation -> search.setHomeLocation(homeLocation));
		viewModel.getWork().observe(this, workLocation -> search.setWorkLocation(workLocation));
		viewModel.getLocations().observe(this, favoriteLocations -> search.setFavoriteLocations(favoriteLocations));
		viewModel.getPeekHeight().observe(this, height -> {
			if (height != null) bottomSheetBehavior.setPeekHeight(height);
		});

		FloatingActionButton directionsFab = findViewById(R.id.directionsFab);
		directionsFab.setOnClickListener(view -> {
			WrapLocation from = new WrapLocation(GPS);
			if (map != null && map.getMyLocation() != null) {
				from = convert(map.getMyLocation());
			}
			WrapLocation to = null;
			if (locationFragment != null && locationFragmentVisible()) {
				to = locationFragment.getLocation();
			}
			findDirections(MapActivity.this, 0, from, null, to, null, true);
		});
		gpsFab = findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(view -> {
			if (map != null) {
				if (map.getMyLocation() == null) return;
				LatLng coords = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coords, LOCATION_ZOOM);
				map.animateCamera(update, 1000, new MapboxMap.CancelableCallback() {
					@Override
					public void onCancel() {

					}

					@Override
					public void onFinish() {
						Log.e("TEST", "FINISHED ANIMATION");
						map.getTrackingSettings().setMyLocationTrackingMode(TRACKING_FOLLOW);
					}
				});
			}
		});

		if (savedInstanceState == null) {
			SavedSearchesFragment f = new SavedSearchesFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, f, SavedSearchesFragment.TAG)
					.commitNow(); // otherwise takes some time and empty bottomSheet will not be shown
			bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_AUTO);
			bottomSheetBehavior.setState(STATE_COLLAPSED);
		} else {
			locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag(LocationFragment.TAG);
		}
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		map = mapboxMap;

//		LatLng latLng = map.getCameraPosition().target;
		Location location = new Location(LocationType.STATION, "fake");
		Bundle args = NearbyLocationsLoader.getBundle(location, 0);
		getSupportLoaderManager().initLoader(LOADER_NEARBY_STATIONS, args, this);

		map.setOnMapClickListener(point -> {
			search.clearFocus();
			search.hideSoftKeyboard();
		});
		map.setOnMapLongClickListener(point -> onLocationItemClick(new WrapLocation(point), FROM));
		map.setOnMarkerClickListener(this);
		map.getMarkerViewManager().setOnMarkerViewClickListener(this);

		map.setOnMyLocationTrackingModeChangeListener(myLocationTrackingMode -> {
			if (myLocationTrackingMode == TRACKING_FOLLOW) {
				ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.fabBackground));
				gpsFab.setBackgroundTintList(colorStateList);
				gpsFab.getDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.fabForegroundFollow), SRC_IN);
			} else {
				ColorStateList colorStateList = ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.fabBackgroundMoved));
				gpsFab.setBackgroundTintList(colorStateList);
				gpsFab.getDrawable().setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.fabForegroundMoved), SRC_IN);
			}
		});

		zoomToMyLocation();

		// observe map related data
		viewModel.getZoomTo().observe(this, this::zoomTo);
		viewModel.getFindNearbyStations().observe(this, this::findNearbyStations);

		// restore marker on map if there was one
		if (locationFragment != null) {
			LatLng latLng = zoomTo(locationFragment.getLocation());
			addMarker(latLng);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		super.onStart();
		mapView.onStart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mapView.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
		mapView.onStop();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
	}

	public void onTransportNetworkChanged(TransportNetwork network) {
		if (transportNetworkInitialized) {
			Log.w("TEST", "TRANSPORT NETWORK HAS CHANGED!!! " + network.getName(this));
			search.setLocation(null);
			closeDrawer();
			recreate();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, new SavedSearchesFragment(), SavedSearchesFragment.TAG)
					.commit();
		} else {
			search.setTransportNetwork(network);
			transportNetworkInitialized = true;
		}
	}

	@Override
	public void onLocationItemClick(final WrapLocation loc, FavLocationType type) {
		viewModel.clickLocation(loc, FROM);

		LatLng latLng = zoomTo(loc);
		addMarker(latLng);

		locationFragment = LocationFragment.newInstance(loc);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, locationFragment, LocationFragment.TAG)
				.commit(); // takes some time and empty bottomSheet will not be shown
		bottomSheetBehavior.setState(STATE_COLLAPSED);
	}

	private void addMarker(LatLng latLng) {
		if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);
		selectedLocationMarker = map.addMarker(new MarkerOptions().position(latLng));
	}

	public LatLng zoomTo(WrapLocation loc) {
		LatLng latLng = getLatLng(loc.getLocation());
		zoomTo(latLng);
		return latLng;
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

	private boolean locationFragmentVisible() {
		return locationFragment != null && locationFragment.isVisible() && bottomSheetBehavior.getState() != STATE_HIDDEN;
	}

	@Override
	public void onLocationCleared(FavLocationType type) {
		bottomSheetBehavior.setState(STATE_HIDDEN);
	}

	public void findNearbyStations(WrapLocation location) {
		// TODO limit maxDistance to visible area at least, some providers return a lot of stations
		Bundle args = NearbyLocationsLoader.getBundle(location.getLocation(), 0);
		getSupportLoaderManager().restartLoader(LOADER_NEARBY_STATIONS, args, this).forceLoad();
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker) {
		if (marker.equals(selectedLocationMarker)) {
			if (locationFragment != null) search.setLocation(locationFragment.getLocation());
			bottomSheetBehavior.setState(STATE_COLLAPSED);
			return true;
		}
		return false;
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker, @NonNull View view, @NonNull MapboxMap.MarkerViewAdapter adapter) {
		// https://github.com/mapbox/mapbox-gl-native/issues/8236
		if (nearbyLocations.containsKey(marker)) {
			WrapLocation wrapLocation = new WrapLocation(nearbyLocations.get(marker));
			onLocationItemClick(wrapLocation, FROM);
			search.clearLocation();
			return true;
		}
		return false;
	}

	@Override
	public Loader<NearbyLocationsResult> onCreateLoader(int id, Bundle args) {
		return new NearbyLocationsLoader(this, args);
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
		if (locationFragment != null) locationFragment.onNearbyStationsLoaded();
	}

	@Override
	public void onLoaderReset(Loader<NearbyLocationsResult> loader) {
		nearbyLocations.clear();
	}

	private Icon getNearbyLocationsIcon(@DrawableRes int res) {
		IconFactory iconFactory = IconFactory.getInstance(MapActivity.this);
		Drawable drawable = ContextCompat.getDrawable(this, res);
		return iconFactory.fromBitmap(getBitmap(drawable));
	}

	private Bitmap getBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	private void ensureTransportNetworkSelected() {
		TransportNetwork network = manager.getTransportNetwork().getValue();
		if (network == null) {
			Intent intent = new Intent(this, PickTransportNetworkActivity.class);
			intent.putExtra(FORCE_NETWORK_SELECTION, true);
			startActivity(intent);
		}
	}

	private void enableStrictMode() {
		StrictMode.ThreadPolicy.Builder threadPolicy = new StrictMode.ThreadPolicy.Builder();
		threadPolicy.detectAll();
		threadPolicy.penaltyLog();
		StrictMode.setThreadPolicy(threadPolicy.build());

		StrictMode.VmPolicy.Builder vmPolicy = new StrictMode.VmPolicy.Builder();
		vmPolicy.detectAll();
		vmPolicy.penaltyLog();
		StrictMode.setVmPolicy(vmPolicy.build());
	}

}
