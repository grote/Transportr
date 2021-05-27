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

import android.text.Html.fromHtml
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat.getColor
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.common.base.Strings
import com.google.common.base.Strings.isNullOrEmpty
import de.grobox.transportr.R
import de.grobox.transportr.trips.BaseViewHolder
import de.grobox.transportr.trips.detail.LegViewHolder.LegType.*
import de.grobox.transportr.ui.LineView
import de.grobox.transportr.utils.DateUtils
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Line
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Style.RED
import de.schildbach.pte.dto.Trip.*
import kotlinx.android.synthetic.main.list_item_leg.view.*


internal class LegViewHolder(v: View, private val listener: LegClickListener, private val showLineName: Boolean) : BaseViewHolder(v) {

    internal enum class LegType {
        FIRST, MIDDLE, LAST, FIRST_LAST
    }

    internal companion object {
        internal val DEFAULT_LINE_COLOR = RED
    }

    private val fromCircle: ImageView = v.fromCircle
    private val fromLocation: TextView = v.fromLocation
    private val fromButton: ImageButton = v.fromButton

    private val lineBar: ImageView = v.lineBar
    private val lineView: LineView = v.lineView
    private val lineDestination: TextView = v.lineDestination
    private val message: TextView = v.message
    private val duration: TextView = v.duration
    private val stopsText: TextView = v.stopsText
    private val stopsButton: ImageButton = v.stopsButton
    private val stopsList: RecyclerView = v.stopsList

    private val toCircle: ImageView = v.toCircle
    private val toLocation: TextView = v.toLocation
    private val toButton: ImageButton = v.toButton

    private val adapter = StopAdapter(listener)

    fun bind(leg: Leg, legType: LegType) {
        // Locations
        fromLocation.text = getLocationName(leg.departure)
        toLocation.text = getLocationName(leg.arrival)

        fromLocation.setOnClickListener { listener.onLocationClick(leg.departure) }
        toLocation.setOnClickListener { listener.onLocationClick(leg.arrival) }

        fromButton.setOnClickListener { LegPopupMenu(fromButton.context, fromLocation, leg, false).show() }
        toButton.setOnClickListener { LegPopupMenu(toButton.context, toLocation, leg, true).show() }

        // Line bar
        if (legType == FIRST || legType == FIRST_LAST) {
            fromCircle.setImageResource(R.drawable.leg_circle_end)
        } else {
            fromCircle.setImageResource(R.drawable.leg_circle_middle)
        }
        if (legType == MIDDLE || legType == FIRST) {
            toCircle.setImageResource(R.drawable.leg_circle_middle)
        } else {
            toCircle.setImageResource(R.drawable.leg_circle_end)
        }
        fromCircle.setOnClickListener { listener.onLegClick(leg) }
        lineBar.setOnClickListener { listener.onLegClick(leg) }
        toCircle.setOnClickListener { listener.onLegClick(leg) }

        lineView.setOnClickListener { listener.onLegClick(leg) }
        lineDestination.setOnClickListener { listener.onLegClick(leg) }

        // Leg duration
        duration.text = formatDuration(leg.departureTime, leg.arrivalTime)

        if (leg is Public) {
            bindPublic(leg)
        } else if (leg is Individual) {
            bindIndividual(leg)
        }
    }

    private fun bindPublic(leg: Public) {
        setDepartureTimes(fromTime, fromDelay, leg.departureStop)
        setArrivalTimes(toTime, toDelay, leg.arrivalStop)

        // Departure and Arrival Platform
        fromLocation.addPlatform(leg.departurePosition)
        toLocation.addPlatform(leg.arrivalPosition)

        // Line
        lineView.setLine(leg.line)
        if (showLineName && !isNullOrEmpty(leg.line.name)) {
            lineDestination.text = leg.line.name
        } else if (leg.destination != null) {
            lineDestination.text = getLocationName(leg.destination!!)
        } else {
            lineDestination.text = null  // don't hide for constraints
        }

        // Line bar
        val lineColor = getLineColor(leg.line)
        fromCircle.setColorFilter(lineColor)
        lineBar.setColorFilter(lineColor)
        toCircle.setColorFilter(lineColor)

        // Stops
        if (leg.intermediateStops != null && leg.intermediateStops!!.size > 0) {
            val numStops = leg.intermediateStops!!.size
            stopsText.text = stopsText.context.resources.getQuantityString(R.plurals.stops, numStops, numStops)

            // Stops Expansion
            fun onStopClicked() {
                if (stopsList.visibility == GONE) {
                    stopsList.layoutManager = LinearLayoutManager(stopsList.context)
                    adapter.changeDate(leg.intermediateStops as List<Stop>, lineColor)
                    stopsList.adapter = adapter
                    stopsList.visibility = VISIBLE
                    stopsButton.setImageResource(R.drawable.ic_action_navigation_unfold_less)
                } else {
                    stopsList.visibility = GONE
                    stopsButton.setImageResource(R.drawable.ic_action_navigation_unfold_more)
                }
            }
            stopsText.setOnClickListener({ onStopClicked() })
            stopsButton.setOnClickListener({ onStopClicked() })

            stopsText.visibility = VISIBLE
            stopsButton.visibility = VISIBLE
        } else {
            stopsText.visibility = GONE
            stopsButton.visibility = GONE
        }
        stopsList.visibility = GONE

        // Optional message
        var hasText = false
        if (!Strings.isNullOrEmpty(leg.message)) {
            message.visibility = VISIBLE
            @Suppress("DEPRECATION")
            message.text = fromHtml(leg.message)
            hasText = true
        }
        if (leg.line.message != null) {
            message.visibility = VISIBLE
            @Suppress("DEPRECATION")
            message.text = "${message.text}\n${fromHtml(leg.line.message)}"
            hasText = true
        }
        if (!hasText) message.visibility = GONE
    }

    private fun bindIndividual(leg: Individual) {
        fromTime.text = DateUtils.formatTime(fromTime.context, leg.departureTime)
        toTime.text = DateUtils.formatTime(toTime.context, leg.arrivalTime)

        fromDelay.visibility = GONE
        toDelay.visibility = GONE

        lineView.setWalk()

        // line color
        fromCircle.setColorFilter(getColor(fromCircle.context, R.color.walking))
        lineBar.setColorFilter(getColor(lineBar.context, R.color.walking))
        toCircle.setColorFilter(getColor(toCircle.context, R.color.walking))

        // show distance
        if (leg.distance > 0) {
            lineDestination.text = context.getString(R.string.meter, leg.distance)
        } else {
            lineDestination.text = null  // don't hide for constraints
        }

        message.visibility = GONE

        stopsText.visibility = GONE
        stopsButton.visibility = GONE
        stopsList.visibility = GONE
    }

    @ColorInt
    private fun getLineColor(line: Line): Int {
        if (line.style == null) return DEFAULT_LINE_COLOR
        if (line.style!!.backgroundColor != 0) return line.style!!.backgroundColor
        if (line.style!!.backgroundColor2 != 0) return line.style!!.backgroundColor2
        if (line.style!!.foregroundColor != 0) return line.style!!.foregroundColor
        return if (line.style!!.borderColor != 0) line.style!!.borderColor else DEFAULT_LINE_COLOR
    }

}
