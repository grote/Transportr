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
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;
import de.grobox.transportr.trips.search.DirectionsActivity;
import de.grobox.transportr.ui.BasePopupMenu;

import static android.content.Intent.EXTRA_SHORTCUT_ICON_RESOURCE;
import static android.content.Intent.EXTRA_SHORTCUT_INTENT;
import static android.content.Intent.EXTRA_SHORTCUT_NAME;
import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
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
		// create launcher shortcut
		Intent addIntent = new Intent();
		addIntent.putExtra(EXTRA_SHORTCUT_INTENT, getShortcutIntent());
		addIntent.putExtra(EXTRA_SHORTCUT_NAME, shortcutName);
		addIntent.putExtra(EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, getShortcutDrawable()));
		addIntent.putExtra("duplicate", false);
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		context.sendBroadcast(addIntent);

		// switch to home-screen to let the user see the new shortcut
		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(startMain);
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
