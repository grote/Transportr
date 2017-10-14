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

package de.grobox.liberario.activities;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
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
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.BuildConfig;
import de.grobox.liberario.R;
import de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.favorites.trips.FavoriteTripsFragment;
import de.grobox.liberario.locations.LocationFragment;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.LocationView.LocationViewListener;
import de.grobox.liberario.locations.LocationsViewModel;
import de.grobox.liberario.locations.NearbyLocationsLoader;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.PickTransportNetworkActivity;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.trips.search.DirectionsActivity;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.support.design.widget.BottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.liberario.locations.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.networks.PickTransportNetworkActivity.FORCE_NETWORK_SELECTION;
import static de.grobox.liberario.utils.Constants.LOADER_NEARBY_STATIONS;
import static de.grobox.liberario.utils.TransportrUtils.findDirections;
import static de.grobox.liberario.utils.TransportrUtils.getLatLng;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;
import static de.grobox.liberario.utils.TransportrUtils.getMarkerForProduct;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

@ParametersAreNonnullByDefault
public class NewMapActivity extends DrawerActivity
		implements LocationViewListener, OnMapReadyCallback, LoaderCallbacks<NearbyLocationsResult> {

	private final static int LOCATION_ZOOM = 14;
	private final static String BOTTOM_SHEET_HEIGHT = "bottomSheetHeight";

	@Inject	ViewModelProvider.Factory viewModelFactory;

	private LocationsViewModel viewModel;
	private MapView mapView;
	private MapboxMap map;
	private LocationView search;
	private BottomSheetBehavior bottomSheetBehavior;

	private @Nullable LocationFragment locationFragment;
	private @Nullable Marker selectedLocationMarker;
	private Map<Marker, Location> nearbyLocations = new HashMap<>();
	private boolean transportNetworkInitialized = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
//		enableStrictMode();
		getComponent().inject(this);
		ensureTransportNetworkSelected();
		setContentView(R.layout.activity_new_map);
		super.onCreate(savedInstanceState);

		mapView = findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);

		View menu = findViewById(R.id.menu);
		menu.setOnClickListener(view -> openDrawer());

		search = findViewById(R.id.search);
		search.setLocationViewListener(this);

		// get view model and observe data
		viewModel = ViewModelProviders.of(this, viewModelFactory).get(LocationsViewModel.class);
		viewModel.getTransportNetwork().observe(this, this::onTransportNetworkChanged);
		viewModel.getHome().observe(this, homeLocation -> search.setHomeLocation(homeLocation));
		viewModel.getWork().observe(this, workLocation -> search.setWorkLocation(workLocation));
		viewModel.getLocations().observe(this, favoriteLocations -> search.setFavoriteLocations(favoriteLocations));

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

		FloatingActionButton directionsFab = findViewById(R.id.directionsFab);
		directionsFab.setOnClickListener(view -> {
			if (locationFragment != null && locationFragmentVisible()) {
				findDirections(NewMapActivity.this, 0, new WrapLocation(GPS), null, locationFragment.getLocation(), null, true);
			} else {
				Intent intent = new Intent(NewMapActivity.this, DirectionsActivity.class);
				startActivity(intent);
			}
		});
		FloatingActionButton gpsFab = findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(view -> {
			// TODO
			Intent intent = new Intent(NewMapActivity.this, MainActivity.class);
			startActivity(intent);
		});

		if (savedInstanceState == null) {
			FavoriteTripsFragment f = FavoriteTripsFragment.newInstance(true);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, f, FavoriteTripsFragment.TAG)
					.commitNow(); // otherwise takes some time and empty bottomSheet will not be shown
			bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_AUTO);
			bottomSheetBehavior.setState(STATE_COLLAPSED);
		} else {
			bottomSheetBehavior.setPeekHeight(savedInstanceState.getInt(BOTTOM_SHEET_HEIGHT));
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
		map.setOnMarkerClickListener(marker -> {
			if (marker.equals(selectedLocationMarker)) {
				if (locationFragment != null) search.setLocation(locationFragment.getLocation());
				bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_AUTO);
				bottomSheetBehavior.setState(STATE_COLLAPSED);
				return true;
			} else if (nearbyLocations.containsKey(marker)) {
				WrapLocation wrapLocation = new WrapLocation(nearbyLocations.get(marker));
				onLocationItemClick(wrapLocation, FROM);
				search.clearLocation();
				return true;
			} else {
				return false;
			}
		});
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
		outState.putInt(BOTTOM_SHEET_HEIGHT, bottomSheetBehavior.getPeekHeight());
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
			recreate();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, FavoriteTripsFragment.newInstance(true), FavoriteTripsFragment.TAG)
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
		bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_AUTO);
		bottomSheetBehavior.setState(STATE_COLLAPSED);
	}

	private void addMarker(LatLng latLng) {
		if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);
		selectedLocationMarker = map.addMarker(new MarkerOptions().position(latLng));
	}

	public LatLng zoomTo(WrapLocation loc) {
		LatLng latLng = getLatLng(loc.getLocation());
		CameraUpdate update = map.getCameraPosition().zoom < LOCATION_ZOOM ?
				CameraUpdateFactory.newLatLngZoom(latLng, LOCATION_ZOOM) : CameraUpdateFactory.newLatLng(latLng);
		map.easeCamera(update, 1500);
		return latLng;
	}

	private boolean locationFragmentVisible() {
		return locationFragment != null && locationFragment.isVisible() && bottomSheetBehavior.getState() != STATE_HIDDEN;
	}

	@Override
	public void onLocationCleared(FavLocationType type) {
		bottomSheetBehavior.setState(STATE_HIDDEN);
	}

	public void findNearbyStations(Location location) {
		Bundle args = NearbyLocationsLoader.getBundle(location, 0);
		getSupportLoaderManager().restartLoader(LOADER_NEARBY_STATIONS, args, this).forceLoad();
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
				Log.e("TEST", location.toString());
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
		IconFactory iconFactory = IconFactory.getInstance(NewMapActivity.this);
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
		if(!BuildConfig.DEBUG) return;

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
