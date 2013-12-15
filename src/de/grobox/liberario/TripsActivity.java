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

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TripsActivity extends FragmentActivity {
	private QueryTripsResult trips;
	private ActionMode mActionMode;
	private Menu mMenu;
	private ViewGroup mSelectedTrip;
	private Location from;
	private Location to;
	private int mContainerId = 1;

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
		// TODO activate PullToRefresh only if provider has the capability

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
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trips_activity_actions, menu);
		mMenu = menu;

		if(Preferences.getShowPlatforms(this)) {
			mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_hide_platforms);
		} else {
			mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_show_platforms);
		}

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
			case R.id.action_platforms:
				boolean show = !Preferences.getShowPlatforms(this);

				// change action icon
				if(show) {
					mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_hide_platforms);
				} else {
					mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_show_platforms);
				}
				showPlatforms(show);
				Preferences.setShowPlatforms(this, show);

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
				final LinearLayout trip_layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.trip, null);
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
					LiberarioUtils.setDepartureTimes(this, departureTimeView, departureDelayView, trip.getFirstPublicLeg().departureStop);
				} else {
					departureTimeView.setText(DateUtils.getTime(this, trip.getFirstDepartureTime()));
				}

				// Arrival Time and Delay
				TextView arrivalTimeView = (TextView) row.findViewById(R.id.arrivalTimeView);
				TextView arrivalDelayView = (TextView) row.findViewById(R.id.arrivalDelayView);
				if(trip.getLastPublicLeg() != null) {
					LiberarioUtils.setArrivalTimes(this, arrivalTimeView, arrivalDelayView, trip.getLastPublicLeg().arrivalStop);
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
					public void onClick(View view) {
						View v = main.getChildAt(main.indexOfChild(view) + 1);

						if(v != null) {
							if(v.getVisibility() == View.GONE) {
								v.setVisibility(View.VISIBLE);
							}
							else if(v.getVisibility() == View.VISIBLE) {
								v.setVisibility(View.GONE);
							}
						}
					}

				});
				trip_layout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						selectTrip(view, trip_layout);
						return true;
					}
				});

				// show more button for trip details
				final ImageView showMoreView = (ImageView) trip_layout.findViewById(R.id.showMoreView);
				showMoreView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						selectTrip(view, trip_layout);
					}
				});

				// Create container for trip details fragment
				FrameLayout fragmentContainer = new FrameLayout(this);
				fragmentContainer.setId(mContainerId);
				fragmentContainer.setVisibility(View.GONE);

				// Create a new Fragment to be placed in the activity layout
				TripDetailFragment tripDetailFragment = new TripDetailFragment();

				// In case this activity was started with special instructions from an
				// Intent, pass the Intent's extras to the fragment as arguments
				Bundle bundle = new Bundle();
				bundle.putSerializable("de.schildbach.pte.dto.Trip", trip);
				bundle.putSerializable("de.schildbach.pte.dto.Trip.from", from);
				bundle.putSerializable("de.schildbach.pte.dto.Trip.to", to);
				tripDetailFragment.setArguments(bundle);

				// Add the fragment to the 'fragment_container' FrameLayout
				getSupportFragmentManager().beginTransaction().add(mContainerId, tripDetailFragment).commit();

				mContainerId++;

				if(append) {
					trip_layout.addView(LiberarioUtils.getDivider(this));
					main.addView(trip_layout);
					main.addView(fragmentContainer);
				}
				else {
					trip_layout.addView(LiberarioUtils.getDivider(this), 0);
					main.addView(trip_layout, 0);
					main.addView(fragmentContainer, 1);
				}
			} // end foreach trip

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

	public void onRefreshComplete() {
		PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_trips);
		pullToRefreshView.onRefreshComplete();
	}

	private void showPlatforms(boolean show) {
		for(Fragment fragment : getSupportFragmentManager().getFragments()) {
			TripDetailFragment frag = (TripDetailFragment) fragment;
			frag.showPlatforms(show);
		}
	}

	private void selectTrip(View view, ViewGroup vg) {
		// take care of cases for ActionMode is already activated
		if(mActionMode != null) {
			if(vg.isSelected()) {
				// disable action mode for current item
				mActionMode.finish();

				// exit here to not start new ActionMode
				return;
			} else {
				// deselect previously selected trip
				mSelectedTrip.setSelected(false);

				// disable action mode for current item
				mActionMode.finish();
			}
		}

		// select clicked trip
		mSelectedTrip = vg;
		mSelectedTrip.setSelected(true);

		// active new ActionMode for clicked trip
		mActionMode = startActionMode(mTripActionMode);
	}

	private ActionMode.Callback mTripActionMode = new ActionMode.Callback() {
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.trip_actions, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after onCreateActionMode,
		// but may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch(item.getItemId()) {
				case R.id.action_trip_share:
					Intent sendIntent = new Intent()
					.setAction(Intent.ACTION_SEND)
					.putExtra(Intent.EXTRA_SUBJECT, LiberarioUtils.tripToSubject(getBaseContext(), (Trip) mSelectedTrip.getTag(), true))
					.putExtra(Intent.EXTRA_TEXT, LiberarioUtils.tripToString(getBaseContext(), (Trip) mSelectedTrip.getTag()))
					.setType("text/plain")
					.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
					startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_trip_via)));

					// Action picked, so close the CAB
					mode.finish();
					return true;
				case R.id.action_trip_calender:
					Trip trip = (Trip) mSelectedTrip.getTag();

					Intent intent = new Intent(Intent.ACTION_EDIT)
					.setType("vnd.android.cursor.item/event")
					.putExtra("beginTime", trip.getFirstDepartureTime().getTime())
					.putExtra("endTime", trip.getLastArrivalTime().getTime())
					.putExtra("title", trip.from.name + " → " + trip.to.name)
					.putExtra("description", LiberarioUtils.tripToString(getBaseContext(), trip));
					if(trip.from.place != null) intent.putExtra("eventLocation", trip.from.place);
					startActivity(intent);

					return true;
				case R.id.action_trip_details:
					if(mSelectedTrip != null) {
						showTripDetails(mSelectedTrip.getTag());
					}
					// Action picked, so close the CAB
					mode.finish();
					return true;
				default:
					return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mSelectedTrip.setSelected(false);
			mActionMode = null;
		}
	};
}
