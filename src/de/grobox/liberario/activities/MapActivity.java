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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.grobox.liberario.FavoriteLocation;
import de.grobox.liberario.settings.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.data.LocationDb;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

@Deprecated
public class MapActivity extends TransportrActivity implements MapEventsReceiver {
	private MapView map;
	private Menu menu;
	private FloatingActionButton fab;
	private List<GeoPoint> points = new ArrayList<>();
	private GpsMyLocationProvider locationProvider;
	private GpsController gpsController;
	private FabController fabController;
	private TransportNetwork network;

	public final static String SHOW_AREA = "de.grobox.liberario.MapActivity.SHOW_AREA";
	public final static String SHOW_LOCATIONS = "de.grobox.liberario.MapActivity.SHOW_LOCATIONS";
	public final static String SHOW_TRIP = "de.grobox.liberario.MapActivity.SHOW_TRIP";

	public final static String LOCATION = "de.schildbach.pte.dto.Location";
	public final static String LOCATIONS = "List<de.schildbach.pte.dto.Location>";
	public final static String TRIP = "de.schildbach.pte.dto.Trip";

	private enum MarkerType {BEGIN, CHANGE, STOP, END, WALK}
	private enum LocationProvider {NONE, GPS, NETWORK, BOTH}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		network = Preferences.getTransportNetwork(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(network.getName(this));
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		map = findViewById(R.id.map);
		if(map != null) {
			map.setMultiTouchControls(true);
			map.setTilesScaledToDpi(true);
		}

		fab = findViewById(R.id.gpsFab);
		fabController = new FabController();

		gpsController = new GpsController();
		gpsController.onRestoreInstanceState(savedInstanceState);

		if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			// Should we show an explanation?
			if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
				Toast.makeText(this, R.string.permission_denied_map, Toast.LENGTH_LONG).show();
				supportFinishAfterTransition();
			} else {
				Toast.makeText(this, R.string.permission_explanation_map, Toast.LENGTH_LONG).show();
				ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, MainActivity.PR_WRITE_EXTERNAL_STORAGE);
			}
			return;
		}

		setupMap();
	}

	private void setupMap() {
		MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(this);
		map.getOverlays().add(0, mapEventsOverlay);

		Intent intent = getIntent();
		if(intent != null) {
			String action = intent.getAction();
			if(action != null) {
				//noinspection IfCanBeSwitch
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
		map.getOverlays().add(gpsController.getOverlay());
		setViewSpan();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(gpsController != null) gpsController.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();
		if(gpsController != null) gpsController.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if(gpsController != null) gpsController.onPause();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch(requestCode) {
			case MainActivity.PR_WRITE_EXTERNAL_STORAGE: {
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					setupMap();
					// for some reason, this needs to be called again here
					setViewSpan();
				} else {
					Toast.makeText(this, R.string.permission_denied_map, Toast.LENGTH_LONG).show();
					supportFinishAfterTransition();
				}
				break;
			}
			case MainActivity.PR_ACCESS_FINE_LOCATION_MAPS: {
				// If request is cancelled, the result arrays are empty.
				if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					gpsController.toggle();
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

		if(gpsController != null) {
			if(gpsController.isActive()) {
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
		switch(item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			case R.id.action_use_gps:
				gpsController.toggle();

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
		marker.setTitle(getString(R.string.location) + ": " + loc_str);
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

	private void showArea() {
		getAreaAndZoom();
	}

	private void getAreaAndZoom() {
		runOnThread(new Runnable() {
			@Override
			public void run() {
				// first try to get area from favourites
				List<FavoriteLocation> favs = LocationDb.getFavLocationList(MapActivity.this);
				ArrayList<GeoPoint> geoPoints = new ArrayList<>(favs.size());
				for(FavoriteLocation fav : favs) {
//					Location loc = fav.getLocation();
//					if(loc.hasLocation()) {
//						geoPoints.add(new GeoPoint(loc.getLatAsDouble(), loc.getLonAsDouble()));
//					}
				}

				// if two few favourites, get area from network provider
				if(geoPoints.size() < 2) {
					Point[] area = null;
					try {
						area = network.getNetworkProvider().getArea();
					} catch(IOException e) {
						e.printStackTrace();
					}
					if(area != null) {
						geoPoints = new ArrayList<>(area.length);
						for(Point point : area) {
							geoPoints.add(new GeoPoint(point.getLatAsDouble(), point.getLonAsDouble()));
						}
					}
				}

				// display area if there's any
				if(geoPoints.size() > 1) {
					BoundingBox box = BoundingBox.fromGeoPoints(geoPoints);
					zoomToArea(box);
				}
			}
		});
	}

	private void zoomToArea(final BoundingBox box) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				IMapController mapController = map.getController();
				mapController.setCenter(box.getCenter());
				mapController.setZoom(16);
				map.zoomToBoundingBox(box, true);
			}
		});
	}

	private void showLocations(List<Location> locations, Location myLocation) {
		// mark locations on map
		for(Location loc : locations) {
			if(loc.hasLocation()) {
				markLocation(loc, ContextCompat.getDrawable(this, R.drawable.ic_marker_station));
			}
		}

		// include my location in view span calculation if available
		if(myLocation != null) {
			points.add(new GeoPoint(myLocation.getLatAsDouble(), myLocation.getLonAsDouble()));
		}

		gpsController.setLocation(myLocation);
	}

	private void showTrip(Trip trip) {
		int width = getResources().getDisplayMetrics().densityDpi / 32;
		// draw leg path first, so it is always at the bottom
		for(Leg leg : trip.legs) {
			// add path if it is missing
			if(leg.path == null) calculatePath(leg);
			if(leg.path == null) continue;

				// draw leg path first, so it is always at the bottom
			Polyline polyline = new Polyline();
			List<GeoPoint> geoPoints = new ArrayList<>(leg.path.size());
			for(Point point : leg.path) {
				geoPoints.add(new GeoPoint(point.getLatAsDouble(), point.getLonAsDouble()));
			}
			polyline.setPoints(geoPoints);
			polyline.setWidth(width);
			if(leg instanceof Public) {
				Line line = ((Public) leg).line;
				polyline.setColor(getBackgroundColor(MarkerType.CHANGE, line));
				if(line != null) polyline.setTitle(line.id);
			} else {
				polyline.setColor(getBackgroundColor(MarkerType.WALK, null));
				polyline.setTitle(getString(R.string.walk));
			}
			map.getOverlays().add(polyline);
		}

		// Now draw intermediate stops on top of path
		for(Leg leg : trip.legs) {
			if(leg instanceof Public) {
				Public public_leg = (Public) leg;

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
		for(Leg leg : trip.legs) {
			// Draw public transportation stations
			if(leg instanceof Public) {
				Public public_leg = (Public) leg;

				// Draw first station or change station
				if(i == 1 || (i == 2 && trip.legs.get(0) instanceof Trip.Individual)) {
					markLocation(leg.departure, getMarkerDrawable(MarkerType.BEGIN, public_leg.line));
				} else {
					markLocation(leg.departure, getMarkerDrawable(MarkerType.CHANGE, public_leg.line));
				}

				// Draw final station only at the end or if end is walking
				if(i == trip.legs.size() || (i == trip.legs.size() - 1 && trip.legs.get(i) instanceof Trip.Individual)) {
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
		map.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}

	private void calculatePath(Leg leg) {
		if(leg.path == null) leg.path = new ArrayList<>();

		if(leg.departure != null && leg.departure.hasLocation()) {
			leg.path.add(new Point(leg.departure.lat, leg.departure.lon));
		}

		if(leg instanceof Public && ((Public) leg).intermediateStops != null) {
			//noinspection ConstantConditions
			for(Stop stop : ((Public) leg).intermediateStops) {
				if(stop.location != null && stop.location.hasLocation()) {
					leg.path.add(new Point(stop.location.lat, stop.location.lon));
				}
			}
		}

		if(leg.arrival != null && leg.arrival.hasLocation()) {
			leg.path.add(new Point(leg.arrival.lat, leg.arrival.lon));
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

		double maxLat = -180;
		double minLat = 180;
		double maxLon = -180;
		double minLon = 180;

		for(GeoPoint point : points) {
			maxLat = Math.max(point.getLatitude(), maxLat);
			minLat = Math.min(point.getLatitude(), minLat);
			maxLon = Math.max(point.getLongitude(), maxLon);
			minLon = Math.min(point.getLongitude(), minLon);
		}

		if(gpsController.getLocation() != null) {
			maxLat = Math.max(gpsController.getLocation().getLatitude(), maxLat);
			minLat = Math.min(gpsController.getLocation().getLatitude(), minLat);
			maxLon = Math.max(gpsController.getLocation().getLongitude(), maxLon);
			minLon = Math.min(gpsController.getLocation().getLongitude(), minLon);
		}

		double center_lat = (maxLat + minLat) / 2;
		double center_lon = (maxLon + minLon) / 2;
		final GeoPoint center = new GeoPoint(center_lat, center_lon);

		IMapController mapController = map.getController();
		mapController.setCenter(center);
		mapController.setZoom(18);
		mapController.zoomToSpan(maxLat - minLat, maxLon - minLon);
	}

	private void markLocation(Location loc, Drawable drawable) {
		GeoPoint pos = new GeoPoint(loc.getLatAsDouble(), loc.getLonAsDouble());

		Log.i(getClass().getSimpleName(), "Mark location: " + loc.toString());

		points.add(pos);
		Marker marker = new Marker(map);
		marker.setIcon(drawable);
		marker.setPosition(pos);
		marker.setTitle(TransportrUtils.getLocationName(loc));
		marker.setInfoWindow(new LocationInfoWindow(map));
		marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
		marker.setRelatedObject(loc);
		map.getOverlays().add(marker);
	}

	private class LocationInfoWindow extends InfoWindow {

		LocationInfoWindow(MapView mapView) {
			super(R.layout.location_info_window, mapView);

			// close it when clicking on the bubble
			mView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent e) {
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

			ViewGroup productsView = mView.findViewById(R.id.productsView);
			productsView.removeAllViews();

			// Add product icons if available
			if(loc.products != null && loc.products.size() > 0) {
				for(Product product : loc.products) {
					ImageView image = new ImageView(productsView.getContext());
					image.setImageDrawable(TransportrUtils.getTintedDrawable(productsView.getContext(), TransportrUtils.getDrawableForProduct(product)));
					productsView.addView(image);
				}
			}

			TextView fromHere = mView.findViewById(R.id.fromHere);
			TextView toHere = mView.findViewById(R.id.toHere);
			if(network.getNetworkProvider().hasCapabilities(NetworkProvider.Capability.TRIPS)) {
				// From Here
				fromHere.setCompoundDrawables(TransportrUtils.getTintedDrawable(MapActivity.this, fromHere.getCompoundDrawables()[0]), null, null, null);
				fromHere.setOnClickListener(new View.OnClickListener() {
					                            @Override
					                            public void onClick(View v) {
//						                            TransportrUtils.presetDirections(MapActivity.this, loc, null, null);
					                            }
				                            }
				);

				// To Here
				toHere.setCompoundDrawables(TransportrUtils.getTintedDrawable(MapActivity.this, toHere.getCompoundDrawables()[0]), null, null, null);
				toHere.setOnClickListener(new View.OnClickListener() {
					                          @Override
					                          public void onClick(View v) {
//						                          TransportrUtils.presetDirections(MapActivity.this, null, null, loc);
					                          }
				                          }
				);
			} else {
				fromHere.setVisibility(View.GONE);
				toHere.setVisibility(View.GONE);
			}

			// Departures
			TextView departures = mView.findViewById(R.id.departures);
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
			TextView nearbyStations = mView.findViewById(R.id.nearbyStations);
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
		}
	}

	private Bitmap getBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return bitmap;
	}

	private class GpsController implements GpsStatus.Listener {
		private final LocationManager locationManager;
		private final MyLocationNewOverlay myLocationOverlay;
		private boolean gpsWasOn = false, hasFix = false;
		private long lastLocationTime = 0;
		private final static int GPS_FIX_LOST_TIME = 2500;
		private final static String GPS_WAS_ON = "de.grobox.liberario.MapActivity.GPS_WAS_ON";

		private GpsController() {
			locationProvider = new GpsMyLocationProvider(MapActivity.this) {
				@Override
				public void onLocationChanged(final android.location.Location location) {
					super.onLocationChanged(location);
					if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
						lastLocationTime = SystemClock.elapsedRealtime();
					}
					fabController.onLocationChanged();
				}
				@Override
				public void onProviderDisabled(final String provider) {
					LocationProvider providerState = getActiveLocationProviders();
					if(providerState == LocationProvider.NONE && isActive()) {
						turnOff();
					}
				}
			};

			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

			// create my location overlay that shows the current position and updates automatically
			myLocationOverlay = new MyLocationNewOverlay(locationProvider, map) {
				@Override
				public void setDirectionArrow(final Bitmap personBitmap, final Bitmap directionArrowBitmap) {
					super.setDirectionArrow(personBitmap, directionArrowBitmap);
					mCirclePaint.setColor(ContextCompat.getColor(MapActivity.this, R.color.holo_red_light));
				}
				@Override
				public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
					if(event.getAction() == MotionEvent.ACTION_MOVE) {
						fabController.onMapMove();
					}
					return super.onTouchEvent(event, mapView);
				}
			};
			myLocationOverlay.setDrawAccuracyEnabled(true);

			// set new icons
			Bitmap person = getBitmap(ContextCompat.getDrawable(MapActivity.this, R.drawable.map_position));
			Bitmap directionArrow = getBitmap(ContextCompat.getDrawable(MapActivity.this, R.drawable.map_position_bearing));
			myLocationOverlay.setDirectionArrow(person, directionArrow);

			// properly position person icon
			float scale = getResources().getDisplayMetrics().density;
			myLocationOverlay.setPersonHotspot(12.0f * scale + 0.5f, 12.0f * scale + 0.5f);
		}

		@Override
		public void onGpsStatusChanged(int event) {
			switch (event) {
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					boolean hasFixNow = false;
					if(getLocation() != null) {
						hasFixNow = (SystemClock.elapsedRealtime() - lastLocationTime) < GPS_FIX_LOST_TIME;
					}
					if (!hasFixNow && hasFix) {
						onFixLost();
					}
					if (hasFixNow && !hasFix) {
						onFixReacquired();
					}
					hasFix = hasFixNow;
					break;
				case GpsStatus.GPS_EVENT_FIRST_FIX:
					hasFix = true;
					break;
			}
		}

		private void onResume() {
			if(gpsWasOn) {
				gpsWasOn = false;
				toggle();
			}
		}

		private void onPause() {
			if(myLocationOverlay != null && isActive()) {
				gpsWasOn = true;
				toggle();
			}
		}

		private void onSaveInstanceState(Bundle state) {
			// remember whether GPS was on or not, so it can be re-activated
			state.putBoolean(GPS_WAS_ON, gpsWasOn);
		}

		private void onRestoreInstanceState(Bundle state) {
			if(state != null) {
				gpsWasOn = state.getBoolean(GPS_WAS_ON);
			}
		}

		private void onFixLost() {
			myLocationOverlay.setPersonIcon(getBitmap(ContextCompat.getDrawable(MapActivity.this, R.drawable.map_position_no_fix)));
			map.postInvalidate();
			fabController.onFixLost();
		}

		private void onFixReacquired() {
			myLocationOverlay.setPersonIcon(getBitmap(ContextCompat.getDrawable(MapActivity.this, R.drawable.map_position)));
			map.postInvalidate();
		}

		private MyLocationNewOverlay getOverlay() {
			return myLocationOverlay;
		}

		private boolean isActive() {
			return myLocationOverlay.isMyLocationEnabled();
		}

		private boolean hasFix() {
			return hasFix;
		}

		private GeoPoint getLocation() {
			return myLocationOverlay.getMyLocation();
		}

		private void setLocation(Location l) {
			if(l != null) {
				// create temporary location object with last known position
				android.location.Location tmp_loc = new android.location.Location("");
				tmp_loc.setLatitude(l.getLatAsDouble());
				tmp_loc.setLongitude(l.getLonAsDouble());

				// set last known position
				locationProvider.onLocationChanged(tmp_loc);
			}
		}

		private void toggle() {
			if(ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				// Should we show an explanation?
				if(ActivityCompat.shouldShowRequestPermissionRationale(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
					Toast.makeText(MapActivity.this, R.string.permission_explanation_gps, Toast.LENGTH_LONG).show();
				} else {
					ActivityCompat.requestPermissions(MapActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, MainActivity.PR_ACCESS_FINE_LOCATION_MAPS);
				}
				return;
			}

			// check we have location providers available
			LocationProvider provider = getActiveLocationProviders();
			if(provider == LocationProvider.NONE) {
				Toast.makeText(MapActivity.this, R.string.error_no_location_provider, Toast.LENGTH_SHORT).show();
				return;
			} else if(provider == LocationProvider.NETWORK) {
				Toast.makeText(MapActivity.this, R.string.position_no_gps, Toast.LENGTH_SHORT).show();
			}

			if(isActive()) {
				turnOff();
			} else {
				turnOn();
			}
		}

		private void turnOn() {
			//noinspection MissingPermission
//			locationManager.addGpsStatusListener(this);
			myLocationOverlay.enableMyLocation();
			fabController.show();
			if(menu != null) {
				MenuItem gpsItem = menu.findItem(R.id.action_use_gps);
				gpsItem.setIcon(TransportrUtils.getToolbarDrawable(MapActivity.this, R.drawable.ic_gps_off));
			}
		}

		private void turnOff() {
			locationManager.removeGpsStatusListener(this);
			myLocationOverlay.disableMyLocation();
			fabController.hide();
			if(menu != null) {
				MenuItem gpsItem = menu.findItem(R.id.action_use_gps);
				gpsItem.setIcon(TransportrUtils.getToolbarDrawable(MapActivity.this, R.drawable.ic_gps));
			}
		}

		private LocationProvider getActiveLocationProviders() {
			List<String> providers = locationManager.getProviders(true);
			boolean providerGps = providers.contains(LocationManager.GPS_PROVIDER);
			boolean providerNetwork = providers.contains(LocationManager.NETWORK_PROVIDER);
			if(providerGps && providerNetwork) return LocationProvider.BOTH;
			else if(providerGps) return LocationProvider.GPS;
			else if(providerNetwork) return LocationProvider.NETWORK;
			return LocationProvider.NONE;
		}
	}

	private class FabController implements View.OnClickListener {
		private final ColorStateList fabBg = ColorStateList.valueOf(ContextCompat.getColor(MapActivity.this, R.color.fabBackground));
		private final ColorStateList fabBgMoved = ColorStateList.valueOf(ContextCompat.getColor(MapActivity.this, R.color.fabBackgroundMoved));
		private final int fabFg = ContextCompat.getColor(MapActivity.this, R.color.fabForegroundInitial);
		private final int fabFgMoved = ContextCompat.getColor(MapActivity.this, R.color.fabForegroundMoved);
		private final int fabFgFollow = ContextCompat.getColor(MapActivity.this, R.color.fabForegroundFollow);

		private FabController() {
			fab.hide();
			fab.setOnClickListener(this);
			init();
		}

		private void init() {
			fab.setBackgroundTintList(fabBg);
			fab.getDrawable().setColorFilter(fabFg, PorterDuff.Mode.SRC_IN);
		}

		@Override
		public void onClick(View v) {
			if(gpsController.getLocation() != null) {
				map.getController().animateTo(gpsController.getLocation());
				gpsController.getOverlay().enableFollowLocation();
				if(gpsController.hasFix) {
					fab.setBackgroundTintList(fabBg);
					fab.getDrawable().setColorFilter(fabFgFollow, PorterDuff.Mode.SRC_ATOP);
				}
			} else {
				Toast.makeText(MapActivity.this, R.string.position_not_yet_known, Toast.LENGTH_SHORT).show();
			}
		}

		private void show() {
			fab.show();
		}

		private void hide() {
			fab.hide();
		}

		private void onMapMove() {
			if(gpsController.hasFix()) {
				fab.setBackgroundTintList(fabBgMoved);
				fab.getDrawable().setColorFilter(fabFgMoved, PorterDuff.Mode.SRC_ATOP);
			} else {
				init();
			}
		}

		private void onFixLost() {
			init();
		}

		private void onLocationChanged() {
			if(gpsController.hasFix() && gpsController.getOverlay().isFollowLocationEnabled()) {
				fab.setBackgroundTintList(fabBg);
				fab.getDrawable().setColorFilter(fabFgFollow, PorterDuff.Mode.SRC_ATOP);
			} else {
				onMapMove();
			}
		}
	}

}