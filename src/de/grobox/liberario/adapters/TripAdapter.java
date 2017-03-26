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

package de.grobox.liberario.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.grobox.liberario.ListTrip;
import de.grobox.liberario.R;
import de.grobox.liberario.ui.LegPopupMenu;
import de.grobox.liberario.ui.SwipeDismissRecyclerViewTouchListener;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripHolder>{

	private SortedList<ListTrip> trips = new SortedList<>(ListTrip.class, new SortedList.Callback<ListTrip>(){
		@Override
		public void onInserted(int position, int count) {
			notifyItemRangeInserted(position, count);
		}

		@Override
		public void onChanged(int position, int count) {
			notifyItemRangeChanged(position, count);
		}

		@Override
		 public void onMoved(int fromPosition, int toPosition) {
			notifyItemMoved(fromPosition, toPosition);
		}

		@Override
		 public void onRemoved(int position, int count) {
			notifyItemRangeRemoved(position, count);
		}

		@Override
		 public int compare(ListTrip t1, ListTrip t2) {
			return t1.trip.getFirstDepartureTime().compareTo(t2.trip.getFirstDepartureTime());
		}

		@Override
		public boolean areItemsTheSame(ListTrip t1, ListTrip t2) {
			return t1.trip.equals(t2.trip);
		}

		@Override
		 public boolean areContentsTheSame(ListTrip t_old, ListTrip t_new) {
			// keep expanded state when trip is updated in case Provider returns same trips
			t_new.expanded = t_old.expanded;

			// return whether the trips' visual representations are the same or not
			return t_old.trip.equals(t_new.trip);
		}
	});
	private SwipeDismissRecyclerViewTouchListener touchListener;
	private Context context;
	private List<ListTrip> removed;
	private final boolean showLineName;

	public TripAdapter(List<ListTrip> trips, SwipeDismissRecyclerViewTouchListener touchListener, Context context, boolean showLineName) {
		this.touchListener = touchListener;
		this.context = context;
		this.removed = new ArrayList<>();
		this.showLineName = showLineName;

		addAll(trips);
	}

	@Override
	public int getItemViewType(int position) {
		return trips.get(position).trip.legs.size();
	}

	@Override
	public TripHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(context).inflate(R.layout.trip, viewGroup, false);

		return new TripHolder(v, i);
	}

	@Override
	public void onBindViewHolder(final TripHolder ui, final int position) {
		// get trip with position from View Holder as this one gets updated when trips are removed
		final ListTrip trip = trips.get(ui.getAdapterPosition());
		ViewCompat.setTransitionName(ui.card, trip.trip.getId());

		// listen to touches also in the card, because it does not work only in the RecyclerView
		ui.card.setOnTouchListener(touchListener);

		// re-apply current expansion saved in trip
		expandTrip(ui, !trip.expanded);

		// Clear Transport Icons to avoid accumulation when same trips are returned
		ui.lines.removeAllViews();

		int i = 0;
		for(final Trip.Leg leg : trip.trip.legs) {
			bindLeg(context, ui.legs.get(i), leg, false, ui.lines, showLineName);

			// show departure delay also on overview when trip is folded
			if(i == 0 && leg instanceof Trip.Public) {
				TransportrUtils.setDepartureTimes(context, ui.legs.get(i).departureTime, ui.departureDelay, ((Trip.Public)leg).departureStop);
			}

			i += 1;
		}

		// hide last divider
		ui.legs.get(trip.trip.legs.size() - 1).divider.setVisibility(View.GONE);

		// Show Number of Changes for long trips
		Integer numChanges = trip.trip.getNumChanges();
		if(numChanges != null && numChanges > 3) {
			ui.changes.setText(String.valueOf(numChanges));
			ui.changes.setVisibility(View.VISIBLE);
		} else {
			ui.changes.setVisibility(View.GONE);
		}

		// Show Trip Duration
		ui.duration.setText(DateUtils.getDuration(trip.trip.getDuration()));

		// Re-color arrow down
		ui.lineArrowDown.setImageDrawable(TransportrUtils.getTintedDrawable(context, ui.lineArrowDown.getDrawable()));

		// Show Trip on Map
		ui.map.setImageDrawable(TransportrUtils.getTintedDrawable(context, ui.map.getDrawable()));
		ui.map.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TransportrUtils.showTripOnMap(context, trip.trip);
			}
		});

		// Share Trip
		ui.share.setImageDrawable(TransportrUtils.getTintedDrawable(context, ui.share.getDrawable()));
		ui.share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TransportrUtils.share(context, trip.trip);
			}
		});

		// Add Trip to Calendar
		ui.calendar.setImageDrawable(TransportrUtils.getTintedDrawable(context, ui.calendar.getDrawable()));
		ui.calendar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TransportrUtils.intoCalendar(context, trip.trip);
			}
		});

		// Expand Card
		if(trip.trip.legs.size() == 1) {
			ui.expand.setVisibility(View.GONE);
		} else {
			ui.expand.setOnClickListener(new View.OnClickListener() {
				                             @Override
				                             public void onClick(View view) {
					                             expandTrip(ui, trip.expanded);
					                             trip.expanded = !trip.expanded;
				                             }
			                             }
			);
		}
	}

	@Override
	public int getItemCount() {
		return trips == null ? 0 : trips.size();
	}

	public int getPosition(Trip trip) {
		return trips.indexOf(new ListTrip(trip));
	}

	private static void bindLeg(Context context, final LegHolder leg_holder, Trip.Leg leg, boolean detail, FlowLayout lines, boolean showLineName) {
		// Locations
		leg_holder.departureLocation.setText(TransportrUtils.getLocName(leg.departure));
		leg_holder.arrivalLocation.setText(TransportrUtils.getLocName(leg.arrival));

		if(detail) {
			final LegPopupMenu departurePopup = new LegPopupMenu(context, leg_holder.departureLocation, leg);
			leg_holder.departureLocation.setOnClickListener(new View.OnClickListener() {
				                                                @Override
				                                                public void onClick(View view) {
					                                                departurePopup.show();
				                                                }
			                                                }
			);
			final LegPopupMenu arrivalPopup = new LegPopupMenu(context, leg_holder.arrivalLocation, leg, true);
			leg_holder.arrivalLocation.setOnClickListener(new View.OnClickListener() {
				                                                @Override
				                                                public void onClick(View view) {
					                                                arrivalPopup.show();
				                                                }
			                                                }
			);
		} else {
			leg_holder.departureLocation.setBackgroundResource(android.R.color.transparent);
			leg_holder.arrivalLocation.setBackgroundResource(android.R.color.transparent);
		}

		// Re-color arrow down
		leg_holder.arrowDown.setImageDrawable(TransportrUtils.getTintedDrawable(context, leg_holder.arrowDown.getDrawable()));

		// Leg duration
		leg_holder.duration.setText(DateUtils.getDuration(leg.getDepartureTime(), leg.getArrivalTime()));

		if(leg instanceof Trip.Public) {
			Trip.Public public_leg = ((Trip.Public) leg);

			TransportrUtils.setArrivalTimes(context, leg_holder.arrivalTime, leg_holder.arrivalDelay, public_leg.arrivalStop);
			TransportrUtils.setDepartureTimes(context, leg_holder.departureTime, leg_holder.departureDelay, public_leg.departureStop);

			if(leg_holder.arrivalDelay.getText() == null || leg_holder.arrivalDelay.getText().equals("")) {
				// hide delay view if there's no delay
				leg_holder.arrivalDelay.setVisibility(View.GONE);
			}

			// Departure Platform
			if(detail && public_leg.getDeparturePosition() != null) {
				leg_holder.departurePlatform.setText(public_leg.getDeparturePosition().toString());
			} else {
				leg_holder.departurePlatform.setVisibility(View.GONE);
			}

			// Arrival Platform
			if(detail && public_leg.getArrivalPosition() != null) {
				leg_holder.arrivalPlatform.setText(public_leg.getArrivalPosition().toString());
			} else {
				leg_holder.arrivalPlatform.setVisibility(View.GONE);
			}

			leg_holder.line.removeViewAt(0);
			TransportrUtils.addLineBox(context, leg_holder.line, public_leg.line, 0);
			if(lines != null) TransportrUtils.addLineBox(context, lines, public_leg.line);

			if(showLineName && public_leg.line.name != null) {
				leg_holder.lineDestination.setText(public_leg.line.name);
				leg_holder.arrow.setVisibility(View.INVISIBLE);
			}
			else if(public_leg.destination != null) {
				leg_holder.lineDestination.setText(TransportrUtils.getLocName(public_leg.destination));
				leg_holder.arrow.setImageDrawable(TransportrUtils.getTintedDrawable(context, leg_holder.arrow.getDrawable()));
			} else {
				// hide arrow because this line has no name and no destination
				leg_holder.arrow.setVisibility(View.GONE);
			}

			if(detail) {
				// show intermediate stops and leg duration
				bindStops(context, leg_holder, public_leg.intermediateStops);

				// optional trip message
				if(public_leg.message != null) {
					leg_holder.message.setVisibility(View.VISIBLE);
					leg_holder.message.setText(Html.fromHtml(public_leg.message).toString());
				}
				if(public_leg.line.message != null) {
					leg_holder.message.setVisibility(View.VISIBLE);
					leg_holder.message.setText(leg_holder.message.getText() + "\n" + Html.fromHtml(public_leg.line.message).toString());
				}
			}
		}
		else if(leg instanceof Trip.Individual) {
			final Trip.Individual individual = (Trip.Individual) leg;

			leg_holder.arrivalTime.setText(DateUtils.getTime(context, individual.arrivalTime));
			leg_holder.departureTime.setText(DateUtils.getTime(context, individual.departureTime));

			// TODO carry over delay from last leg somehow?
			leg_holder.arrivalDelay.setVisibility(View.GONE);

			leg_holder.line.removeViewAt(0);
			TransportrUtils.addWalkingBox(context, leg_holder.line, 0);
			if(lines != null) TransportrUtils.addWalkingBox(context, lines);

			if(detail) {
				final LegPopupMenu walkPopup = new LegPopupMenu(context, leg_holder.line, leg);
				leg_holder.info.setOnClickListener(new View.OnClickListener() {
					                                                @Override
					                                                public void onClick(View view) {
						                                                walkPopup.show();
					                                                }
				                                                }
				);
			} else {
				leg_holder.info.setBackgroundResource(android.R.color.transparent);
			}

			// hide arrow because this line has no destination
			leg_holder.arrow.setVisibility(View.GONE);

			// show distance
			if(individual.distance > 0) {
				leg_holder.lineDestination.setText("  " + Integer.toString(individual.distance) + " " + context.getString(R.string.meter));
			} else {
				leg_holder.lineDestination.setVisibility(View.GONE);
			}
		}
	}

	static public void bindLeg(Context context, LegHolder leg_holder, Trip.Leg leg, boolean detail, boolean showLineName) {
		bindLeg(context, leg_holder, leg, detail, null, showLineName);
	}

	private static void bindStops(Context context, final LegHolder leg_holder, List<Stop> stops) {
		leg_holder.stops.setVisibility(View.GONE);
		leg_holder.duration.setVisibility(View.GONE);

		leg_holder.info.setOnClickListener(new View.OnClickListener() {
			                                   @Override
			                                   public void onClick(View v) {
				                                   if(leg_holder.stops.getVisibility() == View.GONE) {
					                                   leg_holder.stops.setVisibility(View.VISIBLE);
					                                   leg_holder.duration.setVisibility(View.VISIBLE);
				                                   } else {
					                                   leg_holder.stops.setVisibility(View.GONE);
					                                   leg_holder.duration.setVisibility(View.GONE);

				                                   }
			                                   }
		                                   }
		);

		// highlight duration to set it apart from platforms/positions
		leg_holder.duration.setTextColor(leg_holder.departureLocation.getCurrentTextColor());

		// stop here, if there are no stops
		if(stops == null) return;

		// Remove previous stops in case we are refreshing the view.
		leg_holder.stops.removeAllViews();

		for(final Stop stop : stops) {
			TableRow stopView = (TableRow) LayoutInflater.from(context).inflate(R.layout.stop, leg_holder.stops, false);

			StopHolder stopHolder = new StopHolder(stopView);

			Date arrivalTime = stop.getArrivalTime();
			Date departureTime = stop.getDepartureTime();

			if(arrivalTime != null) {
				TransportrUtils.setArrivalTimes(context, stopHolder.arrivalTime, stopHolder.arrivalDelay, stop);
			}
			else {
				stopHolder.arrivalTime.setVisibility(View.GONE);
				stopHolder.arrivalDelay.setVisibility(View.GONE);
			}

			if(departureTime != null) {
				TransportrUtils.setDepartureTimes(context, stopHolder.departureTime, stopHolder.departureDelay, stop);
			}
			else {
				stopHolder.departureTime.setVisibility(View.GONE);
				stopHolder.departureDelay.setVisibility(View.GONE);
			}

			stopHolder.location.setText(TransportrUtils.getLocName(stop.location));

			if(stop.plannedArrivalPosition != null) {
				stopHolder.arrivalPlatform.setText(stop.plannedArrivalPosition.name);
			}
			if(stop.plannedDeparturePosition != null) {
				stopHolder.departurePlatform.setText(stop.plannedDeparturePosition.name);
			}

			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
			if(settings.getBoolean("pref_key_hide_departure_time", false)) {
				stopHolder.departureTime.setVisibility(View.GONE);
				stopHolder.departureDelay.setVisibility(View.GONE);
				stopHolder.departurePlatform.setVisibility(View.GONE);
			}

			// Creating PopupMenu for stop
			final LegPopupMenu popup = new LegPopupMenu(context, stopView, stop);

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

			leg_holder.stops.addView(stopView);
		}
	}

	public ListTrip getItem(int position) {
		return trips.get(position);
	}

	public void addAll(final List<ListTrip> trips) {
		this.trips.addAll(trips);
	}

	public boolean remove(ListTrip trip) {
		removed.add(trip);

		return this.trips.remove(trip);
	}

	public ListTrip removeItemAt(int index) {
		ListTrip trip = this.trips.removeItemAt(index);
		removed.add(trip);

		return trip;
	}

	public void undo() {
		trips.add(removed.remove(removed.size() - 1));
	}

	private void expandTrip(final TripHolder ui, boolean expand) {
		Drawable icon;
		int state, ostate;

		if(expand) {
			//noinspection deprecation
			icon = TransportrUtils.getTintedDrawable(context, R.drawable.ic_action_navigation_unfold_more);
			state = View.GONE;
			ostate = View.VISIBLE;
		}
		else {
			//noinspection deprecation
			icon = TransportrUtils.getTintedDrawable(context, R.drawable.ic_action_navigation_unfold_less);
			state = View.VISIBLE;
			ostate = View.GONE;
		}
		ui.expand.setImageDrawable(icon);

		// show view with all trip lines if everything else is gone
		ui.linesView.setVisibility(ostate);

		if(ui.legs.size() <= 1) {
			// show/hide additional trip info
			ui.legs.get(0).info.setVisibility(state);

			return;
		}

		int i = 0;
		for(LegHolder leg : ui.legs) {
			if(i == 0) {
				// first leg
				leg.arrival.setVisibility(state);
				leg.info.setVisibility(state);
				leg.divider.setVisibility(state);
			} else if(i == ui.legs.size() - 1) {
				// last leg
				leg.departure.setVisibility(state);
				leg.info.setVisibility(state);
			} else {
				// all middle legs
				leg.arrival.setVisibility(state);
				leg.info.setVisibility(state);
				leg.departure.setVisibility(state);
				leg.divider.setVisibility(state);
			}
			i += 1;
		}
	}


	public static class BaseTripHolder extends RecyclerView.ViewHolder {
		public CardView card;
		ViewGroup legsView;
		public List<LegHolder> legs;

		public BaseTripHolder(View v, int size) {
			super(v);

			card = (CardView) v.findViewById(R.id.cardView);
			legsView = (ViewGroup) v.findViewById(R.id.legsView);

			legs = new ArrayList<>();

			// add more leg views for number of legs
			for(int i = 0; i < size; i++) {
				ViewGroup legView = (ViewGroup) LayoutInflater.from(v.getContext()).inflate(R.layout.leg, legsView, false);
				legsView.addView(legView);

				LegHolder legHolder = new LegHolder(legView);
				legs.add(legHolder);
			}
		}
	}

	static class TripHolder extends BaseTripHolder {
		CardView card;
		TableLayout firstLeg;
		TableRow linesView;
		TextView departureDelay;
		ImageView lineArrowDown;
		public FlowLayout lines;
		TextView changes;
		private TextView duration;
		private ImageView map;
		private ImageView share;
		ImageView calendar;
		ImageView expand;

		TripHolder(View v, int size) {
			super(v, size - 1);

			card = (CardView) v.findViewById(R.id.cardView);
			firstLeg = (TableLayout) v.findViewById(R.id.firstLegView);
			map = (ImageView) v.findViewById(R.id.mapView);
			share = (ImageView) v.findViewById(R.id.shareView);
			calendar = (ImageView) v.findViewById(R.id.calendarView);
			expand = (ImageView) v.findViewById(R.id.expandView);

			LayoutTransition transition = new LayoutTransition();
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				transition.enableTransitionType(LayoutTransition.CHANGING);
				transition.enableTransitionType(LayoutTransition.APPEARING);
				transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
				transition.enableTransitionType(LayoutTransition.DISAPPEARING);
				transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
			}
			legsView.setLayoutTransition(transition);

			LegHolder firstLegHolder = new LegHolder(firstLeg);
			legs.add(0, firstLegHolder);

			// inflate special view that contains all lines on that trip
			linesView = (TableRow) LayoutInflater.from(v.getContext()).inflate(R.layout.lines, firstLeg, false);

			firstLeg.addView(linesView, 1);

			// remember where the lines are inserted and the trip duration
			departureDelay = (TextView) linesView.findViewById(R.id.departureDelayView);
			lineArrowDown = (ImageView) linesView.findViewById(R.id.lineArrowDown);
			lines = (FlowLayout) linesView.findViewById(R.id.lineLayout);
			changes = (TextView) linesView.findViewById(R.id.changesView);
			duration = (TextView) linesView.findViewById(R.id.durationView);
		}
	}

	public static class LegHolder extends RecyclerView.ViewHolder {
		public TableLayout layout;
		public ViewGroup departure;
		TextView departureTime;
		TextView departureDelay;
		TextView departureLocation;
		TextView departurePlatform;
		ViewGroup arrival;
		TextView arrivalTime;
		TextView arrivalDelay;
		TextView arrivalLocation;
		TextView arrivalPlatform;
		private ViewGroup info;
		private TextView message;
		ViewGroup stops;
		public ViewGroup line;
		ImageView arrow;
		ImageView arrowDown;
		TextView lineDestination;
		public TextView	duration;
		public View divider;

		LegHolder(ViewGroup v) {
			super(v);

			layout = (TableLayout) v;
			departure = (ViewGroup) v.findViewById(R.id.departureView);
			departureTime = (TextView) v.findViewById(R.id.departureTimeView);
			departureDelay = (TextView) v.findViewById(R.id.departureDelayView);
			departureLocation = (TextView) v.findViewById(R.id.departureLocationView);
			departurePlatform =  (TextView) v.findViewById(R.id.departurePlatformView);
			arrival = (ViewGroup) v.findViewById(R.id.arrivalView);
			arrivalTime = (TextView) v.findViewById(R.id.arrivalTimeView);
			arrivalDelay = (TextView) v.findViewById(R.id.arrivalDelayView);
			arrivalLocation =  (TextView) v.findViewById(R.id.arrivalLocationView);
			arrivalPlatform =  (TextView) v.findViewById(R.id.arrivalPlatformView);
			info = (ViewGroup) v.findViewById(R.id.infoView);
			message =  (TextView) v.findViewById(R.id.messageView);
			stops = (ViewGroup) v.findViewById(R.id.stopsView);
			line = (ViewGroup) v.findViewById(R.id.lineView);
			arrow = (ImageView) v.findViewById(R.id.arrowView);
			arrowDown = (ImageView) v.findViewById(R.id.arrowDown);
			lineDestination = (TextView) v.findViewById(R.id.lineDestinationView);
			duration = (TextView) v.findViewById(R.id.durationView);
			divider = v.findViewById(R.id.dividerView);

			message.setVisibility(View.GONE);
		}
	}

	private static class StopHolder {
		public TableRow layout;
		TextView arrivalTime;
		TextView departureTime;
		TextView arrivalDelay;
		TextView departureDelay;

		public TextView location;

		ViewGroup platformView;
		TextView arrivalPlatform;
		TextView	departurePlatform;

		StopHolder(TableRow v) {
			layout = v;
			arrivalTime = (TextView) v.findViewById(R.id.arrivalTimeView);
			departureTime = (TextView) v.findViewById(R.id.departureTimeView);
			arrivalDelay = (TextView) v.findViewById(R.id.arrivalDelayView);
			departureDelay = (TextView) v.findViewById(R.id.departureDelayView);

			location = (TextView) v.findViewById(R.id.locationView);

			platformView = (ViewGroup) v.findViewById(R.id.platformView);
			arrivalPlatform = (TextView) v.findViewById(R.id.arrivalPlatformView);
			departurePlatform = (TextView) v.findViewById(R.id.departurePlatformView);
		}
	}
}
