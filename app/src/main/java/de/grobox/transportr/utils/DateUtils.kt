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
package de.grobox.transportr.utils

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import android.view.View
import androidx.core.content.ContextCompat
import de.grobox.transportr.R
import java.util.*
import kotlin.math.abs

object DateUtils {
    fun millisToMinutes(millis: Long): Long {
        val seconds = millis / 1000
        return seconds / 60 + when {
            seconds % 60 >= 30 -> 1
            seconds % 60 <= -30 -> -1
            else -> 0
        }
    }

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
        val durationMinutes = millisToMinutes(duration)
        val m = durationMinutes % 60
        val h = durationMinutes / 60
        return "$h:${m.toString().padStart(2, '0')}"
    }

    fun formatDuration(start: Date, end: Date): String {
        return formatDuration(end.time - start.time)
    }

    fun formatDelay(context: Context, delay: Long): Delay {
        val delayMinutes = millisToMinutes(delay)
        return Delay(
            delay = "${if (delayMinutes >= 0) '+' else ""}$delayMinutes",
            color = ContextCompat.getColor(context, if (delayMinutes > 0) R.color.md_red_500 else R.color.md_green_500)
        )
    }

    data class Delay(
        val delay: String,
        val color: Int
    )

    fun isToday(calendar: Calendar): Boolean {
        return DateUtils.isToday(calendar.timeInMillis)
    }

    fun isWithinMinutes(calendar: Calendar, minutes: Int): Boolean {
        val diff = abs(calendar.timeInMillis - Calendar.getInstance().timeInMillis)
        return diff < minutes * DateUtils.MINUTE_IN_MILLIS
    }

    fun isNow(calendar: Calendar): Boolean = isWithinMinutes(calendar, 1)

    fun formatRelativeTime(context: Context, date: Date, max: Int = 99): RelativeTime {
        val difference = getDifferenceInMinutes(date)
        return RelativeTime(
            relativeTime = when {
                difference !in -max..max -> ""
                difference == 0L -> context.getString(R.string.now_small)
                difference > 0 -> context.getString(R.string.in_x_minutes, difference)
                else -> context.getString(R.string.x_minutes_ago, difference * -1)
            },
            visibility = if (difference in -max..max) View.VISIBLE else View.GONE
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
        return millisToMinutes(d2.time - d1.time)
    }

    private fun getDifferenceInMinutes(date: Date): Long {
        return getDifferenceInMinutes(Date(), date)
    }
}