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
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment

abstract class BaseMapFragment : TransportrFragment(), OnMapReadyCallback {

    protected lateinit var mapView: MapView
    private lateinit var attribution: TextView
    protected var map: MapboxMap? = null
    protected var mapPadding: Int = 0

    @get:LayoutRes
    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val v = inflater.inflate(layout, container, false)
        mapView = v.findViewById(R.id.map)
        attribution = v.findViewById(R.id.attribution)

        mapPadding = resources.getDimensionPixelSize(R.dimen.mapPadding)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
        attribution.movementMethod = LinkMovementMethod.getInstance()
        attribution.text = Html.fromHtml(getString(R.string.map_attribution, getString(R.string.map_attribution_improve)))
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    @CallSuper
    override fun onMapReady(mapboxMap: MapboxMap) {
        map = mapboxMap
        activity?.run {
            // work-around to force update map style after theme switching
            obtainStyledAttributes(intArrayOf(R.attr.mapStyle)).apply {
                val mapStyle = getString(0)
                if (mapStyle != null && mapboxMap.styleUrl != mapStyle) {
                    mapboxMap.setStyleUrl(mapStyle)
                }
                recycle()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    protected open fun animateTo(latLng: LatLng?, zoom: Int) {
        if (latLng == null) return
        map?.let { map ->
            val update = if (map.cameraPosition.zoom < zoom) CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoom.toDouble()
            ) else CameraUpdateFactory.newLatLng(latLng)
            map.easeCamera(update, 1500)
        }
    }

    protected open fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
        if (latLngBounds == null) return
        val update = CameraUpdateFactory.newLatLngBounds(latLngBounds, mapPadding)
        map?.let { map ->
            if (animate) {
                map.easeCamera(update)
            } else {
                map.moveCamera(update)
            }
        }
    }

    protected fun zoomToBounds(latLngBounds: LatLngBounds?) {
        zoomToBounds(latLngBounds, false)
    }

    protected fun animateToBounds(latLngBounds: LatLngBounds?) {
        zoomToBounds(latLngBounds, true)
    }

}
