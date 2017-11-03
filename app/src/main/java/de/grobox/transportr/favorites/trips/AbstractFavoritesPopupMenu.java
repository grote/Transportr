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

package de.grobox.transportr.favorites.trips;

import android.content.Context;
import android.support.annotation.MenuRes;
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;
import de.grobox.transportr.ui.BasePopupMenu;

import static de.grobox.transportr.utils.IntentUtils.findDirections;
import static de.grobox.transportr.utils.IntentUtils.presetDirections;

abstract class AbstractFavoritesPopupMenu extends BasePopupMenu {

	protected final FavoriteTripItem trip;
	protected final FavoriteTripListener listener;

	AbstractFavoritesPopupMenu(Context context, View anchor, FavoriteTripItem trip, FavoriteTripListener listener) {
		super(context, anchor);

		this.trip = trip;
		this.listener = listener;
		getMenuInflater().inflate(getMenuRes(), getMenu());

		showIcons();
	}

	@MenuRes
	protected abstract int getMenuRes();

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			// Swap Locations
			case R.id.action_swap_locations:
				findDirections(context, 0, trip.getTo(), trip.getVia(), trip.getFrom(), true, true);
				return true;
			// Preset Locations
			case R.id.action_set_locations:
				presetDirections(context, trip.getUid(), trip.getFrom(), trip.getVia(), trip.getTo(), true);
				return true;
			default:
				return super.onMenuItemClick(item);
		}
	}

}
