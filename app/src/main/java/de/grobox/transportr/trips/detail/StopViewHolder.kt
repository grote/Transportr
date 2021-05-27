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


import android.view.View
import android.view.View.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import de.grobox.transportr.trips.BaseViewHolder
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Stop
import kotlinx.android.synthetic.main.list_item_stop.view.*
import java.util.Date


internal class StopViewHolder(v: View, private val listener : LegClickListener) : BaseViewHolder(v) {

    private val circle: ImageView = v.circle
    private val stopLocation: TextView = v.stopLocation
    private val stopButton: ImageButton = v.stopButton

    fun bind(stop: Stop, color: Int) {
        if (stop.arrivalTime != null) {
            setArrivalTimes(fromTime, fromDelay, stop)
            fromTime.visibility = VISIBLE
        } else {
            fromDelay.visibility = GONE
            if (stop.departureTime == null) {
                // insert dummy time field for stops without times set, so that stop circles align
                fromTime.text = formatTime(context, Date())
                fromTime.visibility = INVISIBLE
            } else {
                fromTime.visibility = GONE
            }
        }

        if (stop.departureTime != null) {
            if (stop.departureTime == stop.arrivalTime) {
                toTime.visibility = GONE
                toDelay.visibility = GONE
            } else {
                setDepartureTimes(toTime, toDelay, stop)
                toTime.visibility = VISIBLE
            }
        } else {
            toTime.visibility = GONE
            toDelay.visibility = GONE
        }

        circle.setColorFilter(color)

        stopLocation.text = getLocationName(stop.location)
        stopLocation.setOnClickListener { listener.onLocationClick(stop.location) }
        stopLocation.addPlatform(stop.arrivalPosition)

        // show popup on button click
        stopButton.setOnClickListener { LegPopupMenu(stopButton.context, stopButton, stop).show() }
    }

}
