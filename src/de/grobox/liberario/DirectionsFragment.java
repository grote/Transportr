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
import java.util.LinkedList;
import java.util.List;

import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class DirectionsFragment extends LiberarioFragment implements LocationListener {
	private View mView;
	private boolean mChange = false;
	private FavLocation.LOC_TYPE mHomeClicked;
	private LocationManager locationManager;
	private Location gps_loc = null;
	private boolean mGpsPressed = false;
	private AsyncQueryTripsTask mAfterGpsTask = null;
	private List<Product> mProducts = new LinkedList<Product>(Product.ALL);
	public ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_directions, container, false);
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();

		checkPreferences();

		setFromUI();
		setToUI();

		// timeView
		final TextView timeView = (TextView) mView.findViewById(R.id.timeView);
		timeView.setText(DateUtils.getcurrentTime(getActivity()));
		((View) mView.findViewById(R.id.timeLayout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showTimePickerDialog(v);
			}
		});

		Button plus10Button = (Button) mView.findViewById(R.id.plus15Button);
		plus10Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addToTime(15);
			}
		});

		// dateView
		final TextView dateView = (TextView) mView.findViewById(R.id.dateView);
		dateView.setText(DateUtils.getcurrentDate(getActivity()));
		((View) mView.findViewById(R.id.dateLayout)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				showDatePickerDialog(v);
			}
		});

		// Trip Date Type Spinner (departure or arrival)
		final Spinner spinner = (Spinner) mView.findViewById(R.id.dateTypeSpinner);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.trip_date_type, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		// Products
		ViewGroup productsLayout = (ViewGroup) mView.findViewById(R.id.productsLayout);
		for(int i = 0; i < productsLayout.getChildCount(); ++i) {
			final ImageView productView = (ImageView) productsLayout.getChildAt(i);
			final Product product = Product.fromCode(productView.getTag().toString().charAt(0));

			// make active products blue
			if(mProducts.contains(product)) {
				productView.getDrawable().setColorFilter(getResources().getColor(R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
			} else {
				productView.getDrawable().setColorFilter(null);
			}

			// handle click on product icon
			productView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(mProducts.contains(product)) {
						productView.getDrawable().setColorFilter(null);
						mProducts.remove(product);
					} else {
						productView.getDrawable().setColorFilter(getResources().getColor(R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
						mProducts.add(product);
					}
				}
			});
		}

		Button searchButton = (Button) mView.findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				// check and set from location
				if(mGpsPressed) {
					if(getFrom() != null) {
						query_trips.setFrom(getFrom());
					} else {
						mAfterGpsTask = query_trips;

						pd = new ProgressDialog(getActivity());
						pd.setMessage(getResources().getString(R.string.stations_searching_position));
						pd.setCancelable(false);
						pd.setIndeterminate(true);
						pd.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								mAfterGpsTask = null;
								dialog.dismiss();
							}
						});
						pd.show();
					}
				} else {
					if(checkLocation(FavLocation.LOC_TYPE.FROM, (AutoCompleteTextView) mView.findViewById(R.id.from))) {
						query_trips.setFrom(getFrom());
					} else {
						Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_from), Toast.LENGTH_SHORT).show();
						return;
					}
				}

				// check and set to location
				if(checkLocation(FavLocation.LOC_TYPE.TO, (AutoCompleteTextView) mView.findViewById(R.id.to))) {
					query_trips.setTo(getTo());
				}
				else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), Toast.LENGTH_SHORT).show();
					return;
				}

				// remember trip if not from GPS
				if(!mGpsPressed) {
					FavFile.updateFavTrip(getActivity(), new FavTrip(getFrom(), getTo()));
				}

				// set date
				query_trips.setDate(DateUtils.mergeDateTime(getActivity(), dateView.getText(), timeView.getText()));

				// set departure to true of first item is selected in spinner
				query_trips.setDeparture(spinner.getSelectedItem().equals(spinner.getItemAtPosition(0)));

				// set products
				query_trips.setProducts(mProducts);

				// don't execute if we still have to wait for GPS position
				if(mAfterGpsTask != null) return;

				query_trips.execute();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		mAfterGpsTask = null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.directions, menu);

		View moreLayout = mView.findViewById(R.id.moreLayout);
		if(moreLayout.getVisibility() == View.GONE) {
			menu.findItem(R.id.action_navigation_expand).setIcon(R.drawable.ic_action_navigation_expand);
		} else {
			menu.findItem(R.id.action_navigation_expand).setIcon(R.drawable.ic_action_navigation_collapse);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_navigation_expand:
				View moreLayout = mView.findViewById(R.id.moreLayout);
				if(moreLayout.getVisibility() == View.GONE) {
					moreLayout.setVisibility(View.VISIBLE);
					item.setIcon(R.drawable.ic_action_navigation_collapse);
				} else {
					moreLayout.setVisibility(View.GONE);
					item.setIcon(R.drawable.ic_action_navigation_expand);
				}

				return true;
			case R.id.action_swap_locations:
				Location tmp = getFrom();
				setFrom(getTo());
				setTo(tmp);

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
			mView.findViewById(R.id.fromClearButton).setVisibility(View.GONE);
			setFrom(null);

			((AutoCompleteTextView) mView.findViewById(R.id.to)).setText("");
			mView.findViewById(R.id.toClearButton).setVisibility(View.GONE);
			setTo(null);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after new home location was selected, put it right into the input field
		if(resultCode == FragmentActivity.RESULT_OK && requestCode == MainActivity.CHANGED_HOME) {
			if(mHomeClicked == FavLocation.LOC_TYPE.FROM) {
				setFrom(FavFile.getHome(getActivity()));
			}
			else if(mHomeClicked == FavLocation.LOC_TYPE.TO) {
				setTo(FavFile.getHome(getActivity()));
			}
		}
	}

	private void setFromUI() {
		// Home Button
		ImageButton fromHomeButton = (ImageButton) mView.findViewById(R.id.fromHomeButton);
		fromHomeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Location home = FavFile.getHome(getActivity());

				if(home != null) {
					setFrom(home);
				}
				else {
					startSetHome(true, FavLocation.LOC_TYPE.FROM);
				}
				cancelGpsButton();
			}
		});
		// Home Button Long Click
		fromHomeButton.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				startSetHome(false, FavLocation.LOC_TYPE.FROM);
				cancelGpsButton();

				return true;
			}
		});

		// From text input
		final AutoCompleteTextView from = (AutoCompleteTextView) mView.findViewById(R.id.from);
		from.setAdapter(new LocationAdapter(getActivity(), R.layout.list_item));
		from.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				setFrom((Location) parent.getItemAtPosition(position));
				from.requestFocus();
			}
		});

		// clear from text button
		final ImageButton fromClearButton = (ImageButton) mView.findViewById(R.id.fromClearButton);
		fromClearButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				from.setText("");
				from.requestFocus();
				setFrom(null);
				fromClearButton.setVisibility(View.GONE);
			}
		});

		// GPS Button
		final ImageButton fromGpsButton = (ImageButton) getView().findViewById(R.id.fromGpsButton);
		fromGpsButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if(mGpsPressed) {
					cancelGpsButton();
				} else {
					// clear from text
					from.setText(null);
					setFrom(null);
					fromClearButton.setVisibility(View.GONE);

					// focus to text
					AutoCompleteTextView to = (AutoCompleteTextView) mView.findViewById(R.id.to);
					to.requestFocus();

					pressGpsButton();

					fromGpsButton.getDrawable().setColorFilter(getResources().getColor(R.color.holo_blue_light), PorterDuff.Mode.SRC_ATOP);
				}
			}
		});

		// From text input changed
		from.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear location
				setFrom(null);

				// show clear button
				if(s.length() > 0) {
					fromClearButton.setVisibility(View.VISIBLE);
				} else {
					fromClearButton.setVisibility(View.GONE);
				}

				cancelGpsButton();
			}
			public void afterTextChanged(Editable s) {}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		});
		((View) mView.findViewById(R.id.fromFavButton)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				fromFavClick(v);
				cancelGpsButton();
			}
		});
	}

	private void setToUI() {
		// Home Button
		ImageButton toHomeButton = (ImageButton) mView.findViewById(R.id.toHomeButton);
		toHomeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Location home = FavFile.getHome(getActivity());

				if(home != null) {
					setTo(home);
				}
				else {
					startSetHome(true, FavLocation.LOC_TYPE.TO);
				}
			}
		});
		// Home Button Long Click
		toHomeButton.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View v) {
				startSetHome(false, FavLocation.LOC_TYPE.TO);

				return true;
			}
		});

		// To text input
		final AutoCompleteTextView to = (AutoCompleteTextView) mView.findViewById(R.id.to);
		to.setAdapter(new LocationAdapter(getActivity(), R.layout.list_item));
		to.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				setTo((Location) parent.getItemAtPosition(position));
				to.requestFocus();
			}
		});

		// clear from text button
		final ImageButton toClearButton = (ImageButton) mView.findViewById(R.id.toClearButton);
		toClearButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				to.setText("");
				to.requestFocus();
				setTo(null);
				toClearButton.setVisibility(View.GONE);
			}
		});

		// To text input changed
		to.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// clear location
				setTo(null);

				// show clear button
				if(s.length() > 0) {
					toClearButton.setVisibility(View.VISIBLE);
				} else {
					toClearButton.setVisibility(View.GONE);
				}
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

	private void startSetHome(boolean new_home, FavLocation.LOC_TYPE home_clicked) {
		Intent intent = new Intent(getActivity(), SetHomeActivity.class);
		intent.putExtra("new", new_home);

		mHomeClicked = home_clicked;

		startActivityForResult(intent, MainActivity.CHANGED_HOME);
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
		int size = ((LocationAdapter) from.getAdapter()).addFavs(FavLocation.LOC_TYPE.FROM);

		if(size > 0) {
			from.showDropDown();
		}
		else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_no_favs), Toast.LENGTH_SHORT).show();
		}
	}

	public void toFavClick(View v) {
		AutoCompleteTextView to = ((AutoCompleteTextView) mView.findViewById(R.id.to));
		int size = ((LocationAdapter) to.getAdapter()).addFavs(FavLocation.LOC_TYPE.TO);

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
			((LocationAdapter) from.getAdapter()).clearFavs();

			AutoCompleteTextView to = ((AutoCompleteTextView) mView.findViewById(R.id.to));
			((LocationAdapter) to.getAdapter()).clearFavs();
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
					setFrom(new Location(LocationType.ANY, null, view.getText().toString(), view.getText().toString()));
				}
				else if(loc_type == FavLocation.LOC_TYPE.TO) {
					setTo(new Location(LocationType.ANY, null, view.getText().toString(), view.getText().toString()));
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


	private void pressGpsButton() {
		mGpsPressed = true;

		List<String> providers = locationManager.getProviders(true);

		for(String provider : providers) {
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestSingleUpdate(provider, this, null);

			Log.d(getClass().getSimpleName(), "Register provider for location updates: " + provider);
		}

		// check if there is a non-passive provider available
		if(providers.size() == 0 || (providers.size() == 1 && providers.get(0).equals(LocationManager.PASSIVE_PROVIDER)) ) {
			removeUpdates();
			Toast.makeText(getActivity(), getResources().getString(R.string.error_no_location_provider), Toast.LENGTH_LONG).show();

			return;
		}

		gps_loc = null;
	}

	private void cancelGpsButton() {
		mGpsPressed = false;

		ImageButton fromGpsButton = (ImageButton) mView.findViewById(R.id.fromGpsButton);
		fromGpsButton.getDrawable().setColorFilter(null);

		removeUpdates();
	}

	private void removeUpdates() {
		locationManager.removeUpdates(this);
	}

	// Called when a new location is found by the network location provider.
	public void onLocationChanged(android.location.Location location) {
		// no more updates to prevent this method from being called more than once
		removeUpdates();

		// only execute if we still do not have a location to make super sure this is not run twice
		if(gps_loc == null) {
			Log.d(getClass().getSimpleName(), "Found location: " + location.toString());

			int lat = (int) Math.round(location.getLatitude() * 1E6);
			int lon = (int) Math.round(location.getLongitude() * 1E6);

			String lat_str = String.valueOf(location.getLatitude());
			if(lat_str.length() > 9) lat_str = lat_str.substring(0, 8);
			String lon_str = String.valueOf(location.getLongitude());
			if(lon_str.length() > 9) lon_str = lon_str.substring(0, 8);

			// create location based on GPS coordinates
			gps_loc = new Location(LocationType.ADDRESS, null, lat, lon, null, lat_str + "/" + lon_str);
			setFrom(gps_loc);

			if(pd != null) {
				pd.dismiss();
			}

			// query for trips if user pressed search already and we just have been waiting for the location
			if(mAfterGpsTask != null) {
				mAfterGpsTask.setFrom(gps_loc);
				mAfterGpsTask.execute();
			}
		}
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}


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

	private void addToTime(int min) {
		TextView timeView = (TextView) getActivity().findViewById(R.id.timeView);
		Calendar c = Calendar.getInstance();

		Date date = DateUtils.parseTime(getActivity().getApplicationContext(), timeView.getText());

		if(date != null) {
			c.setTime(date);
		} else {
			// if time couldn't be parsed, use current time
			c.setTime(new Date());
		}

		// add min minutes
		c.add(Calendar.MINUTE, min);

		// FIXME adapt also date if necessary or show warning about date

		timeView.setText(DateUtils.getTime(getActivity(), c));
	}

}

