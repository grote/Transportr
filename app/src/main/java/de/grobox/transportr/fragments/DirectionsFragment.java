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

package de.grobox.transportr.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;

import de.grobox.transportr.R;
import de.grobox.transportr.activities.TripsActivity;
import de.grobox.transportr.locations.AmbiguousLocationActivity;
import de.grobox.transportr.locations.LocationGpsView;
import de.grobox.transportr.locations.LocationView;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.NetworkProviderFactory;
import de.grobox.transportr.settings.Preferences;
import de.grobox.transportr.tasks.AsyncQueryTripsTask;
import de.grobox.transportr.tasks.AsyncQueryTripsTask.TripHandler;
import de.grobox.transportr.ui.TimeAndDateView;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryTripsResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.TO;
import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.VIA;
import static de.grobox.transportr.settings.Preferences.SHOW_ADV_DIRECTIONS;
import static de.grobox.transportr.utils.TransportrUtils.fixToolbarIcon;
import static de.grobox.transportr.utils.TransportrUtils.getButtonIconColor;
import static de.grobox.transportr.utils.TransportrUtils.getToolbarDrawable;

@Deprecated
public class DirectionsFragment extends TransportrFragment implements TripHandler {

	public final static String TAG = "de.grobox.liberario.directions";
	private ProgressDialog pd;

	private DirectionsViewHolder ui;
	private AsyncQueryTripsTask mAfterGpsTask = null;
	private EnumSet<Product> products = EnumSet.allOf(Product.class);
//	private FastItemAdapter<ProductItem> productsAdapter;
	private boolean showingMore = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_directions, container, false);

		ui = new DirectionsViewHolder(v);

		ui.from.setType(FROM);

		ui.via.setType(VIA);
		ui.to.setType(TO);

		// Set Type to Departure=True
		ui.type.setTag(true);

		// Trip Date Type Spinner (departure or arrival)
		ui.type.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				setType(!(boolean) ui.type.getTag());
			}
		});

		// Products
//		productsAdapter = new FastItemAdapter<>();
//		ui.products.setHasFixedSize(true);
//		ui.products.setAdapter(productsAdapter);
//		products = Preferences.getProducts(getContext());
//		onProductsChanged(products);
//		ui.products.getBackground().setColorFilter(getButtonIconColor(getActivity()), SRC_ATOP);
//		ui.products.setOnTouchListener(new View.OnTouchListener() {
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// onClickListener doesn't work for some reasion, so use touch instead
//				if(event.getAction() == ACTION_UP) {
//					showProductDialog();
//					return true;
//				}
//				return false;
//			}
//		});

		ui.search.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						search();
					}
				});

		ui.fav_trips_separator_star.setColorFilter(getButtonIconColor(getActivity()));
		ui.fav_trips_separator_line.setBackgroundColor(getButtonIconColor(getActivity()));

		ui.favourites.setLayoutManager(new LinearLayoutManager(getContext()));

		if(!Preferences.getPref(getActivity(), SHOW_ADV_DIRECTIONS, false)) {
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

		if(savedInstanceState != null) {
			setType(savedInstanceState.getBoolean("type"));
			Serializable tmpProducts = savedInstanceState.getSerializable("products");
//			if(tmpProducts instanceof EnumSet) {
//				//noinspection unchecked
//				products = (EnumSet<Product>) tmpProducts;
//				onProductsChanged(products);
//			}
//			// re-attach fragment listener
//			Fragment fragment = getActivity().getSupportFragmentManager().findFragmentByTag(ProductDialogFragment.TAG);
//			if(fragment != null && fragment.getUserVisibleHint()) {
//				((ProductDialogFragment) fragment).setOnProductsChangedListener(this);
//			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(ui != null) {
			outState.putBoolean("type", (boolean) ui.type.getTag());
			outState.putSerializable("products", products);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		mAfterGpsTask = null;

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
		if(ui.products.getVisibility() == GONE) {
			expandItem.setIcon(getToolbarDrawable(context, R.drawable.ic_action_navigation_unfold_more));
		} else {
			expandItem.setIcon(getToolbarDrawable(context, R.drawable.ic_action_navigation_unfold_less));
		}

		MenuItem swapItem = menu.findItem(R.id.action_swap_locations);
		fixToolbarIcon(context, swapItem);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_navigation_expand:
				if(ui.products.getVisibility() == GONE) {
					item.setIcon(getToolbarDrawable(getContext(), R.drawable.ic_action_navigation_unfold_less));
					Preferences.setPref(getActivity(), SHOW_ADV_DIRECTIONS, true);
					showMore(true);
				} else {
					item.setIcon(getToolbarDrawable(getContext(), R.drawable.ic_action_navigation_unfold_more));
					Preferences.setPref(getActivity(), SHOW_ADV_DIRECTIONS, false);
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
	public void onTripRetrieved(QueryTripsResult result) {
		if(getContext() == null) {
			// FIXME: This is not a proper solution, we need to use a loader instead
			return;
		}

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
			Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_no_trips_found), LENGTH_LONG).show();
		}
	}

	@Override
	public void onTripRetrievalError(String error) { }

