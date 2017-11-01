package de.grobox.transportr.utils;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;

import de.grobox.transportr.departures.DeparturesActivity;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.trips.search.DirectionsActivity;
import de.schildbach.pte.dto.Location;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static de.grobox.transportr.utils.Constants.DATE;
import static de.grobox.transportr.utils.Constants.FAV_TRIP_UID;
import static de.grobox.transportr.utils.Constants.FROM;
import static de.grobox.transportr.utils.Constants.SEARCH;
import static de.grobox.transportr.utils.Constants.TO;
import static de.grobox.transportr.utils.Constants.VIA;
import static de.grobox.transportr.utils.Constants.WRAP_LOCATION;

public class IntentUtils {

	public static void findDirections(Context context, long uid, @Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to,
	                                  @Nullable Date date, boolean search) {
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

	public static void presetDirections(Context context, long uid, @Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to) {
		findDirections(context, uid, from, via, to, null, false);
	}

	public static void findDirections(Context context, long uid, @Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to,
	                                  @Nullable Date date) {
		findDirections(context, uid, from, via, to, date, true);
	}

	public static void findDirections(Context context, long uid, @Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to) {
		findDirections(context, uid, from, via, to, null);
	}

	public static void findDepartures(Context context, WrapLocation location) {
		Intent intent = new Intent(context, DeparturesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra(WRAP_LOCATION, location);
		context.startActivity(intent);
	}

	public static void findNearbyStations(Context context, Location location) {
		// TODO
		Toast.makeText(context, "Not yet implemented :(", Toast.LENGTH_SHORT).show();
	}

	public static void startGeoIntent(Context context, Location loc) {
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

		Log.d(context.getClass().getSimpleName(), "Starting geo intent: " + geo.toString());

		// show station on external map
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(geo);
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			context.startActivity(intent);
		}
	}

}
