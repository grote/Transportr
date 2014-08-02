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
import java.util.Date;
import java.util.List;

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Individual;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView;

public class TripDetailFragment extends LiberarioFragment {
	private TableLayout view;
	private Trip trip;
	private Location from;
	private Location to;
	private Menu mMenu;
	private boolean mEmbedded = true;
	private List<TableLayout> mStops = new ArrayList<TableLayout>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		view = (TableLayout) inflater.inflate(R.layout.fragment_trip_details, container, false);

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();

		Bundle bundle = getArguments();

		trip = (Trip) bundle.getSerializable("de.schildbach.pte.dto.Trip");
		// also get locations, because the trip locations are sometimes still ambiguous
		from = (Location) bundle.getSerializable("de.schildbach.pte.dto.Trip.from");
		to = (Location) bundle.getSerializable("de.schildbach.pte.dto.Trip.to");

		// set options menu only after retrieving arguments since options depend on them
		setHasOptionsMenu(true);

		addLegs(trip.legs);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(!mEmbedded) {
			// Inflate the menu items for use in the action bar
			inflater.inflate(R.menu.trip_detail_activity_actions, menu);
			mMenu = menu;

			if(Preferences.getShowPlatforms(getActivity())) {
				mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_hide_platforms);
			} else {
				mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_show_platforms);
			}

			// Favorite Trip Button
			MenuItem action_fav_trip = menu.findItem(R.id.action_fav_trip);
			if(from != null && to != null) {
				if(FavFile.isFavTrip(getActivity(), new FavTrip(from, to))) {
					action_fav_trip.setIcon(R.drawable.ic_action_star);
				} else {
					action_fav_trip.setIcon(R.drawable.ic_menu_fav_off);
				}
			} else {
				// this should not even happen, but it might
				action_fav_trip.setVisible(false);
			}
		}

		// show/hide platforms depending on preference
		if(Preferences.getShowPlatforms(getActivity())) {
			showPlatforms(true);
		} else {
			showPlatforms(false);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_platforms:
				boolean show = view.isColumnCollapsed(2);

				// change action icon
				if(show) {
					mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_hide_platforms);
				} else {
					mMenu.findItem(R.id.action_platforms).setIcon(R.drawable.ic_menu_show_platforms);
				}
				showPlatforms(show);
				Preferences.setShowPlatforms(getActivity(), show);

				return true;
			case R.id.action_fav_trip:
				if(FavFile.isFavTrip(getActivity(), new FavTrip(from, to))) {
					new AlertDialog.Builder(getActivity())
					.setMessage(getResources().getString(R.string.clear_fav_trips, 1))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							FavFile.unfavTrip(getActivity(), new FavTrip(from, to));
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
					FavFile.updateFavTrip(getActivity(), new FavTrip(from, to));
					item.setIcon(R.drawable.ic_action_star);
				}

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void setEmbedded(boolean embedded) {
		mEmbedded = embedded;
	}

	private void addLegs(List<Leg> legs) {
		int i = 1;
		TableRow legViewOld = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.trip_details_row, null);
		TableRow legViewNew;

		if(!mEmbedded) legViewOld.setBackgroundResource(R.drawable.selector_list_item);

		// for each leg
		for(final Leg leg : legs) {
			legViewNew = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.trip_details_row, null);
			if(!mEmbedded) legViewNew.setBackgroundResource(R.drawable.selector_list_item);

			TextView dDepartureViewNew = ((TextView) legViewNew.findViewById(R.id.dDepartureView));

			// only for the first leg
			if(i == 1) {
				// hide arrival time for start location of trip
				((TextView) legViewOld.findViewById(R.id.dArrivalTimeView)).setVisibility(View.GONE);
				((TextView) legViewOld.findViewById(R.id.dArrivalDelayView)).setVisibility(View.GONE);
				((TextView) legViewOld.findViewById(R.id.dArrivalPositionView)).setVisibility(View.GONE);
				// only add old view the first time, because it isn't old there
				view.addView(legViewOld);
			}
			// only for the last leg
			if(i >= legs.size()) {
				// hide stuff for last stop (destination)
				((TextView) legViewNew.findViewById(R.id.dDepartureTimeView)).setVisibility(View.GONE);
				((TextView) legViewNew.findViewById(R.id.dDepartureDelayView)).setVisibility(View.GONE);

				((TextView) legViewNew.findViewById(R.id.dDestinationView)).setVisibility(View.GONE);
				((LinearLayout) legViewNew.findViewById(R.id.dLineView)).setVisibility(View.GONE);
				((ImageView) legViewNew.findViewById(R.id.dArrowView)).setVisibility(View.GONE);
				((TextView) legViewNew.findViewById(R.id.dDeparturePositionView)).setVisibility(View.GONE);
				((ImageView) legViewNew.findViewById(R.id.dShowMoreView)).setVisibility(View.GONE);
				((TextView) legViewNew.findViewById(R.id.dMessageView)).setVisibility(View.GONE);
			}

			if(leg instanceof Trip.Public) {
				Public public_leg = ((Public) leg);

				// Departure Time and Delay
				TextView dDepartureTimeView = (TextView) legViewOld.findViewById(R.id.dDepartureTimeView);
				TextView dDepartureDelayView = (TextView) legViewOld.findViewById(R.id.dDepartureDelayView);
				LiberarioUtils.setDepartureTimes(getActivity(), dDepartureTimeView, dDepartureDelayView, public_leg.departureStop);

				// Arrival Time and Delay
				TextView dArrivalTimeView = (TextView) legViewNew.findViewById(R.id.dArrivalTimeView);
				TextView dArrivalDelayView = (TextView) legViewNew.findViewById(R.id.dArrivalDelayView);
				LiberarioUtils.setArrivalTimes(getActivity(), dArrivalTimeView, dArrivalDelayView, public_leg.arrivalStop);

				// set departure location
				((TextView) legViewOld.findViewById(R.id.dDepartureView)).setText(public_leg.departureStop.location.uniqueShortName());

				// set line box
				LiberarioUtils.addLineBox(getActivity(), (LinearLayout) legViewOld.findViewById(R.id.dLineView), public_leg.line);

				// set destination of line
				if(public_leg.destination != null) {
					((TextView) legViewOld.findViewById(R.id.dDestinationView)).setText(public_leg.destination.uniqueShortName());
				} else {
					// hide arrow because this line has no destination
					((View) legViewOld.findViewById(R.id.dArrowView)).setVisibility(View.GONE);
				}

				// set positions
				if(public_leg.departureStop.plannedDeparturePosition != null) {
					((TextView) legViewOld.findViewById(R.id.dDeparturePositionView)).setText(public_leg.departureStop.plannedDeparturePosition.name);
				}
				if(public_leg.arrivalStop.plannedArrivalPosition != null) {
					((TextView) legViewNew.findViewById(R.id.dArrivalPositionView)).setText(public_leg.arrivalStop.plannedArrivalPosition.name);
				}

				// set arrival location in next row
				dDepartureViewNew.setText(public_leg.arrivalStop.location.uniqueShortName());

				// deal with optional trip message
				TextView msgView = (TextView) legViewOld.findViewById(R.id.dMessageView);
				msgView.setVisibility(View.GONE);
				if(public_leg.message != null) {
					msgView.setVisibility(View.VISIBLE);
					msgView.setText(public_leg.message);
				}
				if(public_leg.line.message != null) {
					msgView.setVisibility(View.VISIBLE);
					msgView.setText(msgView.getText() + "\n" + public_leg.line.message);
				}

				if(public_leg.intermediateStops != null && public_leg.intermediateStops.size() > 0) {
					// get and add intermediate stops
					view.addView(getStops(public_leg.intermediateStops));

					// set row as clickable
					legViewOld.setClickable(true);

					// show 'show more' indicator when there are intermediate stops
					((ImageView) legViewOld.findViewById(R.id.dShowMoreView)).setVisibility(View.VISIBLE);

					// make intermediate stops fold out and in on click
					legViewOld.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							ViewGroup parent = ((ViewGroup) view.getParent());
							View v = parent.getChildAt(parent.indexOfChild(view) + 1);
							if(v != null) {
								if(v.getVisibility() == View.GONE) {
									v.setVisibility(View.VISIBLE);
									((ImageView) view.findViewById(R.id.dShowMoreView)).setRotation(180);
								}
								else if(v.getVisibility() == View.VISIBLE) {
									v.setVisibility(View.GONE);
									((ImageView) view.findViewById(R.id.dShowMoreView)).setRotation(0);
								}
							}
						}

					});
				}
			}
			else if(leg instanceof Trip.Individual) {
				final Individual individual = (Trip.Individual) leg;

				((TextView) legViewOld.findViewById(R.id.dDepartureTimeView)).setText(DateUtils.getTime(getActivity(), individual.departureTime));
				// TODO check why time doesn't change
				((TextView) legViewNew.findViewById(R.id.dArrivalTimeView)).setText(DateUtils.getTime(getActivity(), individual.arrivalTime));

				((TextView) legViewOld.findViewById(R.id.dDepartureView)).setText(individual.departure.uniqueShortName());

				LiberarioUtils.addWalkingBox(getActivity(), (LinearLayout) legViewOld.findViewById(R.id.dLineView));

				// show time for walk and optionally distance
				String walk = Integer.toString(individual.min) + " min ";
				if(individual.distance > 0) walk += Integer.toString(individual.distance) + " m";
				((TextView) legViewOld.findViewById(R.id.dDestinationView)).setText(walk);

				((TextView) legViewNew.findViewById(R.id.dDestinationView)).setText(individual.arrival.uniqueShortName());
				dDepartureViewNew.setText(individual.arrival.uniqueShortName());

				// show map button
				ImageView dShowMapView = ((ImageView) legViewOld.findViewById(R.id.dShowMapView));
				dShowMapView.setVisibility(View.VISIBLE);

				// when row clicked, show map
				legViewOld.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// remember arrival station
						List<Location> loc_list = new ArrayList<Location>();
						loc_list.add(individual.arrival);

						// show station on internal map
/*						Intent intent = new Intent(getActivity(), MapStationsActivity.class);
						intent.putExtra("List<de.schildbach.pte.dto.Location>", (ArrayList<Location>) loc_list);
						intent.putExtra("de.schildbach.pte.dto.Location", individual.departure);
						startActivity(intent);
*/
						String aLat = Double.toString(individual.arrival.lat / 1E6);
						String aLon = Double.toString(individual.arrival.lon / 1E6);
						String dLat = Double.toString(individual.departure.lat / 1E6);
						String dLon = Double.toString(individual.departure.lon / 1E6);

						Uri geo = Uri.parse("geo:"+aLat+","+aLon+"?q="+dLat+","+dLon+"(\""+individual.departure.name+"\")");

						// show station on external map
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setData(geo);
						if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
							startActivity(intent);
						}

					}
				});

				// hide arrow and show more icon
				((ImageView) legViewOld.findViewById(R.id.dArrowView)).setVisibility(View.GONE);
				((ImageView) legViewOld.findViewById(R.id.dShowMoreView)).setVisibility(View.GONE);
			}

			view.addView(LiberarioUtils.getDivider(getActivity()));
			view.addView(legViewNew);

			// save new leg view for next run of the loop
			legViewOld = legViewNew;
			i += 1;
		}
		// add horizontal divider at the end
		view.addView(LiberarioUtils.getDivider(getActivity()));
	}

	private TableLayout getStops(List<Stop> stops) {
		TableLayout stopsView = new TableLayout(getActivity());
		stopsView.setVisibility(View.GONE);
		stopsView.setColumnCollapsed(3, true);

		if(stops != null) {
			for(final Stop stop : stops) {
				TableRow stopView = (TableRow) LayoutInflater.from(getActivity()).inflate(R.layout.stop, null);
				if(!mEmbedded) stopView.setBackgroundResource(R.drawable.selector_list_item);

				Date arrivalTime = stop.getArrivalTime();
				Date departureTime = stop.getDepartureTime();

				if(arrivalTime != null) {
					LiberarioUtils.setArrivalTimes(getActivity(), (TextView) stopView.findViewById(R.id.sArrivalTimeView), (TextView) stopView.findViewById(R.id.sArrivalDelayView), stop);
				}
				else {
					((TextView) stopView.findViewById(R.id.sArrivalTimeView)).setVisibility(View.GONE);
					((TextView) stopView.findViewById(R.id.sArrivalDelayView)).setVisibility(View.GONE);
				}

				if(departureTime != null) {
					LiberarioUtils.setDepartureTimes(getActivity(), (TextView) stopView.findViewById(R.id.sDepartureTimeView), (TextView) stopView.findViewById(R.id.sDepartureDelayView), stop);
				}
				else {
					((TextView) stopView.findViewById(R.id.sDepartureTimeView)).setVisibility(View.GONE);
					((TextView) stopView.findViewById(R.id.sDepartureDelayView)).setVisibility(View.GONE);
				}

				((TextView) stopView.findViewById(R.id.sLocationView)).setText(stop.location.uniqueShortName());

				if(stop.plannedArrivalPosition != null) {
					((TextView) stopView.findViewById(R.id.sArrivalPositionView)).setText(stop.plannedArrivalPosition.name);
				}
				if(stop.plannedDeparturePosition != null) {
					((TextView) stopView.findViewById(R.id.sDeparturePositionView)).setText(stop.plannedDeparturePosition.name);
				}

				stopsView.addView(stopView);
			}
		}

		mStops.add(stopsView);
		return stopsView;
	}

	public void showPlatforms(boolean show) {
		// collapse/expand platforms on stops and intermediate stops
		view.setColumnCollapsed(2, !show);
		for(final TableLayout stopView : mStops) {
			stopView.setColumnCollapsed(3, !show);
		}
	}

}
