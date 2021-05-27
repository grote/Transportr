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
package de.grobox.transportr.trips.search

import android.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel
import de.grobox.transportr.locations.LocationLiveData
import de.grobox.transportr.locations.LocationView.LocationViewListener
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.getTransportNetwork
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.trips.TripQuery
import de.grobox.transportr.trips.search.TripsRepository.QueryMoreState
import de.grobox.transportr.ui.TimeDateFragment.TimeDateListener
import de.grobox.transportr.utils.DateUtils
import de.grobox.transportr.utils.LiveTrigger
import de.grobox.transportr.utils.SingleLiveEvent
import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.Trip
import java.util.*
import javax.inject.Inject

class DirectionsViewModel @Inject internal constructor(
    application: TransportrApplication, transportNetworkManager: TransportNetworkManager, settingsManager: SettingsManager,
    locationRepository: LocationRepository, searchesRepository: SearchesRepository
) : SavedSearchesViewModel(application, transportNetworkManager, locationRepository, searchesRepository), TimeDateListener, LocationViewListener {

    private val _tripsRepository: TripsRepository
    private val _fromLocation = MutableLiveData<WrapLocation?>()
    private val _viaLocation = MutableLiveData<WrapLocation?>()
    val viaSupported: LiveData<Boolean>
    private val _toLocation = MutableLiveData<WrapLocation?>()
    val locationLiveData = LocationLiveData(application.applicationContext)
    val findGpsLocation = MutableLiveData<FavLocationType?>()
    val timeUpdate = LiveTrigger()
    private val _now = MutableLiveData(true)
    private val _calendar = MutableLiveData(Calendar.getInstance())
    private val _products = MutableLiveData<EnumSet<Product>>(EnumSet.allOf(Product::class.java))
    private val _isDeparture = MutableLiveData(true)
    private val _isExpanded = MutableLiveData(false)
    val showTrips = SingleLiveEvent<Void>()
    val topSwipeEnabled = MutableLiveData(false)

    val fromLocation: LiveData<WrapLocation?> = _fromLocation
    val viaLocation: LiveData<WrapLocation?> = _viaLocation
    val toLocation: LiveData<WrapLocation?> = _toLocation

    fun setFromLocation(location: WrapLocation?) {
        _fromLocation.value = location
    }

    fun setViaLocation(location: WrapLocation?) {
        _viaLocation.value = location
    }

    fun setToLocation(location: WrapLocation?) {
        _toLocation.value = location
    }

    fun swapFromAndToLocations() {
        val tmp = _toLocation.value
        if (_fromLocation.value?.wrapType == WrapLocation.WrapType.GPS) {
            findGpsLocation.value = null
            // TODO: GPS currently only supports from location, so don't swap it for now
            _toLocation.value = null
        } else {
            _toLocation.value = _fromLocation.value
        }
        _fromLocation.value = tmp
    }

    val lastQueryCalendar: LiveData<Calendar?> = _calendar

    override fun onTimeAndDateSet(calendar: Calendar) {
        setCalendar(calendar)
        search()
    }

    override fun onDepartureOrArrivalSet(departure: Boolean) {
        setIsDeparture(departure)
        search()
    }

    fun resetCalender() {
        _now.value = true
        search()
    }

    private fun setCalendar(calendar: Calendar) {
        _calendar.value = calendar
        _now.value = DateUtils.isNow(calendar)
    }

    val products: LiveData<EnumSet<Product>> = _products

    fun setProducts(newProducts: EnumSet<Product>) {
        _products.value = newProducts
        search()
    }

    val isDeparture: LiveData<Boolean> = _isDeparture

    fun setIsDeparture(departure: Boolean) {
        _isDeparture.value = departure
        search()
    }

    val isExpanded: LiveData<Boolean> = _isExpanded

    fun setIsExpanded(expanded: Boolean) {
        _isExpanded.value = expanded
    }

    fun toggleIsExpanded() {
        _isExpanded.value = !_isExpanded.value!!
    }

    val isFavTrip: MutableLiveData<Boolean>
        get() = _tripsRepository.isFavTrip

    fun toggleFavTrip() {
        _tripsRepository.toggleFavState()
    }

    override fun onLocationItemClick(loc: WrapLocation, type: FavLocationType) {
        when (type) {
            FavLocationType.FROM -> setFromLocation(loc)
            FavLocationType.VIA -> setViaLocation(loc)
            FavLocationType.TO -> setToLocation(loc)
        }
        search()
        // clear finding GPS location request
        if (findGpsLocation.value == type) findGpsLocation.value = null
    }

    override fun onLocationCleared(type: FavLocationType) {
        when (type) {
            FavLocationType.FROM -> setFromLocation(null)
            FavLocationType.VIA -> {
                setViaLocation(null)
                search()
            }
            FavLocationType.TO -> setToLocation(null)
        }
        // clear finding GPS location request
        if (findGpsLocation.value == type) findGpsLocation.value = null
    }

    /* Trip Queries */
    fun search() {
        val from = _fromLocation.value; val to = _toLocation.value
        val via = if (_isExpanded.value != null && _isExpanded.value!!)
            _viaLocation.value else null
        val calendar = if (_now.value != null && _now.value!!)
            Calendar.getInstance() else _calendar.value
        if (from == null || to == null || calendar == null) return
        _calendar.value = calendar

        val tripQuery = TripQuery(from, via, to, calendar.time, _isDeparture.value, _products.value)
        _tripsRepository.search(tripQuery)
        showTrips.call()
    }

    fun searchMore(later: Boolean) {
        _tripsRepository.searchMore(later)
    }

    val queryMoreState: LiveData<QueryMoreState>
        get() = _tripsRepository.queryMoreState
    val trips: LiveData<Set<Trip>>
        get() = _tripsRepository.trips
    val queryError: LiveData<String>
        get() = _tripsRepository.queryError
    val queryPTEError: LiveData<Pair<String, String>>
        get() = _tripsRepository.queryPTEError
    val queryMoreError: LiveData<String>
        get() = _tripsRepository.queryMoreError

    init {
        var network = transportNetwork.value
        if (network == null) network = getTransportNetwork(NetworkId.DB)
        requireNotNull(network)
        _tripsRepository = TripsRepository(
            application.applicationContext, network.networkProvider,
            settingsManager, locationRepository, searchesRepository
        )
        viaSupported = MutableLiveData(network.networkProvider.hasCapabilities(NetworkProvider.Capability.TRIPS_VIA))
    }
}