/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.liberario.networks;

import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.liberario.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

class RegionViewHolder extends RecyclerView.ViewHolder {

	private final TextView flag;
	private final TextView name;
	private final ImageView chevron;

	public RegionViewHolder(View v) {
		super(v);
		flag = (TextView) v.findViewById(R.id.flag);
		name = (TextView) v.findViewById(R.id.name);
		chevron = (ImageView) v.findViewById(R.id.chevron);
	}

	void bind(Region region, boolean expanded) {
		if (Build.VERSION.SDK_INT >= 21) {
			flag.setText(region.getFlag());
			flag.setVisibility(VISIBLE);
		} else {
			flag.setVisibility(GONE);
		}
		name.setText(region.getName());
		if (expanded) ViewCompat.setRotation(chevron, 0);
		else ViewCompat.setRotation(chevron, 180);
	}

}
