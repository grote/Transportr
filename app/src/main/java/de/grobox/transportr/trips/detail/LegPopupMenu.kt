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

package de.grobox.transportr.trips.detail

import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.view.View
import de.grobox.transportr.R
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.trips.detail.TripUtils.legToString
import de.grobox.transportr.ui.BasePopupMenu
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.IntentUtils.findDepartures
import de.grobox.transportr.utils.IntentUtils.findNearbyStations
import de.grobox.transportr.utils.IntentUtils.presetDirections
import de.grobox.transportr.utils.IntentUtils.startGeoIntent
import de.grobox.transportr.utils.TransportrUtils.copyToClipboard
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Trip.Leg

class LegPopupMenu private constructor(context: Context, anchor: View, location: Location, private val text: String) :
    BasePopupMenu(context, anchor) {

    private val loc1: WrapLocation = WrapLocation(location)

    internal constructor(context: Context, anchor: View, leg: Leg, isLast: Boolean) :
            this(context, anchor, if (isLast) leg.arrival else leg.departure, legToString(context, leg))

    internal constructor(context: Context, anchor: View, stop: Stop) :
            this(context, anchor, stop.location, "${formatTime(context, stop.arrivalTime)} ${getLocationName(stop.location)}")

    init {
        this.menuInflater.inflate(R.menu.leg_location_actions, menu)

        if (!loc1.hasId()) {
            menu.removeItem(R.id.action_show_departures)
        }
        showIcons()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean = when (item.itemId) {
    // Show On External Map
        R.id.action_show_on_external_map -> {
            startGeoIntent(context, loc1)
            true
        }
    // From Here
        R.id.action_from_here -> {
            presetDirections(context, loc1, null, null)
            true
        }
    // To Here
        R.id.action_to_here -> {
            presetDirections(context, null, null, loc1)
            true
        }
    // Show Departures
        R.id.action_show_departures -> {
            findDepartures(context, loc1)
            true
        }
    // Show Nearby Stations
        R.id.action_show_nearby_stations -> {
            findNearbyStations(context, loc1)
            true
        }
    // Share Leg
        R.id.action_share -> {
            val sendIntent = Intent()
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_SUBJECT, loc1.getName())
                    .putExtra(Intent.EXTRA_TEXT, text)
                    .setType("text/plain")
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
            context.startActivity(Intent.createChooser(sendIntent, context.resources.getText(R.string.action_share)))
            true
        }
    // Copy Leg to Clipboard
        R.id.action_copy -> {
            copyToClipboard(context, loc1.getName())
            true
        }
        else -> false
    }

}
