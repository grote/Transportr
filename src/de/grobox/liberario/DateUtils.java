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
			return df.parse((String) date_string);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	static public Date parseTime(Context context, CharSequence time_string) {
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);

		try {
			return tf.parse((String) time_string);
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


	@SuppressWarnings("deprecation")
	static public String getTime(Date date) {
		String time;

		int hours = date.getHours();
		int minutes = date.getMinutes();

		if(hours > 9) {
			time = Integer.toString(hours);
		}
		else {
			time = "0" + Integer.toString(hours);
		}

		time += ":";

		if(minutes > 9) {
			time += Integer.toString(minutes);
		}
		else {
			time += "0" + Integer.toString(minutes);
		}

		return time;
	}


	static public String getDuration(Date start, Date end) {
		String duration;

		long min = (end.getTime() - start.getTime()) / 1000 / 60;

		int hours = (int) (min / 60);
		int minutes = (int) (min % 60);

		duration = Integer.toString(hours) + ":";

		if(minutes > 9) {
			duration += Integer.toString(minutes);
		}
		else {
			duration += "0" + Integer.toString(minutes);
		}

		return duration;
	}

}
