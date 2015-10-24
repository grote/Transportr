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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.adapters.FavouritesAdapter;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.tasks.AsyncQueryTripsTask;
import de.grobox.liberario.ui.LocationInputGPSView;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

public class DirectionsFragment extends TransportrFragment {
	private View mView;
	private ViewHolder ui = new ViewHolder();
	private FavLocation.LOC_TYPE mHomeClicked;
	private AsyncQueryTripsTask mAfterGpsTask = null;
	private Set<Product> mProducts = EnumSet.allOf(Product.class);
	public ProgressDialog pd;
	private LocationInputGPSView from;
	private LocationInputView to;
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
		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_directions, container, false);

		populateViewHolders();

		from = new FromInputView(getActivity(), ui.from);
		to = new ToInputView(getActivity(), ui.to);

		// Set Type to Departure=True
		ui.type.setTag(true);

		// Trip Date Type Spinner (departure or arrival)
		ui.type.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setType(!(boolean) ui.type.getTag());
			}
		});

		DateUtils.setUpTimeDateUi(mView);

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

		List<RecentTrip> favourites = RecentsDB.getFavouriteTripList(getContext());
		mFavAdapter = new FavouritesAdapter(getContext(), favourites);
		ui.favourites.setAdapter(mFavAdapter);
		final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
		ui.favourites.setLayoutManager(layoutManager);

		if(!Preferences.getPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, false)) {
			// don't animate here, since this method is called on each fragment change from the drawer
			showLess(false);
		} else {
			showingMore = true;
		}

		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		restart = true;

		if(savedInstanceState != null) {
			Location from_loc = (Location) savedInstanceState.getSerializable("from");
			if(from_loc != null) {
				from.setLocation(from_loc, TransportrUtils.getDrawableForLocation(getContext(), from_loc));
			}

			Location to_loc = (Location) savedInstanceState.getSerializable("to");
			if(to_loc != null) {
				to.setLocation(to_loc, TransportrUtils.getDrawableForLocation(getContext(), to_loc));
			}

			setType(savedInstanceState.getBoolean("type"));

			String time = savedInstanceState.getString("time", null);
			if(time != null) {
				ui.time.setText(time);
			}

			String date = savedInstanceState.getString("date", null);
			if(date != null) {
				ui.date.setText(date);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(from != null) {
			outState.putSerializable("from", from.getLocation());
		}
		if(to != null) {
			outState.putSerializable("to", to.getLocation());
		}

		if(ui.type != null) {
			outState.putBoolean("type", (boolean) ui.type.getTag());
		}

		if(ui.time != null) {
			outState.putCharSequence("time", ui.time.getText());
		}
		if(ui.date != null) {
			outState.putCharSequence("date", ui.date.getText());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();

		if(!restart) {
			long date = DateUtils.getDateFromUi(mView).getTime();
			long now = new Date().getTime();

			// reset date and time if older than 2 hours, so user doesn't search in the past by accident
			if((now - date) / (60 * 60 * 1000) > 2) {
				DateUtils.resetTime(getContext(), ui.time, ui.date);
			}
		} else {
			restart = false;
		}

		mAfterGpsTask = null;

		displayFavourites();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.directions, menu);

		if(ui.productsScrollView.getVisibility() == View.GONE) {
			menu.findItem(R.id.action_navigation_expand).setIcon(R.drawable.ic_action_navigation_unfold_more);
		} else {
			menu.findItem(R.id.action_navigation_expand).setIcon(R.drawable.ic_action_navigation_unfold_less);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_navigation_expand:
				if(ui.productsScrollView.getVisibility() == View.GONE) {
					item.setIcon(R.drawable.ic_action_navigation_unfold_less);
					Preferences.setPref(getActivity(), Preferences.SHOW_ADV_DIRECTIONS, true);
					showMore(true);
				} else {
					item.setIcon(R.drawable.ic_action_navigation_unfold_more);
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
	// change things for a different network provider
	public void onNetworkProviderChanged(TransportNetwork network) {
		refreshFavs();
		displayFavourites();

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
				//noinspection deprecation
				from.setLocation(RecentsDB.getHome(getActivity()), TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_home));
			} else if(mHomeClicked.equals(FavLocation.LOC_TYPE.TO)) {
				//noinspection deprecation
				to.setLocation(RecentsDB.getHome(getActivity()), TransportrUtils.getTintedDrawable(getContext(), R.drawable.ic_action_home));
			}
		}
	}

	private void displayFavourites() {
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

		if(to == null && from == null) {
			// the activity is most likely being recreated after configuration change, don't search again
			return;
		}

		AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(getActivity());

		// check and set to location
		if(checkLocation(to)) {
			query_trips.setTo(to.getLocation());
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), Toast.LENGTH_SHORT).show();
			return;
		}

		// check and set from location
		if(from.isSearching()) {
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
		RecentsDB.updateRecentTrip(getActivity(), new RecentTrip(from.getLocation(), to.getLocation()));

		// set date
		query_trips.setDate(DateUtils.getDateFromUi(mView));

		// set departure to true of first item is selected in spinner
		query_trips.setDeparture((boolean) ui.type.getTag());

		// set products
		query_trips.setProducts(mProducts);

		// don't execute if we still have to wait for GPS position
		if(mAfterGpsTask != null) return;

		query_trips.execute();
	}

	public void presetFromTo(Location from, Location to) {
		if(this.from != null) {
			this.from.setLocation(from, TransportrUtils.getDrawableForLocation(getContext(), from));
		}

		if(this.to != null) {
			this.to.setLocation(to, TransportrUtils.getDrawableForLocation(getContext(), to));
		}
	}

	public void searchFromTo(Location from, Location to) {
		presetFromTo(from, to);
		search();
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

	public void activateGPS() {
		if(from != null) {
			from.activateGPS();
		}
	}

	private Boolean checkLocation(LocationInputView loc_view) {
		Location loc = loc_view.getLocation();

		if(loc == null) {
			// no location was selected by user
			if(!loc_view.ui.location.getText().toString().equals("")) {
				// no location selected, but text entered. So let's try create locations from text
				loc_view.setLocation(new Location(LocationType.ANY, null, loc_view.ui.location.getText().toString(), loc_view.ui.location.getText().toString()), null);

				return true;
			}
			return false;
		}
		// we have a location, so make it a favorite
		else {
			RecentsDB.updateFavLocation(getActivity(), loc, loc_view.getType());
		}

		return true;
	}

	// TODO use method from LocationInputView and kill this
	private void handleLocationItemClick(Location loc, FavLocation.LOC_TYPE type, View view) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();

		if(loc.id != null && loc.id.equals("Transportr.GPS")) {
			from.activateGPS();
			ui.to.location.requestFocus();
		}
		else {
			// home location
			if (loc.id != null && loc.id.equals("Transportr.HOME")) {
				Location home = RecentsDB.getHome(getActivity());

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
				from.deactivateGPS();
				imm.hideSoftInputFromWindow(ui.from.location.getWindowToken(), 0);
				ui.to.location.requestFocus();
			} else {
				imm.hideSoftInputFromWindow(ui.to.location.getWindowToken(), 0);
			}
		}
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


	class FromInputView extends LocationInputGPSView {
		public FromInputView(Activity context, LocationInputViewHolder holder) {
			super(context, holder, MainActivity.PR_ACCESS_FINE_LOCATION_DIRECTIONS);
			setType(FavLocation.LOC_TYPE.FROM);
			setHome(true);
			setFavs(true);

			setHint(R.string.from);
		}

		@Override
		public void onLocationItemClick(Location loc, View view) {
			handleLocationItemClick(loc, FavLocation.LOC_TYPE.FROM, view);
		}

		@Override
		public void onLocationChanged(Location location) {
			super.onLocationChanged(location);

			if(pd != null) {
				pd.dismiss();
			}

			// query for trips if user pressed search already and we just have been waiting for the location
			if(mAfterGpsTask != null) {
				mAfterGpsTask.setFrom(location);
				mAfterGpsTask.execute();
			}
		}
	}

	class ToInputView extends LocationInputView {
		// adapt activateGPS if you ever make this a LocationInputGPSView
		public ToInputView(Activity context, LocationInputViewHolder holder) {
			super(context, holder);
			setType(FavLocation.LOC_TYPE.TO);
			setHome(true);
			setFavs(true);

			setHint(R.string.to);
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
		RecyclerView favourites;
		CardView no_favourites;
		LinearLayout fav_trips_separator;
		View fav_trips_separator_line;
		ImageView fav_trips_separator_star;
	}

	private void populateViewHolders() {
		ui.fromLocation = (ViewGroup) mView.findViewById(R.id.fromLocation);
		ui.from = new LocationInputView.LocationInputViewHolder(ui.fromLocation);

		ui.toLocation = (ViewGroup) mView.findViewById(R.id.toLocation);
		ui.to = new LocationInputView.LocationInputViewHolder(ui.toLocation);

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

		ui.favourites = (RecyclerView) mView.findViewById(R.id.favourites);
		ui.no_favourites = (CardView) mView.findViewById(R.id.no_favourites);
		ui.fav_trips_separator = (LinearLayout) mView.findViewById(R.id.fav_trips_separator);
		ui.fav_trips_separator = (LinearLayout) mView.findViewById(R.id.fav_trips_separator);
		ui.fav_trips_separator_line = mView.findViewById(R.id.fav_trips_separator_line);
		ui.fav_trips_separator_star = (ImageView) mView.findViewById(R.id.fav_trips_separator_star);
	}

	public void swapLocations() {
		Animation slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -1.0f);

		slideUp.setDuration(400);
		slideUp.setFillAfter(true);
		slideUp.setFillEnabled(true);
		ui.toLocation.startAnimation(slideUp);

		Animation slideDown = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 1.0f);

		slideDown.setDuration(400);
		slideDown.setFillAfter(true);
		slideDown.setFillEnabled(true);
		ui.fromLocation.startAnimation(slideDown);

		slideUp.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) { }

			@Override
			public void onAnimationRepeat(Animation animation) { }

			@Override
			public void onAnimationEnd(Animation animation) {
				// swap location objects and drawables
				final Drawable icon = ui.to.status.getDrawable();
				Location tmp = to.getLocation();
				if(!from.isSearching()) {
					to.setLocation(from.getLocation(), ui.from.status.getDrawable());
				} else {
					// TODO: GPS currently only supports from location, so don't swap it for now
					to.clearLocation();
				}
				from.setLocation(tmp, icon);

				ui.fromLocation.clearAnimation();
				ui.toLocation.clearAnimation();
			}
		});
	}
}

