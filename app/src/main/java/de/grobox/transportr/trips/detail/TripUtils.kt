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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME
import android.provider.CalendarContract.EXTRA_EVENT_END_TIME
import android.provider.CalendarContract.Events
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.formatDate
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.DateUtils.isToday
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Fare
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.Trip
import java.text.NumberFormat
import java.util.*

internal object TripUtils {

    @JvmStatic
    fun share(context: Context, trip: Trip?) {
        if (trip == null) throw IllegalStateException()
        val sendIntent = Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_SUBJECT, tripToSubject(context, trip))
                .putExtra(Intent.EXTRA_TEXT, tripToString(context, trip))
                .setType("text/plain")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        context.startActivity(Intent.createChooser(sendIntent, context.resources.getText(R.string.share_trip_via)))
    }

    @JvmStatic
    fun intoCalendar(context: Context, trip: Trip?) {
        if (trip == null) throw IllegalStateException()
        val intent = Intent(Intent.ACTION_INSERT).apply {
            type = "vnd.android.cursor.item/event"
            putExtra(EXTRA_EVENT_BEGIN_TIME, trip.firstDepartureTime.time)
            putExtra(EXTRA_EVENT_END_TIME, trip.lastArrivalTime.time)
            putExtra(Events.TITLE, trip.from.name + " → " + trip.to.name)
            putExtra(Events.DESCRIPTION, tripToString(context, trip))
            if (trip.from.place != null) putExtra(Events.EVENT_LOCATION, trip.from.place)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_calendar), LENGTH_LONG).show()
        }
    }

    private fun tripToSubject(context: Context, trip: Trip): String {
        var str = "[${context.resources.getString(R.string.app_name)}] "

        str += "${formatTime(context, trip.firstDepartureTime)} "
        str += "${getLocationName(trip.from)} → ${getLocationName(trip.to)} "
        str += formatTime(context, trip.lastArrivalTime)
        str += " (${formatDate(context, trip.firstDepartureTime)})"

        return str
    }

    private fun tripToString(context: Context, trip: Trip): String {
        val sb = StringBuilder()

        // show date first, if trip doesn't start today
        val calendar = Calendar.getInstance()
        calendar.time = trip.firstDepartureTime
        val isToday = isToday(calendar)
        if (!isToday) {
            sb.append(context.getString(R.string.trip_share_date, formatDate(context, trip.firstDepartureTime))).append("\n\n")
        }

        for (leg in trip.legs) {
            sb.append(legToString(context, leg)).append("\n\n")
        }
        if (isToday) sb.append(context.getString(R.string.times_include_delays)).append("\n\n")
        sb.append(context.getString(R.string.created_by, context.getString(R.string.app_name)))
                .append("\n").append(context.getString(R.string.website)).append(context.getString(R.string.website_source_shared))
        return sb.toString()
    }

    @JvmStatic
    fun legToString(context: Context, leg: Trip.Leg): String {
        var str = "${formatTime(context, leg.departureTime)} ${getLocationName(leg.departure)}"

        if (leg is Trip.Public) {
            // show departure position if existing
            if (leg.departurePosition != null) {
                str += " " + context.getString(R.string.platform, leg.departurePosition.toString())
            }
            str += "\n  ${getEmojiForProduct(leg.line?.product)} "
            leg.line?.label?.let {
                str += it
                leg.destination?.let {
                    str += " → ${getLocationName(it)}"
                }
            }
        } else if (leg is Trip.Individual) {
            str += "\n  \uD83D\uDEB6 ${context.getString(R.string.walk)} "
            if (leg.distance > 0) str += context.resources.getString(R.string.meter, leg.distance)
            if (leg.min > 0) str += " ${context.resources.getString(R.string.for_x_min, leg.min)}"
        }
        str += "\n${formatTime(context, leg.arrivalTime)} ${getLocationName(leg.arrival)}"

        // add arrival position if existing
        if (leg is Trip.Public && leg.arrivalPosition != null) {
            str += " ${context.getString(R.string.platform, leg.arrivalPosition.toString())}"
        }
        return str
    }

    private fun getEmojiForProduct(p: Product?): String = when (p) {
        Product.HIGH_SPEED_TRAIN -> "🚄"
        Product.REGIONAL_TRAIN -> "🚆"
        Product.SUBURBAN_TRAIN -> "🚈"
        Product.SUBWAY -> "🚇"
        Product.TRAM -> "🚊"
        Product.BUS -> "🚌"
        Product.FERRY -> "⛴️"
        Product.CABLECAR -> "🚡"
        Product.ON_DEMAND -> "🚖"
        null -> ""
    }


    fun Trip.hasFare(): Boolean {
        return fares?.isNotEmpty() ?: false
    }

    fun Trip.getStandardFare(): String? {
        fares?.find { fare -> fare.type == Fare.Type.ADULT }?.let {
            val format = NumberFormat.getCurrencyInstance()
            format.currency = it.currency
            return format.format(it.fare)
        }
        return null
    }

}
