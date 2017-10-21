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

package de.grobox.transportr.adapters;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.grobox.transportr.R;
import de.grobox.transportr.trips.ListTrip;
import de.grobox.transportr.ui.SwipeDismissRecyclerViewTouchListener;
import de.grobox.transportr.utils.DateUtils;
import de.grobox.transportr.utils.TransportrUtils;
import de.schildbach.pte.dto.Trip;

@Deprecated
public class TripDetailsAdapter extends RecyclerView.Adapter<TripDetailsAdapter.TripHolder>{

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

	public TripDetailsAdapter(List<ListTrip> trips, SwipeDismissRecyclerViewTouchListener touchListener, Context context, boolean showLineName) {
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
		View v = LayoutInflater.from(context).inflate(R.layout.list_item_trip, viewGroup, false);

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

		int i = 0;
		for(final Trip.Leg leg : trip.trip.legs) {
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
	}

	@Override
	public int getItemCount() {
		return trips == null ? 0 : trips.size();
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

	void clear() {
		trips.clear();
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

			card = v.findViewById(R.id.cardView);
			legsView = v.findViewById(R.id.legsView);

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
		TableRow linesView;
		TextView departureDelay;
		ImageView lineArrowDown;
		TextView changes;
		private TextView duration;
		private ImageView map;
		private ImageView share;
		ImageView calendar;
		ImageView expand;

		TripHolder(View v, int size) {
			super(v, size - 1);

			map = v.findViewById(R.id.mapView);
			share = v.findViewById(R.id.shareView);
			calendar = v.findViewById(R.id.calendarView);
			expand = v.findViewById(R.id.expandView);

			LayoutTransition transition = new LayoutTransition();
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				transition.enableTransitionType(LayoutTransition.CHANGING);
				transition.enableTransitionType(LayoutTransition.APPEARING);
				transition.enableTransitionType(LayoutTransition.CHANGE_APPEARING);
				transition.enableTransitionType(LayoutTransition.DISAPPEARING);
				transition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
			}
			legsView.setLayoutTransition(transition);

			// remember where the lines are inserted and the trip duration
			departureDelay = linesView.findViewById(R.id.departureDelayView);
			lineArrowDown = linesView.findViewById(R.id.lineArrowDown);
			changes = linesView.findViewById(R.id.changesView);
			duration = linesView.findViewById(R.id.durationView);
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
			departure = v.findViewById(R.id.departureView);
			departureTime = v.findViewById(R.id.departureTimeView);
			departureDelay = v.findViewById(R.id.departureDelayView);
			departureLocation = v.findViewById(R.id.departureLocationView);
			departurePlatform = v.findViewById(R.id.departurePlatformView);
			arrival = v.findViewById(R.id.arrivalView);
			arrivalTime = v.findViewById(R.id.arrivalTimeView);
			arrivalDelay = v.findViewById(R.id.arrivalDelayView);
			arrivalLocation = v.findViewById(R.id.arrivalLocationView);
			arrivalPlatform = v.findViewById(R.id.arrivalPlatformView);
			info = v.findViewById(R.id.infoView);
			message = v.findViewById(R.id.messageView);
			stops = v.findViewById(R.id.stopsView);
			line = v.findViewById(R.id.lineView);
			arrow = v.findViewById(R.id.arrowView);
			arrowDown = v.findViewById(R.id.arrowDown);
			lineDestination = v.findViewById(R.id.lineDestinationView);
			duration = v.findViewById(R.id.durationView);
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
			arrivalTime = v.findViewById(R.id.arrivalTimeView);
			departureTime = v.findViewById(R.id.departureTimeView);
			arrivalDelay = v.findViewById(R.id.arrivalDelayView);
			departureDelay = v.findViewById(R.id.departureDelayView);

			location = v.findViewById(R.id.locationView);

			platformView = v.findViewById(R.id.platformView);
			arrivalPlatform = v.findViewById(R.id.arrivalPlatformView);
			departurePlatform = v.findViewById(R.id.departurePlatformView);
		}
	}
}
