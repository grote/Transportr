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

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.grobox.liberario.R;
import de.grobox.liberario.ui.StationPopupMenu;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.StationHolder>{

	private Location start;
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
			int d1 = TransportrUtils.computeDistance(start, s1);
			int d2 = TransportrUtils.computeDistance(start, s2);

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

		ui.item.setOnClickListener(new View.OnClickListener() {
			                           @Override
			                           public void onClick(View v) {
				                           StationPopupMenu popup = new StationPopupMenu(ui.item.getContext(), ui.item, loc, start);
				                           popup.show();
			                           }
		                           }
		);
		ui.station.setText(loc.uniqueShortName());

		int dist = TransportrUtils.computeDistance(start, loc);

		if(dist >= 0) {
			ui.distance.setText(String.valueOf(dist) + "m");
			ui.distance.setVisibility(View.VISIBLE);
		} else {
			ui.distance.setVisibility(View.GONE);
		}

		// Show products if available for location
		ui.products.removeAllViews();
		if(loc.products != null && loc.products.size() > 0) {
			for(Product product : loc.products) {
				ImageView image = new ImageView(ui.products.getContext());
				image.setImageDrawable(TransportrUtils.getTintedDrawable(ui.products.getContext(), TransportrUtils.getDrawableForProduct(product)));
				ui.products.addView(image);
			}
		}
	}

	@Override
	public int getItemCount() {
		return stations == null ? 0 : stations.size();
	}

	public void setStart(Location loc) {
		start = loc;
	}

	public Location getStart() {
		return start;
	}

	public ArrayList<Location> getStations() {
		ArrayList<Location> list = new ArrayList<>(stations.size());

		for(int i = 0; i < stations.size(); i++) {
			list.add(getItem(i));
		}

		return list;
	}

	public Location getItem(int position) {
		return stations.get(position);
	}

	public void addAll(final List<Location> stations) {
		this.stations.addAll(stations);
	}

	public void clear() {
		this.stations.beginBatchedUpdates();

		while(stations.size() != 0) {
			stations.removeItemAt(0);
		}

		this.stations.endBatchedUpdates();
	}

	public static class StationHolder extends RecyclerView.ViewHolder {
		public ViewGroup item;
		public TextView station;
		public TextView distance;
		public ViewGroup products;

		public StationHolder(View v) {
			super(v);

			item = (ViewGroup) v.findViewById(R.id.stationView);
			station = (TextView) v.findViewById(R.id.stationNameView);
			distance = (TextView) v.findViewById(R.id.distanceView);
			products = (ViewGroup) v.findViewById(R.id.productsView);
		}
	}
}
