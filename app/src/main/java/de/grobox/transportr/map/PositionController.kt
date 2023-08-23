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


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import android.os.Bundle
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.AbstractManager
import de.grobox.transportr.locations.ReverseGeocoder
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.utils.NotifyingLiveData
import java.util.concurrent.TimeUnit

 internal class PositionController(val context: Context)
    : AbstractManager(), NotifyingLiveData.OnActivationCallback, ReverseGeocoder.ReverseGeocoderCallback, LocationListener {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val geoCoder = ReverseGeocoder(context, this)

    private val _position = NotifyingLiveData<Location>(this)
    private val _positionState = MutableLiveData<PositionState>()
    private val _positionName = MediatorLiveData<WrapLocation>().apply {
        addSource(_position) {
            geoCoder.findLocation(it)
        }
    }

    val position: LiveData<Location> = _position
    val positionState: LiveData<PositionState> = _positionState
    val positionName: LiveData<WrapLocation> = _positionName

    init {
        _positionState.value = when {
            ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ->
                PositionState.DENIED
            LocationManagerCompat.isLocationEnabled(locationManager) ->
                PositionState.ENABLED
            else -> PositionState.DISABLED
        }
    }

    fun permissionGranted() {
        _positionState.value = if (LocationManagerCompat.isLocationEnabled(locationManager))
            PositionState.ENABLED
        else
            PositionState.DISABLED
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    override fun onActive() {
        for (provider in LOCATION_PROVIDERS) {
            locationManager.requestLocationUpdates(provider, MIN_UPDATE_INTERVAL, MIN_UPDATE_DISTANCE, this, Looper.getMainLooper())
        }
    }

    override fun onInactive() {
        for (provider in LOCATION_PROVIDERS) {
            locationManager.removeUpdates(this)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (isBetterPosition(location, _position.value)) {
            _position.value = location
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onProviderEnabled(provider: String) {
        if (provider == GPS_PROVIDER && LocationManagerCompat.isLocationEnabled(locationManager)) {
            _positionState.value = PositionState.ENABLED
        }
    }

    override fun onProviderDisabled(provider: String) {
        if (provider == GPS_PROVIDER && !LocationManagerCompat.isLocationEnabled(locationManager)) {
            _positionState.value = PositionState.DISABLED
        }
    }

    @WorkerThread
    override fun onLocationRetrieved(location: WrapLocation) {
        _positionName.postValue(location)
    }

    companion object {
        val LOCATION_PROVIDERS = arrayOf(GPS_PROVIDER, NETWORK_PROVIDER)
        val FIX_EXPIRY = TimeUnit.SECONDS.toMillis(5)
        val MIN_UPDATE_INTERVAL = TimeUnit.SECONDS.toMillis(1)
        const val MIN_UPDATE_DISTANCE = 0.0f
        const val ACCURACY_THRESHOLD_METERS = 200

        /**
         * Determines whether one position reading is better than the current position fix
         *
         *
         * (c) https://developer.android.com/guide/topics/location/strategies
         *
         * @param position            The new Location that you want to evaluate
         * @param currentBestPosition The current Location fix, to which you want to compare the new one
         */
        fun isBetterPosition(position: Location?, currentBestPosition: Location?): Boolean {
            if (position == null) {
                return false
            }
            if (currentBestPosition == null) {
                // A new location is always better than no location
                return true
            }

            // Check whether the new location fix is newer or older
            val timeDelta = position.time - currentBestPosition.time
            val isSignificantlyNewer = timeDelta > TimeUnit.MINUTES.toMillis(2)
            val isSignificantlyOlder = timeDelta < -TimeUnit.MINUTES.toMillis(2)
            val isNewer = timeDelta > 0

            // Check whether the new location fix is more or less accurate
            val accuracyDelta = (position.accuracy - currentBestPosition.accuracy).toInt()
            val isLessAccurate = accuracyDelta > 0
            val isMoreAccurate = accuracyDelta < 0
            val isSignificantlyLessAccurate = accuracyDelta > ACCURACY_THRESHOLD_METERS

            // Check if the old and new location are from the same provider
            val isFromSameProvider = position.provider.equals(currentBestPosition.provider)

            // Determine location quality using a combination of timeliness and accuracy
            return when {
                // the user has likely moved
                isSignificantlyNewer -> true
                // If the new location is more than two minutes older, it must be worse
                isSignificantlyOlder -> return false
                isMoreAccurate -> true
                isNewer && !isLessAccurate -> true
                isNewer && !isSignificantlyLessAccurate && isFromSameProvider -> true
                else -> false
            }
        }
    }

    enum class PositionState {
        DENIED,
        DISABLED,
        ENABLED,
    }
}
