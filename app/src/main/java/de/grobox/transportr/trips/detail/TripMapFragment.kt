package de.grobox.transportr.trips.detail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.annotation.LayoutRes
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

    @Inject internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TripDetailViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState)

        component.inject(this)
        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(TripDetailViewModel::class.java)
        gpsController = viewModel.gpsController

        if (savedInstanceState == null) {
            requestPermission()
        }
        return v
    }

    @LayoutRes
    override fun getLayout(): Int {
        return R.layout.fragment_trip_map
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)

        // set padding, so everything gets centered in top half of screen
        val metrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(metrics)
        val topPadding = mapPadding / 2
        val bottomPadding = mapView.height / 4
        map.setPadding(0, topPadding, 0, bottomPadding)

        viewModel.getTrip().observe(this, Observer { onTripChanged(it) })
        viewModel.getZoomLocation().observe(this, Observer { this.animateTo(it) })
        viewModel.getZoomLeg().observe(this, Observer { this.animateToBounds(it) })
    }

    private fun onTripChanged(trip: Trip?) {
        if (trip == null) return

        val tripDrawer = TripDrawer(context)
        val zoom = viewModel.isFreshStart.value ?: throw IllegalStateException()
        tripDrawer.draw(map, trip, zoom)
        if (zoom) {
            // do not zoom again when returning to this Fragment
            viewModel.isFreshStart.value = false
        }
    }

    private fun animateTo(latLng: LatLng?) {
        animateTo(latLng, 16)
    }

}
