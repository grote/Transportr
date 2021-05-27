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

import com.mapbox.mapboxsdk.geometry.LatLng
import de.grobox.transportr.locations.WrapLocation
import org.junit.Assert
import org.junit.Test


class IntentUtilsTest {

    @Test
    fun getWrapLocation() {
        Assert.assertEquals(get(1.0, 1.0), IntentUtils.getWrapLocation("geo:1,1"))
        Assert.assertEquals(get(-90.0, -126.0), IntentUtils.getWrapLocation("geo:-90,-126"))
        Assert.assertEquals(get(3.14159265, -3.14159265), IntentUtils.getWrapLocation("geo:3.14159265,-3.14159265?z=20"))
        Assert.assertEquals(get(-48.123, 126.0), IntentUtils.getWrapLocation("geo:-48.123,126(label)"))
        Assert.assertEquals(get(90.0, -126.0), IntentUtils.getWrapLocation("geo:90,-126?q=my+street+address"))
        Assert.assertEquals(get(-48.123, 126.0), IntentUtils.getWrapLocation("geo:0,0?q=-48.123,126(label)"))

        Assert.assertNull(IntentUtils.getWrapLocation("geo:90"))
        Assert.assertNull(IntentUtils.getWrapLocation("geo:90,"))
        Assert.assertNull(IntentUtils.getWrapLocation("geo:90,.23"))
        Assert.assertNull(IntentUtils.getWrapLocation("geo:,23"))
        Assert.assertNull(IntentUtils.getWrapLocation("geo:0,0"))
    }

    private operator fun get(lat: Double, lon: Double): WrapLocation {
        return WrapLocation(LatLng(lat, lon))
    }

}
