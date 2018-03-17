/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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
import android.os.Build;
import android.support.annotation.StringRes;
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;

abstract class SpecialLocationPopupMenu extends AbstractFavoritesPopupMenu {

	SpecialLocationPopupMenu(Context context, View anchor, FavoriteTripItem trip, FavoriteTripListener listener) {
		super(context, anchor, trip, listener);
		if (Build.VERSION.SDK_INT >= 26) { // https://developer.android.com/about/versions/oreo/android-8.0-changes.html#as
			getMenu().findItem(R.id.action_add_shortcut).setVisible(false);
		}
	}

	@Override
	protected int getMenuRes() {
		return R.menu.special_location_actions;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_add_shortcut:
				addShortcut(context.getString(getShortcutName()));
				return true;
			default:
				return super.onMenuItemClick(item);
		}
	}

	@StringRes
	protected abstract int getShortcutName();

}
