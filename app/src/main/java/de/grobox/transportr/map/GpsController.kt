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

package de.grobox.transportr.map


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.annotation.WorkerThread
import de.grobox.transportr.AbstractManager
import de.grobox.transportr.locations.ReverseGeocoder
import de.grobox.transportr.locations.ReverseGeocoder.ReverseGeocoderCallback
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.GpsController.Companion.GPS_FIX_EXPIRY
import de.schildbach.pte.dto.Location.coord
import java.util.*
import java.util.concurrent.TimeUnit

data class GpsState(var hasFix: Boolean, var isOld: Boolean, var isTracking: Boolean)

class GpsController(val context: Context) : AbstractManager(), ReverseGeocoderCallback {

    companion object {
        internal val GPS_FIX_EXPIRY = TimeUnit.SECONDS.toMillis(3)
    }

    private val geoCoder = ReverseGeocoder(context, this)

    private val gpsState = MutableLiveData<GpsState>()

    private var location: Location? = null
    private var lastLocation: Location? = null
    private var wrapLocation: WrapLocation? = null

    init {
        gpsState.value = GpsState(false, false, false)
    }

    @WorkerThread
    override fun onLocationRetrieved(location: WrapLocation) {
        runOnUiThread { wrapLocation = location }
    }

    fun setLocation(newLocation: Location?, useGeoCoder: Boolean) {
        if (newLocation == null) return

        if (location == null || location!!.distanceTo(newLocation) > 50) {
            // store location and last location
            lastLocation = location
            location = newLocation

            if (useGeoCoder) {
                // check if we need to use the reverse geo coder
                val isNew = wrapLocation.let { it == null || !it.isSamePlace(newLocation.latitude, newLocation.longitude) }
                if (isNew) geoCoder.findLocation(newLocation)
            }
        }
        // set new FAB state if location is recent
        if (!newLocation.isOld()) {
            updateGpsState(hasFix = true, isOld = false)
        }
    }

    internal fun updateGpsState(hasFix: Boolean? = null, isOld: Boolean? = null, isTracking: Boolean? = null) {
        val newState = GpsState(
            hasFix ?: gpsState.value!!.hasFix,
            isOld ?: gpsState.value!!.isOld,
            isTracking ?: gpsState.value!!.isTracking
        )
        // only set a new value if it is different from the old
        if (newState != gpsState.value) gpsState.value = newState
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

fun Location.toWrapLocation(): WrapLocation? {
    if (latitude == 0.0 && longitude == 0.0) return null
    val loc = coord((latitude * 1E6).toInt(), (longitude * 1E6).toInt())
    return WrapLocation(loc)
}

fun Location.isOld(): Boolean {
    return Date().time > time + GPS_FIX_EXPIRY
}

fun hasLocationProviders(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val activeProviders = locationManager.getProviders(true)

    return activeProviders.size > 1 || (activeProviders.size == 1 && activeProviders[0] != "passive")
}
