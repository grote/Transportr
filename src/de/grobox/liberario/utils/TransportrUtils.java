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

package de.grobox.liberario.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.WrapLocation;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.MapActivity;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.fragments.DeparturesFragment;
import de.grobox.liberario.fragments.DirectionsFragment;
import de.grobox.liberario.fragments.NearbyStationsFragment;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Style.Shape;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;

public class TransportrUtils {

	@SuppressWarnings("deprecation")
	static public void addLineBox(Context context, ViewGroup lineLayout, Line line, int index, boolean check_duplicates) {
		if(check_duplicates) {
			// loop through all line boxes in the linearLayout
			for(int i = 0; i < lineLayout.getChildCount(); ++i) {
				// check if current line box is the same as the one we are about to add
				if(line.label != null && line.label.equals(((TextView) lineLayout.getChildAt(i)).getText())) {
					// lines are equal, so bail out from here and don't add new line box
					return;
				}
			}
		}

		TextView transportsView =  (TextView) LayoutInflater.from(context).inflate(R.layout.line_box, lineLayout, false);
		transportsView.setText(line.label);

		if(line.style != null) {
			GradientDrawable line_box = (GradientDrawable) ContextCompat.getDrawable(context, R.drawable.line_box);

			if(line_box != null) {
				// change shape and mutate before to not share state with other instances
				line_box.mutate();

				line_box.setColor(line.style.backgroundColor);
				if(line.style.shape == Shape.CIRCLE) line_box.setShape(GradientDrawable.OVAL);

				transportsView.setBackgroundDrawable(line_box);
			}
			transportsView.setTextColor(line.style.foregroundColor);
		}

		// set margin, because setting in in xml does not work
		FlowLayout.LayoutParams llp = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		llp.setMargins(0, 5, 15, 5);
		transportsView.setLayoutParams(llp);

		lineLayout.addView(transportsView, index);
	}

	static public void addLineBox(Context context, ViewGroup lineLayout, Line line) {
		addLineBox(context, lineLayout, line, lineLayout.getChildCount());
	}

	static public void addLineBox(Context context, ViewGroup lineLayout, Line line, boolean check_duplicates) {
		addLineBox(context, lineLayout, line, lineLayout.getChildCount(), check_duplicates);
	}

	static public void addLineBox(Context context, ViewGroup lineLayout, Line line, int index) {
		addLineBox(context, lineLayout, line, index, false);
	}

