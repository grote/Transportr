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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

public class DateUtils {

	static public String getcurrentTime(Context context) {
		Calendar date = Calendar.getInstance();
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		return tf.format(date.getTime());
	}

	static public String getcurrentDate(Context context) {
		Calendar date = Calendar.getInstance();
		DateFormat df = android.text.format.DateFormat.getDateFormat(context);

		return df.format(date.getTime());
	}

	static public Date mergeDateTime(Context context, CharSequence date_string, CharSequence time_string) {
		Date date = parseDate(context, date_string);
		Date time = parseTime(context, time_string);

		Calendar ct = Calendar.getInstance();
		Calendar cd = Calendar.getInstance();

		ct.setTime(time);
		cd.setTime(date);

		ct.set(cd.get(Calendar.YEAR), cd.get(Calendar.MONTH), cd.get(Calendar.DAY_OF_MONTH));

		return ct.getTime();
	}

	static public Date parseDate(Context context, CharSequence date_string) {
		DateFormat df = android.text.format.DateFormat.getDateFormat(context);

		try {
			return df.parse(date_string.toString());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public Date parseTime(Context context, CharSequence time_string) {
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		try {
			return tf.parse(time_string.toString());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public String formatDate(Context context, int year, int month, int day) {
		Calendar c = Calendar.getInstance();

		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);

		DateFormat df = android.text.format.DateFormat.getDateFormat(context);

		return df.format(c.getTime());
	}

	static public String formatTime(Context context, int hourOfDay, int minute) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, hourOfDay);
		c.set(Calendar.MINUTE, minute);

		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		return tf.format(c.getTime());
	}

	static public String getDate(Context context, Date date) {
		DateFormat df = android.text.format.DateFormat.getDateFormat(context);

		return df.format(date);
	}

	static public String getTime(Context context, Calendar c) {
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		return tf.format(c.getTime());
	}

	static public String getTime(Context context, Date date) {
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		if(date == null) return "";
		else return tf.format(date);
	}


	static public String getDuration(long duration) {
		String str;

		// get duration in minutes
		duration = duration / 1000 / 60;

		long m = duration % 60;
		long h = duration / 60;

		return Long.toString(h) + ":" + (m < 10 ? "0" : "") + Long.toString(m);
	}

}
