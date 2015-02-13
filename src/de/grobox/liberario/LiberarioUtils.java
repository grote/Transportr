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

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Date;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Style.Shape;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;

public class LiberarioUtils {

	@SuppressWarnings("deprecation")
	static public void addLineBox(Context context, ViewGroup lineLayout, Line line, int index, boolean check_duplicates) {
		if(check_duplicates) {
			// loop through all line boxes in the linearLayout
			for(int i = 0; i < lineLayout.getChildCount(); ++i) {
				// check if current line box is the same as the one we are about to add
				if(line.label.substring(1).equals(((TextView)lineLayout.getChildAt(i)).getText())) {
					// lines are equal, so bail out from here and don't add new line box
					return;
				}
			}
		}

		TextView transportsView =  (TextView) LayoutInflater.from(context).inflate(R.layout.line_box, null);
		transportsView.setText(line.label.substring(1));

		if(line.style != null) {
			GradientDrawable line_box = (GradientDrawable) context.getResources().getDrawable(R.drawable.line_box);
			line_box.setColor(line.style.backgroundColor);

			// change shape and mutate before to not share state with other instances
			line_box.mutate();
			if(line.style.shape == Shape.CIRCLE) line_box.setShape(GradientDrawable.OVAL);

			transportsView.setBackgroundDrawable(line_box);
			transportsView.setTextColor(line.style.foregroundColor);
		}

		// set margin, because setting in in xml didn't work
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(3, 0, 3, 0);
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

	static public void addWalkingBox(Context context, ViewGroup lineLayout) {
		ImageView transportsView = (ImageView) LayoutInflater.from(context).inflate(R.layout.walking_box, null);

		// set margin, because setting in in xml didn't work
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(3, 0, 3, 0);
		transportsView.setLayoutParams(llp);

		lineLayout.addView(transportsView);
	}

	static public View getDivider(Context context) {
		return LayoutInflater.from(context).inflate(R.layout.divider_horizontal, null);
	}


	static public void setArrivalTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		Date time = new Date(stop.getArrivalTime().getTime());

		if(stop.isArrivalTimePredicted() && stop.getArrivalDelay() != null) {
			long delay = stop.getArrivalDelay();
			time.setTime(time.getTime() - delay);

			if(delay > 0) {
				delayView.setText("+" + Long.toString(delay / 1000 / 60));
			}
		}
		timeView.setText(DateUtils.getTime(context, time));
	}

	static public void setDepartureTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		Date time = new Date(stop.getDepartureTime().getTime());

		if(stop.isDepartureTimePredicted() && stop.getDepartureDelay() != null) {
			long delay = stop.getDepartureDelay();
			time.setTime(time.getTime() - delay);

			if(delay > 0) {
				delayView.setText("+" + Long.toString(delay / 1000 / 60));
			}
		}
		timeView.setText(DateUtils.getTime(context, time));
	}

	static public String tripToSubject(Context context, Trip trip, boolean tag) {
		String str = "";

		if(tag) str += "[" + context.getResources().getString(R.string.app_name) + "] ";

		str += DateUtils.getTime(context, trip.getFirstDepartureTime()) + " ";
		str += trip.from.name;
		str += " → ";
		str += trip.to.name + " ";
		str += DateUtils.getTime(context, trip.getLastArrivalTime());
		str += " (" + DateUtils.getDate(context, trip.getFirstDepartureTime()) + ")";

		return str;
	}

	static public String tripToString(Context context, Trip trip) {
		String str = "";

		for(Leg leg : trip.legs) {
			str += legToString(context, leg) + "\n\n";
		}

		return str;
	}

	static public String legToString(Context context, Leg leg) {
		String str = "";
		String apos = "";

		str += DateUtils.getTime(context, leg.getDepartureTime()) + " ";
		str += leg.departure.name;

		if(leg instanceof Trip.Public) {
			Trip.Public pub = (Trip.Public) leg;
			if(pub.line != null && pub.line.label != null) {
				str += " (" + pub.line.label.substring(0, 1) + " ";
				str += pub.line.label.substring(1);
				if(pub.destination  != null) str += " → " + pub.destination.uniqueShortName();
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
		str += leg.arrival.name;
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

	static void startGeoIntent(Context context, Location loc) {
		String lat = Double.toString(loc.lat / 1E6);
		String lon = Double.toString(loc.lon / 1E6);

		String uri1 = "geo:0,0?q=" + lat + "," + lon;
		String uri2;

		try {
			uri2 = "(" + URLEncoder.encode(loc.uniqueShortName(), "utf-8") + ")";
		} catch (UnsupportedEncodingException e) {
			uri2 = "(" + loc.uniqueShortName() + ")";
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

	static void copyToClipboard(Context context, String text) {
		ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("label", text);
		clipboard.setPrimaryClip(clip);
	}

	static void showPopupIcons(PopupMenu popup) {
		// very ugly hack to show icons in PopupMenu
		// see: http://stackoverflow.com/a/18431605
		try {
			Field[] fields = popup.getClass().getDeclaredFields();
			for(Field field : fields) {
				if("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(popup);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
					setForceIcons.invoke(menuPopupHelper, true);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
