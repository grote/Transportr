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

package de.grobox.transportr.map


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import android.support.annotation.WorkerThread
import com.mapbox.mapboxsdk.constants.MyLocationTracking.TRACKING_FOLLOW
import de.grobox.transportr.AbstractManager
import de.grobox.transportr.locations.ReverseGeocoder
import de.grobox.transportr.locations.ReverseGeocoder.ReverseGeocoderCallback
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.GpsController.GpsState.*

internal class GpsController(val context: Context) : AbstractManager(), ReverseGeocoderCallback {

    internal enum class GpsState {
        NO_FIX, FIX, FOLLOW
    }

    private val geoCoder = ReverseGeocoder(context, this)

    private val gpsState = MutableLiveData<GpsState>()

    private var location: Location? = null
    private var lastLocation: Location? = null
    private var wrapLocation: WrapLocation? = null

    init {
        gpsState.value = NO_FIX
    }

    @WorkerThread
    override fun onLocationRetrieved(location: WrapLocation?) {
        runOnUiThread { if (location != null) wrapLocation = location }
    }

    fun setLocation(newLocation: Location?) {
        if (newLocation == null) return

        if (location == null || location!!.distanceTo(newLocation) > 50) {
            // store location and last location
            lastLocation = location
            location = newLocation

            // check if we need to use the reverse geo coder
            val isNew = wrapLocation.let { it == null || !it.isSamePlace(newLocation.latitude, newLocation.longitude) }
            if (isNew) geoCoder.findLocation(newLocation)

            // set new FAB state
            if (gpsState.value == NO_FIX) {
                gpsState.value = FIX
            }
        }
    }

    fun setTrackingMode(mode: Int) = when (mode) {
        TRACKING_FOLLOW -> gpsState.value = FOLLOW
        else -> if (location == null) gpsState.value = NO_FIX else gpsState.value = FIX
    }

    fun setGpsState(gpsState: GpsState) {
        this.gpsState.value = gpsState
    }

    fun getGpsState(): LiveData<GpsState> = gpsState

    fun getWrapLocation(): WrapLocation? {
        if (wrapLocation == null) {
            location?.let { return it.toWrapLocation() }
        }
        wrapLocation?.let { return it }
        return null
    }

}

fun Location.toWrapLocation(): WrapLocation {
    val loc = de.schildbach.pte.dto.Location.coord((latitude * 1E6).toInt(), (longitude * 1E6).toInt())
    return WrapLocation(loc)
}
