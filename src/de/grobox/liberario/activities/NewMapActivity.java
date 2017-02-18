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
import android.support.v4.app.LoaderManager.LoaderCallbacks;
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

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.BuildConfig;
import de.grobox.liberario.R;
import de.grobox.liberario.favorites.FavoritesFragment;
import de.grobox.liberario.locations.LocationFragment;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.LocationView.LocationViewListener;
import de.grobox.liberario.locations.NearbyLocationsLoader;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.PickTransportNetworkActivity;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.trips.DirectionsActivity;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.support.design.widget.BottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.liberario.data.LocationDb.updateFavLocation;
import static de.grobox.liberario.locations.FavLocation.FavLocationType.FROM;
import static de.grobox.liberario.locations.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.networks.PickTransportNetworkActivity.FORCE_NETWORK_SELECTION;
import static de.grobox.liberario.utils.Constants.LOADER_NEARBY_STATIONS;
import static de.grobox.liberario.utils.Constants.REQUEST_NETWORK_PROVIDER_CHANGE;
import static de.grobox.liberario.utils.TransportrUtils.findDirections;
import static de.grobox.liberario.utils.TransportrUtils.getLatLng;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;
import static de.grobox.liberario.utils.TransportrUtils.getMarkerForProduct;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

@ParametersAreNonnullByDefault
public class NewMapActivity extends DrawerActivity
		implements LocationViewListener, OnMapReadyCallback, LoaderCallbacks<NearbyLocationsResult> {

	private final static int LOCATION_ZOOM = 14;

	private MapView mapView;
	private MapboxMap map;
	private LocationView search;
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
		ensureTransportNetworkSelected();
		setContentView(R.layout.activity_new_map);
		super.onCreate(savedInstanceState);

		mapView = (MapView) findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		MapboxEventManager eventManager = MapboxEventManager.getMapboxEventManager();
		if (eventManager.isTelemetryEnabled()) eventManager.setTelemetryEnabled(false);
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
					if (selectedLocationMarker != null) {
						map.removeMarker(selectedLocationMarker);
						selectedLocationMarker = null;
					}
					if (locationFragment != null) {
						getSupportFragmentManager().beginTransaction().remove(locationFragment).commit();
						locationFragment = null;
					}
					search.clearLocation();
					search.reset();
				}
			}
			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
		});

		FloatingActionButton directionsFab = (FloatingActionButton) findViewById(R.id.directionsFab);
		directionsFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (locationFragment != null && locationFragmentVisible()) {
					findDirections(NewMapActivity.this, new WrapLocation(GPS), null, locationFragment.getLocation(), null, true);
				} else {
					Intent intent = new Intent(NewMapActivity.this, DirectionsActivity.class);
					startActivity(intent);
				}
			}
		});
		FloatingActionButton gpsFab = (FloatingActionButton) findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				// TODO
				Intent intent = new Intent(NewMapActivity.this, MainActivity.class);
				startActivity(intent);
			}
		});

		if (savedInstanceState == null) {
			FavoritesFragment f = FavoritesFragment.newInstance(true);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, f, FavoritesFragment.TAG)
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
		manager.addOnTransportNetworkChangedListener(this);
		mapView.onResume();
	}

	@Override
	public void onStop() {
		super.onStop();
		manager.removeOnTransportNetworkChangedListener(this);
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
	public void onTransportNetworkChanged(TransportNetwork network) {
		// TODO
		Log.e("TEST", "Transport Network Changed, recreating...");
		recreate();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, FavoritesFragment.newInstance(true), FavoritesFragment.TAG)
				.commit();
	}

	@Override
	public void onLocationItemClick(final WrapLocation loc) {
		runOnThread(new Runnable() {
			@Override
			public void run() {
				// TODO do this with TransportManager
				updateFavLocation(NewMapActivity.this, loc.getLocation(), FROM);
			}
		});

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
		CameraUpdate update = map.getCameraPosition().zoom < LOCATION_ZOOM ?
				CameraUpdateFactory.newLatLngZoom(latLng, LOCATION_ZOOM) : CameraUpdateFactory.newLatLng(latLng);
		map.easeCamera(update, 1500);
		return latLng;
	}

	private boolean locationFragmentVisible() {
		return locationFragment != null && locationFragment.isVisible() && bottomSheetBehavior.getState() != STATE_HIDDEN;
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
		Drawable iconDrawable = ContextCompat.getDrawable(NewMapActivity.this, res);
		return iconFactory.fromDrawable(iconDrawable);
	}

	private void ensureTransportNetworkSelected() {
		TransportNetwork network = manager.getTransportNetwork();
		if (network == null) {
			Intent intent = new Intent(this, PickTransportNetworkActivity.class);
			intent.putExtra(FORCE_NETWORK_SELECTION, true);
			startActivityForResult(intent, REQUEST_NETWORK_PROVIDER_CHANGE);
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
