/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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
import android.content.Intent;
import android.net.Uri;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.core.content.pm.ShortcutInfoCompat;
import de.grobox.transportr.R;
import de.grobox.transportr.trips.search.DirectionsActivity;
import de.grobox.transportr.ui.BasePopupMenu;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static androidx.core.content.pm.ShortcutManagerCompat.isRequestPinShortcutSupported;
import static androidx.core.content.pm.ShortcutManagerCompat.requestPinShortcut;
import static androidx.core.graphics.drawable.IconCompat.createWithResource;
import static de.grobox.transportr.trips.search.DirectionsActivity.ACTION_SEARCH;
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
		if (!isRequestPinShortcutSupported(context)) {
			MenuItem item = getMenu().findItem(R.id.action_add_shortcut);
			if (item != null) item.setVisible(false);
		}
		showIcons();
	}

	@MenuRes
	protected abstract int getMenuRes();

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			// Swap Locations
			case R.id.action_swap_locations:
				findDirections(context, trip.getTo(), trip.getVia(), trip.getFrom(), true, true);
				return true;
			// Preset Locations
			case R.id.action_set_locations:
				presetDirections(context, trip.getFrom(), trip.getVia(), trip.getTo(), true);
				return true;
			default:
				return super.onMenuItemClick(item);
		}
	}

	void addShortcut(String shortcutName) {
		ShortcutInfoCompat info = new ShortcutInfoCompat.Builder(context, shortcutName)
				.setIntent(getShortcutIntent())
				.setShortLabel(shortcutName)
				.setIcon(createWithResource(context, getShortcutDrawable()))
				.setAlwaysBadged()
				.build();
		requestPinShortcut(context, info, null);
	}

	protected Intent getShortcutIntent() {
		Intent shortcutIntent = new Intent(context, DirectionsActivity.class);
		shortcutIntent.setAction(ACTION_SEARCH);
		shortcutIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.setData(Uri.parse(getShortcutIntentString()));
		return shortcutIntent;
	}

	protected abstract String getShortcutIntentString();

	@DrawableRes
	protected abstract int getShortcutDrawable();

}
