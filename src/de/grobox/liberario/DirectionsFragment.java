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
import java.util.LinkedList;
import java.util.List;

import de.grobox.liberario.data.FavDB;
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
import android.graphics.drawable.Drawable;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
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

		checkPreferences();

		setFromUI();
		setToUI();

		// timeView
		final Button timeView = (Button) mView.findViewById(R.id.timeView);
		timeView.setText(DateUtils.getcurrentTime(getActivity()));
		timeView.setTag(Calendar.getInstance());
		timeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTimePickerDialog();
			}
		});

		// set current time on long click
		timeView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				timeView.setText(DateUtils.getcurrentTime(getActivity()));
				timeView.setTag(Calendar.getInstance());
				return true;
			}
		});

		Button plus10Button = (Button) mView.findViewById(R.id.plus15Button);
		plus10Button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addToTime(15);
			}
		});

		// dateView
		final Button dateView = (Button) mView.findViewById(R.id.dateView);
		dateView.setText(DateUtils.getcurrentDate(getActivity()));
		dateView.setTag(Calendar.getInstance());
		dateView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDatePickerDialog();
			}
		});

		// set current date on long click
		dateView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				dateView.setText(DateUtils.getcurrentDate(getActivity()));
				dateView.setTag(Calendar.getInstance());
				return true;
			}
		});

		// Trip Date Type Spinner (departure or arrival)
		final TextView dateType = (TextView) mView.findViewById(R.id.dateType);
		dateType.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(dateType.getText().equals(getString(R.string.trip_dep))) {
					dateType.setText(getString(R.string.trip_arr));
				} else {
					dateType.setText(getString(R.string.trip_dep));
				}
			}
		});

		// Products
		final ViewGroup productsLayout = (ViewGroup) mView.findViewById(R.id.productsLayout);
		for(int i = 0; i < productsLayout.getChildCount(); ++i) {
			final ImageView productView = (ImageView) productsLayout.getChildAt(i);
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

		if(!Preferences.getPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS)) {
			(mView.findViewById(R.id.productsScrollView)).setVisibility(View.GONE);
		}

		Button searchButton = (Button) mView.findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getActivity()));
				if(!np.hasCapabilities(NetworkProvider.Capability.TRIPS)) {
					Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_no_trips_capability), Toast.LENGTH_SHORT).show();
					return;
				}

				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				// check and set to location
				if(checkLocation(FavLocation.LOC_TYPE.TO)) {
					query_trips.setTo(getLocation(FavLocation.LOC_TYPE.TO));
				}
				else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), Toast.LENGTH_SHORT).show();
					return;
				}

				// check and set from location
				if(mGpsPressed) {
					if(getLocation(FavLocation.LOC_TYPE.FROM) != null) {
						query_trips.setFrom(getLocation(FavLocation.LOC_TYPE.FROM));
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
					if(checkLocation(FavLocation.LOC_TYPE.FROM)) {
						query_trips.setFrom(getLocation(FavLocation.LOC_TYPE.FROM));
					} else {
						Toast.makeText(getActivity(), getString(R.string.error_invalid_from), Toast.LENGTH_SHORT).show();
						return;
					}
				}

				// remember trip if not from GPS
				if(!mGpsPressed) {
					FavDB.updateFavTrip(getActivity(), new FavTrip(getLocation(FavLocation.LOC_TYPE.FROM), getLocation(FavLocation.LOC_TYPE.TO)));
				}

				// set date
				query_trips.setDate(DateUtils.mergeDateTime(getActivity(), dateView.getText(), timeView.getText()));

				// set departure to true of first item is selected in spinner
				query_trips.setDeparture(dateType.getText().equals(getString(R.string.trip_dep)));

				// set products
				query_trips.setProducts(mProducts);

				// don't execute if we still have to wait for GPS position
				if(mAfterGpsTask != null) return;

				query_trips.execute();
			}
		});

		return mView;
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

		View productsScrollView = mView.findViewById(R.id.productsScrollView);
		if(productsScrollView.getVisibility() == View.GONE) {
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
				View productsScrollView = mView.findViewById(R.id.productsScrollView);
				if(productsScrollView.getVisibility() == View.GONE) {
					productsScrollView.setVisibility(View.VISIBLE);
					item.setIcon(R.drawable.ic_action_navigation_collapse);
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, true);
				} else {
					productsScrollView.setVisibility(View.GONE);
					item.setIcon(R.drawable.ic_action_navigation_expand);
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, false);
				}

				return true;
			case R.id.action_swap_locations:
				// get location icons to be swapped as well
				final ImageView fromStatusButton = (ImageView) mView.findViewById(R.id.fromStatusButton);
				final Drawable icon = ((ImageView) mView.findViewById(R.id.toStatusButton)).getDrawable();

				// swap location objects and drawables
				Location tmp = getLocation(FavLocation.LOC_TYPE.TO);
				if(!mGpsPressed) {
					setLocation(getLocation(FavLocation.LOC_TYPE.FROM), FavLocation.LOC_TYPE.TO, fromStatusButton.getDrawable());
				} else {
					// GPS currently only supports from location, so don't swap it
					clearLocation(FavLocation.LOC_TYPE.TO);
				}
				setLocation(tmp, FavLocation.LOC_TYPE.FROM, icon);

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
			mView.findViewById(R.id.fromClearButton).setVisibility(View.GONE);
			clearLocation(FavLocation.LOC_TYPE.FROM);

			mView.findViewById(R.id.toClearButton).setVisibility(View.GONE);
			clearLocation(FavLocation.LOC_TYPE.TO);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after new home location was selected, put it right into the input field
		if(resultCode == FragmentActivity.RESULT_OK && requestCode == MainActivity.CHANGED_HOME) {
			setLocation(FavDB.getHome(getActivity()), mHomeClicked, getResources().getDrawable(R.drawable.ic_action_home));
		}
	}

	private void setFromUI() {
		// From text input
		final AutoCompleteTextView from = (AutoCompleteTextView) mView.findViewById(R.id.from);
		final TextView fromText = (TextView) mView.findViewById(R.id.fromText);

		OnClickListener fromListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(from.getText().length() > 0) {
					from.showDropDown();
				} else {
					handleInputClick(FavLocation.LOC_TYPE.FROM);
				}
			}
		};

		from.setOnClickListener(fromListener);
		fromText.setOnClickListener(fromListener);

		// From Location List for Dropdown
		final LocationAdapter locAdapter = new LocationAdapter(getActivity(), FavLocation.LOC_TYPE.FROM);
		locAdapter.setFavs(true);
		locAdapter.setHome(true);
		locAdapter.setGPS(true);
		from.setAdapter(locAdapter);
		from.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				handleLocationItemClick(locAdapter.getItem(position), view, FavLocation.LOC_TYPE.FROM);
			}
		});

		// TODO itemLongClickListener to change homeLocation

		final ImageView fromStatusButton = (ImageView) mView.findViewById(R.id.fromStatusButton);
		fromStatusButton.setImageDrawable(null);
		fromStatusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleInputClick(FavLocation.LOC_TYPE.FROM);
			}
		});

		// clear from text button
		final ImageButton fromClearButton = (ImageButton) mView.findViewById(R.id.fromClearButton);
		fromClearButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				from.requestFocus();
				clearLocation(FavLocation.LOC_TYPE.FROM);
				fromClearButton.setVisibility(View.GONE);
			}
		});

		// From text input changed
		from.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// show clear button
				if(s.length() > 0) {
					fromClearButton.setVisibility(View.VISIBLE);
					// clear location
					setLocation(null, FavLocation.LOC_TYPE.FROM, null, false);
				} else {
					fromClearButton.setVisibility(View.GONE);
					clearLocation(FavLocation.LOC_TYPE.FROM);
					// clear drop-down list
					locAdapter.resetList();
				}

				cancelGpsButton();
			}

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
	}

	private void setToUI() {
		// To text input
		final AutoCompleteTextView to = (AutoCompleteTextView) mView.findViewById(R.id.to);
		final TextView toText = (TextView) mView.findViewById(R.id.toText);

		OnClickListener toListener = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(to.getText().length() > 0) {
					to.showDropDown();
				} else {
					handleInputClick(FavLocation.LOC_TYPE.TO);
				}
			}
		};

		to.setOnClickListener(toListener);
		toText.setOnClickListener(toListener);

		// To Location List for Dropdown
		final LocationAdapter locAdapter = new LocationAdapter(getActivity(), FavLocation.LOC_TYPE.TO);
		locAdapter.setFavs(true);
		locAdapter.setHome(true);
		to.setAdapter(locAdapter);
		to.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				handleLocationItemClick(locAdapter.getItem(position), view, FavLocation.LOC_TYPE.TO);
			}
		});

		// TODO implement something to allow change of homeLocation

		final ImageView toStatusButton = (ImageView) mView.findViewById(R.id.toStatusButton);
		toStatusButton.setImageDrawable(null);
		toStatusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleInputClick(FavLocation.LOC_TYPE.TO);
			}
		});

		// clear from text button
		final ImageButton toClearButton = (ImageButton) mView.findViewById(R.id.toClearButton);
		toClearButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				to.requestFocus();
				clearLocation(FavLocation.LOC_TYPE.TO);
				toClearButton.setVisibility(View.GONE);
			}
		});

		// To text input changed
		to.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// show clear button
				if(s.length() > 0) {
					toClearButton.setVisibility(View.VISIBLE);
					// clear location
					setLocation(null, FavLocation.LOC_TYPE.TO, null, false);
				} else {
					toClearButton.setVisibility(View.GONE);
					clearLocation(FavLocation.LOC_TYPE.TO);
					// clear drop-down list
					locAdapter.resetList();
				}
			}

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
	}

	private Location getLocation(FavLocation.LOC_TYPE loc_type) {
		if(loc_type.equals(FavLocation.LOC_TYPE.FROM)) {
			AutoCompleteTextView fromView = (AutoCompleteTextView) mView.findViewById(R.id.from);
			return (Location) fromView.getTag();
		} else {
			AutoCompleteTextView toView = (AutoCompleteTextView) mView.findViewById(R.id.to);
			return (Location) toView.getTag();
		}
	}

	private void setLocation(Location loc, FavLocation.LOC_TYPE loc_type, Drawable icon, boolean setText) {
		if(!mChange) {
			mChange = true;
			final ImageView statusButton;
			AutoCompleteTextView textView;

			if(loc_type.equals(FavLocation.LOC_TYPE.FROM)) {
				statusButton = (ImageView) mView.findViewById(R.id.fromStatusButton);
				textView = (AutoCompleteTextView) mView.findViewById(R.id.from);
			} else {
				statusButton = (ImageView) mView.findViewById(R.id.toStatusButton);
				textView = (AutoCompleteTextView) mView.findViewById(R.id.to);
			}

			textView.setTag(loc);

			if(loc != null) {
				if(setText) textView.setText(loc.uniqueShortName());
			} else {
				if(setText) textView.setText(null);
			}

			statusButton.setImageDrawable(icon);
			textView.dismissDropDown();

			mChange = false;
		}
	}

	private void setLocation(Location loc, FavLocation.LOC_TYPE loc_type, Drawable icon) {
		setLocation(loc, loc_type, icon, true);
	}

	private void clearLocation(FavLocation.LOC_TYPE loc_type) {
		setLocation(null, loc_type, null);
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
			editor.apply();

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

	public void handleInputClick(FavLocation.LOC_TYPE loc_type) {
		AutoCompleteTextView textView;

		if(loc_type.equals(FavLocation.LOC_TYPE.FROM)) {
			textView = ((AutoCompleteTextView) mView.findViewById(R.id.from));
		} else {
			textView = ((AutoCompleteTextView) mView.findViewById(R.id.to));
		}

		LocationAdapter locAdapter = (LocationAdapter) textView.getAdapter();
		int size = locAdapter.addFavs();

		if(size > 0) {
			textView.showDropDown();
		}
		else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_no_favs), Toast.LENGTH_SHORT).show();
		}
	}

	public void refreshFavs() {
		if(mView != null) {
			AutoCompleteTextView from = ((AutoCompleteTextView) mView.findViewById(R.id.from));
			((LocationAdapter) from.getAdapter()).resetList();

			AutoCompleteTextView to = ((AutoCompleteTextView) mView.findViewById(R.id.to));
			((LocationAdapter) to.getAdapter()).resetList();
		}
	}

	private Boolean checkLocation(FavLocation.LOC_TYPE loc_type) {
		Location loc = getLocation(loc_type);

		AutoCompleteTextView view;
		if(loc_type.equals(FavLocation.LOC_TYPE.FROM)) {
			view = (AutoCompleteTextView) mView.findViewById(R.id.from);
		} else {
			view = (AutoCompleteTextView) mView.findViewById(R.id.to);
		}

		if(loc == null) {
			// no location was selected by user
			if(!view.getText().toString().equals("")) {
				// no location selected, but text entered. So let's try create locations from text
				setLocation(new Location(LocationType.ANY, null, view.getText().toString(), view.getText().toString()), loc_type, null);

				return true;
			}
			return false;
		}
		// we have a location, so make it a favorite
		else {
			FavDB.updateFavLocation(getActivity(), loc, loc_type);
		}

		return true;
	}

	private void handleLocationItemClick(Location loc, View view, FavLocation.LOC_TYPE loc_type) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();
		AutoCompleteTextView from = (AutoCompleteTextView) mView.findViewById(R.id.from);
		AutoCompleteTextView to = (AutoCompleteTextView) mView.findViewById(R.id.to);

		if(loc.id != null && loc.id.equals("Liberario.GPS")) {
			if(mGpsPressed) {
				cancelGpsButton();
			}
			else {
				// clear from text
				from.setText(null);
				setLocation(null, FavLocation.LOC_TYPE.FROM, icon);
				ImageButton fromClearButton = (ImageButton) mView.findViewById(R.id.fromClearButton);
				fromClearButton.setVisibility(View.VISIBLE);

				to.requestFocus();

				pressGpsButton();
			}
		}
		else {
			// home location
			if (loc.id != null && loc.id.equals("Liberario.HOME")) {
				Location home = FavDB.getHome(getActivity());

				if(home != null) {
					setLocation(home, loc_type, icon);
				} else {
					// prevent home.toString() from being shown in the TextView
					if (loc_type.equals(FavLocation.LOC_TYPE.FROM)) {
						from.setText("");
					} else {
						to.setText("");
					}
					// show dialog to set home screen
					startSetHome(true, loc_type);
				}
			}
			// locations from favorites or auto-complete
			else {
				setLocation(loc, loc_type, icon);
			}

			// prepare to hide soft-keyboard
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

			if (loc_type.equals(FavLocation.LOC_TYPE.FROM)) {
				// cancel GPS Button if different from location was clicked
				cancelGpsButton();
				imm.hideSoftInputFromWindow(from.getWindowToken(), 0);
				to.requestFocus();
			} else {
				imm.hideSoftInputFromWindow(to.getWindowToken(), 0);
			}
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
		mView.findViewById(R.id.fromStatusButton).setAnimation(animation);

		mGpsPressed = true;
		gps_loc = null;
	}

	private void cancelGpsButton() {
		mGpsPressed = false;

		// deactivate button
		mView.findViewById(R.id.fromStatusButton).clearAnimation();

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
			setLocation(gps_loc, FavLocation.LOC_TYPE.FROM, getResources().getDrawable(R.drawable.ic_gps));

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

}

