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

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.transportr.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

class ContinentViewHolder extends ParentRegionViewHolder {

	private final ImageView contour;

	ContinentViewHolder(View v) {
		super(v);
		contour = v.findViewById(R.id.contour);
	}

	void bind(Region region, boolean expanded) {
		super.bind(region, expanded);
		Continent continent = (Continent)region;
		contour.setImageResource(continent.getContour());
	}
}
