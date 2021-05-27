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

package de.grobox.transportr.locations;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;
import de.grobox.transportr.ui.BasePopupMenu;

import static de.grobox.transportr.utils.TransportrUtils.copyToClipboard;
import static de.grobox.transportr.utils.IntentUtils.presetDirections;

public class LocationPopupMenu extends BasePopupMenu {

	private final WrapLocation location;

	LocationPopupMenu(Context context, View anchor, WrapLocation location) {
		super(context, anchor);

		this.getMenuInflater().inflate(R.menu.location_actions, getMenu());
		this.location = location;

		showIcons();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			// From Here
			case R.id.action_from_here:
				presetDirections(context, location, null, null);
				return true;
			// Copy Station to Clipboard
			case R.id.action_copy:
				copyToClipboard(context, location.getFullName());
				return true;
			default:
				return super.onMenuItemClick(item);
		}
	}

}
