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
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;

public class FavoriteTripPopupMenu extends AbstractFavoritesPopupMenu {

	FavoriteTripPopupMenu(Context context, View anchor, FavoriteTripItem trip, FavoriteTripListener listener) {
		super(context, anchor, trip, listener);
		setFavState(getMenu().findItem(R.id.action_mark_favorite), trip.isFavorite());
	}

	@Override
	protected int getMenuRes() {
		return R.menu.favorite_actions;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_mark_favorite:
				if (listener != null) {
					listener.onFavoriteChanged(trip, !trip.isFavorite());
				}
				setFavState(item, trip.isFavorite());
				return true;
			case R.id.action_trip_delete:
				listener.onFavoriteDeleted(trip);
				return true;
			default:
				return super.onMenuItemClick(item);
		}
	}

	private void setFavState(MenuItem item, boolean is_fav) {
		if (is_fav) {
			item.setTitle(R.string.action_unfav_trip);
			item.setIcon(R.drawable.ic_action_star_empty);
			DrawableCompat.setTint(item.getIcon(), iconColor);
		} else {
			item.setTitle(R.string.action_fav_trip);
			item.setIcon(R.drawable.ic_action_star);
			DrawableCompat.setTint(item.getIcon(), iconColor);
		}
	}

}
