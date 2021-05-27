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

package de.grobox.transportr.trips.detail

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import de.grobox.transportr.R
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.GpsController
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkViewModel
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.trips.TripQuery
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.MIDDLE
import de.grobox.transportr.utils.SingleLiveEvent
import de.grobox.transportr.utils.hasLocation
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.Trip.Leg
import javax.inject.Inject

class TripDetailViewModel @Inject internal constructor(
        application: TransportrApplication,
        transportNetworkManager: TransportNetworkManager,
        val gpsController: GpsController,
        private val settingsManager: SettingsManager) : TransportNetworkViewModel(application, transportNetworkManager), LegClickListener {

    enum class SheetState {
        BOTTOM, MIDDLE, EXPANDED
    }

    private val trip = MutableLiveData<Trip>()
    private val zoomLeg = SingleLiveEvent<LatLngBounds>()
    private val zoomLocation = SingleLiveEvent<LatLng>()

    val tripReloadError = SingleLiveEvent<String>()
    val sheetState = MutableLiveData<SheetState>()
    val isFreshStart = MutableLiveData<Boolean>()
    var from: WrapLocation? = null
    var via: WrapLocation? = null
    var to: WrapLocation? = null

    init {
        isFreshStart.value = true
    }

    fun showWhenLocked(): Boolean {
        return settingsManager.showWhenLocked()
    }

    fun getTrip(): LiveData<Trip> {
        return trip
    }

    fun setTrip(trip: Trip) {
        this.trip.value = trip
    }

    override fun onLegClick(leg: Leg) {
        if (leg.path == null || leg.path.size < 2) return

        val latLngs = leg.path.map { LatLng(it.latAsDouble, it.lonAsDouble) }
        zoomLeg.value = LatLngBounds.Builder().includes(latLngs).build()
        sheetState.value = MIDDLE
    }

    override fun onLocationClick(location: Location) {
        if (!location.hasLocation()) return
        val latLng = LatLng(location.latAsDouble, location.lonAsDouble)
        zoomLocation.value = latLng
        sheetState.value = MIDDLE
    }

    fun getZoomLocation(): LiveData<LatLng> {
        return zoomLocation
    }

    fun getZoomLeg(): LiveData<LatLngBounds> {
        return zoomLeg
    }

    fun reloadTrip() {
        val network = transportNetwork.value ?: throw IllegalStateException()

        val oldTrip = trip.value ?: throw IllegalStateException()

        if (from == null || to == null) throw IllegalStateException()

        val errorString = getApplication<Application>().getString(R.string.error_trip_refresh_failed)
        val query = TripQuery(from!!, via, to!!, oldTrip.firstDepartureTime, true, oldTrip.products())
        TripReloader(network.networkProvider, settingsManager, query, trip, errorString, tripReloadError)
                .reload()
    }

}
