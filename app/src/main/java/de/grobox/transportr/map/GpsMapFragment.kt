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
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import de.grobox.transportr.R
import de.grobox.transportr.map.GpsMapViewModel.GpsFabState
import de.grobox.transportr.map.PositionController.Companion.FIX_EXPIRY
import de.grobox.transportr.map.PositionController.PositionState
import de.grobox.transportr.map.PositionController.PositionState.*
import de.grobox.transportr.utils.Constants.REQUEST_LOCATION_PERMISSION

internal abstract class GpsMapFragment<ViewModel : GpsMapViewModel> : BaseMapFragment() {

    internal abstract val viewModel: ViewModel

    private var locationComponent: LocationComponent? = null
    private lateinit var gpsFab: FloatingActionButton

    companion object {
        const val LOCATION_ZOOM = 14
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = super.onCreateView(inflater, container, savedInstanceState) as View

        gpsFab = v.findViewById(R.id.gpsFab)
        gpsFab.setOnClickListener { onGpsFabClick() }
        viewModel.gpsFabState.observe(viewLifecycleOwner) { state ->
            // Floating GPS Action Button Style
            val (iconColor, backgroundColor) = when (state) {
                GpsFabState.TRACKING -> Pair(
                    ContextCompat.getColor(context, R.color.fabForegroundFollow),
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackground))
                )
                GpsFabState.ENABLED -> Pair(
                    ContextCompat.getColor(context, R.color.fabForegroundMoved),
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackgroundMoved))
                )
                else -> Pair(
                    ContextCompat.getColor(context, R.color.fabForegroundInitial),
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.fabBackground))
                )
            }
            gpsFab.drawable.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
            gpsFab.backgroundTintList = backgroundColor
        }

        return v
    }

    @CallSuper
    override fun onMapReady(mapboxMap: MapboxMap) {
        super.onMapReady(mapboxMap)
    }

    @CallSuper
    override fun onMapStyleLoaded(style: Style) {
        tryActivateLocationComponent()
    }

    private fun tryActivateLocationComponent() {
        // Check if permissions are enabled and if not request
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            activateLocationComponent()
        } else {
            requestPermission()
        }
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    private fun activateLocationComponent() {
        locationComponent = map?.locationComponent
        map?.getStyle { style ->
            locationComponent?.apply {

                activateLocationComponent(
                    LocationComponentActivationOptions.builder(context, style)
                        .locationComponentOptions(LocationComponentOptions.builder(context).staleStateTimeout(FIX_EXPIRY).build())
                        .useDefaultLocationEngine(false)
                        .build()
                )

                addOnCameraTrackingChangedListener(object : OnCameraTrackingChangedListener {
                    override fun onCameraTrackingDismissed() {}

                    override fun onCameraTrackingChanged(currentMode: Int) {
                        viewModel.isCameraTracking.value = currentMode == CameraMode.TRACKING
                    }
                })

                addOnLocationStaleListener {
                    viewModel.isPositionStale.value = it
                }

                cameraMode = CameraMode.NONE
                renderMode = RenderMode.COMPASS
            }

            viewModel.positionController.position.observe(viewLifecycleOwner) {
                locationComponent?.forceLocationUpdate(it)
            }

            viewModel.positionController.positionState.observe(viewLifecycleOwner) {
                locationComponent?.isLocationComponentEnabled = when (it) {
                    ENABLED -> true
                    else -> false
                }
            }
        }

    }

    @SuppressLint("MissingPermission") //todo: remove
    private fun onGpsFabClick() {
        when (viewModel.positionController.positionState.value) {
            DENIED -> requestPermission()
            DISABLED -> Toast.makeText(context, R.string.warning_gps_off, Toast.LENGTH_SHORT).show()
            ENABLED -> {
                locationComponent?.lastKnownLocation?.let {
                    map?.zoomToMyLocation()
                } ?: Toast.makeText(context, R.string.warning_no_gps_fix, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != REQUEST_LOCATION_PERMISSION) return
        if (grantResults.isNotEmpty() && permissions[0] == ACCESS_FINE_LOCATION && grantResults[0] == PERMISSION_GRANTED) {
            viewModel.positionController.permissionGranted()
            tryActivateLocationComponent()
        }
    }

    protected fun MapboxMap.zoomToMyLocation() {
        locationComponent.setCameraMode(
            CameraMode.TRACKING, 750,
            if (cameraPosition.zoom < LOCATION_ZOOM) LOCATION_ZOOM.toDouble() else null,
            null, null, null
        )
    }

    protected fun getLastKnownLocation() = viewModel.positionController.position.value

}
