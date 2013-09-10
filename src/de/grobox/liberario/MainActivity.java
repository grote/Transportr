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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private Location loc_from;
	private Location loc_to;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// From text input
		AutoCompleteTextView from = (AutoCompleteTextView) findViewById(R.id.from);
		from.setAdapter(new LocationAutoCompleteAdapter(this, R.layout.list_item));
		from.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				loc_from = (Location) parent.getItemAtPosition(position);
			}
		});
		from.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear location
				loc_from = null;
			}
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
		});

		// To text input
		AutoCompleteTextView to = (AutoCompleteTextView) findViewById(R.id.to);
		to.setAdapter(new LocationAutoCompleteAdapter(this, R.layout.list_item));
		to.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				loc_to = (Location) parent.getItemAtPosition(position);
			}
		});
		to.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear location
				loc_to = null;
			}
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
		});

		// timeView
		final TextView timeView = (TextView) findViewById(R.id.timeView);
		timeView.setText(DateUtils.getcurrentTime(getApplicationContext()));

		// Trip Date Type Spinner (departure or arrival)
		final Spinner spinner = (Spinner) findViewById(R.id.dateTypeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.trip_date_type, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		// dateView
		final TextView dateView = (TextView) findViewById(R.id.dateView);
		dateView.setText(DateUtils.getcurrentDate(getApplicationContext()));

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				// check and set from location
				if(checkLocation(FavLocation.LOC_TYPE.FROM, (AutoCompleteTextView) findViewById(R.id.from))) {
					query_trips.setFrom(loc_from);
				}
				else {
					// TODO localize
					Toast.makeText(getBaseContext(), "Invalid From Location.", Toast.LENGTH_SHORT).show();
					return;
				}

				// check and set to location
				if(checkLocation(FavLocation.LOC_TYPE.TO, (AutoCompleteTextView) findViewById(R.id.to))) {
					query_trips.setTo(loc_to);
				}
				else {
					// TODO localize
					Toast.makeText(getBaseContext(), "Invalid To Location.", Toast.LENGTH_SHORT).show();
					return;
				}

				// set date
				query_trips.setDate(DateUtils.mergeDateTime(getApplicationContext(), dateView.getText(), timeView.getText()));

				// set departure to true of first item is selected in spinner
				query_trips.setDeparture(spinner.getSelectedItem().equals(spinner.getItemAtPosition(0)));

				query_trips.execute();
			}
		});

	}

	public void fromFavClick(View v) {
		AutoCompleteTextView from = ((AutoCompleteTextView) findViewById(R.id.from));
		int size = ((LocationAutoCompleteAdapter) from.getAdapter()).addFavs(FavLocation.LOC_TYPE.FROM);

		if(size > 0) {
			from.showDropDown();
		}
		else {
			// TODO localize
			Toast.makeText(getBaseContext(), "No favorite locations saved", Toast.LENGTH_SHORT).show();
		}
	}

	public void toFavClick(View v) {
		AutoCompleteTextView to = ((AutoCompleteTextView) findViewById(R.id.to));
		int size = ((LocationAutoCompleteAdapter) to.getAdapter()).addFavs(FavLocation.LOC_TYPE.TO);

		if(size > 0) {
			to.showDropDown();
		}
		else {
			// TODO localize
			Toast.makeText(getBaseContext(), "No favorite locations saved", Toast.LENGTH_SHORT).show();
		}
	}

	private Boolean checkLocation(FavLocation.LOC_TYPE loc_type, AutoCompleteTextView view) {
		// ugly hack to have one method for all private location vars because call by reference isn't possible
		Location loc = null;
		if(loc_type == FavLocation.LOC_TYPE.FROM) loc = loc_from;
		else if(loc_type == FavLocation.LOC_TYPE.TO) loc = loc_to;

		if(loc == null) {
			// no location was selected by user
			if(!view.getText().toString().equals("")) {
				// no location selected, but text entered. So let's try create locations from text
				if(loc_type == FavLocation.LOC_TYPE.FROM) {
					loc_from = new Location(LocationType.ANY, 0, view.getText().toString(), view.getText().toString());
				}
				else if(loc_type == FavLocation.LOC_TYPE.TO) {
					loc_to = new Location(LocationType.ANY, 0, view.getText().toString(), view.getText().toString());
				}
				return true;
			}
			return false;
		}
		// we have a location, so make it a favorite
		else {
			//FavFile.resetFavList(getBaseContext());
			List<FavLocation> fav_list = FavFile.getFavList(getBaseContext());
			FavLocation fav_loc = new FavLocation(loc);
			if(fav_list.contains(fav_loc)){
				// increase counter by one for existing location
				if(loc_type == FavLocation.LOC_TYPE.FROM) fav_list.get(fav_list.indexOf(fav_loc)).addFrom();
				else if(loc_type == FavLocation.LOC_TYPE.TO) fav_list.get(fav_list.indexOf(fav_loc)).addTo();
			}
			else {
				// add new favorite location
				// increase counter by one for existing location
				if(loc_type == FavLocation.LOC_TYPE.FROM) fav_loc.addFrom();
				else if(loc_type == FavLocation.LOC_TYPE.TO) fav_loc.addTo();

				fav_list.add(fav_loc);
			}
			FavFile.setFavList(getBaseContext(), fav_list);
		}

		return true;
	}

	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
	}

	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			TextView timeView = (TextView) getActivity().findViewById(R.id.timeView);
			Calendar c = Calendar.getInstance();

			Date date = DateUtils.parseTime(getActivity().getApplicationContext(), timeView.getText());

			if(date != null) {
				c.setTime(date);
			} else {
				// if time couldn't be parsed, use current time
				c.setTime(new Date());
			}

			// set time for picker
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			TextView timeView = (TextView) getActivity().findViewById(R.id.timeView);
			timeView.setText(DateUtils.formatTime(getActivity().getApplicationContext(), hourOfDay, minute));
		}
	}

	public void showDatePickerDialog(View v) {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getSupportFragmentManager(), "datePicker");
	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			TextView dateView = (TextView) getActivity().findViewById(R.id.dateView);
			Calendar c = Calendar.getInstance();

			Date date = DateUtils.parseDate(getActivity().getApplicationContext(), dateView.getText()); 

			if(date != null) {
				c.setTime(date);
			} else {
				// if date couldn't be parsed, use current date
				c.setTime(new Date());
			}

			// set date for picker
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			TextView dateView = (TextView) getActivity().findViewById(R.id.dateView);
			dateView.setText(DateUtils.formatDate(getActivity().getApplicationContext(), year, month, day));
		}
	}

}

