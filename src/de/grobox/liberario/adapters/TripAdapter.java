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

import android.content.Context;
import android.content.Intent;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TripDetailActivity;
import de.grobox.liberario.ui.FlowLayout;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.dto.Trip;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder>{

	private SortedList<Trip> trips = new SortedList<Trip>(Trip.class, new SortedList.Callback<Trip>(){
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
		 public int compare(Trip t1, Trip t2) {
			return t1.getFirstDepartureTime().compareTo(t2.getFirstDepartureTime());
		}

		@Override
		public boolean areItemsTheSame(Trip t1, Trip t2) {
			return t1.equals(t2);
		}

		@Override
		 public boolean areContentsTheSame(Trip t_old, Trip t_new) {
			// return whether the trips' visual representations are the same or not
			return t_old.equals(t_new);
		}
	});
	private int rowLayout;
	private Context context;

	public TripAdapter(List<Trip> trips, int rowLayout, Context context) {
		addAll(trips);
		this.rowLayout = rowLayout;
		this.context = context;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);

		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(ViewHolder ui, int i) {
		final Trip trip = trips.get(i);

		// Locations
		ui.from.setText(trip.from.uniqueShortName());
		ui.to.setText(trip.to.uniqueShortName());

		// Departure Time and Delay
		final Trip.Leg first_leg = trip.legs.get(0);
		if(trip.legs.size() > 0 && first_leg instanceof Trip.Public) {
			LiberarioUtils.setDepartureTimes(context, ui.departureTime, ui.departureDelay, ((Trip.Public) first_leg).departureStop);
		} else {
			ui.departureTime.setText(DateUtils.getTime(context, trip.getFirstDepartureTime()));
			// show delay for last public leg
			final Trip.Public pleg = trip.getFirstPublicLeg();
			if(pleg != null && pleg.getDepartureDelay() != null) {
				ui.departureDelay.setText(LiberarioUtils.getDelayText(pleg.getDepartureDelay()));
			}
		}

		// Arrival Time and Delay
		final Trip.Leg last_leg = trip.legs.get(trip.legs.size() - 1);
		if(last_leg != null && last_leg instanceof Trip.Public) {
			LiberarioUtils.setArrivalTimes(context, ui.arrivalTime, ui.arrivalDelay, ((Trip.Public) last_leg).arrivalStop);
		} else {
			ui.arrivalTime.setText(DateUtils.getTime(context, trip.getLastArrivalTime()));
			// show delay for last public leg
			final Trip.Public pleg = trip.getLastPublicLeg();
			if(pleg != null && pleg.getArrivalDelay() != null) {
				ui.arrivalDelay.setText(LiberarioUtils.getDelayText(pleg.getArrivalDelay()));
			}
		}

		// Duration
		ui.duration.setText(DateUtils.getDuration(trip.getDuration()));

		// Clear Transport Icons to avoid accumulation when same trips are returned
		ui.lines.removeAllViews();

		// Transport Line Icons
		for(final Trip.Leg leg : trip.legs) {
			if(leg instanceof Trip.Public) {
				LiberarioUtils.addLineBox(context, ui.lines, ((Trip.Public) leg).line);
			}
			else if(leg instanceof Trip.Individual) {
				LiberarioUtils.addWalkingBox(context, ui.lines);
			}
		}

		// Share Trip
		ui.share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent sendIntent = new Intent()
						                    .setAction(Intent.ACTION_SEND)
						                    .putExtra(Intent.EXTRA_SUBJECT, LiberarioUtils.tripToSubject(context, trip, true))
						                    .putExtra(Intent.EXTRA_TEXT, LiberarioUtils.tripToString(context, trip))
						                    .setType("text/plain")
						                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.share_trip_via)));
			}
		});

		// Add Trip to Calendar
		ui.calendar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Intent.ACTION_EDIT)
						                .setType("vnd.android.cursor.item/event")
						                .putExtra("beginTime", trip.getFirstDepartureTime().getTime())
						                .putExtra("endTime", trip.getLastArrivalTime().getTime())
						                .putExtra("title", trip.from.name + " → " + trip.to.name)
						                .putExtra("description", LiberarioUtils.tripToString(context, trip));
				if(trip.from.place != null) intent.putExtra("eventLocation", trip.from.place);
				context.startActivity(intent);
			}
		});

		ui.card.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(context, TripDetailActivity.class);
				intent.putExtra("de.schildbach.pte.dto.Trip", trip);
				intent.putExtra("de.schildbach.pte.dto.Trip.from", trip.from);
				intent.putExtra("de.schildbach.pte.dto.Trip.to", trip.to);
				context.startActivity(intent);
			}
		});
	}

	@Override
	public int getItemCount() {
		return trips == null ? 0 : trips.size();
	}

	public void addAll(final List<Trip> trips) {
		this.trips.beginBatchedUpdates();
		for(final Trip item : trips) {
			this.trips.add(item);
		}
		this.trips.endBatchedUpdates();
	}

	public boolean remove(Trip trip) {
		return this.trips.remove(trip);
	}

	public Trip removeItemAt(int index) {
		return this.trips.removeItemAt(index);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public CardView card;
		public TextView departureTime;
		public TextView arrivalTime;
		public TextView departureDelay;
		public TextView arrivalDelay;
		public TextView from;
		public TextView to;
		public FlowLayout lines;
		public TextView duration;
		public ImageView share;
		public ImageView calendar;
		public ImageView expand;

		public ViewHolder(View itemView) {
			super(itemView);

			card = (CardView) itemView.findViewById(R.id.cardView);
			departureTime = (TextView) itemView.findViewById(R.id.departureTimeView);
			arrivalTime = (TextView) itemView.findViewById(R.id.arrivalTimeView);
			departureDelay = (TextView) itemView.findViewById(R.id.departureDelayView);
			arrivalDelay = (TextView) itemView.findViewById(R.id.arrivalDelayView);
			from = (TextView) itemView.findViewById(R.id.fromView);
			to = (TextView) itemView.findViewById(R.id.toView);
			lines = (FlowLayout) itemView.findViewById(R.id.lineLayout);
			duration = (TextView) itemView.findViewById(R.id.durationView);
			share = (ImageView) itemView.findViewById(R.id.shareView);
			calendar = (ImageView) itemView.findViewById(R.id.calendarView);
			expand = (ImageView) itemView.findViewById(R.id.expandView);
		}

	}
}