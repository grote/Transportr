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
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment

abstract class BaseMapFragment : TransportrFragment(), OnMapReadyCallback {

    protected lateinit var mapView: MapView
    private lateinit var attribution: TextView
    protected var map: MapboxMap? = null
    protected var mapPadding: Int = 0
    protected var mapInset: MapPadding = MapPadding()

    @get:LayoutRes
    protected abstract val layout: Int

    // Returns the Jawg url depending on the style given (jawg-streets by default)
    // taken from https://www.jawg.io/docs/integration/maplibre-gl-android/simple-map/
    private fun makeStyleUrl(style: String = "jawg-streets") =
        "${getString(R.string.jawg_styles_url) + style}.json?access-token=${getString(R.string.jawg_access_token)}"

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
        attribution.text = HtmlCompat.fromHtml(getString(R.string.map_attribution, getString(R.string.map_attribution_improve)), FROM_HTML_MODE_LEGACY)
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
                val mapStyle = getString(0)?.let { makeStyleUrl(it) }
                if (mapStyle != null && mapboxMap.style?.uri != mapStyle) {
                    mapboxMap.setStyle(mapStyle, ::onMapStyleLoaded)
                }
                recycle()
            }
        }
    }

    abstract fun onMapStyleLoaded(style: Style)

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
            val padding = mapInset + mapPadding
            map.moveCamera(CameraUpdateFactory.paddingTo(padding.left.toDouble(), padding.top.toDouble(), padding.right.toDouble(), padding.bottom.toDouble()))
            val update = if (map.cameraPosition.zoom < zoom) CameraUpdateFactory.newLatLngZoom(
                latLng,
                zoom.toDouble()
            ) else CameraUpdateFactory.newLatLng(latLng)
            map.easeCamera(update, 1500)
        }
    }

    protected open fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
        if (latLngBounds == null) return
        val padding = mapInset + mapPadding
        val update = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding.left, padding.top, padding.right, padding.bottom)
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

    protected fun setPadding(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
        // store map padding to be retained even after CameraBoundsUpdates
        // and update directly for subsequent camera updates in MapDrawer
        mapInset = MapPadding(left, top, right, bottom)
        map?.moveCamera(CameraUpdateFactory.paddingTo(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble()))
    }

    data class MapPadding(
        val left: Int = 0,
        val top: Int = 0,
        val right: Int = 0,
        val bottom: Int = 0
    ) {
        constructor(padding: DoubleArray) : this(padding[0].toInt(), padding[1].toInt(), padding[2].toInt(), padding[3].toInt())

        operator fun plus(other: Int) =
            MapPadding(left + other, top + other, right + other, bottom + other)
    }

}
