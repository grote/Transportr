/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import de.grobox.transportr.R;

import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class DateUtils {

	public static String getDate(Context context, Date date) {
		DateFormat df = android.text.format.DateFormat.getDateFormat(context);

		return df.format(date);
	}

	public static String getTime(Context context, @Nullable Date date) {
		if (date == null) return "";
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		if (tf.getNumberFormat().getMinimumIntegerDigits() == 1) {
			String formattedTime = tf.format(date);
			if (formattedTime.indexOf(':') == 1) {
				// ensure times always have the same length, so views (like intermediate stops) align
				return "0" + formattedTime;
			}
			return formattedTime;
		}
		return tf.format(date);
	}

	public static String getDuration(long duration) {
		// get duration in minutes
		duration = duration / 1000 / 60;

		long m = duration % 60;
		long h = duration / 60;

		return Long.toString(h) + ":" + (m < 10 ? "0" : "") + Long.toString(m);
	}

	public static String getDuration(Date start, Date end) {
		return getDuration(end.getTime() - start.getTime());
	}

	/**
	 * Returns delay in minutes
	 */
	public static long getDelay(Date plannedTime, Date predictedTime) {
		return (predictedTime.getTime() - plannedTime.getTime()) / 1000 / 60;
	}

	public static String getDelayString(long delay) {
		return (delay > 0 ? "+" : "") + Long.toString(delay);
	}

	@Nullable
	public static String getDelayText(long delay) {
		if (delay >= 0) {
			return "+" + Long.toString(delay / 1000 / 60);
		} else if (delay < 0) {
			return "-" + Long.toString(delay / 1000 / 60);
		} else {
			return null;
		}
	}

	public static boolean isToday(Calendar calendar) {
		return android.text.format.DateUtils.isToday(calendar.getTimeInMillis());
	}

	public static boolean isNow(Calendar calendar) {
		long diff = Math.abs(calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
		return diff < 10 * MINUTE_IN_MILLIS;
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

	private static long getDifferenceInMinutes(Date date) {
		return getDelay(new Date(), date);
	}

}
