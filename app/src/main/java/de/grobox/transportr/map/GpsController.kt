package de.grobox.transportr.map


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.location.Location
import android.support.annotation.WorkerThread
import com.mapbox.mapboxsdk.constants.MyLocationTracking.TRACKING_FOLLOW
import de.grobox.transportr.AbstractManager
import de.grobox.transportr.locations.OsmReverseGeocoder
import de.grobox.transportr.locations.OsmReverseGeocoder.OsmReverseGeocoderCallback
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.locations.WrapLocation.WrapType.GPS
import de.grobox.transportr.map.GpsController.FabState.*

internal class GpsController : AbstractManager(), OsmReverseGeocoderCallback {

    internal enum class FabState {
        NO_FIX, FIX, FOLLOW
    }

    private val geoCoder = OsmReverseGeocoder(this)

    private val fabState = MutableLiveData<FabState>()

    private var location: Location? = null
    private var lastLocation: Location? = null
    private var wrapLocation : WrapLocation? = null

    init {
        fabState.value = NO_FIX
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
            if (fabState.value == NO_FIX) {
                fabState.value = FIX
            }
        }
    }

    fun setTrackingMode(mode: Int) = when (mode) {
        TRACKING_FOLLOW -> fabState.value = FOLLOW
        else -> if (location == null) fabState.value = NO_FIX else fabState.value = FIX
    }

    fun getFabState(): LiveData<FabState> = fabState

    fun getWrapLocation(): WrapLocation {
        if (wrapLocation == null) {
            location?.let { return it.toWrapLocation() }
        }
        wrapLocation?.let { return it }
        return WrapLocation(GPS)
    }

}

fun Location.toWrapLocation(): WrapLocation {
    val loc = de.schildbach.pte.dto.Location.coord((latitude * 1E6).toInt(), (longitude * 1E6).toInt())
    return WrapLocation(loc)
}