	static public void addWalkingBox(Context context, ViewGroup lineLayout, int index) {
		ImageView v = (ImageView) LayoutInflater.from(context).inflate(R.layout.walking_box, lineLayout, false);

		// set margin, because setting in in xml does not work
		FlowLayout.LayoutParams llp = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		llp.setMargins(0, 5, 15, 5);
		v.setLayoutParams(llp);

		lineLayout.addView(v, index);
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
		}
		timeView.setText(DateUtils.getTime(context, time));
	}

	static public void setDepartureTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		if(stop.getDepartureTime() == null) return;

		Date time = new Date(stop.getDepartureTime().getTime());

		if(stop.isDepartureTimePredicted() && stop.getDepartureDelay() != null) {
			long delay = stop.getDepartureDelay();
			time.setTime(time.getTime() - delay);
			delayView.setText(getDelayText(delay));
		}
		timeView.setText(DateUtils.getTime(context, time));
	}

	static public String getDelayText(long delay) {
		if(delay > 0) {
			return "+" + Long.toString(delay / 1000 / 60);
		}
		else if(delay < 0) {
			return Long.toString(delay / 1000 / 60);
		} else {
			return null;
		}
	}

	static public String tripToSubject(Context context, Trip trip, boolean tag) {
		String str = "";

		if(tag) str += "[" + context.getResources().getString(R.string.app_name) + "] ";

		str += DateUtils.getTime(context, trip.getFirstDepartureTime()) + " ";
		str += getLocName(trip.from);
		str += " → ";
		str += getLocName(trip.to) + " ";
		str += DateUtils.getTime(context, trip.getLastArrivalTime());
		str += " (" + DateUtils.getDate(context, trip.getFirstDepartureTime()) + ")";

		return str;
	}

	static public String tripToString(Context context, Trip trip) {
		String str = context.getString(R.string.times_include_delays) + "\n\n";

		for(Leg leg : trip.legs) {
			str += legToString(context, leg) + "\n\n";
		}

		return str;
	}

	static public String legToString(Context context, Leg leg) {
		String str = "";
		String apos = "";

		str += DateUtils.getTime(context, leg.getDepartureTime()) + " ";
		str += getLocName(leg.departure);

		if(leg instanceof Trip.Public) {
			Trip.Public pub = (Trip.Public) leg;
			if(pub.line != null && pub.line.label != null) {
				str += " (" + pub.line.label;
				if(pub.destination  != null) str += " → " + getLocName(pub.destination);
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
		str += DateUtils.getTime(context, leg.getArrivalTime()) + " ";
		str += getLocName(leg.arrival);
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

	static public int getDrawableForProduct(Product p) {
		int image_res = R.drawable.ic_product_bus;

		switch(p) {
			case HIGH_SPEED_TRAIN:
				image_res = R.drawable.ic_product_high_speed_train;
				break;
			case REGIONAL_TRAIN:
				image_res = R.drawable.ic_product_regional_train;
				break;
			case SUBURBAN_TRAIN:
				image_res = R.drawable.ic_product_suburban_train;
				break;
			case SUBWAY:
				image_res = R.drawable.ic_product_subway;
				break;
			case TRAM:
				image_res = R.drawable.ic_product_tram;
				break;
			case BUS:
				image_res = R.drawable.ic_product_bus;
				break;
			case FERRY:
				image_res = R.drawable.ic_product_ferry;
				break;
			case CABLECAR:
				image_res = R.drawable.ic_product_cablecar;
				break;
			case ON_DEMAND:
				image_res = R.drawable.ic_product_on_demand;
				break;
		}

		return image_res;
	}

	static public void share(Context context, Trip trip) {
		//noinspection deprecation
		Intent sendIntent = new Intent()
				                    .setAction(Intent.ACTION_SEND)
				                    .putExtra(Intent.EXTRA_SUBJECT, tripToSubject(context, trip, true))
				                    .putExtra(Intent.EXTRA_TEXT, tripToString(context, trip))
				                    .setType("text/plain")
				                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.share_trip_via)));
	}

	static public void intoCalendar(Context context, Trip trip) {
		Intent intent = new Intent(Intent.ACTION_EDIT)
				                .setType("vnd.android.cursor.item/event")
				                .putExtra("beginTime", trip.getFirstDepartureTime().getTime())
				                .putExtra("endTime", trip.getLastArrivalTime().getTime())
				                .putExtra("title", trip.from.name + " → " + trip.to.name)
				                .putExtra("description", TransportrUtils.tripToString(context, trip));
		if(trip.from.place != null) intent.putExtra("eventLocation", trip.from.place);
		context.startActivity(intent);
	}

	static public void presetDirections(Context context, Location from, Location via, Location to) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(DirectionsFragment.TAG);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("from", from);
		intent.putExtra("via", via);
		intent.putExtra("to", to);
		intent.putExtra("search", false);

		context.startActivity(intent);
	}

	static public void findDirections(Context context, Location from, Location via, Location to, Date date) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(DirectionsFragment.TAG);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("from", from);
		intent.putExtra("via", via);
		intent.putExtra("to", to);
		intent.putExtra("search", true);
		if (date != null) {
			intent.putExtra("date", date);
		}

		context.startActivity(intent);
	}

	static public void findDirections(Context context, Location from, Location via, Location to) {
		findDirections(context, from, via, to, null);
	}

	static public void findDepartures(Context context, Location loc) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(DeparturesFragment.TAG);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("location", loc);

		context.startActivity(intent);
	}

	static public void findNearbyStations(Context context, Location loc) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.setAction(NearbyStationsFragment.TAG);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("location", loc);

		context.startActivity(intent);
	}

	static public void showMap(Context context) {
		Intent intent = new Intent(context, MapActivity.class);
		intent.setAction(MapActivity.SHOW_AREA);

		// TODO take fav location area, if area is empty
		context.startActivity(intent);
	}

	static public void showLocationsOnMap(Context context, ArrayList<Location> loc_list, Location my_loc) {
		// show station on internal map
		Intent intent = new Intent(context, MapActivity.class);
		intent.setAction(MapActivity.SHOW_LOCATIONS);
		intent.putExtra(MapActivity.LOCATIONS, loc_list);
		if(my_loc != null) {
			intent.putExtra(MapActivity.LOCATION, my_loc);
		}
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

	static public void showLocationOnMap(Context context, Location loc) {
		showLocationOnMap(context, loc, null);
	}

	static public void showTripOnMap(Context context, Trip trip) {
		Intent intent = new Intent(context, MapActivity.class);
		intent.setAction(MapActivity.SHOW_TRIP);
		intent.putExtra(MapActivity.TRIP, trip);
		context.startActivity(intent);
	}

	static public void startGeoIntent(Context context, Location loc) {
		String lat = Double.toString(loc.lat / 1E6);
		String lon = Double.toString(loc.lon / 1E6);

		String uri1 = "geo:0,0?q=" + lat + "," + lon;
		String uri2;

		try {
			uri2 = "(" + URLEncoder.encode(TransportrUtils.getLocName(loc), "utf-8") + ")";
		} catch (UnsupportedEncodingException e) {
			uri2 = "(" + TransportrUtils.getLocName(loc) + ")";
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

	static public String getLocName(Location l) {
		if(l == null) {
			return "";
		} else if(l.type.equals(LocationType.COORD)) {
			return getCoordinationName(l.getLatAsDouble(), l.getLonAsDouble());
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
			return getLocName(l);
		}
	}

	static public String getCoordinationName(double lat, double lon) {
		String latStr;
		try {
			latStr = String.valueOf(lat).substring(0, 7);
		} catch(StringIndexOutOfBoundsException e) {
			latStr = String.valueOf(lat);
		}

		String lonStr;
		try {
			lonStr = String.valueOf(lon).substring(0, 7);
		} catch(StringIndexOutOfBoundsException e) {
			lonStr = String.valueOf(lon);
		}

		return latStr + "/" + lonStr;
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

	static public Drawable getToolbarDrawable(Context context, Drawable drawable) {
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

	static public int getButtonIconColor(Context context, boolean on) {
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

	static public Drawable getDrawableForLocation(Context context, Location l, boolean is_fav) {
		if(l == null) return null;

		if( (l.id != null && l.id.equals(LocationAdapter.HOME)) || l.equals(RecentsDB.getHome(context))) {
			return getTintedDrawable(context, R.drawable.ic_action_home);
		}
		else if(l.id != null && l.id.equals(LocationAdapter.GPS)) {
			return getTintedDrawable(context, R.drawable.ic_gps);
		}
		else if(l.id != null && l.id.equals(LocationAdapter.MAP)) {
			return getTintedDrawable(context, R.drawable.ic_action_location_map);
		}
		else if(is_fav) {
			return getTintedDrawable(context, R.drawable.ic_action_star);
		}
		else {
			if(l.type.equals(LocationType.ADDRESS)) {
				return getTintedDrawable(context, R.drawable.ic_location_address);
			} else if(l.type.equals(LocationType.POI)) {
				return getTintedDrawable(context, R.drawable.ic_action_about);
			} else if(l.type.equals(LocationType.STATION)) {
				return getTintedDrawable(context, R.drawable.ic_tab_stations);
			} else if(l.type.equals(LocationType.COORD)) {
				return getTintedDrawable(context, R.drawable.ic_gps);
			} else {
				return null;
			}
		}
	}

	static public Drawable getDrawableForLocation(Context context, Location l) {
		if(l == null) return getTintedDrawable(context, R.drawable.ic_location);

		List<WrapLocation> fav_list = RecentsDB.getFavLocationList(context, FavLocation.LOC_TYPE.FROM, false);

		return getDrawableForLocation(context, l, fav_list.contains(new WrapLocation(l)));
	}


	static public void setFavState(Context context, MenuItem item, boolean is_fav, boolean is_toolbar) {
		if(is_fav) {
			item.setTitle(R.string.action_unfav_trip);
			item.setIcon(is_toolbar ? TransportrUtils.getToolbarDrawable(context, R.drawable.ic_action_star) : TransportrUtils.getTintedDrawable(context, R.drawable.ic_action_star));
		} else {
			item.setTitle(R.string.action_fav_trip);
			item.setIcon(is_toolbar ? TransportrUtils.getToolbarDrawable(context, R.drawable.ic_action_star_empty) : TransportrUtils.getTintedDrawable(context, R.drawable.ic_action_star_empty));
		}
	}

	static public NetworkProvider.Optimize getOptimize(Context context) {
		String optimizeString = Preferences.getOptimize(context).toUpperCase();

		return NetworkProvider.Optimize.valueOf(optimizeString);
	}

	static public NetworkProvider.WalkSpeed getWalkSpeed(Context context) {
		String walkString = Preferences.getWalkSpeed(context).toUpperCase();

		return NetworkProvider.WalkSpeed.valueOf(walkString);
	}

}
