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
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
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

public class TimeAndDateView extends LinearLayout {

	private final String SUPER_STATE = "superState";
	private final String DATE = "date";
	private final TimeAndDateViewHolder ui;
	private Calendar calendar;

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
				DialogFragment newFragment = new TimePickerFragment();
				newFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "timePicker");

			}
		});
		// set current time on long click
		ui.time.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				resetTime();
				Toast.makeText(getContext(), R.string.current_time_set, Toast.LENGTH_SHORT).show();
				return true;
			}
		});

		// Date
		ui.date.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DialogFragment newFragment = new DatePickerFragment();
				newFragment.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "datePicker");
			}
		});
		// set current date on long click
		ui.date.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				resetDate();
				Toast.makeText(getContext(), R.string.current_date_set, Toast.LENGTH_SHORT).show();
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
				Toast.makeText(getContext(), R.string.added_1h, Toast.LENGTH_SHORT).show();
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
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) { // implicit null check
			Bundle bundle = (Bundle) state;
			calendar = (Calendar) bundle.getSerializable(DATE);
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

	public Calendar getCalendar() {
		return calendar;
	}

	public Date getDate() {
		return calendar.getTime();
	}

	public void setDate(Date date) {
		calendar.setTime(date);
	}

	private void updateTexts() {
		ui.time.setText(getTimeString());
		ui.date.setText(getDateString());
	}

	private String getTimeString() {
		DateFormat tf = android.text.format.DateFormat.getTimeFormat(getContext().getApplicationContext());

		return tf.format(calendar.getTime());
	}

	private String getDateString() {
		DateFormat tf = android.text.format.DateFormat.getDateFormat(getContext().getApplicationContext());

		return tf.format(calendar.getTime());
	}

	private void addTime(int min) {
		// add min minutes
		calendar.add(Calendar.MINUTE, min);

		// update text of buttons
		updateTexts();
	}

	private void resetTime() {
		Calendar c = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));

		ui.time.setText(getTimeString());
	}

	private void resetDate() {
		Calendar c = Calendar.getInstance();
		calendar.set(Calendar.YEAR, c.get(Calendar.YEAR));
		calendar.set(Calendar.MONTH, c.get(Calendar.MONTH));
		calendar.set(Calendar.DAY_OF_MONTH, c.get(Calendar.DAY_OF_MONTH));

		ui.date.setText(getDateString());
	}

	public void reset() {
		calendar = Calendar.getInstance();
		updateTexts();
	}

	public static class TimeAndDateViewHolder {
		public Button time;
		public Button plus15;
		public Button date;

		public TimeAndDateViewHolder(View view) {
			time = (Button) view.findViewById(R.id.timeButton);
			plus15 = (Button) view.findViewById(R.id.plus15Button);
			date = (Button) view.findViewById(R.id.dateButton);
		}
	}

	class TimePickerFragment extends DialogFragment
			implements TimePickerDialog.OnTimeSetListener {

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute,
					android.text.format.DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			updateTexts();
		}
	}

	class DatePickerFragment extends DialogFragment
			implements DatePickerDialog.OnDateSetListener {

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH);
			int day = calendar.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.DAY_OF_MONTH, day);
			updateTexts();
		}
	}

}