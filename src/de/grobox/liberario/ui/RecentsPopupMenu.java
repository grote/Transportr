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
import android.view.MenuItem;
import android.view.View;

import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.R;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.utils.TransportrUtils;

public class RecentsPopupMenu extends BasePopupMenu {

	private RecentTrip trip;
	private FavouriteRemovedListener removedListener = null;

	public RecentsPopupMenu(Context context, View anchor, RecentTrip trip) {
		super(context, anchor);

		this.trip = trip;
		this.getMenuInflater().inflate(R.menu.recent_trip_actions, getMenu());

		if(trip.isFavourite()) {
			getMenu().findItem(R.id.action_mark_favourite).setIcon(R.drawable.ic_action_star);
		} else {
			getMenu().findItem(R.id.action_mark_favourite).setIcon(R.drawable.ic_action_star_empty);
		}

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
					case R.id.action_mark_favourite:
						RecentsDB.toggleFavouriteTrip(context, trip);
						trip.setFavourite(!trip.isFavourite());
						if(trip.isFavourite()) {
							item.setIcon(TransportrUtils.getTintedDrawable(context, R.drawable.ic_action_star));
						} else {
							item.setIcon(TransportrUtils.getTintedDrawable(context, R.drawable.ic_action_star_empty));
						}
						if (removedListener != null) {
							removedListener.onFavouriteRemoved();
						}
						return true;
					default:
						return false;
				}
			}
		};
	}

	public void setRemovedListener(FavouriteRemovedListener l) {
		this.removedListener = l;
	}

	public static abstract class FavouriteRemovedListener {
		public abstract void onFavouriteRemoved();
	}

}
