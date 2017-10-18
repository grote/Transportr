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

package de.grobox.transportr.trips.detail;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Stop;

public class StopAdapter extends Adapter<StopViewHolder> {

	private final List<Stop> stops;
	private final LegClickListener listener;
	private final int color;

	StopAdapter(List<Stop> stops, LegClickListener listener, int color) {
		this.stops = stops;
		this.listener = listener;
		this.color = color;
	}

	@Override
	public StopViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_stop, viewGroup, false);
		return new StopViewHolder(v);
	}

	@Override
	public void onBindViewHolder(StopViewHolder ui, int i) {
		Stop stop = stops.get(i);
		ui.bind(stop, listener, color);
	}

	@Override
	public int getItemCount() {
		return stops.size();
	}

}
