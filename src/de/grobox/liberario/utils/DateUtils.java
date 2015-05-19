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

package de.grobox.liberario.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import de.grobox.liberario.R;

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
		// get duration in minutes
		duration = duration / 1000 / 60;

		long m = duration % 60;
		long h = duration / 60;

		return Long.toString(h) + ":" + (m < 10 ? "0" : "") + Long.toString(m);
	}

	static public String getDuration(Date start, Date end) {
		return getDuration(end.getTime() - start.getTime());
	}

	static public void addToTime(Context context, Button timeView, Button dateView, int min) {
		Calendar c = (Calendar) timeView.getTag();
		Calendar c_date = (Calendar) dateView.getTag();

		// set the date to the calendar, so it can calculate a day overflow
		c.set(Calendar.YEAR, c_date.get(Calendar.YEAR));
		c.set(Calendar.MONTH, c_date.get(Calendar.MONTH));
		c.set(Calendar.DAY_OF_MONTH, c_date.get(Calendar.DAY_OF_MONTH));

		// add min minutes
		c.add(Calendar.MINUTE, min);

		timeView.setText(getTime(context, c));
		timeView.setTag(c);
		dateView.setText(getDate(context, c.getTime()));
		dateView.setTag(c);
	}

	static public void setUpTimeDateUi(final View view) {
		// Time

		final Button time = (Button) view.findViewById(R.id.timeView);
		final DateUtils.TimePicker timePicker = new DateUtils.TimePicker(view.getContext(), time);
		time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				timePicker.show();
			}
		});
		// set current time on long click
		time.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				time.setText(DateUtils.getcurrentTime(view.getContext()));
				time.setTag(Calendar.getInstance());
				return true;
			}
		});

		// Date

		final Button date = (Button) view.findViewById(R.id.dateView);
		final DateUtils.DatePicker datePicker = new DateUtils.DatePicker(view.getContext(), date);
		date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				datePicker.show();
			}
		});
		// set current date on long click
		date.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				date.setText(DateUtils.getcurrentDate(view.getContext()));
				date.setTag(Calendar.getInstance());
				return true;
			}
		});

		// Plus 15

		final Button plus15 = (Button) view.findViewById(R.id.plus15Button);
		plus15.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DateUtils.addToTime(view.getContext(), time, date, 15);
			}
		});
		// plus 1 hour on long click
		plus15.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				DateUtils.addToTime(view.getContext(), time, date, 60);
				return true;
			}
		});
	}

	static public class TimePicker extends TimePickerDialog {

		private Button button;

		public TimePicker(final Context context, final Button button) {
			super(context, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(android.widget.TimePicker timePicker, int selectedHour, int selectedMinute) {
					button.setText(formatTime(context, selectedHour, selectedMinute));

					// store Calendar instance with Button
					Calendar c = Calendar.getInstance();
					c.set(Calendar.HOUR_OF_DAY, selectedHour);
					c.set(Calendar.MINUTE, selectedHour);
					button.setTag(c);
				}
			}, 0, 0, android.text.format.DateFormat.is24HourFormat(context));

			// initialize button
			button.setText(getcurrentTime(context));
			button.setTag(Calendar.getInstance());

			this.button = button;

			update();
		}

		@Override
		public void show() {
			update();

			super.show();
		}

		private void update() {
			Calendar c;

			if(button.getTag() != null) {
				c = (Calendar) button.getTag();
			} else {
				c = Calendar.getInstance();
			}

			updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
		}

	}

	static public class DatePicker extends DatePickerDialog {

		private Button button;

		public DatePicker(final Context context, final Button button) {
			super(context, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(android.widget.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
					button.setText(DateUtils.formatDate(context, year, monthOfYear, dayOfMonth));

					// store Calendar instance with Button
					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, year);
					c.set(Calendar.MONTH, monthOfYear);
					c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
					button.setTag(c);
				}
			}, 2015, 1, 1);

			// initialize button
			button.setText(getcurrentDate(context));
			button.setTag(Calendar.getInstance());

			this.button = button;

			update();
		}

		@Override
		public void show() {
			update();

			super.show();
		}

		private void update() {
			Calendar c;

			if(button.getTag() != null) {
				c = (Calendar) button.getTag();
			} else {
				c = Calendar.getInstance();
			}

			updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		}

	}

}
