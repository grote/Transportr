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

package de.grobox.transportr.trips.detail

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.getDelayText
import de.grobox.transportr.utils.DateUtils.getTime
import de.schildbach.pte.dto.Position
import de.schildbach.pte.dto.Stop
import java.util.*

internal abstract class BaseViewHolder(v: View, protected val listener: LegClickListener) : RecyclerView.ViewHolder(v) {

    protected val context: Context = v.context
    protected val fromTime: TextView = v.findViewById(R.id.fromTime)
    protected val toTime: TextView = v.findViewById(R.id.toTime)
    protected val fromDelay: TextView = v.findViewById(R.id.fromDelay)
    protected val toDelay: TextView = v.findViewById(R.id.toDelay)

    fun setArrivalTimes(timeView: TextView, delayView: TextView, stop: Stop) {
        if (stop.arrivalTime == null) return

        val time = Date(stop.arrivalTime.time)

        if (stop.isArrivalTimePredicted && stop.arrivalDelay != null) {
            val delay = stop.arrivalDelay!!
            time.time = time.time - delay
            delayView.text = getDelayText(delay)
            if (delay <= 0)
                delayView.setTextColor(ContextCompat.getColor(context, R.color.md_green_500))
            else
                delayView.setTextColor(ContextCompat.getColor(context, R.color.md_red_500))
            delayView.visibility = VISIBLE
        } else {
            delayView.visibility = GONE
        }
        timeView.text = getTime(context, time)
    }

    fun setDepartureTimes(timeView: TextView, delayView: TextView, stop: Stop) {
        if (stop.departureTime == null) return

        val time = Date(stop.departureTime.time)

        if (stop.isDepartureTimePredicted && stop.departureDelay != null) {
            val delay = stop.departureDelay!!
            time.time = time.time - delay
            delayView.text = getDelayText(delay)
            if (delay <= 0)
                delayView.setTextColor(ContextCompat.getColor(context, R.color.md_green_500))
            else
                delayView.setTextColor(ContextCompat.getColor(context, R.color.md_red_500))
            delayView.visibility = VISIBLE
        } else {
            delayView.visibility = GONE
        }
        timeView.text = getTime(context, time)
    }

    protected fun TextView.addPlatform(position: Position?) {
        if (position == null) return
        text = "$text ${context.getString(R.string.platform, position.toString())}"
    }

}