//	@Override
//	public void onProductsChanged(EnumSet<Product> products) {
//		this.products = products;
//		productsAdapter.clear();
//		for(Product product : products) {
//			productsAdapter.add(new ProductItem(product));
//		}
//	}

//	private void showProductDialog() {
//		ProductDialogFragment productFragment = ProductDialogFragment.newInstance(products);
//		productFragment.setOnProductsChangedListener(DirectionsFragment.this);
//		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
//		productFragment.show(ft, ProductDialogFragment.TAG);
//	}

	private void search() {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getActivity()));
		if(!np.hasCapabilities(NetworkProvider.Capability.TRIPS)) {
			Toast.makeText(getActivity(), getString(R.string.error_no_trips_capability), LENGTH_SHORT).show();
			return;
		}

		if(ui.to == null && ui.from == null) {
			// the activity is most likely being recreated after configuration change, don't search again
			return;
		}

//		if(ui.to.isChangingHome()) {
//			// we are currently in a state of changing home in the to field, a search is not possible
//			return;
//		}

		AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(getActivity(), this);

		// check and set to location
		if(checkLocation(ui.to)) {
//			query_trips.setTo(ui.to.getLocation());
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_to), LENGTH_SHORT).show();
			return;
		}

		// check and set via location
		if(checkLocation(ui.via)) {
//			query_trips.setVia(ui.via.getLocation());
		}

		// check and set from location
		if(ui.from.isSearching()) {
			if(ui.from.getLocation() != null) {
//				query_trips.setFrom(ui.from.getLocation());
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
//				query_trips.setFrom(ui.from.getLocation());
			} else {
				Toast.makeText(getActivity(), getString(R.string.error_invalid_from), LENGTH_SHORT).show();
				return;
			}
		}

		// remember trip
