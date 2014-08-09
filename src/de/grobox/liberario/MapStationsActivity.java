/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

package de.grobox.liberario;

import java.util.ArrayList;
import java.util.List;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.ExtendedOverlayItem;
import org.osmdroid.bonuspack.overlays.InfoWindow;
import org.osmdroid.bonuspack.overlays.ItemizedOverlayWithBubble;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class MapStationsActivity extends Activity {
	private MapView mMapView;
	Menu mMenu;
	private GpsMyLocationProvider mLocProvider;
	private MyLocationNewOverlay mMyLocationOverlay;
	private boolean mGps;
	private ArrayList<StationOverlayItem> mStations = new ArrayList<StationOverlayItem>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mMapView = new MapView(this, 256);

		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);
		mMapView.setMultiTouchControls(true);

		Intent intent = getIntent();
		List<Location> locations = (ArrayList<Location>) intent.getSerializableExtra("List<de.schildbach.pte.dto.Location>");
		Location myLoc = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Location");

		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		// find location area and mark locations on map
		for(Location loc : locations) {
			if(loc.hasLocation()){
				maxLat = Math.max(loc.lat, maxLat);
				minLat = Math.min(loc.lat, minLat);
				maxLon = Math.max(loc.lon, maxLon);
				minLon = Math.min(loc.lon, minLon);

				markLocation(loc, new ArrayList<Line>());
			}
		}

		final GeoPoint center = new GeoPoint( (maxLat + minLat)/2, (maxLon + minLon)/2 );

		IMapController mapController = mMapView.getController();
		mapController.setCenter(center);
		mapController.setZoom(15);

		// work around for center issue: https://github.com/osmdroid/osmdroid/issues/22
		mMapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
					mMapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				else
					mMapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				mMapView.getController().setCenter(center);
			}
		});

		ItemizedOverlayWithBubble<StationOverlayItem> stationMarkers = new ItemizedOverlayWithBubble<StationOverlayItem>(this, mStations, mMapView, new StationInfoWindow(mMapView));
		mMapView.getOverlays().add(stationMarkers);

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
		mMyLocationOverlay.enableFollowLocation(); // without this there's no position marker!?
		mMyLocationOverlay.setDrawAccuracyEnabled(true);

		mMapView.getOverlays().add(mMyLocationOverlay);

		// turn GPS off by default
		mGps = false;
		mLocProvider.stopLocationProvider();

		setContentView(mMapView);
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

	private void markLocation(Location loc, List<Line> lines) {
		GeoPoint pos = new GeoPoint(loc.lat / 1E6, loc.lon / 1E6);

		StationOverlayItem station = new StationOverlayItem(loc.name, lines, pos, this);
		station.setMarker(getResources().getDrawable(R.drawable.ic_marker_station));
		station.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
		mStations.add(station);
	}

	private void toggleGPS() {
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

	public class StationOverlayItem extends ExtendedOverlayItem {
		List<Line> mLines;

		public StationOverlayItem(String aTitle, List<Line> lines, GeoPoint aGeoPoint, Context context) {
			super(aTitle, null, aGeoPoint);

			mLines = lines;
		}

		public List<Line> getLines() {
			return mLines;
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

		@Override
		public void onOpen(Object item) {
			StationOverlayItem stationOverlayItem = (StationOverlayItem) item;

			((TextView) mView.findViewById(R.id.bubble_title)).setText(stationOverlayItem.getTitle());

			ViewGroup bubble_lines = (ViewGroup) mView.findViewById(R.id.bubble_lines);
			for(Line line : stationOverlayItem.getLines()) {
				LiberarioUtils.addLineBox(mMapView.getContext(), bubble_lines, line);
			}
		}

		@Override
		public void onClose() {
			// do nothing
		}
	}
}