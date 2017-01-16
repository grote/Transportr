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

import android.content.Context;
import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.util.Date;

public class DateUtils {

	static public String getDate(Context context, Date date) {
		DateFormat df = android.text.format.DateFormat.getDateFormat(context);

		return df.format(date);
	}

	static public String getTime(Context context, Date date) {
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		if(date == null) return "";
		else return tf.format(date);
	}

	static public String getDuration(long duration) {
		// get duration in minutes
		duration = duration / 1000 / 60;

		long m = duration % 60;
		long h = duration / 60;

		return Long.toString(h) + ":" + (m < 10 ? "0" : "") + Long.toString(m);
	}

	static public String getDuration(Date start, Date end) {
		return getDuration(end.getTime() - start.getTime());
	}

	static public long getDifferenceInMinutes(Date date) {
		return getDelay(new Date(), date);
	}

	/**
	 * Returns delay in minutes
	 */
	static public long getDelay(Date plannedTime, Date predictedTime) {
		return (predictedTime.getTime() - plannedTime.getTime()) / 1000 / 60;
	}

	@Nullable
	static public String getDelayString(long delay) {
		return (delay > 0 ? "+" : "-") + Long.toString(delay);
	}

}
