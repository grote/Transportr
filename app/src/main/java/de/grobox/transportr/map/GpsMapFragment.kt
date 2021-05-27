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
import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import androidx.annotation.CallSuper
import androidx.annotation.RequiresPermission
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLng
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerMode
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
import com.mapbox.services.android.telemetry.location.LocationEngineListener
import com.mapbox.services.android.telemetry.location.LocationEnginePriority
import com.mapbox.services.android.telemetry.location.LostLocationEngine
import de.grobox.transportr.R
import de.grobox.transportr.map.GpsController.Companion.GPS_FIX_EXPIRY
import de.grobox.transportr.utils.Constants.REQUEST_LOCATION_PERMISSION
import java.util.concurrent.TimeUnit.SECONDS

abstract class GpsMapFragment : BaseMapFragment(), LocationEngineListener {

    internal lateinit var gpsController: GpsController

    private var locationPlugin: LocationLayerPlugin? = null
    private var locationEngine: LostLocationEngine? = null
    private lateinit var gpsFab: FloatingActionButton

    protected open var useGeoCoder = false

    companion object {
        const val LOCATION_ZOOM = 14
    }

    private val timer = object : CountDownTimer(Long.MAX_VALUE, GPS_FIX_EXPIRY) {
        override fun onTick(millisUntilFinished: Long) {
            val location = locationPlugin?.lastKnownLocation
            if (location == null) gpsController.updateGpsState(hasFix = false)
            else if (location.isOld()) {
                gpsController.updateGpsState(isOld = true)
            }
        }

        override fun onFinish() {}
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
            it.addLocationEngineListener(this)
            // work-around for https://github.com/mapbox/mapbox-plugins-android/issues/371
            it.requestLocationUpdates()
        }
        timer.start()
    }

    @CallSuper
    override fun onStop() {
        locationPlugin?.onStop()
        locationEngine?.let {
            it.removeLocationEngineListener(this)
            it.removeLocationUpdates()
        }
        super.onStop()
        timer.cancel()
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
                gpsController.setLocation(it.lastLocation, useGeoCoder)
            }
        }

        gpsController.getGpsState().observe(this, Observer<GpsState> { onNewGpsState(it!!) })
        map?.addOnScrollListener {
            gpsController.getGpsState().value?.let {
                if (it.isTracking) {
                    gpsController.updateGpsState(isTracking = false)
                }
            }
        }
    }

    private fun enableLocationPlugin() {
        // Check if permissions are enabled and if not request
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            // Create an instance of LOST location engine
            initializeLocationEngine()

            if (locationPlugin == null && map != null) {
                locationPlugin = LocationLayerPlugin(mapView, map!!, locationEngine, R.style.LocationLayer)
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
            it.interval = SECONDS.toMillis(1).toInt()
            it.smallestDisplacement = 0f
            it.activate()
            it.addLocationEngineListener(this)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onConnected() {
        locationEngine?.requestLocationUpdates()
    }

    private fun onGpsFabClick() {
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            Toast.makeText(context, R.string.permission_denied_gps, Toast.LENGTH_SHORT).show()
            return
        }
        if (!hasLocationProviders(context)) {
            Toast.makeText(context, R.string.warning_gps_off, Toast.LENGTH_SHORT).show()
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
            gpsController.updateGpsState(isTracking = true)
        }
    }

    private fun onNewGpsState(gpsState: GpsState) {
        // Floating GPS Action Button Style
        var iconColor = ContextCompat.getColor(context, R.color.fabForegroundInitial)
        var backgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackground))
        if (gpsState.hasFix && !gpsState.isOld && !gpsState.isTracking) {
            iconColor = ContextCompat.getColor(context, R.color.fabForegroundMoved)
            backgroundColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackgroundMoved))
        } else if (gpsState.hasFix && !gpsState.isOld && gpsState.isTracking) {
            iconColor = ContextCompat.getColor(context, R.color.fabForegroundFollow)
        }
        gpsFab.drawable.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        gpsFab.backgroundTintList = backgroundColor

        // Location Marker Icon Style
        locationPlugin?.applyStyle(if (gpsState.isOld) R.style.LocationLayerOld else R.style.LocationLayer)
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

    override fun onLocationChanged(location: Location) {
        if (gpsController.getGpsState().value!!.isTracking) {
            map?.animateCamera(newLatLng(LatLng(location.latitude, location.longitude)))
        }
        gpsController.setLocation(location, useGeoCoder)
    }

    fun zoomToMyLocation() {
        val location = getLastKnownLocation() ?: return
        val latLng = LatLng(location.latitude, location.longitude)
        val update = CameraUpdateFactory.newLatLngZoom(latLng, LOCATION_ZOOM.toDouble())
        map?.moveCamera(update)
    }

    override fun animateTo(latLng: LatLng?, zoom: Int) {
        gpsController.updateGpsState(isTracking = false)
        super.animateTo(latLng, zoom)
    }

    override fun zoomToBounds(latLngBounds: LatLngBounds?, animate: Boolean) {
        gpsController.updateGpsState(isTracking = false)
        super.zoomToBounds(latLngBounds, animate)
    }

    protected fun getLastKnownLocation() = locationPlugin?.lastKnownLocation

}
