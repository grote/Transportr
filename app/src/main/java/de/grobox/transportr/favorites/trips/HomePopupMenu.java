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
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;

class HomePopupMenu extends SpecialLocationPopupMenu {

	HomePopupMenu(Context context, View anchor, FavoriteTripItem trip, FavoriteTripListener listener) {
		super(context, anchor, trip, listener);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_change:
				listener.changeHome();
			default:
				return super.onMenuItemClick(item);
		}
	}

}
