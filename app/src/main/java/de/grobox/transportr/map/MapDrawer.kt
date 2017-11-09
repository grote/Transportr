package de.grobox.transportr.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.exceptions.InvalidLatLngBoundsException
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.grobox.transportr.R
import de.schildbach.pte.dto.Location

internal abstract class MapDrawer(protected val context: Context) {

    private val iconFactory = IconFactory.getInstance(context)

    protected fun markLocation(map: MapboxMap, location: Location, icon: Icon, title: String, snippet: String? = null): Marker? {
        if (!location.hasLocation()) return null
        return map.addMarker(MarkerOptions()
                .icon(icon)
                .position(LatLng(location.latAsDouble, location.lonAsDouble))
                .title(title)
                .snippet(snippet)
        )
    }

    protected fun zoomToBounds(map: MapboxMap, builder: LatLngBounds.Builder, animate: Boolean) {
        try {
            val latLngBounds = builder.build()
            val padding = context.resources.getDimensionPixelSize(R.dimen.mapPadding)
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding)
            if (animate) {
                map.easeCamera(cameraUpdate)
            } else {
                map.moveCamera(cameraUpdate)
            }
        } catch (ignored: InvalidLatLngBoundsException) {
        }
    }

    protected fun Drawable.toIcon(): Icon {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return iconFactory.fromBitmap(bitmap)
    }

}