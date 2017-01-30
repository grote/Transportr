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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

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
import com.mapbox.mapboxsdk.telemetry.MapboxEventManager;

import java.util.HashMap;
import java.util.Map;

import de.grobox.liberario.BuildConfig;
import de.grobox.liberario.R;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.favorites.FavoritesFragment;
import de.grobox.liberario.locations.LocationFragment;
import de.grobox.liberario.locations.NearbyLocationsLoader;
import de.grobox.liberario.networks.PickTransportNetworkActivity;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.LocationView.LocationViewListener;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.support.design.widget.BottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.liberario.locations.FavLocation.FavLocationType.FROM;
import static de.grobox.liberario.activities.MainActivity.CHANGED_NETWORK_PROVIDER;
import static de.grobox.liberario.data.RecentsDB.updateFavLocation;
import static de.grobox.liberario.settings.Preferences.getTransportNetwork;
import static de.grobox.liberario.utils.Constants.LOADER_NEARBY_STATIONS;
import static de.grobox.liberario.utils.TransportrUtils.getLatLng;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;
import static de.grobox.liberario.utils.TransportrUtils.getMarkerForProduct;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

public class NewMapActivity extends DrawerActivity
		implements LocationViewListener, OnMapReadyCallback, LoaderManager.LoaderCallbacks<NearbyLocationsResult> {

	private final static int LOCATION_ZOOM = 14;

	private MapView mapView;
	private MapboxMap map;
	private LocationView search;
	private FloatingActionButton gpsFab, directionsFab;
	private BottomSheetBehavior bottomSheetBehavior;

	@Nullable
	LocationFragment locationFragment;
	@Nullable
	Marker selectedLocationMarker;
	Map<Marker, Location> nearbyLocations = new HashMap<>();

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
//		enableStrictMode();
		getComponent().inject(this);
		setContentView(R.layout.activity_new_map);
		super.onCreate(savedInstanceState);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		MapboxEventManager eventManager = MapboxEventManager.getMapboxEventManager();
		if (eventManager.isTelemetryEnabled()) {
			eventManager.setTelemetryEnabled(false);
		}
		mapView.getMapAsync(this);

		View menu = findViewById(R.id.menu);
		menu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				openDrawer();
			}
		});

		search = (LocationView) findViewById(R.id.search);
		search.setLocationViewListener(this);

		View bottomSheet = findViewById(R.id.bottomSheet);
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == STATE_HIDDEN) {
					if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);
					search.clearLocation();
					directionsFab.show();
				}
			}
			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
		});

		directionsFab = (FloatingActionButton) findViewById(R.id.directionsFab);
		directionsFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(NewMapActivity.this, DirectionsActivity.class);
				startActivity(intent);
			}
		});
		gpsFab = (FloatingActionButton) findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
				Intent intent = new Intent(NewMapActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});

		runOnThread(new Runnable() {
			@Override
			public void run() {
				TransportNetwork network = getTransportNetwork(NewMapActivity.this);
				if (network == null) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(NewMapActivity.this, PickTransportNetworkActivity.class);
							// force choosing a network provider
							intent.putExtra("FirstRun", true);
							startActivityForResult(intent, CHANGED_NETWORK_PROVIDER);
						}
					});
				}
			}
		});

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, FavoritesFragment.newInstance(), FavoritesFragment.TAG)
					.commitNow(); // otherwise takes some time and empty bottomSheet will not be shown
			bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_AUTO);
			bottomSheetBehavior.setState(STATE_COLLAPSED);
			directionsFab.hide();
		}
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		map = mapboxMap;

//		LatLng latLng = map.getCameraPosition().target;
		Location location = new Location(LocationType.STATION, "fake");
		Bundle args = NearbyLocationsLoader.getBundle(location, 0);
		getSupportLoaderManager().initLoader(LOADER_NEARBY_STATIONS, args, this);

		map.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
			@Override
			public void onMapClick(@NonNull LatLng point) {
				search.clearFocus();
				search.hideSoftKeyboard();
			}
		});
		map.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(@NonNull LatLng point) {
				onLocationItemClick(new WrapLocation(point));
			}
		});
		map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(@NonNull Marker marker) {
				if (!nearbyLocations.containsKey(marker)) return false;
				WrapLocation wrapLocation = new WrapLocation(nearbyLocations.get(marker));
				onLocationItemClick(wrapLocation);
				search.clearLocation();
				return true;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		if (savedInstanceState != null && bottomSheetBehavior.getState() != STATE_HIDDEN) {
			directionsFab.hide();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mapView.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		mapView.onPause();
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

	@Override
	public void onLocationItemClick(final WrapLocation loc) {
		runOnThread(new Runnable() {
			@Override
			public void run() {
				updateFavLocation(NewMapActivity.this, loc.getLocation(), FROM);
			}
		});
		if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);

		LatLng latLng = zoomTo(loc);
		selectedLocationMarker = map.addMarker(new MarkerOptions().position(latLng));

		locationFragment = LocationFragment.newInstance(loc);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, locationFragment, LocationFragment.TAG)
				.commit(); // takes some time and empty bottomSheet will not be shown
		bottomSheetBehavior.setState(STATE_COLLAPSED);
		directionsFab.hide();
	}

	public LatLng zoomTo(WrapLocation loc) {
		LatLng latLng = getLatLng(loc.getLocation());
		CameraUpdate update = map.getCameraPosition().zoom < LOCATION_ZOOM ?
				CameraUpdateFactory.newLatLngZoom(latLng, LOCATION_ZOOM) : CameraUpdateFactory.newLatLng(latLng);
		map.easeCamera(update, 1500);
		return latLng;
	}

	@Override
	public void onLocationCleared() {
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
	public void onLoadFinished(Loader<NearbyLocationsResult> loader, NearbyLocationsResult result) {
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
		Drawable iconDrawable = ContextCompat.getDrawable(NewMapActivity.this, res);
		return iconFactory.fromDrawable(iconDrawable);
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
