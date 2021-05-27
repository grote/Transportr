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

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.grobox.transportr.R
import de.grobox.transportr.map.GpsMapFragment
import de.schildbach.pte.dto.Trip
import javax.inject.Inject

class TripMapFragment : GpsMapFragment() {

    companion object {
        @JvmField
        val TAG: String = TripMapFragment::class.java.simpleName
    }

    override val layout: Int
        @LayoutRes
        get() = R.layout.fragment_trip_map

    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TripDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        component.inject(this)
        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(TripDetailViewModel::class.java)
        gpsController = viewModel.gpsController

        return v
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)

        // set padding, so everything gets centered in top half of screen
        val metrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(metrics)
        val topPadding = mapPadding / 2
        val bottomPadding = mapView.height / 4
        map!!.setPadding(0, topPadding, 0, bottomPadding)

        viewModel.getTrip().observe(this, Observer { onTripChanged(it) })
        viewModel.getZoomLocation().observe(this, Observer { this.animateTo(it) })
        viewModel.getZoomLeg().observe(this, Observer { this.animateToBounds(it) })
    }

    private fun onTripChanged(trip: Trip?) {
        if (trip == null) return

        val tripDrawer = TripDrawer(context)
        val zoom = viewModel.isFreshStart.value ?: throw IllegalStateException()
        tripDrawer.draw(map!!, trip, zoom)
        if (zoom) {
            // do not zoom again when returning to this Fragment
            viewModel.isFreshStart.value = false
        }
    }

    private fun animateTo(latLng: LatLng?) {
        animateTo(latLng, 16)
    }

}
