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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;

public class MapActivity extends TransportrActivity implements MapEventsReceiver {
	private MapView map;
	private Menu menu;
	private List<GeoPoint> points = new ArrayList<>();
	private GpsMyLocationProvider locationProvider;
	private MyLocationNewOverlay myLocationOverlay;
	private boolean gps;
	private TransportNetwork network;

	public final static String SHOW_AREA = "de.grobox.liberario.MapActivity.SHOW_AREA";
	public final static String SHOW_LOCATIONS = "de.grobox.liberario.MapActivity.SHOW_LOCATIONS";
	public final static String SHOW_TRIP = "de.grobox.liberario.MapActivity.SHOW_TRIP";

	public final static String LOCATION = "de.schildbach.pte.dto.Location";
	public final static String LOCATIONS = "List<de.schildbach.pte.dto.Location>";
	public final static String TRIP = "de.schildbach.pte.dto.Trip";

	private enum MarkerType { BEGIN, CHANGE, STOP, END, WALK }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stations_map);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		network = Preferences.getTransportNetwork(this);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(network.getName());
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
		inflater.inflate(R.menu.map_actions, menu);
		this.menu = menu;

		MenuItem gpsItem = this.menu.findItem(R.id.action_use_gps);

		if(locationProvider != null) {
			if(gps) {
				gpsItem.setIcon(TransportrUtils.getToolbarDrawable(this, R.drawable.ic_gps_off));
			} else {
				gpsItem.setIcon(TransportrUtils.getToolbarDrawable(this, R.drawable.ic_gps));
			}
		} else {
			TransportrUtils.fixToolbarIcon(this, gpsItem);
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
			case R.id.action_show_all:
				setViewSpan();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean singleTapConfirmedHelper(GeoPoint p) {
		InfoWindow.closeAllInfoWindowsOn(map);
		return true;
	}

	@Override
	public boolean longPressHelper(GeoPoint p) {
		String loc_str = TransportrUtils.getCoordinationName(p.getLatitude(), p.getLongitude());
		Marker marker = new Marker(map);
		marker.setIcon(new ColorDrawable(Color.TRANSPARENT));
		marker.setPosition(p);
		marker.setTitle(getString(R.string.location)+ ": " + loc_str);
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setRelatedObject(Location.coord(p.getLatitudeE6(), p.getLongitudeE6()));
		marker.setInfoWindow(new LocationInfoWindow(map));
		map.getOverlays().add(marker);

		// center map smoothly where clicked
		map.getController().animateTo(p);

		// show info window
		marker.showInfoWindow();

		return true;
	}

	private void setupMap() {
		map = new MapView(this);

		map.setClickable(true);
		map.setMultiTouchControls(true);
		map.setTilesScaledToDpi(true);

		MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this, this);
		map.getOverlays().add(0, mapEventsOverlay);

		// add map to root view
		ViewGroup root = (ViewGroup) findViewById(R.id.root);
		if(root == null) return;
		root.addView(map);

		Intent intent = getIntent();
		if(intent != null) {
			String action = intent.getAction();
			if(action != null) {
				if(action.equals(SHOW_AREA)) {
					showArea();
				} else if(action.equals(SHOW_LOCATIONS)) {
					@SuppressWarnings("unchecked")
					List<Location> locations = (ArrayList<Location>) intent.getSerializableExtra(LOCATIONS);
					Location myLoc = (Location) intent.getSerializableExtra(LOCATION);
					showLocations(locations, myLoc);
				} else if(action.equals(SHOW_TRIP)) {
					Trip trip = (Trip) intent.getSerializableExtra(TRIP);
					showTrip(trip);
				}
			}
		}

		setViewSpan();
	}

	private void showArea() {
		// TODO implement loader that calls np.getArea()
		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			setupGPS(null);
		}
	}

	private void showLocations(List<Location> locations, Location myLocation) {
		// mark locations on map
		for(Location loc : locations) {
			if(loc.hasLocation()){
				markLocation(loc, ContextCompat.getDrawable(this, R.drawable.ic_marker_station));
			}
		}

		// include my location in view span calculation if available
		if(myLocation != null) {
			points.add(new GeoPoint(myLocation.getLatAsDouble(), myLocation.getLonAsDouble()));
		}

		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
			setupGPS(myLocation);
		}
	}

	private void showTrip(Trip trip) {
		// draw leg path first, so it is always at the bottom
		int width = getResources().getDisplayMetrics().densityDpi / 32;
		boolean havePolyLine = false;
		for(Trip.Leg leg : trip.legs) {
			// draw leg path first, so it is always at the bottom
			if(leg.path != null) {
				Polyline polyline = new Polyline(this);
				List<GeoPoint> geoPoints = new ArrayList<>(leg.path.size());
				for(Point point : leg.path) {
					geoPoints.add(new GeoPoint(point.getLatAsDouble(), point.getLonAsDouble()));
				}
				polyline.setPoints(geoPoints);
				polyline.setWidth(width);
				if(leg instanceof Trip.Public) {
					Line line = ((Trip.Public) leg).line;
					polyline.setColor(getBackgroundColor(MarkerType.CHANGE, line));
					if(line != null) polyline.setTitle(line.id);
				} else {
					polyline.setColor(getBackgroundColor(MarkerType.WALK, null));
					polyline.setTitle(getString(R.string.walk));
				}
				map.getOverlays().add(polyline);
				havePolyLine = true;
			}
		}

		// Now draw intermediate stops on top of path
		for(Trip.Leg leg : trip.legs) {
			if (leg instanceof Trip.Public) {
				Trip.Public public_leg = (Trip.Public) leg;

				if(public_leg.intermediateStops != null) {
					Drawable stop_drawable = getMarkerDrawable(MarkerType.STOP, public_leg.line);
					for(Stop stop : public_leg.intermediateStops) {
						if(stop.location != null) {
							markLocation(stop.location, stop_drawable);
						}
					}
				}
			}
		}

		// At last, draw the beginning, the end and the changing stations
		int i = 1;
		for(Trip.Leg leg : trip.legs) {
			// Draw public transportation stations
			if (leg instanceof Trip.Public) {
				Trip.Public public_leg = (Trip.Public) leg;

				// Draw first station or change station
				if(i == 1 || (i == 2 && trip.legs.get(0) instanceof Trip.Individual)) {
					markLocation(leg.departure, getMarkerDrawable(MarkerType.BEGIN, public_leg.line));
				} else {
					markLocation(leg.departure, getMarkerDrawable(MarkerType.CHANGE, public_leg.line));
				}

				// Draw final station only at the end or if end is walking
				if(i == trip.legs.size() || (i == trip.legs.size()-1 && trip.legs.get(i) instanceof Trip.Individual)) {
					markLocation(leg.arrival, getMarkerDrawable(MarkerType.END, public_leg.line));
				}
			}
			// Walking
			else if(leg instanceof Trip.Individual) {
				if(i != trip.legs.size() || trip.legs.size() == 1) {
					markLocation(leg.departure, getMarkerDrawable(MarkerType.WALK, null));
				}
				// draw walking icon for arrival only at the end of the trip
				if(i == trip.legs.size()) {
					markLocation(leg.arrival, getMarkerDrawable(MarkerType.WALK, null));
				}
			}

			i += 1;
		}
		// turn off hardware rendering to work around this issue:
		// https://github.com/MKergall/osmbonuspack/issues/168
		if(havePolyLine) {
			map.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		}
	}

	private Drawable getMarkerDrawable(MarkerType type, Line line) {
		// Get colors
		int bg = getBackgroundColor(type, line);
		int fg = getForegroundColor(type, line);

		// Get Drawable
		int res;
		if(type == MarkerType.BEGIN) {
			res = R.drawable.ic_marker_trip_begin;
		} else if(type == MarkerType.CHANGE) {
			res = R.drawable.ic_marker_trip_change;
		} else if(type == MarkerType.END) {
			res = R.drawable.ic_marker_trip_end;
		} else if(type == MarkerType.WALK) {
			res = R.drawable.ic_marker_trip_walk;
		} else {
			res = R.drawable.ic_marker_intermediate_stop;
		}
		LayerDrawable drawable = (LayerDrawable) ContextCompat.getDrawable(this, res);
		if(drawable != null) {
			drawable.getDrawable(0).mutate().setColorFilter(bg, PorterDuff.Mode.MULTIPLY);
			drawable.getDrawable(1).mutate().setColorFilter(fg, PorterDuff.Mode.SRC_IN);
		}

		return drawable;
	}

	private int getBackgroundColor(MarkerType type, Line line) {
		int bg;
		if(type != MarkerType.WALK) {
			if(line != null && line.style != null && line.style.backgroundColor != 0) {
				bg = line.style.backgroundColor;
			} else {
				bg = ContextCompat.getColor(this, R.color.accent);
			}
		} else {
			bg = ContextCompat.getColor(this, R.color.walking);
		}
		return bg;
	}

	private int getForegroundColor(MarkerType type, Line line) {
		int fg;
		if(type != MarkerType.WALK) {
			if(line != null && line.style != null && line.style.foregroundColor != 0) {
				fg = line.style.foregroundColor;
			} else {
				fg = ContextCompat.getColor(this, android.R.color.white);
			}
		} else {
			fg = ContextCompat.getColor(this, android.R.color.black);
		}
		return fg;
	}

	private void setViewSpan() {
		if(points.size() == 0) return;

		int maxLat = Integer.MIN_VALUE;
		int minLat = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;

		for(GeoPoint point : points) {
			maxLat = Math.max(point.getLatitudeE6(), maxLat);
			minLat = Math.min(point.getLatitudeE6(), minLat);
			maxLon = Math.max(point.getLongitudeE6(), maxLon);
			minLon = Math.min(point.getLongitudeE6(), minLon);
		}

		int center_lat = (maxLat + minLat)/2;
		int center_lon = (maxLon + minLon)/2;
		final GeoPoint center = new GeoPoint(center_lat, center_lon);

		IMapController mapController = map.getController();
		mapController.setCenter(center);
		mapController.setZoom(18);
		if(points.size() > 1) {
			mapController.zoomToSpan(maxLat - minLat, maxLon - minLon);
		}
	}

	private void setupGPS(Location myLoc) {
		locationProvider = new GpsMyLocationProvider(this) {
			@Override
			public void onLocationChanged(final android.location.Location location) {
				super.onLocationChanged(location);
				points.add(new GeoPoint(location));
			}
		};

		// show last known position on map
		if(myLoc != null) {
			// create temporary location object with last known position
			android.location.Location tmp_loc = new android.location.Location("");
			tmp_loc.setLatitude(myLoc.lat / 1E6);
			tmp_loc.setLongitude(myLoc.lon / 1E6);

			// set last known position
			locationProvider.onLocationChanged(tmp_loc);
		}

		// create my location overlay that shows the current position and updates automatically
		myLocationOverlay = new MyLocationNewOverlay(locationProvider, map);
		myLocationOverlay.enableMyLocation(locationProvider);
		myLocationOverlay.setDrawAccuracyEnabled(true);

		// set new icons
		Bitmap person = getBitmap(ContextCompat.getDrawable(this, R.drawable.map_pedestrian_location));
		Bitmap directionArrow = getBitmap(ContextCompat.getDrawable(this, R.drawable.map_pedestrian_bearing));
		myLocationOverlay.setDirectionArrow(person, directionArrow);

		// properly position person icon
		float scale = getResources().getDisplayMetrics().density;
		myLocationOverlay.setPersonHotspot(16.0f * scale + 0.5f, 19.0f * scale + 0.5f);

		map.getOverlays().add(myLocationOverlay);

		// turn GPS off by default
		gps = false;
		locationProvider.stopLocationProvider();
	}

	private void markLocation(Location loc, Drawable drawable) {
		GeoPoint pos = new GeoPoint(loc.getLatAsDouble(), loc.getLonAsDouble());

		Log.d(getClass().getSimpleName(), "Mark location: " + loc.toString());

		points.add(pos);
		Marker marker = new Marker(map);
		marker.setIcon(drawable);
		marker.setPosition(pos);
		marker.setTitle(TransportrUtils.getLocName(loc));
		marker.setInfoWindow(new LocationInfoWindow(map));
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setRelatedObject(loc);
		map.getOverlays().add(marker);
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

		if(locationProvider == null) setupGPS(null);

		MenuItem gpsItem = menu.findItem(R.id.action_use_gps);

		if(gps) {
			gps = false;
			locationProvider.stopLocationProvider();
			gpsItem.setIcon(TransportrUtils.getToolbarDrawable(this, R.drawable.ic_gps));
		} else {
			gps = true;
			locationProvider.startLocationProvider(myLocationOverlay);
			gpsItem.setIcon(TransportrUtils.getToolbarDrawable(this, R.drawable.ic_gps_off));
		}
	}

	public class LocationInfoWindow extends InfoWindow {

		public LocationInfoWindow(MapView mapView) {
			super(R.layout.location_info_window, mapView);

			// close it when clicking on the bubble
			mView.setOnTouchListener(new View.OnTouchListener() {
				@Override public boolean onTouch(View v, MotionEvent e) {
					if(e.getAction() == MotionEvent.ACTION_UP) {
						close();
					}
					return true;
				}
			});
		}

		@Override
		public void onOpen(Object item) {
			Marker marker = (Marker) item;

			// close all other windows before opening this one
			closeAllInfoWindowsOn(mMapView);

			((TextView) mView.findViewById(R.id.locationTitle)).setText(marker.getTitle());

			final Location loc = (Location) marker.getRelatedObject();

			ViewGroup productsView = (ViewGroup) mView.findViewById(R.id.productsView);
			productsView.removeAllViews();

			// Add product icons if available
			if(loc.products != null && loc.products.size() > 0) {
				for(Product product : loc.products) {
					ImageView image = new ImageView(productsView.getContext());
					image.setImageDrawable(TransportrUtils.getTintedDrawable(productsView.getContext(), TransportrUtils.getDrawableForProduct(product)));
					productsView.addView(image);
				}
			}

			TextView fromHere = ((TextView) mView.findViewById(R.id.fromHere));
			TextView toHere = ((TextView) mView.findViewById(R.id.toHere));
			if(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.TRIPS)) {
				// From Here
				fromHere.setCompoundDrawables(TransportrUtils.getTintedDrawable(MapActivity.this, fromHere.getCompoundDrawables()[0]), null, null, null);
				fromHere.setOnClickListener(new View.OnClickListener() {
					                            @Override
					                            public void onClick(View v) {
						                            TransportrUtils.presetDirections(MapActivity.this, loc, null, null);
					                            }
				                            }
				);

				// To Here
				toHere.setCompoundDrawables(TransportrUtils.getTintedDrawable(MapActivity.this, toHere.getCompoundDrawables()[0]), null, null, null);
				toHere.setOnClickListener(new View.OnClickListener() {
					                          @Override
					                          public void onClick(View v) {
						                          TransportrUtils.presetDirections(MapActivity.this, null, null, loc);
					                          }
				                          }
				);
			} else {
				fromHere.setVisibility(View.GONE);
				toHere.setVisibility(View.GONE);
			}

			// Departures
			TextView departures = ((TextView) mView.findViewById(R.id.departures));
			if(loc.hasId() && network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.DEPARTURES)) {
				departures.setCompoundDrawables(TransportrUtils.getTintedDrawable(MapActivity.this, departures.getCompoundDrawables()[0]), null, null, null);
				departures.setOnClickListener(new View.OnClickListener() {
					                              @Override
					                              public void onClick(View v) {
						                              TransportrUtils.findDepartures(MapActivity.this, loc);
					                              }
				                              }
				);
			} else {
				departures.setVisibility(View.GONE);
			}

			// Nearby Stations
			TextView nearbyStations = ((TextView) mView.findViewById(R.id.nearbyStations));
			if(loc.hasLocation() && network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.NEARBY_LOCATIONS)) {
				nearbyStations.setCompoundDrawables(TransportrUtils.getTintedDrawable(MapActivity.this, nearbyStations.getCompoundDrawables()[0]), null, null, null);
				nearbyStations.setOnClickListener(new View.OnClickListener() {
					                                  @Override
					                                  public void onClick(View v) {
						                                  TransportrUtils.findNearbyStations(MapActivity.this, loc);
					                                  }
				                                  }
				);
			} else {
				nearbyStations.setVisibility(View.GONE);
			}
		}

		@Override
		public void onClose() {
			// do nothing
		}
	}

	private Bitmap getBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

}