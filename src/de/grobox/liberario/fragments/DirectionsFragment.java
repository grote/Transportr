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

package de.grobox.liberario.fragments;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.tasks.AsyncQueryTripsTask;
import de.grobox.liberario.FavLocation;
import de.grobox.liberario.FavTrip;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.ui.DelayAutoCompleteTextView;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

public class DirectionsFragment extends LiberarioFragment implements LocationListener {
	private View mView;
	private ViewHolder ui = new ViewHolder();
	private FavLocation.LOC_TYPE mHomeClicked;
	private LocationManager locationManager;
	private Location gps_loc = null;
	private boolean mGpsPressed = false;
	private AsyncQueryTripsTask mAfterGpsTask = null;
	private Set<Product> mProducts = EnumSet.allOf(Product.class);
	public ProgressDialog pd;
	private LocationInputView from;
	private LocationInputView to;

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

		TransportNetwork network = Preferences.getTransportNetwork(getActivity());
		if(network != null) {
			((MaterialNavigationDrawer) getActivity()).getToolbar().setSubtitle(network.getName());
		}

		populateViewHolders();

		from = new FromInputView(getActivity(), ui.from);
		to = new ToInputView(getActivity(), ui.to);

		// timeView
		ui.time.setText(DateUtils.getcurrentTime(getActivity()));
		ui.time.setTag(Calendar.getInstance());
		ui.time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog();
			}
		});

		// set current time on long click
		ui.time.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				ui.time.setText(DateUtils.getcurrentTime(getActivity()));
				ui.time.setTag(Calendar.getInstance());
				return true;
			}
		});

		ui.plus15.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addToTime(15);
			}
		});
		ui.plus15.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				addToTime(60);
				return true;
			}
		});

		// dateView
		ui.date.setText(DateUtils.getcurrentDate(getActivity()));
		ui.date.setTag(Calendar.getInstance());
		ui.date.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog();
			}
		});

		// set current date on long click
		ui.date.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				ui.date.setText(DateUtils.getcurrentDate(getActivity()));
				ui.date.setTag(Calendar.getInstance());
				return true;
			}
		});

		// Set Type to Departure=True
		ui.type.setTag(true);

		// Trip Date Type Spinner (departure or arrival)
		ui.type.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if((boolean) ui.type.getTag()) {
					// departure is set, so set arrival now
					ui.type.setText(getString(R.string.trip_arr));
					ui.type.setTag(false);
				} else {
					// departure is not set, so set it now
					ui.type.setText(getString(R.string.trip_dep));
					ui.type.setTag(true);
				}
			}
		});

		// Products
		for(int i = 0; i < ui.productsLayout.getChildCount(); ++i) {
			final ImageView productView = (ImageView) ui.productsLayout.getChildAt(i);
			final Product product = Product.fromCode(productView.getTag().toString().charAt(0));

			// make inactive products gray
			if(mProducts.contains(product)) {
				productView.getDrawable().setColorFilter(null);
			} else {
				productView.getDrawable().setColorFilter(getResources().getColor(R.color.highlight), PorterDuff.Mode.SRC_ATOP);
			}

			// handle click on product icon
			productView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(mProducts.contains(product)) {
						productView.getDrawable().setColorFilter(getResources().getColor(R.color.highlight), PorterDuff.Mode.SRC_ATOP);
						mProducts.remove(product);
						Toast.makeText(v.getContext(), LiberarioUtils.productToString(v.getContext(), product), Toast.LENGTH_SHORT).show();
					} else {
						productView.getDrawable().setColorFilter(null);
						mProducts.add(product);
						Toast.makeText(v.getContext(), LiberarioUtils.productToString(v.getContext(), product), Toast.LENGTH_SHORT).show();
					}
				}
			});

			// handle long click on product icon by showing product name
			productView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					Toast.makeText(view.getContext(), LiberarioUtils.productToString(view.getContext(), product), Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		}

		if(!Preferences.getPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, false)) {
			// don't animate here, since this method is called on each fragment change from the drawer
			showLess(false);
		}

		ui.search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getActivity()));
				if(!np.hasCapabilities(NetworkProvider.Capability.TRIPS)) {
					Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_no_trips_capability), Toast.LENGTH_SHORT).show();
					return;
				}

				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				// check and set to location
				if(checkLocation(to)) {
					query_trips.setTo(to.getLocation());
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), Toast.LENGTH_SHORT).show();
					return;
				}

				// check and set from location
				if(mGpsPressed) {
					if(from.getLocation() != null) {
						query_trips.setFrom(from.getLocation());
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
					if(checkLocation(from)) {
						query_trips.setFrom(from.getLocation());
					} else {
						Toast.makeText(getActivity(), getString(R.string.error_invalid_from), Toast.LENGTH_SHORT).show();
						return;
					}
				}

				// remember trip
				FavDB.updateFavTrip(getActivity(), new FavTrip(from.getLocation(), to.getLocation()));

				// set date
				query_trips.setDate(DateUtils.mergeDateTime(getActivity(), ui.date.getText(), ui.time.getText()));

				// set departure to true of first item is selected in spinner
				query_trips.setDeparture((boolean) ui.type.getTag());

				// set products
				query_trips.setProducts(mProducts);

				// don't execute if we still have to wait for GPS position
				if(mAfterGpsTask != null) return;

				query_trips.execute();
			}
		});

		ui.whatHere.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "t+liberario@grobox.de", null));
				intent.putExtra(Intent.EXTRA_SUBJECT, "[Liberario] Below Directions Form");
				intent.putExtra(Intent.EXTRA_TEXT, "Hi,\nI like to see");
				startActivity(Intent.createChooser(intent, "Send Email"));
			}
		});

		return mView;
	}

	@Override
	public void onStart() {
		super.onStart();
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

		if(ui.productsScrollView.getVisibility() == View.GONE) {
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
				if(ui.productsScrollView.getVisibility() == View.GONE) {
					item.setIcon(R.drawable.ic_action_navigation_collapse);
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, true);
					showMore(true);
				} else {
					item.setIcon(R.drawable.ic_action_navigation_expand);
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, false);
					showLess(true);
				}

				return true;
			case R.id.action_swap_locations:
				// swap location objects and drawables
				final Drawable icon = ui.to.status.getDrawable();
				Location tmp = to.getLocation();
				if(!mGpsPressed) {
					to.setLocation(from.getLocation(), ui.from.status.getDrawable());
				} else {
					// TODO: GPS currently only supports from location, so don't swap it for now
					to.clearLocation();
				}
				from.setLocation(tmp, icon);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	// change things for a different network provider
	public void onNetworkProviderChanged(TransportNetwork network) {
		refreshFavs();

		// remove old text from TextViews
		if(mView != null) {
			ui.from.clear.setVisibility(View.GONE);
			from.clearLocation();

			ui.to.clear.setVisibility(View.GONE);
			to.clearLocation();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after new home location was selected, put it right into the input field
		if(resultCode == AppCompatActivity.RESULT_OK && requestCode == MainActivity.CHANGED_HOME) {
			if(mHomeClicked.equals(FavLocation.LOC_TYPE.FROM)) {
				from.setLocation(FavDB.getHome(getActivity()), getResources().getDrawable(R.drawable.ic_action_home));
			} else if(mHomeClicked.equals(FavLocation.LOC_TYPE.TO)) {
				to.setLocation(FavDB.getHome(getActivity()), getResources().getDrawable(R.drawable.ic_action_home));
			}
		}
	}

	private void startSetHome(boolean new_home, FavLocation.LOC_TYPE home_clicked) {
		Intent intent = new Intent(getActivity(), SetHomeActivity.class);
		intent.putExtra("new", new_home);

		mHomeClicked = home_clicked;

		startActivityForResult(intent, MainActivity.CHANGED_HOME);
	}

	public void refreshFavs() {
		if(ui.from != null) ((LocationAdapter) ui.from.location.getAdapter()).resetList();
		if(ui.to != null) ((LocationAdapter) ui.to.location.getAdapter()).resetList();
	}

	private Boolean checkLocation(LocationInputView loc_view) {
		Location loc = loc_view.getLocation();

		if(loc == null) {
			// no location was selected by user
			if(!loc_view.holder.location.getText().toString().equals("")) {
				// no location selected, but text entered. So let's try create locations from text
				loc_view.setLocation(new Location(LocationType.ANY, null, loc_view.holder.location.getText().toString(), loc_view.holder.location.getText().toString()), null);

				return true;
			}
			return false;
		}
		// we have a location, so make it a favorite
		else {
			FavDB.updateFavLocation(getActivity(), loc, loc_view.getType());
		}

		return true;
	}

	private void handleLocationItemClick(Location loc, FavLocation.LOC_TYPE type, View view) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();

		if(loc.id != null && loc.id.equals("Liberario.GPS")) {
			if(mGpsPressed) {
				cancelGpsButton();
			}
			else {
				// clear from text
				ui.from.location.setText(null);
				from.setLocation(null, icon);
				ui.from.clear.setVisibility(View.VISIBLE);

				ui.to.location.requestFocus();

				pressGpsButton();
			}
		}
		else {
			// home location
			if (loc.id != null && loc.id.equals("Liberario.HOME")) {
				Location home = FavDB.getHome(getActivity());

				if(home != null) {
					if(type.equals(FavLocation.LOC_TYPE.FROM)) {
						from.setLocation(home, icon);
					} else if(type.equals(FavLocation.LOC_TYPE.TO)) {
						to.setLocation(home, icon);
					}
				} else {
					// prevent home.toString() from being shown in the TextView
					if (type.equals(FavLocation.LOC_TYPE.FROM)) {
						ui.from.location.setText("");
					} else {
						ui.to.location.setText("");
					}
					// show dialog to set home screen
					startSetHome(true, type);
				}
			}
			// locations from favorites or auto-complete
			else {
				if(type.equals(FavLocation.LOC_TYPE.FROM)) {
					from.setLocation(loc, icon);
				}  else if(type.equals(FavLocation.LOC_TYPE.TO)) {
					to.setLocation(loc, icon);
				}
			}

			// prepare to hide soft-keyboard
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

			if(type.equals(FavLocation.LOC_TYPE.FROM)) {
				// cancel GPS Button if different from location was clicked
				cancelGpsButton();
				imm.hideSoftInputFromWindow(ui.from.location.getWindowToken(), 0);
				ui.to.location.requestFocus();
			} else {
				imm.hideSoftInputFromWindow(ui.to.location.getWindowToken(), 0);
			}
		}
	}

	private void showMore(boolean animate) {
		ui.productsScrollView.setVisibility(View.VISIBLE);
		ui.whatHere.setVisibility(View.VISIBLE);

		if(animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);

			final int distance = dm.widthPixels;
			final int slide = distance / 10;

			ui.productsScrollView.setTranslationX(distance);
			ui.productsScrollView.animate().setDuration(750).translationXBy(-1 * distance - slide).withEndAction(new Runnable() {
				@Override
				public void run() {
					ui.productsScrollView.animate().setDuration(250).translationXBy(slide);
				}
			});

			ui.whatHere.setAlpha(0f);
			ui.whatHere.animate().setDuration(750).alpha(1f);
		}
	}

	private void showLess(boolean animate) {
		if(animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			ui.whatHere.setAlpha(1f);
			ui.whatHere.animate().setDuration(500).alpha(0f).withEndAction(new Runnable() {
				@Override
				public void run() {
					ui.productsScrollView.setVisibility(View.GONE);
					ui.whatHere.setVisibility(View.GONE);
				}
			});
		}
		else {
			ui.productsScrollView.setVisibility(View.GONE);
			ui.whatHere.setVisibility(View.GONE);
		}
	}

	private void pressGpsButton() {
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

		// show GPS button blinking
		final Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(500);
		animation.setInterpolator(new LinearInterpolator());
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		ui.from.status.setAnimation(animation);

		mGpsPressed = true;
		gps_loc = null;
	}

	private void cancelGpsButton() {
		mGpsPressed = false;

		// deactivate button
		ui.from.status.clearAnimation();

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
			gps_loc = new Location(LocationType.ADDRESS, null, lat, lon, "GPS", lat_str + "/" + lon_str);
			from.setLocation(gps_loc, getResources().getDrawable(R.drawable.ic_gps));

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


	public void showTimePickerDialog() {
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
	}

	public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Button timeView = (Button) getActivity().findViewById(R.id.timeView);
			Calendar c = (Calendar) timeView.getTag();

			// set time for picker
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute, android.text.format.DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			Button timeView = (Button) getActivity().findViewById(R.id.timeView);
			timeView.setText(DateUtils.formatTime(getActivity().getApplicationContext(), hourOfDay, minute));

			// store Calendar instance with Button
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, hourOfDay);
			c.set(Calendar.MINUTE, minute);
			timeView.setTag(c);
		}
	}

	public void showDatePickerDialog() {
		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
	}

	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Button dateView = (Button) getActivity().findViewById(R.id.dateView);
			Calendar c = (Calendar) dateView.getTag();

			// set date for picker
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			Button dateView = (Button) getActivity().findViewById(R.id.dateView);
			dateView.setText(DateUtils.formatDate(getActivity().getApplicationContext(), year, month, day));

			// store Calendar instance with Button
			Calendar c = Calendar.getInstance();
			c.set(Calendar.YEAR, year);
			c.set(Calendar.MONTH, month);
			c.set(Calendar.DAY_OF_MONTH, day);
			dateView.setTag(c);
		}
	}

	private void addToTime(int min) {
		Button timeView = (Button) getActivity().findViewById(R.id.timeView);
		Button dateView = (Button) getActivity().findViewById(R.id.dateView);
		Calendar c = (Calendar) timeView.getTag();
		Calendar c_date = (Calendar) dateView.getTag();

		// set the date to the calendar, so it can calculate a day overflow
		c.set(Calendar.YEAR, c_date.get(Calendar.YEAR));
		c.set(Calendar.MONTH, c_date.get(Calendar.MONTH));
		c.set(Calendar.DAY_OF_MONTH, c_date.get(Calendar.DAY_OF_MONTH));

		// add min minutes
		c.add(Calendar.MINUTE, min);

		timeView.setText(DateUtils.getTime(getActivity(), c));
		timeView.setTag(c);
		dateView.setText(DateUtils.getDate(getActivity(), c.getTime()));
		dateView.setTag(c);
	}

	class FromInputView extends LocationInputView {
		public FromInputView(Context context, LocationInputViewHolder holder) {
			super(context, holder);
			setType(FavLocation.LOC_TYPE.FROM);
			setHome(true);
			setFavs(true);
			setGPS(true);

			holder.location.setHint(R.string.from);
		}

		@Override
		public void onLocationItemClick(Location loc, View view) {
			handleLocationItemClick(loc, FavLocation.LOC_TYPE.FROM, view);
		}

		public void handleTextChanged(CharSequence s) {
			super.handleTextChanged(s);

			cancelGpsButton();
		}
	}

	class ToInputView extends LocationInputView {
		public ToInputView(Context context, LocationInputViewHolder holder) {
			super(context, holder);
			setType(FavLocation.LOC_TYPE.TO);
			setHome(true);
			setFavs(true);

			holder.location.setHint(R.string.to);
		}

		@Override
		public void onLocationItemClick(Location loc, View view) {
			handleLocationItemClick(loc, FavLocation.LOC_TYPE.TO, view);
		}
	}

	static class ViewHolder {
		ViewGroup fromLocation;
		LocationInputView.LocationInputViewHolder from;
		ViewGroup toLocation;
		LocationInputView.LocationInputViewHolder to;
		Button type;
		Button time;
		Button plus15;
		Button date;
		HorizontalScrollView productsScrollView;
		ViewGroup productsLayout;
		ImageView high_speed_train;
		ImageView regional_train;
		ImageView suburban_train;
		ImageView subway;
		ImageView tram;
		ImageView bus;
		ImageView on_demand;
		ImageView ferry;
		ImageView cablecar;
		Button search;
		View whatHere;
	}

	private void populateViewHolders() {
		ui.fromLocation = (ViewGroup) mView.findViewById(R.id.fromLocation);
		ui.from = new LocationInputView.LocationInputViewHolder();
		ui.from.status = (ImageView) ui.fromLocation.findViewById(R.id.statusButton);
		ui.from.location = (DelayAutoCompleteTextView) ui.fromLocation.findViewById(R.id.location);
		ui.from.progress = (ProgressBar) ui.fromLocation.findViewById(R.id.progress);
		ui.from.clear = (ImageButton) ui.fromLocation.findViewById(R.id.clearButton);

		ui.toLocation = (ViewGroup) mView.findViewById(R.id.toLocation);
		ui.to = new LocationInputView.LocationInputViewHolder();
		ui.to.status = (ImageView) ui.toLocation.findViewById(R.id.statusButton);
		ui.to.location = (DelayAutoCompleteTextView) ui.toLocation.findViewById(R.id.location);
		ui.to.progress = (ProgressBar) ui.toLocation.findViewById(R.id.progress);
		ui.to.clear = (ImageButton) ui.toLocation.findViewById(R.id.clearButton);

		ui.type = (Button) mView.findViewById(R.id.dateType);
		ui.time = (Button) mView.findViewById(R.id.timeView);
		ui.plus15 = (Button) mView.findViewById(R.id.plus15Button);
		ui.date = (Button) mView.findViewById(R.id.dateView);

		ui.productsScrollView = (HorizontalScrollView) mView.findViewById(R.id.productsScrollView);
		ui.productsLayout = (ViewGroup) mView.findViewById(R.id.productsLayout);
		ui.high_speed_train = (ImageView) mView.findViewById(R.id.ic_product_high_speed_train);
		ui.regional_train = (ImageView) mView.findViewById(R.id.ic_product_regional_train);
		ui.suburban_train = (ImageView) mView.findViewById(R.id.ic_product_suburban_train);
		ui.subway = (ImageView) mView.findViewById(R.id.ic_product_subway);
		ui.tram = (ImageView) mView.findViewById(R.id.ic_product_tram);
		ui.bus = (ImageView) mView.findViewById(R.id.ic_product_bus);
		ui.on_demand = (ImageView) mView.findViewById(R.id.ic_product_on_demand);
		ui.ferry = (ImageView) mView.findViewById(R.id.ic_product_ferry);
		ui.cablecar = (ImageView) mView.findViewById(R.id.ic_product_cablecar);
		ui.search = (Button) mView.findViewById(R.id.searchButton);

		ui.whatHere = mView.findViewById(R.id.whatHereView);
	}
}

