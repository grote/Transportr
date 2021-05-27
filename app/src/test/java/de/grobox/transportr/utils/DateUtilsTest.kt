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
import android.content.res.Resources
import android.view.View
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.formatDelay
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatRelativeTime
import de.grobox.transportr.utils.DateUtils.millisToMinutes
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import java.util.*


class DateUtilsTest {

    @Mock
    lateinit var context: Context
    @Mock
    lateinit var resources: Resources

    private fun minuteToMillis(minutes: Float): Long {
        return (minutes * android.text.format.DateUtils.MINUTE_IN_MILLIS).toLong()
    }

    @Test
    fun millisToMinutesTest() {
        assertEquals(0, millisToMinutes(minuteToMillis(0f)))
        assertEquals(0, millisToMinutes(minuteToMillis(0.3f)))
        assertEquals(0, millisToMinutes(minuteToMillis(-0.3f)))
        assertEquals(1, millisToMinutes(minuteToMillis(0.8f)))
        assertEquals(-1, millisToMinutes(minuteToMillis(-0.8f)))
    }

    @Test
    fun formatDurationTest() {
        assertEquals("0:05", formatDuration(minuteToMillis(5f)))
        assertEquals("0:05", formatDuration(minuteToMillis(5.3f)))
        assertEquals("0:06", formatDuration(minuteToMillis(5.8f)))
        assertEquals("0:15", formatDuration(minuteToMillis(15f)))
        assertEquals("1:05", formatDuration(minuteToMillis(65f)))
        assertEquals("1:05", formatDuration(minuteToMillis(65.3f)))

        assertEquals("0:05", formatDuration(Date(), Date().apply { time += minuteToMillis(5f) }))
        assertEquals("1:05", formatDuration(Date(), Date().apply { time += minuteToMillis(65f) }))
    }

    private val GREEN = 0
    private val RED = 1
    private fun getNow() = "now"
    private fun getIn(difference: Any) = "in $difference"
    private fun getAgo(difference: Any) = "$difference ago"

    @Before
    fun initMocks() {
        MockitoAnnotations.initMocks(this)
        `when`(context.resources).thenReturn(resources)
        `when`(resources.getColor(R.color.md_green_500)).thenReturn(GREEN)
        `when`(resources.getColor(R.color.md_red_500)).thenReturn(RED)

        `when`(context.getString(R.string.now_small)).thenReturn(getNow())
        `when`(context.getString(eq(R.string.in_x_minutes), anyLong())).thenAnswer { i -> getIn(i.arguments[1]) }
        `when`(context.getString(eq(R.string.x_minutes_ago), anyLong())).thenAnswer { i -> getAgo(i.arguments[1]) }
    }

    @Test
    fun formatDelayTest() {
        assertEquals(
            DateUtils.Delay("+0", GREEN),
            formatDelay(context, minuteToMillis(0f))
        )
        assertEquals(
            DateUtils.Delay("+0", GREEN),
            formatDelay(context, minuteToMillis(0.3f))
        )
        assertEquals(
            DateUtils.Delay("+0", GREEN),
            formatDelay(context, minuteToMillis(-0.3f))
        )
        assertEquals(
            DateUtils.Delay("+1", RED),
            formatDelay(context, minuteToMillis(0.8f))
        )
        assertEquals(
            DateUtils.Delay("-1", GREEN),
            formatDelay(context, minuteToMillis(-0.8f))
        )
        assertEquals(
            DateUtils.Delay("+1", RED),
            formatDelay(context, minuteToMillis(1f))
        )
        assertEquals(
            DateUtils.Delay("-1", GREEN),
            formatDelay(context, minuteToMillis(-1f))
        )
        assertEquals(
            DateUtils.Delay("+10", RED),
            formatDelay(context, minuteToMillis(9.8f))
        )
        assertEquals(
            DateUtils.Delay("-10", GREEN),
            formatDelay(context, minuteToMillis(-9.8f))
        )
        assertEquals(
            DateUtils.Delay("+100", RED),
            formatDelay(context, minuteToMillis(100f))
        )
        assertEquals(
            DateUtils.Delay("-100", GREEN),
            formatDelay(context, minuteToMillis(-100f))
        )
    }

    @Test
    fun formatRelativeTimeTest() {
        assertEquals(
            DateUtils.RelativeTime(getNow(), View.VISIBLE),
            formatRelativeTime(context, Date())
        )
        assertEquals(
            DateUtils.RelativeTime(getNow(), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time += minuteToMillis(0.4f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getNow(), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time -= minuteToMillis(0.4f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getIn(1), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time += minuteToMillis(0.8f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getAgo(1), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time -= minuteToMillis(0.8f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getIn(5), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time += minuteToMillis(5f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getAgo(5), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time -= minuteToMillis(5f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getIn(5), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time += minuteToMillis(5.4f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getAgo(5), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time -= minuteToMillis(5.4f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getIn(99), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time += minuteToMillis(99f) })
        )
        assertEquals(
            DateUtils.RelativeTime(getAgo(99), View.VISIBLE),
            formatRelativeTime(context, Date().apply { time -= minuteToMillis(99f) })
        )
        assertEquals(
            DateUtils.RelativeTime("", View.GONE),
            formatRelativeTime(context, Date().apply { time += minuteToMillis(100f) })
        )
        assertEquals(
            DateUtils.RelativeTime("", View.GONE),
            formatRelativeTime(context, Date().apply { time -= minuteToMillis(100f) })
        )

    }

}
