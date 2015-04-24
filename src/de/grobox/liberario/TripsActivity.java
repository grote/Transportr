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
import java.util.Comparator;
import java.util.List;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

import de.grobox.liberario.ui.FlowLayout;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TripsActivity extends AppCompatActivity {
	private List<Trip> trips;
	private QueryTripsResult start_context;
	private QueryTripsResult end_context;
	private Menu mMenu;
	private Location from;
	private Location to;
	private int mContainerId = 1;

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

		final TableLayout main = (TableLayout) findViewById(R.id.activity_trips);

		// add horizontal divider at top
		main.addView(LiberarioUtils.getDivider(this));

		Intent intent = getIntent();
		start_context = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");
		end_context = start_context;
		// also get locations, because the trip locations are sometimes still ambiguous
		from = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.from");
		to = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.Trip.to");

		setHeader();

		addTrips(main, start_context.trips);
	}

	@Override
	protected void onStart() {
		super.onStart();

		PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_trips);

		if(start_context.context.canQueryEarlier() && end_context.context.canQueryLater()) {
			pullToRefreshView.setMode(Mode.BOTH);
		} else if(start_context.context.canQueryEarlier()) {
			pullToRefreshView.setMode(Mode.PULL_FROM_START);
		} else if(end_context.context.canQueryLater()) {
			pullToRefreshView.setMode(Mode.PULL_FROM_END);
		} else {
			pullToRefreshView.setMode(Mode.DISABLED);
			return;
		}

		// Set a listener to be invoked when the list should be refreshed.
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

		if(Preferences.getPref(this, Preferences.SHOW_EXTRA_INFO)) {
			mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_collapse);
		} else {
			mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_expand);
		}
/*
		if(FavDB.isFavTrip(getBaseContext(), new FavTrip(from, to))) {
			menu.findItem(R.id.action_fav_trip).setIcon(R.drawable.ic_action_star);
		} else {
			menu.findItem(R.id.action_fav_trip).setIcon(R.drawable.ic_menu_fav_off);
		}
*/
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			case R.id.action_show_extra_info:
				boolean show = !Preferences.getPref(this, Preferences.SHOW_EXTRA_INFO);

				// change action icon
				if(show) {
					mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_collapse);
				} else {
					mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_expand);
				}
				showExtraInfo(show);
				Preferences.setPref(this, Preferences.SHOW_EXTRA_INFO, show);

				return true;
