/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.getDate
import de.grobox.transportr.utils.DateUtils.getTime
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.Trip

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
        val intent = Intent(Intent.ACTION_EDIT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", trip.firstDepartureTime.time)
                .putExtra("endTime", trip.lastArrivalTime.time)
                .putExtra("title", trip.from.name + " → " + trip.to.name)
                .putExtra("description", tripToString(context, trip))
        if (trip.from.place != null) intent.putExtra("eventLocation", trip.from.place)
        context.startActivity(intent)
    }

    private fun tripToSubject(context: Context, trip: Trip): String {
        var str = "[" + context.resources.getString(R.string.app_name) + "] "

        str += getTime(context, trip.firstDepartureTime) + " "
        str += getLocationName(trip.from)
        str += " → "
        str += getLocationName(trip.to)!! + " "
        str += getTime(context, trip.lastArrivalTime)
        str += " (" + getDate(context, trip.firstDepartureTime) + ")"

        return str
    }

    private fun tripToString(context: Context, trip: Trip): String {
        val sb = StringBuilder()
        for (leg in trip.legs) {
            sb.append(legToString(context, leg)).append("\n\n")
        }
        sb.append("\n")
                .append(context.getString(R.string.times_include_delays))
                .append("\n\n")
                .append(context.getString(R.string.created_by, context.getString(R.string.app_name)))
                .append("\n").append(context.getString(R.string.website))
        return sb.toString()
    }

    @JvmStatic
    fun legToString(context: Context, leg: Trip.Leg): String {
        var str = "${getTime(context, leg.departureTime)} ${getLocationName(leg.departure)}"

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
        str += "\n${getTime(context, leg.arrivalTime)} ${getLocationName(leg.arrival)}"

        // add arrival position if existing
        if (leg is Trip.Public && leg.arrivalPosition != null) {
            str += " ${context.getString(R.string.platform, leg.arrivalPosition.toString())}"
        }
        return str
    }

    private fun getEmojiForProduct(p: Product?): String = when (p) {
        Product.HIGH_SPEED_TRAIN -> "\uD83D\uDE84"
        Product.REGIONAL_TRAIN -> "\uD83D\uDE86"
        Product.SUBURBAN_TRAIN -> "\uD83D\uDE88"
        Product.SUBWAY -> "\uD83D\uDE87"
        Product.TRAM -> "\uD83D\uDE8A"
        Product.BUS -> "\uD83D\uDE8C"
        Product.FERRY -> "⛴️"
        Product.CABLECAR -> "\uD83D\uDEA1"
        Product.ON_DEMAND -> "\uD83D\uDE96"
        null -> ""
    }

}
