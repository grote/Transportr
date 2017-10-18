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
import de.grobox.transportr.trips.detail.LegViewHolder.LegType;
import de.schildbach.pte.dto.Trip.Leg;

import static de.grobox.transportr.trips.detail.LegViewHolder.LegType.FIRST;
import static de.grobox.transportr.trips.detail.LegViewHolder.LegType.FIRST_LAST;
import static de.grobox.transportr.trips.detail.LegViewHolder.LegType.LAST;
import static de.grobox.transportr.trips.detail.LegViewHolder.LegType.MIDDLE;

public class LegAdapter extends Adapter<LegViewHolder> {

	private final List<Leg> legs;
	private final LegClickListener listener;
	private final boolean showLineName;

	LegAdapter(List<Leg> legs, LegClickListener listener, boolean showLineName) {
		this.legs = legs;
		this.listener = listener;
		this.showLineName = showLineName;
	}

	@Override
	public LegViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_leg, viewGroup, false);
		return new LegViewHolder(v);
	}

	@Override
	public void onBindViewHolder(LegViewHolder ui, int i) {
		Leg leg = legs.get(i);
		ui.bind(leg, getLegType(i), listener, showLineName);
	}

	@Override
	public int getItemCount() {
		return legs.size();
	}

	private LegType getLegType(int position) {
		if (legs.size() == 1) return FIRST_LAST;
		else if (position == 0) return FIRST;
		else if (position == legs.size() - 1) return LAST;
		return MIDDLE;
	}

}
