package de.grobox.transportr.trips.detail;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Trip;

import static de.grobox.transportr.utils.DateUtils.getDate;
import static de.grobox.transportr.utils.DateUtils.getTime;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;

class TripUtils {

	static void share(Context context, @Nullable Trip trip) {
		if (trip == null) throw new IllegalStateException();
		Intent sendIntent = new Intent()
				.setAction(Intent.ACTION_SEND)
				.putExtra(Intent.EXTRA_SUBJECT, tripToSubject(context, trip))
				.putExtra(Intent.EXTRA_TEXT, tripToString(context, trip))
				.setType("text/plain")
				.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.share_trip_via)));
	}

	static void intoCalendar(Context context, @Nullable Trip trip) {
		if (trip == null) throw new IllegalStateException();
		Intent intent = new Intent(Intent.ACTION_EDIT)
				.setType("vnd.android.cursor.item/event")
				.putExtra("beginTime", trip.getFirstDepartureTime().getTime())
				.putExtra("endTime", trip.getLastArrivalTime().getTime())
				.putExtra("title", trip.from.name + " → " + trip.to.name)
				.putExtra("description", tripToString(context, trip));
		if (trip.from.place != null) intent.putExtra("eventLocation", trip.from.place);
		context.startActivity(intent);
	}

	private static String tripToSubject(Context context, Trip trip) {
		String str = "[" + context.getResources().getString(R.string.app_name) + "] ";

		str += getTime(context, trip.getFirstDepartureTime()) + " ";
		str += getLocationName(trip.from);
		str += " → ";
		str += getLocationName(trip.to) + " ";
		str += getTime(context, trip.getLastArrivalTime());
		str += " (" + getDate(context, trip.getFirstDepartureTime()) + ")";

		return str;
	}

	private static String tripToString(Context context, Trip trip) {
		StringBuilder sb = new StringBuilder();
		for (Trip.Leg leg : trip.legs) {
			sb.append(legToString(context, leg)).append("\n\n");
		}
		sb.append("\n\n")
				.append(context.getString(R.string.times_include_delays))
				.append("\n\n")
				.append(context.getString(R.string.created_by, context.getString(R.string.app_name)))
				.append("\n").append(context.getString(R.string.website));
		return sb.toString();
	}

	static String legToString(Context context, Trip.Leg leg) {
		String str = "";
		String apos = "";

		str += getTime(context, leg.getDepartureTime()) + " ";
		str += getLocationName(leg.departure);

		if (leg instanceof Trip.Public) {
			Trip.Public pub = (Trip.Public) leg;
			if (pub.line != null && pub.line.label != null) {
				str += " (" + pub.line.label;
				if (pub.destination != null) str += " → " + getLocationName(pub.destination);
				str += ")";
			}
			// show departure position if existing
			if (pub.getDeparturePosition() != null) {
				str += " - " + context.getString(R.string.position) + ": " + pub.getDeparturePosition().name;
			}
			// remember arrival position if existing
			if (pub.getArrivalPosition() != null) {
				apos += " - " + context.getString(R.string.position) + ": " + pub.getArrivalPosition().name;
			}
		} else if (leg instanceof Trip.Individual) {
			Trip.Individual ind = (Trip.Individual) leg;
			str += " (" + context.getString(R.string.walk) + " ";
			if (ind.distance > 0) str += ind.distance + context.getResources().getString(R.string.meter) + " ";
			if (ind.min > 0) str += ind.min + context.getResources().getString(R.string.min);
			str += ")";
		}

		str += "\n";
		str += getTime(context, leg.getArrivalTime()) + " ";
		str += getLocationName(leg.arrival);
		str += apos;

		return str;
	}

}
