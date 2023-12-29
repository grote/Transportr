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

package de.grobox.transportr.departures

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.databinding.ListItemDepartureBinding
import de.grobox.transportr.ui.LineView
import de.grobox.transportr.utils.DateUtils.formatDelay
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.DateUtils.formatRelativeTime
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Departure
import java.util.*

internal class DepartureViewHolder(binding: ListItemDepartureBinding) : RecyclerView.ViewHolder(binding.root) {

    private val card: CardView = binding.root
    private var line: LineView = binding.line
    private val lineName: TextView = binding.lineNameView
    private val timeRel: TextView = binding.departureTimeRel
    private val timeAbs: TextView = binding.departureTimeAbs
    private val delay: TextView = binding.delay
    private val destination: TextView = binding.destinationView
    private val position: TextView = binding.positionView
    private val message: TextView = binding.messageView

    fun bind(dep: Departure) {
        // times and delay
        var plannedTime : Date? = dep.plannedTime
        var predictedTime : Date? = dep.predictedTime
        if (plannedTime == null) {
            if (predictedTime != null) {
                plannedTime = Date(predictedTime.time)
                predictedTime = null
            }
            else throw RuntimeException()
        }
        formatRelativeTime(timeRel.context, predictedTime ?: plannedTime).let {
            timeRel.apply {
                text = it.relativeTime
                visibility = it.visibility
            }
        }
        timeAbs.text = formatTime(timeAbs.context, plannedTime)
        predictedTime?.let {
            val delayTime = it.time - plannedTime.time
            formatDelay(timeRel.context, delayTime).let {
                delay.apply {
                    text = it.delay
                    setTextColor(it.color)
                    visibility = VISIBLE
                }
            }
        } ?: run { delay.visibility = GONE }

        // line icon and name
        line.setLine(dep.line)
        lineName.text = dep.line.name

        // line destination
        dep.destination?.let {
            destination.text = getLocationName(it)
        } ?: run { destination.text = null }

        // platform/position
        dep.position?.let {
            position.text = it.name
            position.visibility = VISIBLE
        } ?: run { position.visibility = GONE }

        // show message if available
        if (dep.message.isNullOrEmpty()) {
            message.visibility = GONE
        } else {
            message.text = dep.message
            message.visibility = VISIBLE
        }

        // TODO show line from here on
        card.isClickable = false
    }

}
