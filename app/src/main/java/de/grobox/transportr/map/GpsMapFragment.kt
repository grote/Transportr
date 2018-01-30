/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode.SRC_IN
import android.location.Location
import android.os.Bundle
import android.support.annotation.CallSuper
import android.support.annotation.RequiresPermission
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLng
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.services.android.telemetry.location.LocationEngineListener
import com.mapbox.services.android.telemetry.location.LocationEnginePriority
import com.mapbox.services.android.telemetry.location.LostLocationEngine
import de.grobox.transportr.R
import de.grobox.transportr.map.GpsController.GpsState.FIX
import de.grobox.transportr.map.GpsController.GpsState.FOLLOW
import de.grobox.transportr.utils.Constants.REQUEST_LOCATION_PERMISSION

abstract class GpsMapFragment : BaseMapFragment(), LocationEngineListener {

    internal lateinit var gpsController: GpsController

    private var locationPlugin: LocationLayerPlugin? = null
    private var locationEngine: LostLocationEngine? = null
    private lateinit var gpsFab: FloatingActionButton

    companion object {
        protected const val LOCATION_ZOOM = 14
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState) as View

        gpsFab = v.findViewById(R.id.gpsFab)
        gpsFab.setOnClickListener { onGpsFabClick() }
        return v
    }

    @CallSuper
    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()
        locationPlugin?.onStart()
        locationEngine?.let {
            it.requestLocationUpdates()
            it.addLocationEngineListener(this)
        }
    }

    @CallSuper
    override fun onStop() {
        locationPlugin?.onStop()
        locationEngine?.let {
            it.removeLocationEngineListener(this)
            it.removeLocationUpdates()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        locationEngine?.deactivate()
    }

    @CallSuper
    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)
        enableLocationPlugin()

        locationEngine?.let {
            if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                gpsController.setLocation(it.lastLocation)
            }
        }

        gpsController.getGpsState().observe(this, Observer<GpsController.GpsState> { this.onNewGpsState(it) })
        map?.addOnScrollListener {
            if (gpsController.getGpsState().value == FOLLOW) {
                gpsController.setGpsState(FIX)
            }
        }
    }

    private fun enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            // Create an instance of LOST location engine
            initializeLocationEngine()

            if (locationPlugin == null && map != null) {
                locationPlugin = LocationLayerPlugin(mapView, map!!, locationEngine)
                locationPlugin!!.setLocationLayerEnabled(LocationLayerMode.COMPASS)
            }
        } else {
            requestPermission()
        }
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    private fun initializeLocationEngine() {
        locationEngine = LostLocationEngine(context)
        locationEngine?.let {
            it.priority = LocationEnginePriority.HIGH_ACCURACY
            it.activate()
            it.addLocationEngineListener(this)
        }
    }

    private fun onGpsFabClick() {
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Toast.makeText(context, R.string.permission_denied_gps, Toast.LENGTH_SHORT).show()
            return
        }
        val location = locationPlugin?.lastKnownLocation
        if (location == null) {
            Toast.makeText(context, R.string.warning_no_gps_fix, Toast.LENGTH_SHORT).show()
            return
        }
        map?.let { map ->
            val latLng = LatLng(location.latitude, location.longitude)
            val update = if (map.cameraPosition.zoom < LOCATION_ZOOM) newLatLngZoom(latLng, LOCATION_ZOOM.toDouble()) else newLatLng(latLng)
            map.easeCamera(update, 750)
            gpsController.setGpsState(FOLLOW)
        }
    }

    private fun onNewGpsState(gpsState: GpsController.GpsState?) {
        var iconColor = ContextCompat.getColor(context, R.color.fabForegroundInitial)
        var backgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackground))
        if (gpsState === FIX) {
            iconColor = ContextCompat.getColor(context, R.color.fabForegroundMoved)
            backgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackgroundMoved))
        } else if (gpsState === FOLLOW) {
            iconColor = ContextCompat.getColor(context, R.color.fabForegroundFollow)
        }
        gpsFab.drawable.setColorFilter(iconColor, SRC_IN)
        gpsFab.backgroundTintList = backgroundColor
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) return
        if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
            enableLocationPlugin()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

    override fun onLocationChanged(location: Location) {
        gpsController.setLocation(location)

        if (gpsController.getGpsState().value == FOLLOW) {
            map?.animateCamera(newLatLng(LatLng(location.latitude, location.longitude)))
        }
    }

    protected fun getLastKnownLocation() = locationPlugin?.lastKnownLocation

}
