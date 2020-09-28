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
package de.grobox.transportr.utils

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.View
import de.grobox.transportr.R
import java.util.*
import kotlin.math.abs

object DateUtils {
    fun formatDate(context: Context, date: Date): String {
        val df = DateFormat.getDateFormat(context)
        return df.format(date)
    }

    fun formatTime(context: Context, date: Date?): String {
        if (date == null) return ""
        val tf = DateFormat.getTimeFormat(context)
        if (tf.numberFormat.minimumIntegerDigits == 1) {
            val formattedTime = tf.format(date)
            return if (formattedTime.indexOf(':') == 1) {
                // ensure times always have the same length, so views (like intermediate stops) align
                "0$formattedTime"
            } else formattedTime
        }
        return tf.format(date)
    }

    fun formatDuration(duration: Long): String {
        // get duration in minutes
        val durationMinutes = duration / 1000 / 60
        val m = durationMinutes % 60
        val h = durationMinutes / 60
        return "$h:${m.toString().padStart(2, '0')}"
    }

    fun formatDuration(start: Date, end: Date): String {
        return formatDuration(end.time - start.time)
    }

    fun formatDelay(delay: Long): String {
        return "${if (delay >= 0) '+' else ""}${delay / 1000 / 60}"
    }

    fun isToday(calendar: Calendar): Boolean {
        return DateUtils.isToday(calendar.timeInMillis)
    }

    fun isWithinMinutes(calendar: Calendar, minutes: Int): Boolean {
        val diff = abs(calendar.timeInMillis - Calendar.getInstance().timeInMillis)
        return diff < minutes * DateUtils.MINUTE_IN_MILLIS
    }

    fun isNow(calendar: Calendar): Boolean = isWithinMinutes(calendar, 1)

    fun formatRelativeTime(context: Context, date: Date): RelativeTime {
        val difference = getDifferenceInMinutes(date)
        return RelativeTime(
            relativeTime = when {
                difference !in -99..99 -> ""
                difference == 0L -> context.getString(R.string.now_small)
                difference > 0 -> context.getString(R.string.in_x_minutes, difference)
                else -> context.getString(R.string.x_minutes_ago, difference * -1)
            },
            visibility = if (difference in -99..99) View.VISIBLE else View.GONE
        )
    }

    data class RelativeTime(
        val relativeTime: String,
        val visibility: Int
    )

    /**
     * Returns difference in minutes
     */
    private fun getDifferenceInMinutes(d1: Date, d2: Date): Long {
        return (d2.time - d1.time) / 1000 / 60
    }

    private fun getDifferenceInMinutes(date: Date): Long {
        return getDifferenceInMinutes(Date(), date)
    }
}