/*			case R.id.action_fav_trip:
				if(FavDB.isFavTrip(this, new FavTrip(from, to))) {
					new AlertDialog.Builder(this)
					.setMessage(getResources().getString(R.string.clear_fav_trips, 1))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							FavDB.unfavTrip(getBaseContext(), new FavTrip(from, to));
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
					FavDB.updateFavTrip(getBaseContext(), new FavTrip(from, to));
					item.setIcon(R.drawable.ic_action_star);
				}

				return true;
*/			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void startGetMoreTrips(boolean later) {
		if(later) (new AsyncQueryMoreTripsTask(this, end_context.context, true)).execute();
		else (new AsyncQueryMoreTripsTask(this, start_context.context, false)).execute();
	}

	private void setHeader() {
		if(start_context != null) {
			((TextView) findViewById(R.id.tripStartTextView)).setText(start_context.from.uniqueShortName());
			((TextView) findViewById(R.id.tripDestinationTextView)).setText(start_context.to.uniqueShortName());

			if(start_context.trips.get(0) != null) {
				// add Date on top
				((TextView) findViewById(R.id.dateView2)).setText(DateUtils.getDate(this, start_context.trips.get(0).getFirstDepartureTime()));
			} else {
				((TextView) findViewById(R.id.dateView2)).setText("???");
			}
		}
	}

	private void addTrips(final TableLayout main, List<Trip> trip_list, boolean append) {
		if(trip_list != null) {
			// sorting trips by departure time
			Comparator<Trip> comp = new Comparator<Trip>() {
				public int compare(Trip trip1, Trip trip2) {
					return trip1.getFirstDepartureTime().compareTo(trip2.getFirstDepartureTime());
				}
			};
			Collections.sort(trip_list, comp);

			// reverse order of trips if they should be prepended
			if(!append) {
				Collections.reverse(trip_list);
			}

			ArrayList<Trip> new_trip_list = new ArrayList<>(trip_list);
			for(final Trip trip : new_trip_list) {
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
				if(trip.legs.size() > 0 && trip.legs.get(0) instanceof Trip.Public) {
					LiberarioUtils.setDepartureTimes(this, departureTimeView, departureDelayView, ((Public) trip.legs.get(0)).departureStop);
				} else {
					departureTimeView.setText(DateUtils.getTime(this, trip.getFirstDepartureTime()));
					// show delay for last public leg
					final Public pleg = trip.getFirstPublicLeg();
					if(pleg != null && pleg.getDepartureDelay() != null) {
						departureDelayView.setText(LiberarioUtils.getDelayText(pleg.getDepartureDelay()));
					}
				}

				// Arrival Time and Delay
				TextView arrivalTimeView = (TextView) row.findViewById(R.id.arrivalTimeView);
				TextView arrivalDelayView = (TextView) row.findViewById(R.id.arrivalDelayView);
				final Leg last_leg = trip.legs.get(trip.legs.size() - 1);
				if(last_leg != null && last_leg instanceof Trip.Public) {
					LiberarioUtils.setArrivalTimes(this, arrivalTimeView, arrivalDelayView, ((Public) last_leg).arrivalStop);
				} else {
					arrivalTimeView.setText(DateUtils.getTime(this, trip.getLastArrivalTime()));
					// show delay for last public leg
					final Public pleg = trip.getLastPublicLeg();
					if(pleg != null && pleg.getArrivalDelay() != null) {
						arrivalDelayView.setText(LiberarioUtils.getDelayText(pleg.getArrivalDelay()));
					}
				}

				// Duration
				TextView durationView = (TextView) trip_layout.findViewById(R.id.durationView);
				durationView.setText(DateUtils.getDuration(trip.getDuration()));

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
								view.setActivated(true);
							}
							else if(v.getVisibility() == View.VISIBLE) {
								v.setVisibility(View.GONE);
								view.setActivated(false);
							}
						}
					}

				});

				// show more button for trip details
				final ImageView showMoreView = (ImageView) trip_layout.findViewById(R.id.showMoreView);

				// Creating PopupMenu
				final PopupMenu popup = new PopupMenu(getApplicationContext(), showMoreView);
				popup.getMenuInflater().inflate(R.menu.trip_actions, popup.getMenu());
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						// handle presses on menu items
						switch(item.getItemId()) {
							// Share Trip
							case R.id.action_trip_share:
								Intent sendIntent = new Intent()
										                    .setAction(Intent.ACTION_SEND)
										                    .putExtra(Intent.EXTRA_SUBJECT, LiberarioUtils.tripToSubject(getBaseContext(), trip, true))
										                    .putExtra(Intent.EXTRA_TEXT, LiberarioUtils.tripToString(getBaseContext(), trip))
										                    .setType("text/plain")
										                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
								startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_trip_via)));

								return true;
							// Add Trip to Calendar
							case R.id.action_trip_calender:
								Intent intent = new Intent(Intent.ACTION_EDIT)
										                .setType("vnd.android.cursor.item/event")
										                .putExtra("beginTime", trip.getFirstDepartureTime().getTime())
										                .putExtra("endTime", trip.getLastArrivalTime().getTime())
										                .putExtra("title", trip.from.name + " → " + trip.to.name)
										                .putExtra("description", LiberarioUtils.tripToString(getBaseContext(), trip));
								if(trip.from.place != null) intent.putExtra("eventLocation", trip.from.place);
								startActivity(intent);

								return true;
							// Show Trip Details on Separate Screen
							case R.id.action_trip_details:
								showTripDetails(trip);
								return true;
							default:
								return false;
						}
					}
				});
				LiberarioUtils.showPopupIcons(popup);

				showMoreView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						popup.show();
					}
				});

				trip_layout.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						popup.show();
						return false;
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

					// save complete list of trips
					if(trips == null) trips = trip_list;
					else trips.addAll(new ArrayList<>(trip_list));
				}
				else {
					trip_layout.addView(LiberarioUtils.getDivider(this), 0);
					main.addView(trip_layout, 0);
					main.addView(fragmentContainer, 1);

					// save complete list of trips
					if(trips == null) trips = trip_list;
					else trips.addAll(0, new ArrayList<>(trip_list));
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

	public void addMoreTrips(QueryTripsResult trip_results, boolean later) {
		QueryTripsResult trips_context;

		if(later) trips_context = end_context;
		else trips_context = start_context;

		if(trips_context != null) {
			// remove old trips for providers that return them
			List<Trip> trips_new = new ArrayList<>();
			for(Trip trip : trip_results.trips) {
				boolean add = true;
				for(Trip trip_old : trips_context.trips) {
					if(trip.equals(trip_old)) {
						add = false;
						break;
					}
				}
				// only add trip if not a duplicate
				if(add) trips_new.add(trip);
			}

			// save trip results to have context for next query
			if(later) end_context = trip_results;
			else start_context = trip_results;

			addTrips((TableLayout) findViewById(R.id.activity_trips), trips_new, later);
		}
	}

	public void onRefreshComplete() {
		PullToRefreshScrollView pullToRefreshView = (PullToRefreshScrollView) findViewById(R.id.pull_to_refresh_trips);
		pullToRefreshView.onRefreshComplete();
	}

	private void showExtraInfo(boolean show) {
		findViewById(R.id.dateView1).setVisibility(show ? View.VISIBLE : View.GONE);
		findViewById(R.id.dateView2).setVisibility(show ? View.VISIBLE : View.GONE);
		for(Fragment fragment : getSupportFragmentManager().getFragments()) {
			TripDetailFragment frag = (TripDetailFragment) fragment;
			frag.showExtraInfo(show);
		}
	}
}
