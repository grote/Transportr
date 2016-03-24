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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.activities.AmbiguousLocationActivity;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.TripsActivity;
import de.grobox.liberario.adapters.FavouritesAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.tasks.AsyncQueryTripsTask;
import de.grobox.liberario.ui.LocationGpsView;
import de.grobox.liberario.ui.LocationView;
import de.grobox.liberario.ui.TimeAndDateView;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryTripsResult;

public class DirectionsFragment extends TransportrFragment implements TransportNetwork.HomeChangeInterface, AsyncQueryTripsTask.TripHandler {

	public final static String TAG = "de.grobox.liberario.directions";
	public ProgressDialog pd;

	private DirectionsViewHolder ui;
	private AsyncQueryTripsTask mAfterGpsTask = null;
	private Set<Product> mProducts = EnumSet.allOf(Product.class);
	private boolean restart = false;
	private boolean showingMore = false;
	private FavouritesAdapter mFavAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_directions, container, false);

		ui = new DirectionsViewHolder(v);

		ui.from.setType(FavLocation.LOC_TYPE.FROM);
		ui.from.setCaller(MainActivity.PR_ACCESS_FINE_LOCATION_DIRECTIONS);
		LocationGpsView.LocationGpsListener listener = new LocationGpsView.LocationGpsListener() {
			@Override
			public void activateGPS() {	}
			@Override
			public void deactivateGPS() { }
			@Override
			public void onLocationChanged(Location location) {
				if(pd != null) {
					pd.dismiss();
				}

				// query for trips if user pressed search already and we just have been waiting for the location
				if(mAfterGpsTask != null) {
					mAfterGpsTask.setFrom(location);
					mAfterGpsTask.execute();
				}
			}
		};
		ui.from.setLocationGpsListener(listener);

		ui.to.setType(FavLocation.LOC_TYPE.TO);

		// Set Type to Departure=True
		ui.type.setTag(true);

		// Trip Date Type Spinner (departure or arrival)
		ui.type.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setType(!(boolean) ui.type.getTag());
			}
		});

		// Products
		for(int i = 0; i < ui.productsLayout.getChildCount(); ++i) {
			final ImageView productView = (ImageView) ui.productsLayout.getChildAt(i);
			final Product product = Product.fromCode(productView.getTag().toString().charAt(0));

			// make inactive products gray
			if(mProducts.contains(product)) {
				productView.setColorFilter(TransportrUtils.getButtonIconColor(getActivity(), true), PorterDuff.Mode.SRC_IN);
			} else {
				productView.setColorFilter(TransportrUtils.getButtonIconColor(getActivity(), false), PorterDuff.Mode.SRC_IN);
			}

			// handle click on product icon
			productView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if(mProducts.contains(product)) {
						productView.setColorFilter(TransportrUtils.getButtonIconColor(getActivity(), false), PorterDuff.Mode.SRC_IN);
						mProducts.remove(product);
						Toast.makeText(v.getContext(), TransportrUtils.productToString(v.getContext(), product), Toast.LENGTH_SHORT).show();
					} else {
						productView.setColorFilter(TransportrUtils.getButtonIconColor(getActivity(), true), PorterDuff.Mode.SRC_IN);
						mProducts.add(product);
						Toast.makeText(v.getContext(), TransportrUtils.productToString(v.getContext(), product), Toast.LENGTH_SHORT).show();
					}
				}
			});

			// handle long click on product icon by showing product name
			productView.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					Toast.makeText(view.getContext(), TransportrUtils.productToString(view.getContext(), product), Toast.LENGTH_SHORT).show();
					return true;
				}
			});
		}

		ui.search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				search();
			}
		});

		ui.fav_trips_separator_star.setColorFilter(TransportrUtils.getButtonIconColor(getActivity()));
		ui.fav_trips_separator_line.setBackgroundColor(TransportrUtils.getButtonIconColor(getActivity()));
		ui.fav_trips_separator_star.setAlpha(0.5f);
		ui.fav_trips_separator_line.setAlpha(0.5f);

		mFavAdapter = new FavouritesAdapter(getContext());
		ui.favourites.setAdapter(mFavAdapter);
		ui.favourites.setLayoutManager(new LinearLayoutManager(getContext()));

		if(!Preferences.getPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, false)) {
			// don't animate here, since this method is called on each fragment change from the drawer
			showLess(false);
		} else {
			showingMore = true;
		}

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		restart = true;

		if(savedInstanceState != null) {
			setType(savedInstanceState.getBoolean("type"));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(ui != null) {
			outState.putBoolean("type", (boolean) ui.type.getTag());
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if(!restart && ui != null) {
			long time = ui.date.getCalendar().getTimeInMillis();
			long now = Calendar.getInstance().getTimeInMillis();

			// reset date and time if older than 10 minutes, so user doesn't search in the past by accident
			if((now - time) / (60 * 1000) > 10) {
				ui.date.reset();
			}
		} else {
			restart = false;
		}

		mAfterGpsTask = null;

		displayFavouriteTrips();
		refreshAutocomplete(false);

		// check if there's an intent for us and if so, act on it
		processIntent();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.directions, menu);

		// in some cases getActivity() and getContext() can be null, so get it somewhere else
		Context context = getContext();

		MenuItem expandItem = menu.findItem(R.id.action_navigation_expand);
		if(ui.productsScrollView.getVisibility() == View.GONE) {
			expandItem.setIcon(TransportrUtils.getToolbarDrawable(context, R.drawable.ic_action_navigation_unfold_more));
		} else {
			expandItem.setIcon(TransportrUtils.getToolbarDrawable(context, R.drawable.ic_action_navigation_unfold_less));
		}

		MenuItem swapItem = menu.findItem(R.id.action_swap_locations);
		TransportrUtils.fixToolbarIcon(context, swapItem);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_navigation_expand:
				if(ui.productsScrollView.getVisibility() == View.GONE) {
					item.setIcon(TransportrUtils.getToolbarDrawable(getContext(), R.drawable.ic_action_navigation_unfold_less));
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, true);
					showMore(true);
				} else {
					item.setIcon(TransportrUtils.getToolbarDrawable(getContext(), R.drawable.ic_action_navigation_unfold_more));
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, false);
					showLess(true);
				}

				return true;
			case R.id.action_swap_locations:
				swapLocations();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onHomeChanged() {
		ui.from.onHomeChanged();
		ui.to.onHomeChanged();
	}

	@Override
	public void onTripRetrieved(QueryTripsResult result) {
		if(result.status == QueryTripsResult.Status.OK && result.trips != null && result.trips.size() > 0) {
			Log.d(getClass().getSimpleName(), result.toString());

			Intent intent = new Intent(getContext(), TripsActivity.class);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult", result);
			fillIntent(intent);
			startActivity(intent);
		}
		else if(result.status == QueryTripsResult.Status.AMBIGUOUS) {
			Log.d(getClass().getSimpleName(), "QueryTripsResult is AMBIGUOUS");

			Intent intent = new Intent(getContext(), AmbiguousLocationActivity.class);
			intent.putExtra("de.schildbach.pte.dto.QueryTripsResult", result);
			fillIntent(intent);
			startActivity(intent);
		}
		else {
			Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_no_trips_found), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onTripRetrievalError(String error) { }

	private void displayFavouriteTrips() {
		if(mFavAdapter == null) return;

		mFavAdapter.clear();
		mFavAdapter.addAll(RecentsDB.getFavouriteTripList(getContext()));

		if(showingMore && mFavAdapter.getItemCount() == 0) {
			ui.no_favourites.setVisibility(View.VISIBLE);
		} else {
			ui.no_favourites.setVisibility(View.GONE);
		}

		if(showingMore || mFavAdapter.getItemCount() != 0) {
			ui.fav_trips_separator.setVisibility(View.VISIBLE);
			ui.fav_trips_separator.setAlpha(1f);
		} else {
			ui.fav_trips_separator.setVisibility(View.GONE);
		}
	}

	private void search() {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getActivity()));
		if(!np.hasCapabilities(NetworkProvider.Capability.TRIPS)) {
			Toast.makeText(getActivity(), getString(R.string.error_no_trips_capability), Toast.LENGTH_SHORT).show();
			return;
		}

		if(ui.to == null && ui.from == null) {
			// the activity is most likely being recreated after configuration change, don't search again
			return;
		}

		AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(getActivity(), this);

		// check and set to location
		if(checkLocation(ui.to)) {
			query_trips.setTo(ui.to.getLocation());
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), Toast.LENGTH_SHORT).show();
			return;
		}

		// check and set from location
		if(ui.from.isSearching()) {
			if(ui.from.getLocation() != null) {
				query_trips.setFrom(ui.from.getLocation());
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
			if(checkLocation(ui.from)) {
				query_trips.setFrom(ui.from.getLocation());
			} else {
				Toast.makeText(getActivity(), getString(R.string.error_invalid_from), Toast.LENGTH_SHORT).show();
				return;
			}
		}

		// remember trip
		RecentsDB.updateRecentTrip(getActivity(), new RecentTrip(ui.from.getLocation(), ui.to.getLocation()));

		// set date
		query_trips.setDate(ui.date.getDate());

		// set departure to true of first item is selected in spinner
		query_trips.setDeparture((boolean) ui.type.getTag());

		// set products
		query_trips.setProducts(mProducts);

		// don't execute if we still have to wait for GPS position
		if(mAfterGpsTask != null) return;

		query_trips.execute();
	}

	private void processIntent() {
		final Intent intent = getActivity().getIntent();
		if(intent != null) {
			final String action = intent.getAction();
			if(action != null && action.equals(TAG)) {
				Location from = (Location) intent.getSerializableExtra("from");
				Location to = (Location) intent.getSerializableExtra("to");
				Date date = (Date) intent.getSerializableExtra("date");
				boolean search = intent.getBooleanExtra("search", false);

				if(search) searchFromTo(from, to, date);
				else presetFromTo(from, to, date);
			}

			// remove the intent (and clear its action) since it was already processed
			// and should not be processed again
			intent.setAction(null);
			getActivity().setIntent(null);
		}
	}

	public void presetFromTo(Location from, Location to, Date date) {
		if(ui.from != null && from != null) {
			ui.from.setLocation(from, TransportrUtils.getDrawableForLocation(getContext(), from));
		}

		if(ui.to != null && to != null) {
			ui.to.setLocation(to, TransportrUtils.getDrawableForLocation(getContext(), to));
		}

		if (date != null) {
			ui.date.setDate(date);
		}
	}

	public void searchFromTo(Location from, Location to, Date date) {
		presetFromTo(from, to, date);
		search();
	}

	public void refreshAutocomplete(boolean always) {
		if(ui.from != null) {
			if(always) ui.from.reset();
			else ui.from.resetIfEmpty();
		}
		if(ui.to != null) {
			if(always) ui.to.reset();
			else ui.to.resetIfEmpty();
		}
	}

	public void activateGPS() {
		if(ui.from != null) {
			ui.from.activateGPS();
		}
	}

	private Boolean checkLocation(LocationView loc_view) {
		Location loc = loc_view.getLocation();

		if(loc == null) {
			// no location was selected by user
			if(loc_view.getText() != null && loc_view.getText().length() > 0) {
				// no location selected, but text entered. So let's try create locations from text
				loc_view.setLocation(new Location(LocationType.ANY, "IS_AMBIGUOUS", loc_view.getText(), loc_view.getText()), null);

				return true;
			}
			return false;
		}
		// we have a location, so make it a favorite (if it is not a coordinate)
		else if(!loc.type.equals(LocationType.COORD)){
			RecentsDB.updateFavLocation(getActivity(), loc, loc_view.getType());
		}

		return true;
	}

	private void showMore(boolean animate) {
		showingMore = true;
		ui.productsScrollView.setVisibility(View.VISIBLE);
		ui.fav_trips_separator.setVisibility(View.VISIBLE);

		if(mFavAdapter.getItemCount() == 0) {
			ui.no_favourites.setVisibility(View.VISIBLE);
		}

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

			if(mFavAdapter.getItemCount() == 0) {
				ui.no_favourites.setAlpha(0f);
				ui.no_favourites.animate().setDuration(500).alpha(1f);
				ui.fav_trips_separator.setAlpha(0f);
				ui.fav_trips_separator.animate().setDuration(500).alpha(1f);
			}
		}
	}

	private void showLess(boolean animate) {
		showingMore = false;
		ui.productsScrollView.setVisibility(View.GONE);

		if(mFavAdapter.getItemCount() == 0) {
			if (animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				ui.no_favourites.setAlpha(1f);
				ui.fav_trips_separator.setAlpha(1f);
				ui.fav_trips_separator.animate().setDuration(500).alpha(0f);
				ui.no_favourites.animate().setDuration(500).alpha(0f).withEndAction(new Runnable() {
					@Override
					public void run() {
						ui.no_favourites.setVisibility(View.GONE);
						ui.fav_trips_separator.setVisibility(View.GONE);
					}
				});
			} else {
				ui.no_favourites.setVisibility(View.GONE);
				ui.fav_trips_separator.setVisibility(View.GONE);
			}
		}
	}

	private void setType(boolean departure) {
		if(departure) {
			ui.type.setText(getString(R.string.trip_dep));
			ui.type.setTag(true);
		} else {
			ui.type.setText(getString(R.string.trip_arr));
			ui.type.setTag(false);
		}
	}

	public void swapLocations() {
		Animation slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -1.0f);

		slideUp.setDuration(400);
		slideUp.setFillAfter(true);
		slideUp.setFillEnabled(true);
		ui.to.startAnimation(slideUp);

		Animation slideDown = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

		slideDown.setDuration(400);
		slideDown.setFillAfter(true);
		slideDown.setFillEnabled(true);
		ui.from.startAnimation(slideDown);

		slideUp.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// swap location objects
				Location tmp = ui.to.getLocation();
				if(!ui.from.isSearching()) {
					ui.to.setLocation(ui.from.getLocation(), TransportrUtils.getDrawableForLocation(getContext(), ui.from.getLocation()));
				} else {
					// TODO: GPS currently only supports from location, so don't swap it for now
					ui.to.clearLocation();
				}
				ui.from.setLocation(tmp, TransportrUtils.getDrawableForLocation(getContext(), tmp));

				ui.from.clearAnimation();
				ui.to.clearAnimation();
			}
		});
	}

	private void fillIntent(Intent intent) {
		intent.putExtra("de.schildbach.pte.dto.Trip.from", ui.from.getLocation());
		intent.putExtra("de.schildbach.pte.dto.Trip.to", ui.to.getLocation());
		intent.putExtra("de.schildbach.pte.dto.Trip.date", ui.date.getDate());
		intent.putExtra("de.schildbach.pte.dto.Trip.departure", (boolean) ui.type.getTag());
		intent.putExtra("de.schildbach.pte.dto.Trip.products", new ArrayList<>(mProducts));
	}

	class DirectionsViewHolder {
		LocationGpsView from;
		LocationView to;
		Button type;
		TimeAndDateView date;
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
		RecyclerView favourites;
		CardView no_favourites;
		LinearLayout fav_trips_separator;
		View fav_trips_separator_line;
		ImageView fav_trips_separator_star;

		DirectionsViewHolder(View mView) {
			from = (LocationGpsView) mView.findViewById(R.id.fromLocation);
			to = (LocationView) mView.findViewById(R.id.toLocation);

			type = (Button) mView.findViewById(R.id.dateType);
			date = (TimeAndDateView) mView.findViewById(R.id.dateView);

			productsScrollView = (HorizontalScrollView) mView.findViewById(R.id.productsScrollView);
			productsLayout = (ViewGroup) mView.findViewById(R.id.productsLayout);
			high_speed_train = (ImageView) mView.findViewById(R.id.ic_product_high_speed_train);
			regional_train = (ImageView) mView.findViewById(R.id.ic_product_regional_train);
			suburban_train = (ImageView) mView.findViewById(R.id.ic_product_suburban_train);
			subway = (ImageView) mView.findViewById(R.id.ic_product_subway);
			tram = (ImageView) mView.findViewById(R.id.ic_product_tram);
			bus = (ImageView) mView.findViewById(R.id.ic_product_bus);
			on_demand = (ImageView) mView.findViewById(R.id.ic_product_on_demand);
			ferry = (ImageView) mView.findViewById(R.id.ic_product_ferry);
			cablecar = (ImageView) mView.findViewById(R.id.ic_product_cablecar);
			search = (Button) mView.findViewById(R.id.searchButton);

			favourites = (RecyclerView) mView.findViewById(R.id.favourites);
			no_favourites = (CardView) mView.findViewById(R.id.no_favourites);
			fav_trips_separator = (LinearLayout) mView.findViewById(R.id.fav_trips_separator);
			fav_trips_separator = (LinearLayout) mView.findViewById(R.id.fav_trips_separator);
			fav_trips_separator_line = mView.findViewById(R.id.fav_trips_separator_line);
			fav_trips_separator_star = (ImageView) mView.findViewById(R.id.fav_trips_separator_star);
		}
	}
}

