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

import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Individual;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ImageView;

public class TripDetailFragment extends LiberarioFragment {
	private TableLayout view;
//	private Location from;
//	private Location to;
	private Menu mMenu;
	private boolean mEmbedded = true;
	private List<TableLayout> mStops = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		view = (TableLayout) inflater.inflate(R.layout.fragment_trip_details, container, false);

		Bundle bundle = getArguments();

		Trip trip = (Trip) bundle.getSerializable("de.schildbach.pte.dto.Trip");

		// also get locations, because the trip locations are sometimes still ambiguous (required for fav trip)
//		from = (Location) bundle.getSerializable("de.schildbach.pte.dto.Trip.from");
//		to = (Location) bundle.getSerializable("de.schildbach.pte.dto.Trip.to");

		// set options menu only after retrieving arguments since options depend on them
		setHasOptionsMenu(true);

		addLegs(trip.legs);

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if(!mEmbedded) {
			// Inflate the menu items for use in the action bar
			inflater.inflate(R.menu.trip_detail_activity_actions, menu);
			mMenu = menu;

			if(Preferences.getPref(getActivity(), Preferences.SHOW_EXTRA_INFO)) {
				mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_collapse);
			} else {
				mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_expand);
			}

			// Favorite Trip Button
/*			MenuItem action_fav_trip = menu.findItem(R.id.action_fav_trip);
			if(from != null && to != null) {
				if(FavDB.isFavTrip(getActivity(), new FavTrip(from, to))) {
					action_fav_trip.setIcon(R.drawable.ic_action_star);
				} else {
					action_fav_trip.setIcon(R.drawable.ic_menu_fav_off);
				}
			} else {
				// this should not even happen, but it might
				action_fav_trip.setVisible(false);
			}
*/
		}

		// show/hide platforms depending on preference
		if(Preferences.getPref(getActivity(), Preferences.SHOW_EXTRA_INFO)) {
			showExtraInfo(true);
		} else {
			showExtraInfo(false);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_show_extra_info:
				boolean show = view.isColumnCollapsed(2);

				// change action icon
				if(show) {
					mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_collapse);
				} else {
					mMenu.findItem(R.id.action_show_extra_info).setIcon(R.drawable.ic_action_navigation_expand);
				}
				showExtraInfo(show);
				Preferences.setPref(getActivity(), Preferences.SHOW_EXTRA_INFO, show);

				return true;
