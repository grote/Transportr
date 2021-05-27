/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.trips.search;

import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Trip;

class TripAdapter extends RecyclerView.Adapter<TripViewHolder> {

	private final SortedList<Trip> items = new SortedList<>(Trip.class, new SortedList.Callback<Trip>() {
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
		public boolean areContentsTheSame(Trip t1, Trip t2) {
			return t1.equals(t2);
		}
	});
	private final OnTripClickListener listener;

	TripAdapter(OnTripClickListener listener) {
		super();
		this.listener = listener;
	}

	@Override
	public TripViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_trip, viewGroup, false);
		return new TripViewHolder(v);
	}

	@Override
	public void onBindViewHolder(final TripViewHolder ui, final int position) {
		Trip dep = items.get(position);
		ui.bind(dep, listener);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	void addAll(Collection<Trip> departures) {
		this.items.addAll(departures);
	}

	interface OnTripClickListener {
		void onClick(Trip trip);
	}

}
