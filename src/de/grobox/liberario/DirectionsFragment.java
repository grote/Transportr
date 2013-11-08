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

import de.grobox.liberario.R;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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

public class DirectionsFragment extends LiberarioFragment {
	private View mView;
	private boolean mChange = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_directions, container, false);

		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();

		checkPreferences();

		// From text input
		final AutoCompleteTextView from = (AutoCompleteTextView) mView.findViewById(R.id.from);
		from.setAdapter(new LocationAutoCompleteAdapter(getActivity(), R.layout.list_item));
		from.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				setFrom((Location) parent.getItemAtPosition(position));
				from.requestFocus();
			}
		});
		from.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear location
				setFrom(null);
			}
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});
		((View) mView.findViewById(R.id.fromFavButton)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				fromFavClick(v);
			}
		});

		// To text input

		final AutoCompleteTextView to = (AutoCompleteTextView) mView.findViewById(R.id.to);
		to.setAdapter(new LocationAutoCompleteAdapter(getActivity(), R.layout.list_item));
		to.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				setTo((Location) parent.getItemAtPosition(position));
				to.requestFocus();
			}
		});
		to.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear location
				setTo(null);
			}
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});
		((View) mView.findViewById(R.id.toFavButton)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				toFavClick(v);
			}
		});

		// timeView
		final TextView timeView = (TextView) mView.findViewById(R.id.timeView);
		timeView.setText(DateUtils.getcurrentTime(getActivity()));
		((View) mView.findViewById(R.id.timeLayout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v);
			}
		});

		// Trip Date Type Spinner (departure or arrival)
		final Spinner spinner = (Spinner) mView.findViewById(R.id.dateTypeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.trip_date_type, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		// dateView
		final TextView dateView = (TextView) mView.findViewById(R.id.dateView);
		dateView.setText(DateUtils.getcurrentDate(getActivity()));
		((View) mView.findViewById(R.id.dateLayout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v);
			}
		});

		Button button = (Button) mView.findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				// check and set from location
				if(checkLocation(FavLocation.LOC_TYPE.FROM, (AutoCompleteTextView) mView.findViewById(R.id.from))) {
					query_trips.setFrom(getFrom());
				}
				else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_from), Toast.LENGTH_SHORT).show();
					return;
				}

				// check and set to location
				if(checkLocation(FavLocation.LOC_TYPE.TO, (AutoCompleteTextView) mView.findViewById(R.id.to))) {
					query_trips.setTo(getTo());
				}
				else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), Toast.LENGTH_SHORT).show();
					return;
				}

				// remember trip
				FavFile.useFavTrip(getActivity(), new FavTrip(getFrom(), getTo()));

				// set date
				query_trips.setDate(DateUtils.mergeDateTime(getActivity(), dateView.getText(), timeView.getText()));

				// set departure to true of first item is selected in spinner
				query_trips.setDeparture(spinner.getSelectedItem().equals(spinner.getItemAtPosition(0)));

				query_trips.execute();
			}
		});

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.directions, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_swap_locations:
				Location tmp = getFrom();
				setFrom(getTo());
				setTo(tmp);

				return true;
			case R.id.action_clear_favs:
				FavFile.resetFavLocationList(getActivity());
				refreshFavs();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	// change things for a different network provider
	public void onNetworkProviderChanged(NetworkProvider np) {
		// get and set new network name for action bar
		SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS, Context.MODE_PRIVATE);
		getActivity().getActionBar().setSubtitle(settings.getString("NetworkId", "???"));

		refreshFavs();

		// remove old text from TextViews
		if(mView != null) {
			((AutoCompleteTextView) mView.findViewById(R.id.from)).setText("");
			((AutoCompleteTextView) mView.findViewById(R.id.to)).setText("");
		}
	}

	private Location getFrom() {
		AutoCompleteTextView fromView = (AutoCompleteTextView) mView.findViewById(R.id.from);
		Location from = (Location) fromView.getTag();
		return from;
	}

	private void setFrom(Location loc) {
		if(!mChange) {
			mChange = true;
			AutoCompleteTextView fromView = (AutoCompleteTextView) mView.findViewById(R.id.from);
			fromView.setTag(loc);

			if(loc != null) {
				fromView.setText(loc.uniqueShortName());
				fromView.getBackground().setColorFilter(getResources().getColor(R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
				fromView.dismissDropDown();
			}
			else {
				fromView.getBackground().setColorFilter(getResources().getColor(R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
			}
			mChange = false;
		}
	}

	private Location getTo() {
		AutoCompleteTextView toView = (AutoCompleteTextView) mView.findViewById(R.id.to);
		Location to = (Location) toView.getTag();
		return to;
	}

	private void setTo(Location loc) {
		if(!mChange) {
			mChange = true;
			AutoCompleteTextView toView = (AutoCompleteTextView) mView.findViewById(R.id.to);
			toView.setTag(loc);

			if(loc != null) {
				toView.setText(loc.uniqueShortName());
				toView.getBackground().setColorFilter(getResources().getColor(R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
				toView.dismissDropDown();
			}
			else {
				toView.getBackground().setColorFilter(getResources().getColor(R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
			}
			mChange = false;
		}
	}

	private void checkPreferences() {
		SharedPreferences settings = getActivity().getSharedPreferences(Preferences.PREFS, Context.MODE_PRIVATE);
		boolean firstRun = settings.getBoolean("FirstRun", true);

		// show about page at first run
		if(firstRun) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("FirstRun", false);
			editor.commit();

			startActivity(new Intent(getActivity(), AboutActivity.class));
		}

		String network = settings.getString("NetworkId", null);

		// return if no network is set
		if(network == null) {
			Intent intent = new Intent(getActivity(), PickNetworkProviderActivity.class);
			intent.putExtra("FirstRun", true);

			startActivityForResult(intent, MainActivity.CHANGED_NETWORK_PROVIDER);
		}
		else {
			getActivity().getActionBar().setSubtitle(network);
		}

	}

	public void fromFavClick(View v) {
		AutoCompleteTextView from = ((AutoCompleteTextView) mView.findViewById(R.id.from));
		int size = ((LocationAutoCompleteAdapter) from.getAdapter()).addFavs(FavLocation.LOC_TYPE.FROM);

		if(size > 0) {
			from.showDropDown();
		}
		else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_no_favs), Toast.LENGTH_SHORT).show();
		}
	}

	public void toFavClick(View v) {
		AutoCompleteTextView to = ((AutoCompleteTextView) mView.findViewById(R.id.to));
		int size = ((LocationAutoCompleteAdapter) to.getAdapter()).addFavs(FavLocation.LOC_TYPE.TO);

		if(size > 0) {
			to.showDropDown();
		}
		else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_no_favs), Toast.LENGTH_SHORT).show();
		}
	}

	public void refreshFavs() {
		if(mView != null) {
			AutoCompleteTextView from = ((AutoCompleteTextView) mView.findViewById(R.id.from));
			((LocationAutoCompleteAdapter) from.getAdapter()).clearFavs();

			AutoCompleteTextView to = ((AutoCompleteTextView) mView.findViewById(R.id.to));
			((LocationAutoCompleteAdapter) to.getAdapter()).clearFavs();
		}
	}

	private Boolean checkLocation(FavLocation.LOC_TYPE loc_type, AutoCompleteTextView view) {
		// ugly hack to have one method for all private location vars because call by reference isn't possible
		Location loc = null;
		if(loc_type == FavLocation.LOC_TYPE.FROM) loc = getFrom();
		else if(loc_type == FavLocation.LOC_TYPE.TO) loc = getTo();

		if(loc == null) {
			// no location was selected by user
			if(!view.getText().toString().equals("")) {
				// no location selected, but text entered. So let's try create locations from text
				if(loc_type == FavLocation.LOC_TYPE.FROM) {
					setFrom(new Location(LocationType.ANY, 0, view.getText().toString(), view.getText().toString()));
				}
				else if(loc_type == FavLocation.LOC_TYPE.TO) {
					setTo(new Location(LocationType.ANY, 0, view.getText().toString(), view.getText().toString()));
				}
				return true;
			}
			return false;
		}
		// we have a location, so make it a favorite
		else {
			FavFile.updateFavLocation(getActivity(), loc, loc_type);
		}

		return true;
	}

	public void showTimePickerDialog(View v) {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
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
		newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
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

