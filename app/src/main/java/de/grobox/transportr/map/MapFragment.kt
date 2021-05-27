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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.app.LoaderManager.LoaderCallbacks
import androidx.loader.content.Loader
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener
import de.grobox.transportr.R
import de.grobox.transportr.locations.NearbyLocationsLoader
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.utils.Constants.LOADER_NEARBY_STATIONS
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType.STATION
import de.schildbach.pte.dto.NearbyLocationsResult
import de.schildbach.pte.dto.NearbyLocationsResult.Status.OK
import javax.inject.Inject

class MapFragment : GpsMapFragment(), LoaderCallbacks<NearbyLocationsResult>, OnMarkerClickListener {

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: MapViewModel
    private lateinit var nearbyStationsDrawer: NearbyStationsDrawer

    private var selectedLocationMarker: Marker? = null

    override var useGeoCoder: Boolean = true

    override val layout: Int
        @LayoutRes
        get() = R.layout.fragment_map

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        component.inject(this)

        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(MapViewModel::class.java)
        viewModel.transportNetwork.observe(viewLifecycleOwner, Observer { onTransportNetworkChanged(it) })
        gpsController = viewModel.gpsController

        nearbyStationsDrawer = NearbyStationsDrawer(context)

        return v
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)

        val location = Location(STATION, "fake")
        val args = NearbyLocationsLoader.getBundle(location, 0)
        LoaderManager.getInstance(this).initLoader(LOADER_NEARBY_STATIONS, args, this)

        mapboxMap.addOnMapClickListener { viewModel.mapClicked.call() }
        mapboxMap.addOnMapLongClickListener { point -> viewModel.selectLocation(WrapLocation(point)) }
        mapboxMap.setOnMarkerClickListener(this)

        if (viewModel.transportNetworkWasChanged || mapboxMap.isInitialPosition()) {
            zoomInOnFreshStart()
            viewModel.transportNetworkWasChanged = false
        }

        // observe map related data
        viewModel.getSelectedLocation().observe(this, Observer { onLocationSelected(it) })
        viewModel.getSelectedLocationClicked().observe(this, Observer { onSelectedLocationClicked(it) })
        viewModel.getFindNearbyStations().observe(this, Observer { findNearbyStations(it) })
    }

    private fun MapboxMap.isInitialPosition(): Boolean {
        return cameraPosition.zoom == minZoomLevel &&
                cameraPosition.target == LatLng(0.0, 0.0)
    }

    private fun zoomInOnFreshStart() {
        // zoom to favorite locations or only current location, if no favorites exist
        viewModel.liveBounds.observe(this, Observer { bounds ->
            if (bounds != null) {
                zoomToBounds(bounds)
            } else if (getLastKnownLocation() != null) {
                zoomToMyLocation()
            }
            viewModel.liveBounds.removeObservers(this)
        })
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        if (marker == selectedLocationMarker) {
            viewModel.markerClicked.call()
            return true
        }
        val wrapLocation = nearbyStationsDrawer.getClickedNearbyStation(marker)
        if (wrapLocation != null) {
            viewModel.selectLocation(wrapLocation)
            return true
        }
        return false
    }

    private fun onTransportNetworkChanged(network: TransportNetwork?) {
        if (network != null && map != null) {
            // activity will reload and then zoom in to new area because this is set
            viewModel.transportNetworkWasChanged = true
            // prevent loader from re-adding nearby stations
            LoaderManager.getInstance(this).destroyLoader(LOADER_NEARBY_STATIONS)
        }
    }

    private fun onLocationSelected(location: WrapLocation?) {
        if (location == null) return
        val latLng = location.latLng
        addMarker(latLng)
        animateTo(latLng, LOCATION_ZOOM)
        viewModel.clearSelectedLocation()
    }

    private fun onSelectedLocationClicked(latLng: LatLng?) {
        if (latLng == null) return
        animateTo(latLng, LOCATION_ZOOM)
    }

    private fun addMarker(latLng: LatLng) {
        selectedLocationMarker?.let { map?.removeMarker(it) }
        selectedLocationMarker = map?.addMarker(MarkerOptions().position(latLng))
    }

    private fun findNearbyStations(location: WrapLocation?) {
        if (location == null) return
        val args = NearbyLocationsLoader.getBundle(location.location, 1000)
        LoaderManager.getInstance(this).restartLoader(LOADER_NEARBY_STATIONS, args, this).forceLoad()
    }

    /* Nearby Stations Loader */

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<NearbyLocationsResult> {
        return NearbyLocationsLoader(context, viewModel.transportNetwork.value, args)
    }

    override fun onLoadFinished(loader: Loader<NearbyLocationsResult>, result: NearbyLocationsResult?) {
        map?.let { map ->
            if (result != null && result.status == OK && result.locations != null && result.locations.size > 0) {
                nearbyStationsDrawer.draw(map, result.locations)
            } else {
                Toast.makeText(context, R.string.error_find_nearby_stations, Toast.LENGTH_SHORT).show()
            }
            viewModel.setNearbyStationsFound(true)
        }
    }

    override fun onLoaderReset(loader: Loader<NearbyLocationsResult>) {
        nearbyStationsDrawer.reset()
    }

}