/*			case R.id.action_fav_trip:
				if(FavDB.isFavTrip(getActivity(), new FavTrip(from, to))) {
					new AlertDialog.Builder(getActivity())
					.setMessage(getResources().getString(R.string.clear_fav_trips, 1))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							FavDB.unfavTrip(getActivity(), new FavTrip(from, to));
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
					FavDB.updateFavTrip(getActivity(), new FavTrip(from, to));
					item.setIcon(R.drawable.ic_action_star);
				}

				return true;
*/			default:
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
				legViewOld.findViewById(R.id.dArrivalTimeView).setVisibility(View.GONE);
				legViewOld.findViewById(R.id.dArrivalDelayView).setVisibility(View.GONE);
				legViewOld.findViewById(R.id.dArrivalPositionView).setVisibility(View.GONE);
				// only add old view the first time, because it isn't old there
				view.addView(legViewOld);
			}
			// only for the last leg
			if(i >= legs.size()) {
				// hide arrow for last stop (destination)
				legViewNew.findViewById(R.id.dArrowView).setVisibility(View.GONE);
			}

			// Creating PopupMenu for leg
			final PopupMenu popup = new PopupMenu(getActivity(), legViewOld);
			popup.getMenuInflater().inflate(R.menu.leg_actions, popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				public boolean onMenuItemClick(MenuItem item) {
					// handle presses on menu items
					switch(item.getItemId()) {
						// Show On Map
						case R.id.action_show_on_external_map:
							LiberarioUtils.startGeoIntent(getActivity(), leg.departure);

							return true;
						// Share Leg
						case R.id.action_leg_share:
							Intent sendIntent = new Intent()
									                    .setAction(Intent.ACTION_SEND)
									                    .putExtra(Intent.EXTRA_SUBJECT, leg.departure.uniqueShortName())
									                    .putExtra(Intent.EXTRA_TEXT, LiberarioUtils.legToString(getActivity(), leg))
									                    .setType("text/plain")
									                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_trip_share)));

							return true;
						// Copy Leg to Clipboard
						case R.id.action_copy:
							LiberarioUtils.copyToClipboard(getActivity(), leg.departure.uniqueShortName());

							return true;
						default:
							return false;
					}
				}
			});
			LiberarioUtils.showPopupIcons(popup);

			legViewOld.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					popup.show();
					return true;
				}
			});

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
					legViewOld.findViewById(R.id.dArrowView).setVisibility(View.GONE);
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
					msgView.setText(Html.fromHtml(public_leg.message).toString());
				}
				if(public_leg.line.message != null) {
					msgView.setVisibility(View.VISIBLE);
					msgView.setText(msgView.getText() + "\n" + Html.fromHtml(public_leg.line.message).toString());
				}

				// show stops if available
				if(public_leg.intermediateStops != null && public_leg.intermediateStops.size() > 0) {
					// get and add intermediate stops
					view.addView(getStops(public_leg.intermediateStops));

					// show 'show more' indicator when there are intermediate stops
					legViewOld.findViewById(R.id.dShowMoreView).setVisibility(View.VISIBLE);

					// make intermediate stops fold out and in on click
					legViewOld.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							ViewGroup parent = ((ViewGroup) view.getParent());
							View v = parent.getChildAt(parent.indexOfChild(view) + 1);
							if(v != null) {
								if(v.getVisibility() == View.GONE) {
									v.setVisibility(View.VISIBLE);
									view.findViewById(R.id.dShowMoreView).setRotation(180);
								}
								else if(v.getVisibility() == View.VISIBLE) {
									v.setVisibility(View.GONE);
									view.findViewById(R.id.dShowMoreView).setRotation(0);
								}
							}
						}

					});
				} else {
					// show popup on simple click if no stops
					legViewOld.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							popup.show();
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

				// set arrival station name
				dDepartureViewNew.setText(individual.arrival.uniqueShortName());

				// show map button
				ImageView dShowMapView = ((ImageView) legViewOld.findViewById(R.id.dShowMapView));
				dShowMapView.setVisibility(View.VISIBLE);

				// when row clicked, show map
				dShowMapView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						// remember arrival station
/*						List<Location> loc_list = new ArrayList<Location>();
						loc_list.add(individual.arrival);

						// show station on internal map
						Intent intent = new Intent(getActivity(), MapStationsActivity.class);
						intent.putExtra("List<de.schildbach.pte.dto.Location>", (ArrayList<Location>) loc_list);
						intent.putExtra("de.schildbach.pte.dto.Location", individual.departure);
						startActivity(intent);
*/
						LiberarioUtils.startGeoIntent(getActivity(), individual.arrival);
					}
				});

				// show popup on simple click as well
				legViewOld.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						popup.show();
					}
				});

				// hide arrow and show more icon
				legViewOld.findViewById(R.id.dArrowView).setVisibility(View.GONE);
				legViewOld.findViewById(R.id.dShowMoreView).setVisibility(View.GONE);
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
					stopView.findViewById(R.id.sArrivalTimeView).setVisibility(View.GONE);
					stopView.findViewById(R.id.sArrivalDelayView).setVisibility(View.GONE);
				}

				if(departureTime != null) {
					LiberarioUtils.setDepartureTimes(getActivity(), (TextView) stopView.findViewById(R.id.sDepartureTimeView), (TextView) stopView.findViewById(R.id.sDepartureDelayView), stop);
				}
				else {
					stopView.findViewById(R.id.sDepartureTimeView).setVisibility(View.GONE);
					stopView.findViewById(R.id.sDepartureDelayView).setVisibility(View.GONE);
				}

				((TextView) stopView.findViewById(R.id.sLocationView)).setText(stop.location.uniqueShortName());

				if(stop.plannedArrivalPosition != null) {
					((TextView) stopView.findViewById(R.id.sArrivalPositionView)).setText(stop.plannedArrivalPosition.name);
				}
				if(stop.plannedDeparturePosition != null) {
					((TextView) stopView.findViewById(R.id.sDeparturePositionView)).setText(stop.plannedDeparturePosition.name);
				}

				// Creating PopupMenu for stop
				final PopupMenu popup = new PopupMenu(getActivity(), stopView);
				popup.getMenuInflater().inflate(R.menu.leg_actions, popup.getMenu());
				popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					public boolean onMenuItemClick(MenuItem item) {
						// handle presses on menu items
						switch(item.getItemId()) {
							// Show On Map
							case R.id.action_show_on_external_map:
								LiberarioUtils.startGeoIntent(getActivity(), stop.location);

								return true;
							// Share Stop
							case R.id.action_leg_share:
								Intent sendIntent = new Intent()
										                    .setAction(Intent.ACTION_SEND)
										                    .putExtra(Intent.EXTRA_SUBJECT, stop.location.uniqueShortName())
										                    .putExtra(Intent.EXTRA_TEXT, DateUtils.getTime(getActivity(), stop.getArrivalTime()) + " " + stop.location.uniqueShortName())
										                    .setType("text/plain")
										                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
								startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.action_trip_share)));

								return true;
							// Copy Stop to Clipboard
							case R.id.action_copy:
								LiberarioUtils.copyToClipboard(getActivity(), stop.location.uniqueShortName());

								return true;
							default:
								return false;
						}
					}
				});
				LiberarioUtils.showPopupIcons(popup);

				// show popup on click
				stopView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						popup.show();
					}
				});
				// show popup also on long click
				stopView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View view) {
						popup.show();
						return true;
					}
				});

				stopsView.addView(stopView);
			}
		}

		mStops.add(stopsView);
		return stopsView;
	}

	public void showExtraInfo(boolean show) {
		// collapse/expand platforms on stops and intermediate stops
		view.setColumnCollapsed(2, !show);
		for(final TableLayout stopView : mStops) {
			stopView.setColumnCollapsed(3, !show);
		}
	}

}
