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

import org.junit.Assert
import org.junit.Test
import java.util.*


class DateUtilsTest {

    private fun minuteToMillis(minutes: Float): Long {
        return (minutes * android.text.format.DateUtils.MINUTE_IN_MILLIS).toLong()
    }

    @Test
    fun formatDuration() {
        Assert.assertEquals("0:05", DateUtils.formatDuration(minuteToMillis(5f)))
        Assert.assertEquals("0:05", DateUtils.formatDuration(minuteToMillis(5.3f)))
        //Assert.assertEquals("0:06", DateUtils.formatDuration(minuteToMillis(5.8f))) //todo: round to nearest minute
        Assert.assertEquals("0:15", DateUtils.formatDuration(minuteToMillis(15f)))
        Assert.assertEquals("1:05", DateUtils.formatDuration(minuteToMillis(65f)))
        Assert.assertEquals("1:05", DateUtils.formatDuration(minuteToMillis(65.3f)))

        Assert.assertEquals("0:05", DateUtils.formatDuration(Date(), Date().apply { time += minuteToMillis(5f) }))
        Assert.assertEquals("1:05", DateUtils.formatDuration(Date(), Date().apply { time += minuteToMillis(65f) }))
    }

    @Test
    fun formatDelay() {
        Assert.assertEquals("+0", DateUtils.formatDelay(minuteToMillis(0f)))
        Assert.assertEquals("+0", DateUtils.formatDelay(minuteToMillis(0.3f)))
        //Assert.assertEquals("+0", DateUtils.formatDelay(minuteToMillis(-0.3f))) //todo: round to nearest minute
        //Assert.assertEquals("+1", DateUtils.formatDelay(minuteToMillis(0.8f))) //todo: round to nearest minute
        //Assert.assertEquals("-1", DateUtils.formatDelay(minuteToMillis(-0.8f))) //todo: round to nearest minute
        Assert.assertEquals("+1", DateUtils.formatDelay(minuteToMillis(1f)))
        Assert.assertEquals("-1", DateUtils.formatDelay(minuteToMillis(-1f)))
        //Assert.assertEquals("+10", DateUtils.formatDelay(minuteToMillis(9.8f))) //todo: round to nearest minute
        //Assert.assertEquals("-10", DateUtils.formatDelay(minuteToMillis(-9.8f))) //todo: round to nearest minute
        Assert.assertEquals("+100", DateUtils.formatDelay(minuteToMillis(100f)))
        Assert.assertEquals("-100", DateUtils.formatDelay(minuteToMillis(-100f)))
    }

}
