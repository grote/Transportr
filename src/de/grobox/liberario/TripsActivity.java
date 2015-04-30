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

import de.schildbach.pte.dto.QueryTripsResult;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

public class TripsActivity extends AppCompatActivity {
	private QueryTripsResult start_context;
	private QueryTripsResult end_context;
	private RecyclerView mRecyclerView;
	private TripAdapter mAdapter;
	private SwipyRefreshLayout swipeRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trips);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(Preferences.getNetwork(this));
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Intent intent = getIntent();
		start_context = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");
		end_context = start_context;

		mRecyclerView = (RecyclerView) findViewById(R.id.trips_recycler_view);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setHasFixedSize(true);

		mAdapter = new TripAdapter(start_context.trips, R.layout.trip, this);
		mRecyclerView.setAdapter(mAdapter);

		swipeRefresh = (SwipyRefreshLayout) findViewById(R.id.swipe_refresh_layout);
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

		swipeRefresh.setDistanceToTriggerSync(150);

		swipeRefresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh(final SwipyRefreshLayoutDirection direction) {
				startGetMoreTrips(direction != SwipyRefreshLayoutDirection.TOP);
			}
		});
	}

	public void startGetMoreTrips(boolean later) {
		if(later) (new AsyncQueryMoreTripsTask(this, end_context.context, true)).execute();
		else    (new AsyncQueryMoreTripsTask(this, start_context.context, false)).execute();
	}

	public void addMoreTrips(QueryTripsResult trip_results, boolean later) {
		if(trip_results != null) {
			mAdapter.addAll(trip_results.trips);

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
