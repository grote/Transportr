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

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.grobox.liberario.R;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.dto.Location;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationHolder>{

	private Location location;
	private SortedList<Location> stations = new SortedList<>(Location.class, new SortedList.Callback<Location>(){
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
		public int compare(Location s1, Location s2) {
			int d1 = LiberarioUtils.computeDistance(location, s1);
			int d2 = LiberarioUtils.computeDistance(location, s2);

			return d1 - d2;
		}

		@Override
		public boolean areItemsTheSame(Location s1, Location s2) {
			return s1.equals(s2);
		}

		@Override
		public boolean areContentsTheSame(Location s_old, Location s_new) {
			// return whether the stations' visual representations are the same or not
			return s_old.equals(s_new);
		}
	});
	private int rowLayout;

	public StationAdapter(List<Location> stations, int rowLayout) {
		this.rowLayout = rowLayout;

		addAll(stations);
	}

	@Override
	public StationHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);

		return new StationHolder(v);
	}

	@Override
	public void onBindViewHolder(final StationHolder ui, final int position) {
		final Location loc = getItem(position);

		ui.station.setText(loc.uniqueShortName());
		ui.distance.setText(String.valueOf(LiberarioUtils.computeDistance(location, loc)) + "m");
	}

	@Override
	public int getItemCount() {
		return stations == null ? 0 : stations.size();
	}

	public void setLocation(Location loc) {
		location = loc;
	}

	public Location getLocation() {
		return location;
	}

	public Location getItem(int position) {
		return stations.get(position);
	}

	public void addAll(final List<Location> stations) {
		this.stations.beginBatchedUpdates();

		for(final Location station : stations) {
			this.stations.add(station);
		}

		this.stations.endBatchedUpdates();
	}

	public void clear() {
		this.stations.beginBatchedUpdates();

		while(stations.size() != 0) {
			stations.removeItemAt(0);
		}

		this.stations.endBatchedUpdates();
	}

	public static class StationHolder extends RecyclerView.ViewHolder {
		public TextView station;
		public TextView distance;

		public StationHolder(View v) {
			super(v);

			station = (TextView) v.findViewById(R.id.stationNameView);
			distance = (TextView) v.findViewById(R.id.distanceView);
		}
	}
}
