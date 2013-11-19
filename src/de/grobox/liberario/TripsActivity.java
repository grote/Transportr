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
import java.util.Date;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TripsActivity extends Activity {
	private QueryTripsResult trips;
	private Menu mMenu;
	private Location from;
	private Location to;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trips);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		final TableLayout main = (TableLayout) findViewById(R.id.activity_trips);

		// add horizontal divider at top
		main.addView(LiberarioUtils.getDivider(this));

		Intent intent = getIntent();
		trips = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");
		// also get locations, because the trip locations are sometimes still ambiguous
		from = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.from");
		to = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.to");

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO show later/earlier options only if provider has the capability

		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trips_activity_actions, menu);
		mMenu = menu;

		if(FavFile.isFavTrip(getBaseContext(), new FavTrip(from, to))) {
			menu.findItem(R.id.action_fav_trip).setIcon(R.drawable.ic_menu_fav_on);
		} else {
			menu.findItem(R.id.action_fav_trip).setIcon(R.drawable.ic_menu_fav_off);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			case R.id.action_fav_trip:
				if(FavFile.isFavTrip(this, new FavTrip(from, to))) {
					new AlertDialog.Builder(this)
					.setMessage(getResources().getString(R.string.clear_fav_trips))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							FavFile.unfavTrip(getBaseContext(), new FavTrip(from, to));
							item.setIcon(R.drawable.ic_menu_fav_off);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					})
					.show();
				} else {
					FavFile.favTrip(getBaseContext(), new FavTrip(from, to));
					item.setIcon(R.drawable.ic_menu_fav_on);
				}

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

	public void startGetMoreTrips(boolean later) {
		(new AsyncQueryMoreTripsTask(this, trips.context, later)).execute();
	}

	private void setHeader() {
		((TextView) findViewById(R.id.tripStartTextView)).setText(trips.from.uniqueShortName());
		((TextView) findViewById(R.id.tripDestinationTextView)).setText(trips.to.uniqueShortName());
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
				fromView.setText(trip.from.uniqueShortName());
				TextView toView = ((TextView) row.findViewById(R.id.toView));
				toView.setText(trip.to.uniqueShortName());

				// Departure Time and Delay
				TextView departureTimeView  = (TextView) row.findViewById(R.id.departureTimeView);
				TextView departureDelayView = (TextView) row.findViewById(R.id.departureDelayView);
				if(trip.getFirstPublicLeg() != null) {
					setDepartureTimes(this, departureTimeView, departureDelayView, trip.getFirstPublicLeg().departureStop);
				} else {
					departureTimeView.setText(DateUtils.getTime(this, trip.getFirstDepartureTime()));
				}

				// Arrival Time and Delay
				TextView arrivalTimeView = (TextView) row.findViewById(R.id.arrivalTimeView);
				TextView arrivalDelayView = (TextView) row.findViewById(R.id.arrivalDelayView);
				if(trip.getLastPublicLeg() != null) {
					setArrivalTimes(this, arrivalTimeView, arrivalDelayView, trip.getLastPublicLeg().arrivalStop);
				} else {
					arrivalTimeView.setText(DateUtils.getTime(this, trip.getLastArrivalTime()));
				}

				// Duration
				TextView durationView = (TextView) trip_layout.findViewById(R.id.durationView);
				durationView.setText(DateUtils.getDuration(trip.getFirstDepartureTime(), trip.getLastArrivalTime()));

				// Transports
				FlowLayout lineLayout = (FlowLayout) trip_layout.findViewById(R.id.lineLayout);

				// for each leg
				for(final Leg leg : trip.legs) {
					if(leg instanceof Trip.Public) {
						LiberarioUtils.addLineBox(this, lineLayout, ((Public) leg).line);
					}
					else if(leg instanceof Trip.Individual) {
						LiberarioUtils.addWalkingBox(this, lineLayout);
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
					trip_layout.addView(LiberarioUtils.getDivider(this));
					main.addView(trip_layout);
				}
				else {
					trip_layout.addView(LiberarioUtils.getDivider(this), 0);
					main.addView(trip_layout, 0);
				}
			}

		}
		else {
			// TODO offer option to query again for trips
		}
	}

	private void addTrips(final TableLayout main, List<Trip> trips) {
		addTrips(main, trips, true);
	}

	private void showTripDetails(Object o) {
		Trip trip = (Trip) o;

		Intent intent = new Intent(this, TripDetailActivity.class);
		intent.putExtra("de.schildbach.pte.dto.Trip", trip);
		intent.putExtra("de.schildbach.pte.dto.Trip.from", from);
		intent.putExtra("de.schildbach.pte.dto.Trip.to", to);
		startActivity(intent);
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

	static public void setArrivalTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		Date time = new Date(stop.getArrivalTime().getTime());

		if(stop.isArrivalTimePredicted() && stop.getArrivalDelay() != null) {
			long delay = stop.getArrivalDelay();
			time.setTime(time.getTime() - delay);

			if(delay > 0) {
				delayView.setText("+" + Long.toString(delay / 1000 / 60));
			}
		}
		timeView.setText(DateUtils.getTime(context, time));
	}

	static public void setDepartureTimes(Context context, TextView timeView, TextView delayView, Stop stop) {
		Date time = new Date(stop.getDepartureTime().getTime());

		if(stop.isDepartureTimePredicted() && stop.getDepartureDelay() != null) {
			long delay = stop.getDepartureDelay();
			time.setTime(time.getTime() - delay);

			if(delay > 0) {
				delayView.setText("+" + Long.toString(delay / 1000 / 60));
			}
		}
		timeView.setText(DateUtils.getTime(context, time));
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
