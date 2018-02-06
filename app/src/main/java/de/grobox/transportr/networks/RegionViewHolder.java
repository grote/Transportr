/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.networks;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.transportr.R;

abstract class RegionViewHolder<Reg extends Region> extends RecyclerView.ViewHolder {
	protected final TextView name;

	RegionViewHolder(View v) {
		super(v);
		name = v.findViewById(R.id.name);
	}

	void bind(Reg region, boolean expanded) {
		 name.setText(region.getName(name.getContext()));
	}
}

abstract class ParentRegionViewHolder<Reg extends Region> extends RegionViewHolder<Reg> {
	protected final ImageView chevron;

	ParentRegionViewHolder(View v) {
		super(v);
		chevron = v.findViewById(R.id.chevron);
	}

	@Override
	void bind(Reg region, boolean expanded) {
		super.bind(region, expanded);
		if (expanded) chevron.setRotation(0);
		else chevron.setRotation(180);
	}
}
