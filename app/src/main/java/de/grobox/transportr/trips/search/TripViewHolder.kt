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

package de.grobox.transportr.trips.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import com.google.common.base.Strings.isNullOrEmpty
import de.grobox.transportr.R
import de.grobox.transportr.trips.search.TripAdapter.OnTripClickListener
import de.grobox.transportr.ui.LineView
import de.grobox.transportr.utils.DateUtils.*
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.Trip.Individual
import de.schildbach.pte.dto.Trip.Public

internal class TripViewHolder(private val root: View) : RecyclerView.ViewHolder(root) {

    private val context = root.context
    private val fromTimeRel: TextView = root.findViewById(R.id.fromTimeRel)
    private val fromTime: TextView = root.findViewById(R.id.fromTime)
    private val fromLocation: TextView = root.findViewById(R.id.fromLocation)
    private val fromDelay: TextView = root.findViewById(R.id.fromDelay)
    private val toTime: TextView = root.findViewById(R.id.toTime)
    private val toLocation: TextView = root.findViewById(R.id.toLocation)
    private val toDelay: TextView = root.findViewById(R.id.toDelay)
    private val warning: View = root.findViewById(R.id.warning)
    private val lines: ViewGroup = root.findViewById(R.id.lines)
    private val duration: TextView = root.findViewById(R.id.duration)

    fun bind(trip: Trip, listener: OnTripClickListener) {
        // Relative Departure Time
        setRelativeDepartureTime(fromTimeRel, trip.firstDepartureTime)

        // Departure Time
        val firstLeg = trip.legs[0]
        if (firstLeg is Public) {
            fromTime.text = getTime(context, firstLeg.getDepartureTime(true))
        } else {
            fromTime.text = getTime(context, firstLeg.departureTime)
        }

        // Departure Delay
        val firstPublicLeg = trip.firstPublicLeg
        if (firstPublicLeg != null && firstPublicLeg.departureDelay != null && firstPublicLeg.departureDelay != 0L) {
            fromDelay.text = getDelayText(firstPublicLeg.departureDelay)
            fromDelay.visibility = VISIBLE
        } else {
            fromDelay.visibility = GONE
        }
        fromLocation.text = getLocationName(trip.from)

        // Lines
        lines.removeAllViews()
        for (leg in trip.legs) {
            val lineView = LayoutInflater.from(context).inflate(R.layout.list_item_line, lines, false) as LineView
            when (leg) {
                is Public -> lineView.setLine(leg.line)
                is Individual -> lineView.setWalk()
                else -> throw RuntimeException()
            }
            lines.addView(lineView)
        }

        // Warning and Duration
        warning.visibility = if (trip.hasProblem()) VISIBLE else GONE
        duration.text = getDuration(trip.duration)

        // Arrival Time
        val lastLeg = trip.legs[trip.legs.size - 1]
        if (lastLeg is Public) {
            toTime.text = getTime(context, lastLeg.getArrivalTime(true))
        } else {
            toTime.text = getTime(context, lastLeg.arrivalTime)
        }

        // Arrival Delay
        val lastPublicLeg = trip.lastPublicLeg
        if (lastPublicLeg != null && lastPublicLeg.arrivalDelay != null && lastPublicLeg.arrivalDelay != 0L) {
            toDelay.text = getDelayText(lastPublicLeg.arrivalDelay)
            toDelay.visibility = VISIBLE
        } else {
            toDelay.visibility = GONE
        }
        toLocation.text = getLocationName(trip.to)

        // Click Listener
        root.setOnClickListener { listener.onClick(trip) }
    }

    private fun Trip.hasProblem(): Boolean {
        for (leg in legs) {
            if (leg !is Public) continue
            if (!isNullOrEmpty(leg.message)) return true
            if (!isNullOrEmpty(leg.line?.message)) return true
        }
        return false
    }

}
