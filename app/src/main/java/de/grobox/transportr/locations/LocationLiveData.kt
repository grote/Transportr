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

package de.grobox.transportr.locations

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mapbox.services.android.telemetry.location.LocationEngineListener
import com.mapbox.services.android.telemetry.location.LocationEnginePriority.BALANCED_POWER_ACCURACY
import com.mapbox.services.android.telemetry.location.LostLocationEngine
import de.grobox.transportr.locations.ReverseGeocoder.ReverseGeocoderCallback
import de.grobox.transportr.map.hasLocationProviders


class LocationLiveData(private val context: Context) : LiveData<WrapLocation>(), LocationEngineListener, ReverseGeocoderCallback {

    private val locationEngine: LostLocationEngine = LostLocationEngine(context)

    @RequiresPermission(ACCESS_FINE_LOCATION)
    override fun observe(owner: LifecycleOwner, observer: Observer<in WrapLocation>) {
        super.observe(owner, observer)
    }

    @SuppressLint("MissingPermission")
    override fun onActive() {
        super.onActive()

        if (hasLocationProviders(context)) {
            locationEngine.priority = BALANCED_POWER_ACCURACY
            locationEngine.interval = 5000
            locationEngine.activate()
            locationEngine.addLocationEngineListener(this)
            // work-around for https://github.com/mapbox/mapbox-plugins-android/issues/371
            locationEngine.requestLocationUpdates()
        } else {
            value = null
        }
    }

    override fun onInactive() {
        super.onInactive()
        locationEngine.removeLocationUpdates()
        locationEngine.removeLocationEngineListener(this)
        locationEngine.deactivate()
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        locationEngine.removeLocationUpdates()
        Thread {
            val geoCoder = ReverseGeocoder(context, this)
            geoCoder.findLocation(location)
        }.start()
    }

    @WorkerThread
    override fun onLocationRetrieved(location: WrapLocation) {
        postValue(location)
    }

}
