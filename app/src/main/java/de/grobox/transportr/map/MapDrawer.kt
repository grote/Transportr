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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import org.maplibre.android.annotations.Marker
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.exceptions.InvalidLatLngBoundsException
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import de.grobox.transportr.R
import de.grobox.transportr.utils.hasLocation
import de.schildbach.pte.dto.Location

internal abstract class MapDrawer(protected val context: Context) {

    private val iconFactory = IconFactory.getInstance(context)

    protected fun markLocation(map: MapLibreMap, location: Location, icon: Icon, title: String, snippet: String? = null): Marker? {
        if (!location.hasLocation()) return null
        return map.addMarker(MarkerOptions()
                .icon(icon)
                .position(LatLng(location.latAsDouble, location.lonAsDouble))
                .title(title)
                .snippet(snippet)
        )
    }

    protected fun zoomToBounds(map: MapLibreMap, builder: LatLngBounds.Builder, animate: Boolean) {
        try {
            val latLngBounds = builder.build()
            val padding = (map.cameraPosition.padding?.let { BaseMapFragment.MapPadding(it) } ?: BaseMapFragment.MapPadding()) +
                    context.resources.getDimensionPixelSize(R.dimen.mapPadding)
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, padding.left, padding.top, padding.right, padding.bottom)
            if (animate) {
                map.easeCamera(cameraUpdate, 750)
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
