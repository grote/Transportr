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

package de.grobox.transportr.favorites.trips

import android.content.Context
import android.view.MenuItem
import android.view.View

import androidx.annotation.StringRes
import de.grobox.transportr.R
import de.grobox.transportr.utils.IntentUtils

internal abstract class SpecialLocationPopupMenu(context: Context, anchor: View, trip: FavoriteTripItem, listener: FavoriteTripListener) :
    AbstractFavoritesPopupMenu(context, anchor, trip, listener) {

    @get:StringRes
    protected abstract val shortcutName: Int

    override fun getMenuRes(): Int {
        return R.menu.special_location_actions
    }

    override fun onMenuItemClick(item: MenuItem): Boolean = when (item.itemId) {
        // Add Launcher Shortcut
        R.id.action_add_shortcut -> {
            addShortcut(context.getString(shortcutName))
            true
        }
        // Show Departures
        R.id.action_show_departures -> {
            IntentUtils.findDepartures(context, trip.to!!)
            true
        }
        else -> super.onMenuItemClick(item)
    }

}
