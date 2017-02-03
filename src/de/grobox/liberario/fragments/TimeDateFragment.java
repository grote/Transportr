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

package de.grobox.liberario.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.util.Calendar;

import de.grobox.liberario.settings.Preferences;
import de.grobox.liberario.R;

import static android.text.format.DateFormat.getDateFormat;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;

public class TimeDateFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePicker.OnTimeChangedListener {

	public static final String TAG = TimeDateFragment.class.getName();
	private static final String CALENDAR = "calendar";

	public static TimeDateFragment newInstance(Calendar calendar) {
		TimeDateFragment f = new TimeDateFragment();

		Bundle args = new Bundle();
		args.putSerializable(CALENDAR, calendar);
		f.setArguments(args);

		return f;
	}

	@Nullable
	private TimeDateListener listener;
	private TimePicker timePicker;
	private TextView dateView;
	private Calendar calendar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Preferences.darkThemeEnabled(getActivity())) {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme);
		} else {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme_Light);
		}

		if (savedInstanceState == null) {
			calendar = (Calendar) getArguments().getSerializable(CALENDAR);
			if (calendar == null) throw new IllegalArgumentException("Calendar missing");
		} else {
			calendar = (Calendar) savedInstanceState.getSerializable(CALENDAR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_time_date, container);

		// Time
		timePicker = (TimePicker) v.findViewById(R.id.timePicker);
		timePicker.setOnTimeChangedListener(this);
		showTime(calendar);

		// Date
		dateView = (TextView) v.findViewById(R.id.dateView);
		dateView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				new DatePickerDialog(getContext(), TimeDateFragment.this, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH)).show();
			}
		});
		showDate(calendar);

		// Previous and Next Date
		ImageButton prevDateButton = (ImageButton) v.findViewById(R.id.prevDateButton);
		prevDateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				calendar.add(DAY_OF_MONTH, -1);
				showDate(calendar);
			}
		});
		ImageButton nextDateButton = (ImageButton) v.findViewById(R.id.nextDateButton);
		nextDateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				calendar.add(DAY_OF_MONTH, 1);
				showDate(calendar);
			}
		});

		// Buttons
		Button okButton = (Button) v.findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (listener != null) {
					long diff = Math.abs(calendar.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
					boolean now = diff < 10 * MINUTE_IN_MILLIS;
					boolean today = DateUtils.isToday(calendar.getTimeInMillis());
					listener.onTimeAndDateSet(calendar, now, today);
				}
				dismiss();
			}
		});
		Button cancelButton = (Button) v.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(CALENDAR, calendar);
	}

	@Override
	public void onTimeChanged(TimePicker timePicker, int hourOfDay, int minute) {
		calendar.set(HOUR_OF_DAY, hourOfDay);
		calendar.set(MINUTE, minute);
	}

	@Override
	public void onDateSet(DatePicker datePicker, int year, int month, int day) {
		calendar.set(YEAR, year);
		calendar.set(MONTH, month);
		calendar.set(DAY_OF_MONTH, day);
		showDate(calendar);
	}

	public void setTimeDateListener(TimeDateListener listener) {
		this.listener = listener;
	}

	@SuppressWarnings("deprecation")
	private void showTime(Calendar c) {
		timePicker.setCurrentHour(c.get(HOUR_OF_DAY));
		timePicker.setCurrentMinute(c.get(MINUTE));
	}

	@SuppressWarnings("WrongConstant")
	private void showDate(Calendar c) {
		Calendar now = Calendar.getInstance();
		if (isSameMonth(now, c) && c.get(DAY_OF_MONTH) == now.get(DAY_OF_MONTH) - 1) {
			dateView.setText(getString(R.string.yesterday));
		} else if (isSameMonth(now, c) && c.get(DAY_OF_MONTH) == now.get(DAY_OF_MONTH)) {
			dateView.setText(getString(R.string.today));
		} else if (isSameMonth(now, c) && c.get(DAY_OF_MONTH) == now.get(DAY_OF_MONTH) + 1) {
			dateView.setText(getString(R.string.tomorrow));
		} else {
			DateFormat tf = getDateFormat(getContext().getApplicationContext());
			dateView.setText(tf.format(calendar.getTime()));
		}
	}

	private boolean isSameMonth(Calendar now, Calendar c) {
		return c.get(YEAR) == now.get(YEAR) && c.get(MONTH) == now.get(MONTH);
	}

	public interface TimeDateListener {
		void onTimeAndDateSet(Calendar calendar, boolean isNow, boolean isToday);
	}

}
