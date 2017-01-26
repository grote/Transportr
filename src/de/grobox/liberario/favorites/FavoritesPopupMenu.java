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

package de.grobox.liberario.favorites;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import de.grobox.liberario.R;
import de.grobox.liberario.ui.BasePopupMenu;

import static de.grobox.liberario.data.RecentsDB.toggleFavouriteTrip;
import static de.grobox.liberario.utils.TransportrUtils.findDirections;
import static de.grobox.liberario.utils.TransportrUtils.presetDirections;
import static de.grobox.liberario.utils.TransportrUtils.setFavState;

public class FavoritesPopupMenu extends BasePopupMenu {

	private final FavoritesItem trip;
	private final FavoriteListener listener;

	public FavoritesPopupMenu(Context context, View anchor, FavoritesItem trip, FavoriteListener listener) {
		super(context, anchor);

		this.trip = trip;
		this.listener = listener;
		this.getMenuInflater().inflate(R.menu.favorite_actions, getMenu());

		setFavState(context, getMenu().findItem(R.id.action_mark_favorite), trip.isFavorite(), false);

		showIcons();
	}

	@Override
	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// handle presses on menu items
				switch(item.getItemId()) {
					// Swap Locations
					case R.id.action_swap_locations:
						findDirections(context, trip.getTo(), trip.getVia(), trip.getFrom());

						return true;
					// Preset Locations
					case R.id.action_set_locations:
						presetDirections(context, trip.getFrom(), trip.getVia(), trip.getTo());

						return true;
					case R.id.action_mark_favorite:
						toggleFavouriteTrip(context, trip);
						trip.setFavourite(!trip.isFavorite());

						setFavState(context, item, trip.isFavorite(), false);

						if(listener != null) {
							listener.onFavoriteRemoved(trip);
						}
						return true;
					default:
						return false;
				}
			}
		};
	}

}
