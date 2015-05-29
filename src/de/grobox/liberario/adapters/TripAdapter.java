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

package de.grobox.liberario.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.grobox.liberario.ListTrip;
import de.grobox.liberario.R;
import de.grobox.liberario.ui.FlowLayout;
import de.grobox.liberario.ui.SwipeDismissRecyclerViewTouchListener;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
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

	public TripAdapter(List<ListTrip> trips, SwipeDismissRecyclerViewTouchListener touchListener, Context context) {
		this.touchListener = touchListener;
		this.context = context;
		this.removed = new ArrayList<>();

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

		// listen to touches also in the card, because it does not work only in the RecyclerView
		ui.card.setOnTouchListener(touchListener);

		// re-apply current expansion saved in trip
		expandTrip(ui, !trip.expanded);

		// Clear Transport Icons to avoid accumulation when same trips are returned
		ui.lines.removeAllViews();

		// Show Trip Duration
		ui.duration.setText(DateUtils.getDuration(trip.trip.getDuration()));

		int i = 0;
		for(final Trip.Leg leg : trip.trip.legs) {
			LegHolder leg_holder = ui.legs.get(i);

			// Locations
			leg_holder.departureLocation.setText(leg.departure.uniqueShortName());
			leg_holder.arrivalLocation.setText(leg.arrival.uniqueShortName());

			// Leg duration
			leg_holder.duration.setText(DateUtils.getDuration(leg.getDepartureTime(), leg.getArrivalTime()));

			// Clear Transport Icons to avoid accumulation when same trips are returned
			leg_holder.line.removeAllViews();

			if(leg instanceof Trip.Public) {
				Trip.Public public_leg = ((Trip.Public) leg);

				LiberarioUtils.setArrivalTimes(context, leg_holder.arrivalTime, leg_holder.arrivalDelay, public_leg.arrivalStop);
				LiberarioUtils.setDepartureTimes(context, leg_holder.departureTime, leg_holder.departureDelay, public_leg.departureStop);

				LiberarioUtils.addLineBox(context, leg_holder.line, public_leg.line);
				LiberarioUtils.addLineBox(context, ui.lines, public_leg.line);

				if(public_leg.destination != null) {
					leg_holder.lineDestination.setText(public_leg.destination.uniqueShortName());
				} else {
					// hide arrow because this line has no destination
					leg_holder.arrow.setVisibility(View.GONE);
				}
			}
			else if(leg instanceof Trip.Individual) {
				leg_holder.arrivalTime.setText(DateUtils.getTime(context, ((Trip.Individual) leg).arrivalTime));
				leg_holder.departureTime.setText(DateUtils.getTime(context, ((Trip.Individual) leg).departureTime));
/*
				// TODO needs adapting
				// show delay for last public leg
				final Trip.Public fpleg = trip.getFirstPublicLeg();
				if(fpleg != null && fpleg.getDepartureDelay() != null) {
					leg_holder.departureDelay.setText(LiberarioUtils.getDelayText(fpleg.getDepartureDelay()));
				}

				// TODO needs adapting
				// show delay for last public leg
				final Trip.Public lpleg = trip.getLastPublicLeg();
				if(lpleg != null && lpleg.getArrivalDelay() != null) {
					leg_holder.arrivalDelay.setText(LiberarioUtils.getDelayText(lpleg.getArrivalDelay()));
				}
*/
				LiberarioUtils.addWalkingBox(context, leg_holder.line);
				LiberarioUtils.addWalkingBox(context, ui.lines);

				// hide arrow because this line has no destination
				leg_holder.arrow.setVisibility(View.GONE);
			}
			i += 1;
		}

		// Share Trip
		ui.share.setImageDrawable(LiberarioUtils.getTintedDrawable(context, ui.share.getDrawable()));
		ui.share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sendIntent = new Intent()
						                    .setAction(Intent.ACTION_SEND)
						                    .putExtra(Intent.EXTRA_SUBJECT, LiberarioUtils.tripToSubject(context, trip.trip, true))
						                    .putExtra(Intent.EXTRA_TEXT, LiberarioUtils.tripToString(context, trip.trip))
						                    .setType("text/plain")
						                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.share_trip_via)));
			}
		});

		// Add Trip to Calendar
		ui.calendar.setImageDrawable(LiberarioUtils.getTintedDrawable(context, ui.calendar.getDrawable()));
		ui.calendar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_EDIT)
						                .setType("vnd.android.cursor.item/event")
						                .putExtra("beginTime", trip.trip.getFirstDepartureTime().getTime())
						                .putExtra("endTime", trip.trip.getLastArrivalTime().getTime())
						                .putExtra("title", trip.trip.from.name + " → " + trip.trip.to.name)
						                .putExtra("description", LiberarioUtils.tripToString(context, trip.trip));
				if(trip.trip.from.place != null) intent.putExtra("eventLocation", trip.trip.from.place);
				context.startActivity(intent);
			}
		});

		// Expand Card
		ui.expand.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				expandTrip(ui, trip.expanded);
				trip.expanded = !trip.expanded;
			}
		});
	}

	@Override
	public int getItemCount() {
		return trips == null ? 0 : trips.size();
	}

	public ListTrip getItem(int position) {
		return trips.get(position);
	}

	public void addAll(final List<ListTrip> trips) {
		this.trips.beginBatchedUpdates();
		for(final ListTrip trip : trips) {
			if(!removed.contains(trip)) {
				this.trips.add(trip);
			}
		}

		this.trips.endBatchedUpdates();
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

	public void expandTrip(final TripHolder ui, boolean expand) {
		Drawable icon;
		int state, ostate;

		if(expand) {
			//noinspection deprecation
			icon = LiberarioUtils.getTintedDrawable(context, R.drawable.ic_action_navigation_unfold_more);
			state = View.GONE;
			ostate = View.VISIBLE;
		}
		else {
			//noinspection deprecation
			icon = LiberarioUtils.getTintedDrawable(context, R.drawable.ic_action_navigation_unfold_less);
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
				leg.arrivalTime.setVisibility(state);
				leg.arrivalDelay.setVisibility(state);
				leg.arrivalLocation.setVisibility(state);
				leg.info.setVisibility(state);
			} else if(i == ui.legs.size() - 1) {
				// last leg
				leg.departureTime.setVisibility(state);
				leg.departureDelay.setVisibility(state);
				leg.departureLocation.setVisibility(state);
				leg.info.setVisibility(state);
			} else {
				// all middle legs
				leg.arrivalTime.setVisibility(state);
				leg.arrivalDelay.setVisibility(state);
				leg.arrivalLocation.setVisibility(state);
				leg.info.setVisibility(state);
				leg.departureTime.setVisibility(state);
				leg.departureDelay.setVisibility(state);
				leg.departureLocation.setVisibility(state);
			}
			i += 1;
		}
	}

	public static class TripHolder extends RecyclerView.ViewHolder {
		public CardView card;
		public ViewGroup legsView;
		public GridLayout firstLeg;
		public ViewGroup linesView;
		public FlowLayout lines;
		public TextView duration;
		public ImageView share;
		public ImageView calendar;
		public ImageView expand;
		List<LegHolder> legs;

		public TripHolder(View v, int size) {
			super(v);

			card = (CardView) v.findViewById(R.id.cardView);
			legsView = (ViewGroup) v.findViewById(R.id.legsView);
			firstLeg = (GridLayout) v.findViewById(R.id.firstLegView);
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
			legs = new ArrayList<>();
			legs.add(firstLegHolder);

			// inflate special view that contains all lines on that trip
			linesView = (ViewGroup) LayoutInflater.from(v.getContext()).inflate(R.layout.line, firstLeg, false);

			// set layout parameters for GridLayout
			GridLayout.LayoutParams params = new GridLayout.LayoutParams();
			params.columnSpec = GridLayout.spec(2);
			params.rowSpec = GridLayout.spec(1);
			params.setGravity(Gravity.FILL_HORIZONTAL);
			linesView.setLayoutParams(params);
			firstLeg.setRowCount(4);
			firstLeg.addView(linesView);

			// hide arrow view since we are just interested in line icons here
			linesView.findViewById(R.id.arrowView).setVisibility(View.GONE);
			linesView.findViewById(R.id.lineDestinationView).setVisibility(View.GONE);

			// remember where the lines are inserted and the trip duration
			lines = (FlowLayout) linesView.findViewById(R.id.lineLayout);
			duration = (TextView) linesView.findViewById(R.id.durationView);

			// add more leg views for number of legs
			for(int i = 1; i < size; i++) {
				ViewGroup legView = (ViewGroup) LayoutInflater.from(v.getContext()).inflate(R.layout.leg, legsView, false);
				legsView.addView(legView);

				LegHolder legHolder = new LegHolder(legView);
				legs.add(legHolder);
			}
		}
	}

	public static class LegHolder extends RecyclerView.ViewHolder {
		public GridLayout layout;
		public TextView departureTime;
		public TextView departureDelay;
		public TextView arrivalTime;
		public TextView arrivalDelay;
		public TextView departureLocation;
		public TextView arrivalLocation;
		public ViewGroup info;
		public FlowLayout line;
		public ImageView arrow;
		public TextView lineDestination;
		public TextView	duration;

		public LegHolder(ViewGroup v) {
			super(v);

			layout = (GridLayout) v;
			departureTime = (TextView) v.findViewById(R.id.departureTimeView);
			departureDelay = (TextView) v.findViewById(R.id.departureDelayView);
			arrivalTime = (TextView) v.findViewById(R.id.arrivalTimeView);
			arrivalDelay = (TextView) v.findViewById(R.id.arrivalDelayView);
			departureLocation = (TextView) v.findViewById(R.id.departureLocationView);
			arrivalLocation =  (TextView) v.findViewById(R.id.arrivalLocationView);
			info = (ViewGroup) v.findViewById(R.id.infoView);
			line = (FlowLayout) v.findViewById(R.id.lineLayout);
			arrow = (ImageView) v.findViewById(R.id.arrowView);
			lineDestination = (TextView) v.findViewById(R.id.lineDestinationView);
			duration = (TextView) v.findViewById(R.id.durationView);
		}
	}
}
