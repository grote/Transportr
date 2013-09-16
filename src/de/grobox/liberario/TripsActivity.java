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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import de.grobox.liberario.R;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Style.Shape;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TripsActivity extends Activity {
	private QueryTripsResult trips;
	private Menu mMenu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trips);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		final TableLayout main = (TableLayout) findViewById(R.id.activity_trips);

		Intent intent = getIntent();
		trips = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");

		setHeader();
		addTrips(main, trips.trips);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Set a listener to be invoked when the list should be refreshed.
		PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_trips);
		pullToRefreshView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				Mode mode = refreshView.getCurrentMode();
				boolean later = true;
				if(mode == Mode.PULL_FROM_START) later = false;
				else if(mode == Mode.PULL_FROM_END) later = true;
				startGetMoreTrips(later);
			}
		});
	}

	public void startGetMoreTrips(boolean later) {
		(new AsyncQueryMoreTripsTask(this, trips.context, later)).execute();
	}

	private void setHeader() {
		((TextView) findViewById(R.id.tripStartTextView)).setText(trips.from.name);
		((TextView) findViewById(R.id.tripDestinationTextView)).setText(trips.to.name);
	}

	private void addTrips(final TableLayout main, List<Trip> trips, boolean append) {
		if(trips != null) {
			// reverse order of trips if they should be prepended
			if(!append) {
				ArrayList<Trip> tempResults = new ArrayList<Trip>(trips);
				Collections.reverse(tempResults);
				trips = tempResults;
			}

			for(final Trip trip : trips) {
				LinearLayout trip_layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.trip, null);
				TableRow row = (TableRow) trip_layout.findViewById(R.id.tripTableRow);

				// Locations
				TextView fromView = (TextView) row.findViewById(R.id.fromView);
				fromView.setText(trip.from.name);
				TextView toView = ((TextView) row.findViewById(R.id.toView));
				toView.setText(trip.to.name);

				// Times
				TextView departureTimeView = ((TextView) row.findViewById(R.id.departureTimeView));
				departureTimeView.setText(DateUtils.getTime(trip.getFirstDepartureTime()));
				TextView arrivalTimeView = ((TextView) row.findViewById(R.id.arrivalTimeView));
				arrivalTimeView.setText(DateUtils.getTime(trip.getLastArrivalTime()));

				// Duration
				TextView durationView = ((TextView) trip_layout.findViewById(R.id.durationView));
				durationView.setText(DateUtils.getDuration(trip.getFirstDepartureTime(), trip.getLastArrivalTime()));

				// Transports
				LinearLayout lineLayout = ((LinearLayout) trip_layout.findViewById(R.id.lineLayout));

				// for each leg
				for(final Leg leg : trip.legs) {
					if(leg instanceof Trip.Public) {
						addLineBox(lineLayout, ((Public) leg).line);
					}
					else if(leg instanceof Trip.Individual) {
						addWalkingBox(lineLayout);
					}
				}

				// remember trip in view for onClick event
				trip_layout.setTag(trip);

				// make trip details fold out and in on click
				trip_layout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showTripDetails(v.getTag());
					}

				});

				if(append) {
					main.addView(trip_layout);
				}
				else {
					main.addView(trip_layout, 0);
				}
			}

		}
		else {
			// TODO offer option to query again for trips
		}
	}

	@SuppressWarnings("deprecation")
	private void addLineBox(LinearLayout lineLayout, Line line) {
		TextView transportsView =  (TextView) LayoutInflater.from(this).inflate(R.layout.line_box, null);

		GradientDrawable line_box = (GradientDrawable) getResources().getDrawable(R.drawable.line_box);
		line_box.setColor(line.style.backgroundColor);

		// change shape and mutate before to not share state with other instances
		line_box.mutate();
		if(line.style.shape == Shape.CIRCLE) line_box.setShape(GradientDrawable.OVAL);

		transportsView.setText(line.label.substring(1));
		transportsView.setBackgroundDrawable(line_box);
		transportsView.setTextColor(line.style.foregroundColor);

		// set margin, because setting in in xml didn't work
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(0, 0, 7, 0);
		transportsView.setLayoutParams(llp);

		lineLayout.addView(transportsView);
	}

	private void addWalkingBox(LinearLayout lineLayout) {
		ImageView transportsView = (ImageView) LayoutInflater.from(this).inflate(R.layout.walking_box, null);

		// set margin, because setting in in xml didn't work
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(0, 0, 7, 0);
		transportsView.setLayoutParams(llp);

		lineLayout.addView(transportsView);
	}

	private void addTrips(final TableLayout main, List<Trip> trips) {
		addTrips(main, trips, true);
	}

	private void showTripDetails(Object o) {
		Trip trip = (Trip) o;

		Intent intent = new Intent(this, TripDetailActivity.class);
		intent.putExtra("de.schildbach.pte.dto.Trip", trip);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO show later/earlier options only if provider has the capability

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trips_activity_actions, menu);
		mMenu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			case R.id.action_earlier:
				setProgress(false, true);
				startGetMoreTrips(false);

				return true;
			case R.id.action_later:
				setProgress(true, true);
				startGetMoreTrips(true);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void addMoreTrips(QueryTripsResult trip_results, boolean later, int num_trips) {
		if(trips != null) {
			TableLayout main = (TableLayout) findViewById(R.id.activity_trips);
			int num_old_trips = trips.trips.size();
			List<Trip> trips_res = new ArrayList<Trip>(trip_results.trips);

			// remove old trips for providers that still return them
			if(trips_res.size() >= num_old_trips + num_trips) {
				if(later) {
					// remove the #num_old_trips first trips
					for(int i = 0; i < num_old_trips; i = i+1) {
						trips_res.remove(0);
					}
				}
				else {
					// remove the #num_old_trips last trips
					for(int i = 0; i < num_old_trips; i = i+1) {
						trips_res.remove(trips_res.size()-1);
					}
				}
			}
			// save trip results to have context for next query
			trips = trip_results;

			addTrips(main, trips_res, later);
		}
	}

	public void setProgress(Boolean later, Boolean progress) {
		MenuItem mMenuButtonMoreTrips = mMenu.findItem((later) ? R.id.action_later : R.id.action_earlier);
		PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_trips);

		if(progress) {
			View mActionButtonProgress = getLayoutInflater().inflate(R.layout.actionbar_progress_actionview, null);

			mMenuButtonMoreTrips.setActionView(mActionButtonProgress);
		}
		else {
			mMenuButtonMoreTrips.setActionView(null);
			pullToRefreshView.onRefreshComplete();
		}
	}


}
