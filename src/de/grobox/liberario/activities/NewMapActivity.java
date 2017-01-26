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

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.WrapLocation;
import de.grobox.liberario.fragments.LocationFragment;
import de.grobox.liberario.tasks.NearbyLocationsLoader;
import de.grobox.liberario.ui.LocationView;
import de.grobox.liberario.ui.LocationView.LocationViewListener;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.liberario.FavLocation.LOC_TYPE.FROM;
import static de.grobox.liberario.activities.MainActivity.CHANGED_NETWORK_PROVIDER;
import static de.grobox.liberario.data.RecentsDB.updateFavLocation;
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
		menu.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				openDrawer();
			}
		});

		search = (LocationView) findViewById(R.id.search);
		search.initialize(this);
		search.setLocationViewListener(this);

		View bottomSheet = findViewById(R.id.bottomSheet);
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_HIDDEN) {
					if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);
					search.clearLocation();
					directionsFab.show();
				}
			}
			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) { }
		});

		directionsFab = (FloatingActionButton) findViewById(R.id.directionsFab);
		gpsFab = (FloatingActionButton) findViewById(R.id.gpsFab);
		gpsFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			}
		});

		runOnThread(new Runnable() {
			@Override
			public void run() {
				TransportNetwork network = Preferences.getTransportNetwork(NewMapActivity.this);
				if (network == null) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent(NewMapActivity.this, PickNetworkProviderActivity.class);
							// force choosing a network provider
							intent.putExtra("FirstRun", true);
							startActivityForResult(intent, CHANGED_NETWORK_PROVIDER);
						}
					});
				}
			}
		});
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		map = mapboxMap;

//		LatLng latLng = map.getCameraPosition().target;
		Location location = new Location(LocationType.STATION, "fake");
		Bundle args = NearbyLocationsLoader.getBundle(location, 0);
		getSupportLoaderManager().initLoader(LOADER_NEARBY_STATIONS, args, this);

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
		if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
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

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, LocationFragment.newInstance(loc))
				.addToBackStack(null)
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

	public void findNearbyStations(LocationFragment locationFragment, Location location) {
		this.locationFragment = locationFragment;
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
			if (locationFragment != null) locationFragment.onNearbyStationsLoaded();
		} else {
			// TODO
			Log.e("TEST", "ERROR loading nearby stations.");
		}
	}

	@Override
	public void onLoaderReset(Loader<NearbyLocationsResult> loader) {

	}

	private Icon getNearbyLocationsIcon(@DrawableRes int res) {
		IconFactory iconFactory = IconFactory.getInstance(NewMapActivity.this);
		Drawable iconDrawable = ContextCompat.getDrawable(NewMapActivity.this, res);
		return iconFactory.fromDrawable(iconDrawable);
	}

}
