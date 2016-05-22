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

package de.grobox.liberario.activities;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;

import de.grobox.liberario.ListTrip;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.adapters.TripAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.tasks.AsyncQueryMoreTripsTask;
import de.grobox.liberario.ui.SwipeDismissRecyclerViewTouchListener;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Trip;

public class TripsActivity extends TransportrActivity {
	private QueryTripsResult start_context;
	private QueryTripsResult end_context;
	private RecyclerView mRecyclerView;
	private TripAdapter mAdapter;
	private SwipyRefreshLayout swipeRefresh;
	private Location from;
	private Location via;
	private Location to;
	private ArrayList<Product> products;
	private boolean isFav, isFavable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trips);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(Preferences.getTransportNetwork(this).getName());
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		final Intent intent = getIntent();
		start_context = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");
		end_context = start_context;

		// retrieve trip data from intent that is not stored properly in trip object
		from = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.from");
		via = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.via");
		to = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.to");
		products = (ArrayList<Product>) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.products");

		swipeRefresh = (SwipyRefreshLayout) findViewById(R.id.swipe_refresh_layout);
		swipeRefresh.setColorSchemeResources(R.color.accent);

		mRecyclerView = (RecyclerView) findViewById(R.id.trips_recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setHasFixedSize(true);

		final SwipeDismissRecyclerViewTouchListener touchListener = new SwipeDismissRecyclerViewTouchListener(mRecyclerView,
			new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
			 @Override
			 public boolean canDismiss(int position) {
			     return true;
			 }

			 @Override
			 public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
			     for (int position : reverseSortedPositions) {
			         mAdapter.removeItemAt(position);
			         Snackbar snackbar = Snackbar.make(recyclerView, R.string.trip_removed, Snackbar.LENGTH_LONG);
			         snackbar.setAction(R.string.undo, new View.OnClickListener() {
			             @Override
			             public void onClick(View view) {
			                 mAdapter.undo();
			             }
			         });
			         snackbar.show();
			     }
			 }

			 @Override
			 public void onItemClick(int position) {
			     Trip trip = mAdapter.getItem(position).trip;

			     Intent intent = new Intent(TripsActivity.this, TripDetailActivity.class);
			     intent.putExtra("de.schildbach.pte.dto.Trip", trip);
			     intent.putExtra("de.schildbach.pte.dto.Trip.from", from);
				 intent.putExtra("de.schildbach.pte.dto.Trip.via", via);
			     intent.putExtra("de.schildbach.pte.dto.Trip.to", to);
				 intent.putExtra("de.schildbach.pte.dto.Trip.products", products);

				 if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					 LinearLayoutManager layoutManager = ((LinearLayoutManager)mRecyclerView.getLayoutManager());
					 int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
					 ActivityOptions options = ActivityOptions.
							 makeSceneTransitionAnimation(TripsActivity.this, mRecyclerView.getChildAt(position-firstVisiblePosition), "card");
					 startActivity(intent, options.toBundle());
				 } else {
					 startActivity(intent);
				 }
			 }
		});

		// Setting this scroll listener is required to ensure that during ListView scrolling, we don't look for swipes.
		mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());
		// TODO also make sure a swipe prevents scrolling

		mAdapter = new TripAdapter(ListTrip.getList(start_context.trips), touchListener, this);
		mAdapter.setHasStableIds(false);
		mRecyclerView.setAdapter(mAdapter);

		if(to.type != LocationType.COORD && from.type != LocationType.COORD) {
			isFav = RecentsDB.isFavedRecentTrip(this, new RecentTrip(from, via, to));
			isFavable = true;
		} else {
			isFav = false;
			isFavable = false;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();

		if(start_context.context.canQueryEarlier() && end_context.context.canQueryLater()) {
			swipeRefresh.setDirection(SwipyRefreshLayoutDirection.BOTH);
		} else if(start_context.context.canQueryEarlier()) {
			swipeRefresh.setDirection(SwipyRefreshLayoutDirection.TOP);
		} else if(end_context.context.canQueryLater()) {
			swipeRefresh.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
		} else {
			swipeRefresh.setEnabled(false);
			return;
		}

		swipeRefresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh(final SwipyRefreshLayoutDirection direction) {
				startGetMoreTrips(direction != SwipyRefreshLayoutDirection.TOP);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trips_activity_actions, menu);

		if(isFavable) {
			TransportrUtils.setFavState(this, menu.findItem(R.id.action_fav_trip), isFav, true);
		} else {
			menu.findItem(R.id.action_fav_trip).setVisible(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		else if(item.getItemId() == R.id.action_fav_trip) {
			RecentsDB.toggleFavouriteTrip(this, new RecentTrip(from, via, to, isFav));
			isFav = !isFav;
			TransportrUtils.setFavState(this, item, isFav, true);

			return true;
		}
		else {
			return false;
		}
	}

	public void startGetMoreTrips(boolean later) {
		if(later) (new AsyncQueryMoreTripsTask(this, end_context.context, true)).execute();
		else    (new AsyncQueryMoreTripsTask(this, start_context.context, false)).execute();
	}

	public void addMoreTrips(QueryTripsResult trip_results, boolean later) {
		if(trip_results != null) {
			mAdapter.addAll(ListTrip.getList(trip_results.trips));

			// save trip results to have context for next query
			if(later) end_context = trip_results;
			else start_context = trip_results;

		}
	}

	public void onRefreshComplete(boolean later) {
		// hide progress indicator
		swipeRefresh.setRefreshing(false);

		// scroll smoothly up or down when we have new trips
		mRecyclerView.smoothScrollBy(0, later ? 200 : -200);
	}
}
