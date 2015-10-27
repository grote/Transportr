/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;

public class MapStationsActivity extends AppCompatActivity {
	private MapView mMapView;
	Menu mMenu;
	private GpsMyLocationProvider mLocProvider;
	private MyLocationNewOverlay mMyLocationOverlay;
	private boolean mGps;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_stations_map);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(Preferences.getTransportNetwork(this).getName());
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// Should we show an explanation?
			if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				Toast.makeText(this, R.string.permission_denied_map, Toast.LENGTH_LONG).show();
				supportFinishAfterTransition();
			} else {
				Toast.makeText(this, R.string.permission_explanation_map, Toast.LENGTH_LONG).show();
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.PR_WRITE_EXTERNAL_STORAGE);
			}
		} else {
			setupMap();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case MainActivity.PR_WRITE_EXTERNAL_STORAGE:{
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// FIXME: For some reason, there are no tiles shown after permission is first granted
					setupMap();
				} else {
					Toast.makeText(this, R.string.permission_denied_map, Toast.LENGTH_LONG).show();
					supportFinishAfterTransition();
				}
				break;
			}
			case MainActivity.PR_ACCESS_FINE_LOCATION_MAPS:{
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					toggleGPS();
				} else {
					Toast.makeText(this, R.string.permission_denied_gps, Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_stations_activity_actions, menu);
		mMenu = menu;

		MenuItem gpsItem = mMenu.findItem(R.id.action_use_gps);

		if(mLocProvider != null) {
			if(mGps) {
				gpsItem.setIcon(R.drawable.ic_gps_off);
			} else {
				gpsItem.setIcon(R.drawable.ic_gps);
			}
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			case R.id.action_use_gps:
				toggleGPS();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void setupMap() {
		mMapView = new MapView(this, 256);

		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);
		mMapView.setTilesScaledToDpi(true);

		((LinearLayout) findViewById(R.id.root)).addView(mMapView);

		Intent intent = getIntent();
		List<Location> locations = (ArrayList<Location>) intent.getSerializableExtra("List<de.schildbach.pte.dto.Location>");
		Location myLoc = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Location");

		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		int count = 0;

		// find location area and mark locations on map
		for(Location loc : locations) {
			if(loc.hasLocation()){
				maxLat = Math.max(loc.lat, maxLat);
				minLat = Math.min(loc.lat, minLat);
				maxLon = Math.max(loc.lon, maxLon);
				minLon = Math.min(loc.lon, minLon);

				count += 1;

				// TODO: show products here instead of lines (which are not available)
				markLocation(loc, new ArrayList<Line>());
			}
		}

		// include my location in center calculation if available
		if(myLoc != null) {
			maxLat = Math.max(myLoc.lat, maxLat);
			minLat = Math.min(myLoc.lat, minLat);
			maxLon = Math.max(myLoc.lon, maxLon);
			minLon = Math.min(myLoc.lon, minLon);
			count += 1;
		}

		final GeoPoint center = new GeoPoint( (maxLat + minLat)/2, (maxLon + minLon)/2 );

		IMapController mapController = mMapView.getController();
		mapController.setCenter(center);
		mapController.setZoom(18);
		if(count > 1) {
			mapController.zoomToSpan(maxLat - minLat, maxLon - minLon);
		}

		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			setupGPS(myLoc);
		}
	}

	private void setupGPS(Location myLoc) {
		mLocProvider = new GpsMyLocationProvider(this);

		// show last known position on map
		if(myLoc != null) {
			// create temporary location object with last known position
			android.location.Location tmp_loc = new android.location.Location("");
			tmp_loc.setLatitude(myLoc.lat / 1E6);
			tmp_loc.setLongitude(myLoc.lon / 1E6);

			// set last known position
			mLocProvider.onLocationChanged(tmp_loc);
		}

		// create my location overlay that shows the current position and updates automatically
		mMyLocationOverlay = new MyLocationNewOverlay(this, mMapView);
		mMyLocationOverlay.enableMyLocation(mLocProvider);
		mMyLocationOverlay.setDrawAccuracyEnabled(true);

		mMapView.getOverlays().add(mMyLocationOverlay);

		// turn GPS off by default
		mGps = false;
		mLocProvider.stopLocationProvider();
	}

	private void markLocation(Location loc, List<Line> lines) {
		GeoPoint pos = new GeoPoint(loc.lat / 1E6, loc.lon / 1E6);

		Log.d(getClass().getSimpleName(), "Mark location: " + loc.toString());

		Marker marker = new Marker(mMapView);
		marker.setIcon(getResources().getDrawable(R.drawable.ic_marker_station));
		marker.setPosition(pos);
		marker.setTitle(loc.uniqueShortName());
		marker.setInfoWindow(new StationInfoWindow(mMapView));
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setRelatedObject(lines);
		mMapView.getOverlays().add(marker);
	}

	private void toggleGPS() {
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// Should we show an explanation?
			if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
				Toast.makeText(this, "You need to grant the location permission in order to see your current position on the map.", Toast.LENGTH_LONG).show();
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MainActivity.PR_ACCESS_FINE_LOCATION_MAPS);
			}
			return;
		}

		if(mLocProvider == null) setupGPS(null);

		MenuItem gpsItem = mMenu.findItem(R.id.action_use_gps);

		if(mGps) {
			mGps = false;
			mLocProvider.stopLocationProvider();
			gpsItem.setIcon(R.drawable.ic_gps);
		} else {
			mGps = true;
			mLocProvider.startLocationProvider(mMyLocationOverlay);
			gpsItem.setIcon(R.drawable.ic_gps_off);
		}
	}

	public class StationInfoWindow extends InfoWindow {

		public StationInfoWindow(MapView mapView) {
			super(R.layout.bubble_station, mapView);

			// close it when clicking on the bubble
			mView.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent e) {
					if (e.getAction() == MotionEvent.ACTION_UP) {
						close();
					}
					return true;
				}
			});
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onOpen(Object item) {
			Marker marker = (Marker) item;

			((TextView) mView.findViewById(R.id.bubble_title)).setText(marker.getTitle());

			ViewGroup bubble_lines = (ViewGroup) mView.findViewById(R.id.bubble_lines);
			for(Line line : (List<Line>) marker.getRelatedObject()) {
				TransportrUtils.addLineBox(mMapView.getContext(), bubble_lines, line);
			}
		}

		@Override
		public void onClose() {
			// do nothing
		}
	}
}