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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.telemetry.MapboxEventManager;

import de.grobox.liberario.R;
import de.grobox.liberario.WrapLocation;
import de.grobox.liberario.fragments.LocationFragment;
import de.grobox.liberario.ui.LocationView;
import de.grobox.liberario.ui.LocationView.LocationViewListener;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.liberario.FavLocation.LOC_TYPE.FROM;
import static de.grobox.liberario.data.RecentsDB.updateFavLocation;

public class NewMapActivity extends DrawerActivity implements LocationViewListener, OnMapReadyCallback {

	private MapView mapView;
	private MapboxMap map;
	private LocationView search;
	private FloatingActionButton gpsFab, directionsFab;
	private BottomSheetBehavior bottomSheetBehavior;

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
					for (Marker m : map.getMarkers()) map.removeMarker(m);
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
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		map = mapboxMap;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
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

		LatLng latLng = new LatLng(loc.getLocation().getLatAsDouble(), loc.getLocation().getLonAsDouble());
		map.addMarker(new MarkerOptions().position(latLng));
		map.easeCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14), 1500);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, LocationFragment.newInstance(loc))
				.addToBackStack(null)
				.commit(); // takes some time and empty bottomSheet will not be shown
		bottomSheetBehavior.setState(STATE_COLLAPSED);
		directionsFab.hide();
	}

	@Override
	public void onLocationCleared() {
		bottomSheetBehavior.setState(STATE_HIDDEN);
	}

}
