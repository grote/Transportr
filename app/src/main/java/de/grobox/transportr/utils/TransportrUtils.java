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

package de.grobox.transportr.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;
import de.grobox.transportr.activities.MainActivity;
import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.fragments.DeparturesFragment;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.map.MapActivity;
import de.grobox.transportr.settings.Preferences;
import de.grobox.transportr.trips.search.DirectionsActivity;
import de.grobox.transportr.ui.LineView;
import de.schildbach.pte.NetworkProvider.Optimize;
import de.schildbach.pte.NetworkProvider.WalkSpeed;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;

import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static de.grobox.transportr.locations.WrapLocation.WrapType.GPS;
import static de.grobox.transportr.utils.Constants.DATE;
import static de.grobox.transportr.utils.Constants.FAV_TRIP_UID;
import static de.grobox.transportr.utils.Constants.FROM;
import static de.grobox.transportr.utils.Constants.SEARCH;
import static de.grobox.transportr.utils.Constants.TO;
import static de.grobox.transportr.utils.Constants.VIA;
import static de.grobox.transportr.utils.DateUtils.getDate;
import static de.grobox.transportr.utils.DateUtils.getDelayText;
import static de.grobox.transportr.utils.DateUtils.getDifferenceInMinutes;
import static de.grobox.transportr.utils.DateUtils.getTime;

@ParametersAreNonnullByDefault
public class TransportrUtils {

	static private void addLineBox(Context context, ViewGroup lineLayout, Line line, int index, boolean check_duplicates) {
		if(check_duplicates) {
			// loop through all line boxes in the linearLayout
			for(int i = 0; i < lineLayout.getChildCount(); ++i) {
				// check if current line box is the same as the one we are about to add
				if(line.label != null && line.label.equals(((LineView) lineLayout.getChildAt(i)).getText())) {
					// lines are equal, so bail out from here and don't add new line box
					return;
				}
			}
		}

		LineView lineView = new LineView(context, null);
		lineView.setLine(line);

		// set margin, because setting in in xml does not work
		FlexboxLayout.LayoutParams llp = new FlexboxLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
		llp.setMargins(0, 5, 10, 5);
		lineView.setLayoutParams(llp);

		lineLayout.addView(lineView, index);
	}

	static public void addLineBox(Context context, ViewGroup lineLayout, Line line) {
		addLineBox(context, lineLayout, line, lineLayout.getChildCount());
	}

	static public void addLineBox(Context context, ViewGroup lineLayout, Line line, int index) {
		addLineBox(context, lineLayout, line, index, false);
	}

	static public void addWalkingBox(Context context, ViewGroup lineLayout, int index) {
		LineView lineView = new LineView(context, null);
		lineView.setWalk();

		// set margin, because setting in in xml does not work
		FlexboxLayout.LayoutParams llp = new FlexboxLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
		llp.setMargins(0, 5, 10, 5);
		lineView.setLayoutParams(llp);

		lineLayout.addView(lineView, index);
	}

	static public void addWalkingBox(Context context, ViewGroup lineLayout) {
		addWalkingBox(context, lineLayout, lineLayout.getChildCount());
	}


	static public void setArrivalTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		if(stop.getArrivalTime() == null) return;

		Date time = new Date(stop.getArrivalTime().getTime());

