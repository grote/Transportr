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

import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MapStationsActivity extends Activity {
	private MapView mMapView;
	private ArrayList<StationOverlayItem> mStations = new ArrayList<StationOverlayItem>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		mMapView = new MapView(this, 256);

		mMapView.setClickable(true);
		mMapView.setBuiltInZoomControls(true);

		Intent intent = getIntent();
		List<Location> locations = (ArrayList<Location>) intent.getSerializableExtra("List<de.schildbach.pte.dto.Location>");

		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;

		for(Location loc : locations) {
			if(loc.hasLocation()){
				maxLat = Math.max(loc.lat, maxLat);
				minLat = Math.min(loc.lat, minLat);
				maxLon = Math.max(loc.lon, maxLon);
				minLon = Math.min(loc.lon, minLon);

				markLocation(loc, new ArrayList<Line>());
			}
		}

		IMapController mapController = mMapView.getController();
		mapController.setZoom(15);
		mapController.setCenter(new GeoPoint( (maxLat + minLat)/2, (maxLon + minLon)/2 ));

		ItemizedOverlayWithBubble<StationOverlayItem> stationMarkers = new ItemizedOverlayWithBubble<StationOverlayItem>(this, mStations, mMapView, new StationInfoWindow(mMapView));
		mMapView.getOverlays().add(stationMarkers);

		setContentView(mMapView);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void markLocation(Location loc, List<Line> lines) {
		GeoPoint pos = new GeoPoint(loc.lat / 1E6, loc.lon / 1E6);

		StationOverlayItem station = new StationOverlayItem(loc.name, lines, pos, this);
		station.setMarker(getResources().getDrawable(R.drawable.ic_marker_station));
		station.setMarkerHotspot(OverlayItem.HotspotPlace.BOTTOM_CENTER);
		mStations.add(station);
	}

	public class StationOverlayItem extends ExtendedOverlayItem {
		List<Line> mLines;

		public StationOverlayItem(String aTitle, List<Line> lines, GeoPoint aGeoPoint, Context context) {
			super(aTitle, null, aGeoPoint, context);

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