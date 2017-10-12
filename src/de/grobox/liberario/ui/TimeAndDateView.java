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

package de.grobox.liberario.ui;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import de.grobox.liberario.R;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateFormat.getTimeFormat;
import static android.widget.Toast.LENGTH_SHORT;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TimeAndDateView extends LinearLayout
		implements OnTimeSetListener, OnDateSetListener {

	private final String SUPER_STATE = "superState";
	private static final String DATE = "date";
	private static final String NOW = "now";
	private static final String TODAY = "today";
	private final TimeAndDateViewHolder ui;
	private Calendar calendar;
	private boolean now = true, today = true;

	public TimeAndDateView(Context context, AttributeSet attr) {
		super(context, attr);

		setOrientation(LinearLayout.HORIZONTAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.time_and_date, this, true);
		ui = new TimeAndDateViewHolder(this);

		// Initialize current Time and Date, display it in UI
		reset();

		// Time
		ui.time.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TimePickerFragment newFragment = new TimePickerFragment();
				newFragment.setOnTimeSetListener(TimeAndDateView.this);
				// show current time also in dialog if time set to now
				if(now) resetTime();
				Bundle bundle = new Bundle();
				bundle.putSerializable(DATE, calendar);
				newFragment.setArguments(bundle);
				newFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "timePicker");

			}
		});
		// set current time on long click
		ui.time.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				resetTime();
				Toast.makeText(getContext(), R.string.current_time_set, LENGTH_SHORT).show();
				return true;
			}
		});

		// Date
		ui.date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DatePickerFragment newFragment = new DatePickerFragment();
				newFragment.setOnDateSetListener(TimeAndDateView.this);
				// show current date also in dialog if set to today
				if(today) resetDate();
				Bundle bundle = new Bundle();
				bundle.putSerializable(DATE, calendar);
				newFragment.setArguments(bundle);
				newFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "datePicker");
			}
		});
		// set current date on long click
		ui.date.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				resetDate();
				Toast.makeText(getContext(), R.string.current_date_set, LENGTH_SHORT).show();
				return true;
			}
		});

		// Plus 15
		ui.plus15.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addTime(15);
			}
		});
		// plus 1 hour on long click
		ui.plus15.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				addTime(60);
				Toast.makeText(getContext(), R.string.added_1h, LENGTH_SHORT).show();
				return true;
			}
		});
	}

	public TimeAndDateView(Context context) {
		this(context, null);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());
		bundle.putSerializable(DATE, calendar);
		bundle.putBoolean(NOW, now);
		bundle.putBoolean(TODAY, today);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) { // implicit null check
			Bundle bundle = (Bundle) state;
			calendar = (Calendar) bundle.getSerializable(DATE);
			now = bundle.getBoolean(NOW);
			today = bundle.getBoolean(TODAY);
			updateTexts();
			state = bundle.getParcelable(SUPER_STATE);
		}
		super.onRestoreInstanceState(state);
	}

	@Override
	 protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
		// Makes sure that the state of the child views are not saved
		super.dispatchFreezeSelfOnly(container);
	}

	@Override
	protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
		// Makes sure that the state of the child views are not restored
		super.dispatchThawSelfOnly(container);
	}

	@Override
	public void onDateSet(@Nullable DatePicker view, int year, int month, int day) {
		calendar.set(YEAR, year);
		calendar.set(MONTH, month);
		calendar.set(DAY_OF_MONTH, day);

		Calendar c = Calendar.getInstance();
		today = c.get(YEAR) == year && c.get(MONTH) == month && c.get(DAY_OF_MONTH) == day;
		updateTexts();
	}

	@Override
	public void onTimeSet(@Nullable TimePicker view, int hourOfDay, int minute) {
		calendar.set(HOUR_OF_DAY, hourOfDay);
		calendar.set(MINUTE, minute);

		// check if time can be considered "now"
		Calendar c1 = Calendar.getInstance();
		c1.set(HOUR_OF_DAY, hourOfDay);
		c1.set(MINUTE, minute);
		Calendar c2 = Calendar.getInstance();
		now = MILLISECONDS.toMinutes(Math.abs(c1.getTimeInMillis() - c2.getTimeInMillis())) < 10;
		updateTexts();
	}

	public Date getDate() {
		if(now) resetTime();
		if(today) resetDate();
		return calendar.getTime();
	}

	public void setDate(Date date) {
		Calendar tmp = Calendar.getInstance();
		tmp.setTime(date);

		onTimeSet(null, tmp.get(HOUR_OF_DAY), tmp.get(MINUTE));
		onDateSet(null, tmp.get(YEAR), tmp.get(MONTH), tmp.get(DAY_OF_MONTH));
	}

	private void updateTexts() {
		ui.time.setText(getTimeString());
		ui.date.setText(getDateString());
	}

	private String getTimeString() {
		if (now) {
			return getContext().getString(R.string.now);
		}
		DateFormat tf = getTimeFormat(getContext().getApplicationContext());
		return tf.format(calendar.getTime());
	}

	private String getDateString() {
		if(today) {
			return getContext().getString(R.string.today);
		}
		DateFormat tf = getDateFormat(getContext().getApplicationContext());
		return tf.format(calendar.getTime());
	}

	private void addTime(int min) {
		// remember day before adding
		int day = calendar.get(DAY_OF_MONTH);

		// update time if it was set to now before
		if(now) calendar = Calendar.getInstance();

		// add min minutes
		calendar.add(MINUTE, min);

		// no more now, but maybe today?
		now = false;
		if (day != calendar.get(DAY_OF_MONTH)) {
			today = false;
		}

		// update text of buttons
		updateTexts();
	}

	private void resetTime() {
		Calendar c = Calendar.getInstance();
		calendar.set(HOUR_OF_DAY, c.get(HOUR_OF_DAY));
		calendar.set(MINUTE, c.get(MINUTE));

		now = true;
		ui.time.setText(getTimeString());
	}

	private void resetDate() {
		Calendar c = Calendar.getInstance();
		calendar.set(YEAR, c.get(YEAR));
		calendar.set(MONTH, c.get(MONTH));
		calendar.set(DAY_OF_MONTH, c.get(DAY_OF_MONTH));

		today = true;
		ui.date.setText(getDateString());
	}

	public void reset() {
		calendar = Calendar.getInstance();
		now = true;
		today = true;
		updateTexts();
	}

	public static class TimeAndDateViewHolder {
		public Button time;
		private Button plus15;
		public Button date;

		private TimeAndDateViewHolder(View view) {
			time = view.findViewById(R.id.timeButton);
			plus15 = view.findViewById(R.id.plus15Button);
			date = view.findViewById(R.id.dateButton);
		}
	}

	public static class TimePickerFragment extends DialogFragment {

		OnTimeSetListener listener;

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar calendar = (Calendar) getArguments().getSerializable(DATE);
			if(calendar == null) calendar = Calendar.getInstance();

			int hour = calendar.get(HOUR_OF_DAY);
			int minute = calendar.get(MINUTE);
			TimePickerDialog tpd = new TimePickerDialog(getActivity(), listener, hour, minute,
					android.text.format.DateFormat.is24HourFormat(getActivity()));
			tpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), tpd);
			tpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), tpd);
			return tpd;
		}

		public void setOnTimeSetListener(OnTimeSetListener listener) {
			// TODO this needs to re-attach on configuration changes
			this.listener = listener;
		}
	}

	public static class DatePickerFragment extends DialogFragment {

		OnDateSetListener listener;

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar calendar = (Calendar) getArguments().getSerializable(DATE);
			if(calendar == null) calendar = Calendar.getInstance();

			int year = calendar.get(YEAR);
			int month = calendar.get(MONTH);
			int day = calendar.get(DAY_OF_MONTH);
			DatePickerDialog dpd = new DatePickerDialog(getActivity(), listener, year, month, day);
			dpd.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), dpd);
			dpd.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.ok), dpd);
			return dpd;
		}

		public void setOnDateSetListener(OnDateSetListener listener) {
			// TODO this needs to re-attach on configuration changes
			this.listener = listener;
		}
	}

}