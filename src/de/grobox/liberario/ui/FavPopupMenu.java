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

package de.grobox.liberario.ui;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import de.grobox.liberario.FavTrip;
import de.grobox.liberario.R;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;

public class FavPopupMenu extends BasePopupMenu {

	private FavTrip trip;

	public FavPopupMenu(Context context, View anchor, FavTrip trip) {
		super(context, anchor);

		this.trip = trip;
		this.getMenuInflater().inflate(R.menu.fav_trip_actions, getMenu());

		showIcons();
	}

	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// handle presses on menu items
				switch(item.getItemId()) {
					// Swap Locations
					case R.id.action_swap_locations:
						TransportrUtils.findDirections(context, trip.getTo(), trip.getFrom());

						return true;
					// Preset Locations
					case R.id.action_set_locations:
						TransportrUtils.presetDirections(context, trip.getFrom(), trip.getTo());

						return true;
					default:
						return false;
				}
			}
		};
	}

}
