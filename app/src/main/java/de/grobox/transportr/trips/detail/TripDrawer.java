package de.grobox.transportr.trips.detail;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

class TripDrawer {

	private enum MarkerType {BEGIN, CHANGE, STOP, END, WALK}

	private final Context context;
	private final IconFactory iconFactory;

	TripDrawer(Context context) {
		this.context = context;
		this.iconFactory = IconFactory.getInstance(context);
	}

	void draw(MapboxMap map, Trip trip) {
		// draw leg path first, so it is always at the bottom
		int i = 1;
		List<LatLng> allPoints = new ArrayList<>();
		for(Leg leg : trip.legs) {
			// add path if it is missing
			if(leg.path == null) calculatePath(leg);
			if(leg.path == null) continue;

			// draw leg path first, so it is always at the bottom
			List<LatLng> points = new ArrayList<>(leg.path.size());
			for(Point point : leg.path) {
				points.add(new LatLng(point.getLatAsDouble(), point.getLonAsDouble()));
			}
			int backgroundColor = getBackgroundColor(leg);
			int foregroundColor = getForegroundColor(leg);
			map.addPolyline(new PolylineOptions()
					.color(backgroundColor)
					.addAll(points)
					.width(5f)
			);

			// Draw public transportation stations
			if(leg instanceof Public) {
				// Draw first station or change station
				Icon icon;
				if(i == 1 || (i == 2 && trip.legs.get(0) instanceof Trip.Individual)) {
					icon = getMarkerIcon(MarkerType.BEGIN, backgroundColor, foregroundColor);
				} else {
					icon = getMarkerIcon(MarkerType.CHANGE, backgroundColor, foregroundColor);
				}
				markLocation(map, leg.departure, icon);

				// Draw final station only at the end or if end is walking
				if(i == trip.legs.size() || (i == trip.legs.size() - 1 && trip.legs.get(i) instanceof Trip.Individual)) {
					markLocation(map, leg.arrival, getMarkerIcon(MarkerType.END, backgroundColor, foregroundColor));
				}
			}
			// Walking
//			else if(leg instanceof Trip.Individual) {
//				if(i != trip.legs.size() || trip.legs.size() == 1) {
//					markLocation(map, leg.departure, getMarkerIcon(MarkerType.WALK, backgroundColor, foregroundColor));
//				}
//				// draw walking icon for arrival only at the end of the trip
//				if(i == trip.legs.size()) {
//					markLocation(map, leg.arrival, getMarkerIcon(MarkerType.WALK, backgroundColor, foregroundColor));
//				}
//			}
			i += 1;
			allPoints.addAll(points);
		}
		centerOnTrip(map, allPoints);
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

	private int getBackgroundColor(Leg leg) {
		if(leg instanceof Public) {
			Line line = ((Public) leg).line;
			if(line != null && line.style != null && line.style.backgroundColor != 0) {
				return line.style.backgroundColor;
			}
			return ContextCompat.getColor(context, R.color.accent);
		}
		return ContextCompat.getColor(context, R.color.walking);
	}

	private int getForegroundColor(Leg leg) {
		if(leg instanceof Public) {
			Line line = ((Public) leg).line;
			if(line != null && line.style != null && line.style.foregroundColor != 0) {
				return line.style.foregroundColor;
			}
			return ContextCompat.getColor(context, android.R.color.white);
		}
		return ContextCompat.getColor(context, android.R.color.black);
	}

	private void markLocation(MapboxMap map, Location location, Icon icon) {
		if (!location.hasLocation()) return;
		LatLng position = new LatLng(location.getLatAsDouble(), location.getLonAsDouble());
		map.addMarker(new MarkerOptions()
				.icon(icon)
				.position(position)
				.title(location.uniqueShortName())
		);
	}

	private Icon getMarkerIcon(MarkerType type, int backgroundColor, int foregroundColor) {
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
		LayerDrawable drawable = (LayerDrawable) ContextCompat.getDrawable(context, res);
		if(drawable != null) {
			drawable.getDrawable(0).mutate().setColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY);
			drawable.getDrawable(1).mutate().setColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
		}
		if (drawable == null) return iconFactory.defaultMarker();

		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);
		return iconFactory.fromBitmap(bitmap);
	}

	private void centerOnTrip(MapboxMap map, List<LatLng> points) {
		LatLngBounds latLngBounds = new LatLngBounds.Builder()
				.includes(points)
				.build();
		int padding = context.getResources().getDimensionPixelSize(R.dimen.mapPadding);
		map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding));
	}

}