		if(stop.isArrivalTimePredicted() && stop.getArrivalDelay() != null) {
			long delay = stop.getArrivalDelay();
			time.setTime(time.getTime() - delay);
			delayView.setText(getDelayText(delay));
			if (delay <= 0) delayView.setTextColor(ContextCompat.getColor(context, R.color.md_green_500));
			else delayView.setTextColor(ContextCompat.getColor(context, R.color.md_red_500));
			delayView.setVisibility(VISIBLE);
		} else {
			delayView.setVisibility(GONE);
		}
		timeView.setText(getTime(context, time));
	}

	static public void setDepartureTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		if(stop.getDepartureTime() == null) return;

		Date time = new Date(stop.getDepartureTime().getTime());

		if(stop.isDepartureTimePredicted() && stop.getDepartureDelay() != null) {
			long delay = stop.getDepartureDelay();
			time.setTime(time.getTime() - delay);
			delayView.setText(getDelayText(delay));
			if (delay <= 0) delayView.setTextColor(ContextCompat.getColor(context, R.color.md_green_500));
			else delayView.setTextColor(ContextCompat.getColor(context, R.color.md_red_500));
			delayView.setVisibility(VISIBLE);
		} else {
			delayView.setVisibility(GONE);
		}
		timeView.setText(getTime(context, time));
	}

	public static void setRelativeDepartureTime(TextView timeRel, Date date) {
		long difference = getDifferenceInMinutes(date);
		if (difference > 99 || difference < -99) {
			timeRel.setVisibility(GONE);
		} else if (difference == 0) {
			timeRel.setText(timeRel.getContext().getString(R.string.now_small));
			timeRel.setVisibility(VISIBLE);
		} else if (difference > 0) {
			timeRel.setText(timeRel.getContext().getString(R.string.in_x_minutes, difference));
			timeRel.setVisibility(VISIBLE);
		} else {
			timeRel.setText(timeRel.getContext().getString(R.string.x_minutes_ago, difference * -1));
			timeRel.setVisibility(VISIBLE);
		}
	}

	static private String tripToSubject(Context context, Trip trip) {
		String str = "[" + context.getResources().getString(R.string.app_name) + "] ";

		str += getTime(context, trip.getFirstDepartureTime()) + " ";
		str += getLocationName(trip.from);
		str += " → ";
		str += getLocationName(trip.to) + " ";
		str += getTime(context, trip.getLastArrivalTime());
		str += " (" + getDate(context, trip.getFirstDepartureTime()) + ")";

		return str;
	}

	static private String tripToString(Context context, Trip trip) {
		StringBuilder sb = new StringBuilder();
		for(Leg leg : trip.legs) {
			sb.append(legToString(context, leg)).append("\n\n");
		}
		sb.append("\n\n")
				.append(context.getString(R.string.times_include_delays))
				.append("\n\n")
				.append(context.getString(R.string.created_by, context.getString(R.string.app_name)))
				.append("\n").append(context.getString(R.string.website));
		return sb.toString();
	}

	static public String legToString(Context context, Leg leg) {
		String str = "";
		String apos = "";

		str += getTime(context, leg.getDepartureTime()) + " ";
		str += getLocationName(leg.departure);

		if(leg instanceof Trip.Public) {
			Trip.Public pub = (Trip.Public) leg;
			if(pub.line != null && pub.line.label != null) {
				str += " (" + pub.line.label;
				if(pub.destination  != null) str += " → " + getLocationName(pub.destination);
				str += ")";
			}
			// show departure position if existing
			if(pub.getDeparturePosition() != null) {
				str += " - " + context.getString(R.string.position) + ": " + pub.getDeparturePosition().name;
			}
			// remember arrival position if existing
			if(pub.getArrivalPosition() != null) {
				apos += " - " + context.getString(R.string.position) + ": " + pub.getArrivalPosition().name;
			}
		} else if(leg instanceof Trip.Individual) {
			Trip.Individual ind = (Trip.Individual) leg;
			str += " (" + context.getString(R.string.walk) + " ";
			if(ind.distance > 0) str += ind.distance + context.getResources().getString(R.string.meter) + " ";
			if(ind.min > 0) str += ind.min + context.getResources().getString(R.string.min);
			str += ")";
		}

		str += "\n";
		str += getTime(context, leg.getArrivalTime()) + " ";
		str += getLocationName(leg.arrival);
		str += apos;

		return str;
	}

	static public String productToString(Context context, Product p) {
		if(p == Product.HIGH_SPEED_TRAIN)
			return context.getString(R.string.product_high_speed_train);
		else if (p == Product.REGIONAL_TRAIN)
			return context.getString(R.string.product_regional_train);
		else if (p == Product.SUBURBAN_TRAIN)
			return context.getString(R.string.product_suburban_train);
		else if (p == Product.SUBWAY)
			return context.getString(R.string.product_subway);
		else if (p == Product.TRAM)
			return context.getString(R.string.product_tram);
		else if (p == Product.BUS)
			return context.getString(R.string.product_bus);
		else if (p == Product.FERRY)
			return context.getString(R.string.product_ferry);
		else if (p == Product.CABLECAR)
			return context.getString(R.string.product_cablecar);
		else if (p == Product.ON_DEMAND)
			return context.getString(R.string.product_on_demand);
		else
			return "";

	}

	@DrawableRes
	static public int getDrawableForProduct(@Nullable Product p) {
		@DrawableRes
		int image_res = R.drawable.product_bus;
		if (p == null) return image_res;

		switch(p) {
			case HIGH_SPEED_TRAIN:
				image_res = R.drawable.product_high_speed_train;
				break;
			case REGIONAL_TRAIN:
				image_res = R.drawable.product_regional_train;
				break;
			case SUBURBAN_TRAIN:
				image_res = R.drawable.product_suburban_train;
				break;
			case SUBWAY:
				image_res = R.drawable.product_subway;
				break;
			case TRAM:
				image_res = R.drawable.product_tram;
				break;
			case BUS:
				image_res = R.drawable.product_bus;
				break;
			case FERRY:
				image_res = R.drawable.product_ferry;
				break;
			case CABLECAR:
				image_res = R.drawable.product_cablecar;
				break;
			case ON_DEMAND:
				image_res = R.drawable.product_on_demand;
				break;
		}

		return image_res;
	}

	@DrawableRes
	static public int getMarkerForProduct(@Nullable Set<Product> p) {
		@DrawableRes
		int image_res = R.drawable.product_bus_marker;

		if (p != null && p.size() > 0) {
			switch (p.iterator().next()) {
				case HIGH_SPEED_TRAIN:
					image_res = R.drawable.product_high_speed_train_marker;
					break;
				case REGIONAL_TRAIN:
					image_res = R.drawable.product_regional_train_marker;
					break;
				case SUBURBAN_TRAIN:
					image_res = R.drawable.product_suburban_train_marker;
					break;
				case SUBWAY:
					image_res = R.drawable.product_subway_marker;
					break;
				case TRAM:
					image_res = R.drawable.product_tram_marker;
					break;
				case BUS:
					image_res = R.drawable.product_bus_marker;
					break;
				case FERRY:
					image_res = R.drawable.product_ferry_marker;
					break;
				case CABLECAR:
					image_res = R.drawable.product_cablecar_marker;
					break;
				case ON_DEMAND:
					image_res = R.drawable.product_on_demand_marker;
					break;
			}
		}
		return image_res;
	}

	static public void share(Context context, @Nullable Trip trip) {
		if (trip == null) throw new IllegalStateException();
		//noinspection deprecation
		Intent sendIntent = new Intent()
				                    .setAction(Intent.ACTION_SEND)
				                    .putExtra(Intent.EXTRA_SUBJECT, tripToSubject(context, trip))
				                    .putExtra(Intent.EXTRA_TEXT, tripToString(context, trip))
				                    .setType("text/plain")
				                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.share_trip_via)));
	}

	static public void intoCalendar(Context context, @Nullable Trip trip) {
		if (trip == null) throw new IllegalStateException();
		Intent intent = new Intent(Intent.ACTION_EDIT)
				                .setType("vnd.android.cursor.item/event")
				                .putExtra("beginTime", trip.getFirstDepartureTime().getTime())
				                .putExtra("endTime", trip.getLastArrivalTime().getTime())
				                .putExtra("title", trip.from.name + " → " + trip.to.name)
				                .putExtra("description", TransportrUtils.tripToString(context, trip));
		if(trip.from.place != null) intent.putExtra("eventLocation", trip.from.place);
		context.startActivity(intent);
	}

	static public void findDirections(Context context, long uid, WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to, @Nullable Date date, boolean search) {
		Intent intent = new Intent(context, DirectionsActivity.class);
		intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(FAV_TRIP_UID, uid);
		intent.putExtra(FROM, from);
		intent.putExtra(VIA, via);
		intent.putExtra(TO, to);
		intent.putExtra(SEARCH, search);
		if (date != null) {
			intent.putExtra(DATE, date);
		}
		context.startActivity(intent);
	}

	static public void presetDirections(Context context, long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to) {
		findDirections(context, uid, from, via, to, null, false);
	}

	static public void findDirections(Context context, long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to, @Nullable Date date) {
		findDirections(context, uid, from, via, to, date, true);
	}

	static public void findDirections(Context context, long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to) {
		findDirections(context, uid, from, via, to, null);
	}

	static public void findDepartures(Context context, Location loc) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(DeparturesFragment.TAG);
		intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("location", loc);

		context.startActivity(intent);
	}

	static public void findNearbyStations(Context context, Location loc) {
//		Intent intent = new Intent(context, MainActivity.class);
//		intent.setAction(NearbyStationsFragment.TAG);
//		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		intent.putExtra("location", loc);
//
//		context.startActivity(intent);
	}

	static public void showMap(Context context) {
		Intent intent = new Intent(context, MapActivity.class);
//		intent.setAction(MapActivity.SHOW_AREA);

		context.startActivity(intent);
	}

	static public void showLocationsOnMap(Context context, ArrayList<Location> loc_list, Location my_loc) {
		// show station on internal map
		Intent intent = new Intent(context, MapActivity.class);
//		intent.setAction(MapActivity.SHOW_LOCATIONS);
//		intent.putExtra(MapActivity.LOCATIONS, loc_list);
//		if(my_loc != null) {
//			intent.putExtra(MapActivity.LOCATION, my_loc);
//		}
		context.startActivity(intent);
	}

	static public void showLocationsOnMap(Context context, ArrayList<Location> loc_list) {
		showLocationsOnMap(context, loc_list, null);
	}

	static public void showLocationOnMap(Context context, Location loc, Location loc2) {
		ArrayList<Location> loc_list = new ArrayList<>(1);
		loc_list.add(loc);

		showLocationsOnMap(context, loc_list, loc2);
	}

	static public void showTripOnMap(Context context, Trip trip) {
		Intent intent = new Intent(context, MapActivity.class);
//		intent.setAction(MapActivity.SHOW_TRIP);
//		intent.putExtra(MapActivity.TRIP, trip);
		context.startActivity(intent);
	}

	static public void startGeoIntent(Context context, Location loc) {
		String lat = Double.toString(loc.lat / 1E6);
		String lon = Double.toString(loc.lon / 1E6);

		String uri1 = "geo:0,0?q=" + lat + "," + lon;
		String uri2;

		try {
			uri2 = "(" + URLEncoder.encode(TransportrUtils.getLocationName(loc), "utf-8") + ")";
		} catch (UnsupportedEncodingException e) {
			uri2 = "(" + TransportrUtils.getLocationName(loc) + ")";
		}

		Uri geo = Uri.parse(uri1 + uri2);

		Log.d(context.getClass().getCanonicalName(), "Starting geo intent: " + geo.toString());

		// show station on external map
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geo);
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			context.startActivity(intent);
		}
	}

	static public int computeDistance(Location location1, Location location2) {
		if(location1 == null || location2 == null) return -1;
		if(!location1.hasLocation() || !location2.hasLocation()) return -1;

		android.location.Location loc1 = new android.location.Location("");
		loc1.setLatitude(location1.lat / 1E6);
		loc1.setLongitude(location1.lon / 1E6);

		android.location.Location loc2 = new android.location.Location("");
		loc2.setLatitude(location2.lat / 1E6);
		loc2.setLongitude(location2.lon / 1E6);

		return Math.round(loc1.distanceTo(loc2));
	}

	static public void copyToClipboard(Context context, String text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("label", text);
		clipboard.setPrimaryClip(clip);
	}

	static public String getLocationName(Location l) {
		if(l == null) {
			return "";
		} else if(l.type.equals(LocationType.COORD)) {
			return getCoordinationName(l);
		} else if(l.uniqueShortName() != null) {
			return l.uniqueShortName();
		} else {
			return "";
		}
	}

	static public String getFullLocName(Location l) {
		if(l.hasName()) {
			return l.place == null ? l.uniqueShortName() : l.name + ", " + l.place;
		} else {
			return getLocationName(l);
		}
	}

	static public String getCoordinationName(Location location) {
		return getCoordinationName(location.getLatAsDouble(), location.getLonAsDouble());
	}

	static public String getCoordinationName(double lat, double lon) {
		DecimalFormat df = new DecimalFormat("#.###");
		return df.format(lat) + '/' + df.format(lon);
	}

	static public Drawable getTintedDrawable(Context context, boolean dark, Drawable drawable) {
		if(dark) {
			drawable.setColorFilter(ContextCompat.getColor(context, R.color.drawableTintDark), PorterDuff.Mode.SRC_IN);
		}
		else {
			drawable.setColorFilter(ContextCompat.getColor(context, R.color.drawableTintLight), PorterDuff.Mode.SRC_IN);
		}
		return drawable.mutate();
	}

	static public Drawable getTintedDrawable(Context context, Drawable drawable) {
		if(Preferences.darkThemeEnabled(context)) {
			return getTintedDrawable(context, true, drawable);
		}
		else {
			return getTintedDrawable(context, false, drawable);
		}
	}

	static public Drawable getTintedDrawable(Context context, boolean dark, int res) {
		return getTintedDrawable(context, dark, ContextCompat.getDrawable(context, res));
	}

	static public Drawable getTintedDrawable(Context context, int res) {
		return getTintedDrawable(context, ContextCompat.getDrawable(context, res));
	}

	static private Drawable getToolbarDrawable(Context context, Drawable drawable) {
		if(drawable != null) {
			drawable.setColorFilter(ContextCompat.getColor(context, R.color.drawableTintDark), PorterDuff.Mode.SRC_IN);
			drawable.setAlpha(255);
			drawable.mutate();
		}
		return drawable;
	}

	static public Drawable getToolbarDrawable(Context context, int res) {
		return getToolbarDrawable(context, ContextCompat.getDrawable(context, res));
	}

	static public void fixToolbarIcon(Context context, MenuItem item) {
		item.setIcon(getToolbarDrawable(context, item.getIcon()));
	}

	static private int getButtonIconColor(Context context, boolean on) {
		if(Preferences.darkThemeEnabled(context)) {
			if(on) return ContextCompat.getColor(context, R.color.drawableTintDark);
			else return ContextCompat.getColor(context, R.color.drawableTintLight);
		} else {
			if(on) return Color.BLACK;
			else return ContextCompat.getColor(context, R.color.drawableTintLight);
		}
	}

	static public int getButtonIconColor(Context context) {
		return getButtonIconColor(context, true);
	}

	static public Drawable getDrawableForLocation(Context context, WrapLocation w, boolean is_fav) {
		return getDrawableForLocation(context, null, w, is_fav);
	}

	@Deprecated
	static public Drawable getDrawableForLocation(Context context, @Nullable HomeLocation home, WrapLocation w, boolean is_fav) {
		if(w == null || w.getLocation() == null) return null;
//		if (home == null) home = SpecialLocationDb.getHome(context);

		if(w.getWrapType() == GPS) {
			return getTintedDrawable(context, R.drawable.ic_gps);
		}
		else if(is_fav) {
			return getTintedDrawable(context, R.drawable.ic_action_star);
		}
		else {
			if(w.type.equals(LocationType.ADDRESS)) {
				return getTintedDrawable(context, R.drawable.ic_location_address);
			} else if(w.type.equals(LocationType.POI)) {
				return getTintedDrawable(context, R.drawable.ic_action_about);
			} else if(w.type.equals(LocationType.STATION)) {
				return getTintedDrawable(context, R.drawable.ic_location_station);
			} else if(w.type.equals(LocationType.COORD)) {
				return getTintedDrawable(context, R.drawable.ic_gps);
			} else {
				return null;
			}
		}
	}

	static public Drawable getDrawableForLocation(Context context, @Nullable WrapLocation l) {
		if(l == null) return getTintedDrawable(context, R.drawable.ic_location);

		return getTintedDrawable(context, l.getDrawable());
	}


	static public void setFavState(Context context, MenuItem item, boolean is_fav, boolean is_toolbar) {
		if(is_fav) {
			item.setTitle(R.string.action_unfav_trip);
			item.setIcon(is_toolbar ? getToolbarDrawable(context, R.drawable.ic_action_star) : getTintedDrawable(context, R.drawable.ic_action_star));
		} else {
			item.setTitle(R.string.action_fav_trip);
			item.setIcon(is_toolbar ? getToolbarDrawable(context, R.drawable.ic_action_star_empty) : getTintedDrawable(context, R.drawable.ic_action_star_empty));
		}
	}

	static public Optimize getOptimize(Context context) {
		String optimizeString = Preferences.getOptimize(context).toUpperCase();

		return Optimize.valueOf(optimizeString);
	}

	static public WalkSpeed getWalkSpeed(Context context) {
		String walkString = Preferences.getWalkSpeed(context).toUpperCase();

		return WalkSpeed.valueOf(walkString);
	}

	static public int getDragDistance(Context context) {
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int) (30 * metrics.density);
	}

	public static Intent getShortcutIntent(Context context, String type) {
		Intent shortcutIntent = new Intent(context, DirectionsActivity.class);
		shortcutIntent.setAction(ACTION_MAIN);
		shortcutIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.putExtra("special", type);
		return shortcutIntent;
	}

	public static LatLng getLatLng(Location location) {
		return new LatLng(location.getLatAsDouble(), location.getLonAsDouble());
	}

	public static WrapLocation convert(android.location.Location location) {
		return new WrapLocation(Location.coord((int) (location.getLatitude() * 1E6), (int) (location.getLongitude() * 1E6)));
	}

	public static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}

}