//		updateFavoriteTrip(getActivity(), new FavoriteTripItem(ui.from.getLocation(), ui.via.getLocation(), ui.to.getLocation()));

		// set date
		query_trips.setDate(ui.date.getDate());

		// set departure to true of first item is selected in spinner
		query_trips.setDeparture((boolean) ui.type.getTag());

		// set products
		query_trips.setProducts(products);

		// don't execute if we still have to wait for GPS position
		if(mAfterGpsTask != null) return;

		query_trips.execute();
	}

	private void processIntent() {
		final Intent intent = getActivity().getIntent();
		if(intent != null) {
			final String action = intent.getAction();
			if(action != null && action.equals(TAG)) {
//				WrapLocation from, via, to;
//				boolean search;
//				Date date;
//				String eSpecial = (String) intent.getSerializableExtra("special");
//				if(eSpecial != null && eSpecial.equals(TASK_HOME)) {
//					from = new WrapLocation(WrapLocation.WrapType.GPS);
//					to = new WrapLocation(WrapLocation.WrapType.HOME);
//					search = true;
//				} else {
//					from = (WrapLocation) intent.getSerializableExtra("from");
//					to = (WrapLocation) intent.getSerializableExtra("to");
//					search = intent.getBooleanExtra("search", false);
//				}
//				via = (WrapLocation) intent.getSerializableExtra("via");
//				date = (Date) intent.getSerializableExtra("date");
//
//				if(search) searchFromTo(from, via, to, date);
//				else presetFromTo(from, via, to, date);
			}

			// remove the intent (and clear its action) since it was already processed
			// and should not be processed again
			intent.setAction(null);
			getActivity().setIntent(null);
		}
	}

	private void presetFromTo(WrapLocation wfrom, WrapLocation wvia, WrapLocation wto, Date date) {
//
//		Location from, via, to;
//
//		// unwrap wfrom
//		if(wfrom != null) {
//			from = wfrom.getLocation();
//			if(wfrom.getWrapType() == WrapLocation.WrapType.GPS) {
//				setSearching();
//				from = null;
//			}
//		} else {
//			from = null;
//		}
//
//
//		// handle from-location
//		if(ui.from != null && from != null) {
//			ui.from.setLocation(from);
//		}
//
//		// unwrap wvia
//		if(wvia != null) {
//			via = wvia.getLocation();
//		} else {
//			via = null;
//		}
//
//		// handle via-location
//		if(ui.via != null) {
//			ui.via.setLocation(via);
//			if(via != null && ui.products.getVisibility() == GONE) {
//				// if there's a via location, make sure to show it in the UI
//				showMore(true);
//			}
//		}
//
//		// unwrap wto
//		if(wto != null) {
//			to = wto.getLocation();
//			if(wto.getWrapType() == WrapLocation.WrapType.HOME){
//				to = null;
//				ui.to.setWrapLocation(wto);
//			}
//		} else {
//			to = null;
//		}
//
//		// handle to-location
//		if(ui.to != null && to != null) {
//			ui.to.setLocation(to);
//		}
//
//		// handle date
//		if (date != null) {
//			ui.date.setDate(date);
//		}
	}

	private void searchFromTo(WrapLocation from, WrapLocation via, WrapLocation to, Date date) {
		presetFromTo(from, via, to, date);
		search();
	}

	private void refreshAutocomplete(boolean always) {
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
			ui.from.setSearching();
		}
	}

	private Boolean checkLocation(LocationView loc_view) {
		Location loc = null; // loc_view.getLocation();

		if(loc == null) {
			// no location was selected by user
			return loc_view.getText() != null && loc_view.getText().length() > 0;
		}
		// we have a location, so make it a favorite
		else {
//			LocationDb.updateFavLocation(getActivity(), loc, loc_view.getType());
		}

		return true;
	}

	private void showMore(boolean animate) {
		showingMore = true;
		ui.via.setVisibility(VISIBLE);
		ui.products.setVisibility(VISIBLE);
		ui.fav_trips_separator.setVisibility(VISIBLE);


		if(animate && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			DisplayMetrics dm = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		}
	}

	private void showLess(boolean animate) {
		showingMore = false;
		ui.via.setLocation(null);
		ui.via.setVisibility(GONE);
		ui.products.setVisibility(GONE);
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

	private void swapLocations() {
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
//				Location tmp = ui.to.getLocation();
				if(!ui.from.isSearching()) {
					ui.to.setLocation(ui.from.getLocation());
				} else {
					// TODO: GPS currently only supports from location, so don't swap it for now
					ui.to.clearLocation();
				}
//				ui.from.setLocation(tmp, getDrawableForLocation(getContext(), tmp));

				ui.from.clearAnimation();
				ui.to.clearAnimation();
			}
		});
	}

	private void fillIntent(Intent intent) {
		intent.putExtra("de.schildbach.pte.dto.Trip.from", ui.from.getLocation());
		intent.putExtra("de.schildbach.pte.dto.Trip.via", ui.via.getLocation());
		intent.putExtra("de.schildbach.pte.dto.Trip.to", ui.to.getLocation());
		intent.putExtra("de.schildbach.pte.dto.Trip.date", ui.date.getDate());
		intent.putExtra("de.schildbach.pte.dto.Trip.departure", (boolean) ui.type.getTag());
		intent.putExtra("de.schildbach.pte.dto.Trip.products", new ArrayList<>(products));
	}

	private static class DirectionsViewHolder {
		LocationGpsView from;
		LocationView via;
		LocationView to;
		Button type;
		TimeAndDateView date;
		RecyclerView products;
		Button search;
		RecyclerView favourites;
		CardView no_favourites;
		LinearLayout fav_trips_separator;
		View fav_trips_separator_line;
		ImageView fav_trips_separator_star;

		DirectionsViewHolder(View mView) {
			from = mView.findViewById(R.id.fromLocation);
			via = mView.findViewById(R.id.viaLocation);
			to = mView.findViewById(R.id.toLocation);

			type = mView.findViewById(R.id.dateType);
			date = mView.findViewById(R.id.dateView);

			products = mView.findViewById(R.id.productsList);
			search = mView.findViewById(R.id.searchButton);

			favourites = mView.findViewById(R.id.favourites);
			no_favourites = mView.findViewById(R.id.no_favourites);
			fav_trips_separator = mView.findViewById(R.id.fav_trips_separator);
			fav_trips_separator = mView.findViewById(R.id.fav_trips_separator);
			fav_trips_separator_line = mView.findViewById(R.id.fav_trips_separator_line);
			fav_trips_separator_star = mView.findViewById(R.id.fav_trips_separator_star);
		}
	}
/*
	class ProductItem { extends AbstractItem<ProductDialogFragment.ProductItem, ProductItem.ViewHolder> {
		private final Product product;

		ProductItem(Product product) {
			this.product = product;
		}

		@Override
		public int getType() {
			return product.ordinal();
		}

		@Override
		public int getLayoutRes() {
			return R.layout.item_product;
		}

		@Override
		public void bindView(final ProductItem.ViewHolder ui, List<Object> payloads) {
			super.bindView(ui, payloads);
			ui.image.setImageDrawable(getTintedDrawable(getContext(), getDrawableForProduct(product)));
			ui.image.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showProductDialog();
				}
			});
		}

		@Override
		public ViewHolder getViewHolder(View view) {
			return new ViewHolder(view);
		}

		class ViewHolder extends RecyclerView.ViewHolder {
			private ImageView image;

			ViewHolder(View v) {
				super(v);
				image = v.findViewById(R.id.productView);
			}
		}
	}
*/
}

