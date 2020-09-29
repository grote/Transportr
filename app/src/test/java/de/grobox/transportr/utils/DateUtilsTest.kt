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
import android.content.res.Resources
import android.view.View
import de.grobox.transportr.R
import org.junit.Assert
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
    fun millisToMinutes() {
        Assert.assertEquals(0, DateUtils.millisToMinutes(minuteToMillis(0f)))
        Assert.assertEquals(0, DateUtils.millisToMinutes(minuteToMillis(0.3f)))
        Assert.assertEquals(0, DateUtils.millisToMinutes(minuteToMillis(-0.3f)))
        Assert.assertEquals(1, DateUtils.millisToMinutes(minuteToMillis(0.8f)))
        Assert.assertEquals(-1, DateUtils.millisToMinutes(minuteToMillis(-0.8f)))
    }

    @Test
    fun formatDuration() {
        Assert.assertEquals("0:05", DateUtils.formatDuration(minuteToMillis(5f)))
        Assert.assertEquals("0:05", DateUtils.formatDuration(minuteToMillis(5.3f)))
        Assert.assertEquals("0:06", DateUtils.formatDuration(minuteToMillis(5.8f)))
        Assert.assertEquals("0:15", DateUtils.formatDuration(minuteToMillis(15f)))
        Assert.assertEquals("1:05", DateUtils.formatDuration(minuteToMillis(65f)))
        Assert.assertEquals("1:05", DateUtils.formatDuration(minuteToMillis(65.3f)))

        Assert.assertEquals("0:05", DateUtils.formatDuration(Date(), Date().apply { time += minuteToMillis(5f) }))
        Assert.assertEquals("1:05", DateUtils.formatDuration(Date(), Date().apply { time += minuteToMillis(65f) }))
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
    fun formatDelay() {
        Assert.assertEquals(
            DateUtils.Delay("+0", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(0f))
        )
        Assert.assertEquals(
            DateUtils.Delay("+0", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(0.3f))
        )
        Assert.assertEquals(
            DateUtils.Delay("+0", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(-0.3f))
        )
        Assert.assertEquals(
            DateUtils.Delay("+1", RED),
            DateUtils.formatDelay(context, minuteToMillis(0.8f))
        )
        Assert.assertEquals(
            DateUtils.Delay("-1", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(-0.8f))
        )
        Assert.assertEquals(
            DateUtils.Delay("+1", RED),
            DateUtils.formatDelay(context, minuteToMillis(1f))
        )
        Assert.assertEquals(
            DateUtils.Delay("-1", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(-1f))
        )
        Assert.assertEquals(
            DateUtils.Delay("+10", RED),
            DateUtils.formatDelay(context, minuteToMillis(9.8f))
        )
        Assert.assertEquals(
            DateUtils.Delay("-10", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(-9.8f))
        )
        Assert.assertEquals(
            DateUtils.Delay("+100", RED),
            DateUtils.formatDelay(context, minuteToMillis(100f))
        )
        Assert.assertEquals(
            DateUtils.Delay("-100", GREEN),
            DateUtils.formatDelay(context, minuteToMillis(-100f))
        )
    }

    @Test
    fun formatRelativeTime() {
        Assert.assertEquals(
            DateUtils.RelativeTime(getNow(), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date())
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getNow(), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time += minuteToMillis(0.4f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getNow(), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time -= minuteToMillis(0.4f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getIn(1), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time += minuteToMillis(0.8f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getAgo(1), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time -= minuteToMillis(0.8f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getIn(5), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time += minuteToMillis(5f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getAgo(5), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time -= minuteToMillis(5f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getIn(5), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time += minuteToMillis(5.4f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getAgo(5), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time -= minuteToMillis(5.4f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getIn(99), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time += minuteToMillis(99f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime(getAgo(99), View.VISIBLE),
            DateUtils.formatRelativeTime(context, Date().apply { time -= minuteToMillis(99f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime("", View.GONE),
            DateUtils.formatRelativeTime(context, Date().apply { time += minuteToMillis(100f) })
        )
        Assert.assertEquals(
            DateUtils.RelativeTime("", View.GONE),
            DateUtils.formatRelativeTime(context, Date().apply { time -= minuteToMillis(100f) })
        )

    }

}
