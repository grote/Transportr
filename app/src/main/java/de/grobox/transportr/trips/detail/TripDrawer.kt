package de.grobox.transportr.trips.detail


import android.content.Context
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.grobox.transportr.R
import de.grobox.transportr.utils.DateUtils.getTime
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Point
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.Trip.*
import java.util.*

internal class TripDrawer(private val context: Context) {

    private val iconFactory = IconFactory.getInstance(context)

    private enum class MarkerType {
        BEGIN, CHANGE, STOP, END, WALK
    }

    fun draw(map: MapboxMap, trip: Trip) {
        // draw leg path first, so it is always at the bottom
        var i = 1
        val allPoints = ArrayList<LatLng>()
        for (leg in trip.legs) {
            // add path if it is missing
            if (leg.path == null) calculatePath(leg)
            if (leg.path == null) continue

            // get colors
            val backgroundColor = getBackgroundColor(leg)
            val foregroundColor = getForegroundColor(leg)

            // draw leg path first, so it is always at the bottom
            val points = ArrayList<LatLng>(leg.path.size)
            leg.path.mapTo(points) { LatLng(it.latAsDouble, it.lonAsDouble) }
            map.addPolyline(PolylineOptions()
                    .color(backgroundColor)
                    .addAll(points)
                    .width(5f)
            )

            // Only draw marker icons for public transport legs
            if (leg is Public) {
                // Draw intermediate stops below all others
                leg.intermediateStops?.let {
                    for (stop in it) {
                        val stopIcon = getMarkerIcon(MarkerType.STOP, backgroundColor, foregroundColor)
                        val text = getStopText(stop)
                        markLocation(map, stop.location, stopIcon, text)
                    }
                }

                // Draw first station or change station
                if (i == 1 || i == 2 && trip.legs[0] is Individual) {
                    val icon = getMarkerIcon(MarkerType.BEGIN, backgroundColor, foregroundColor)
                    markLocation(map, leg.departure, icon, getStationText(leg, MarkerType.BEGIN))
                } else {
                    val icon = getMarkerIcon(MarkerType.CHANGE, backgroundColor, foregroundColor)
                    markLocation(map, leg.departure, icon, getStationText(trip.legs[i - 2], leg))
                }

                // Draw final station only at the end or if end is walking
                if (i == trip.legs.size || i == trip.legs.size - 1 && trip.legs[i] is Individual) {
                    val icon = getMarkerIcon(MarkerType.END, backgroundColor, foregroundColor)
                    markLocation(map, leg.arrival, icon, getStationText(leg, MarkerType.END))
                }
            } else if (leg is Individual) {
                // only draw an icon if walk is required in the middle of a trip
                if (i > 1 && i < trip.legs.size) {
                    val icon = getMarkerIcon(MarkerType.WALK, backgroundColor, foregroundColor)
                    markLocation(map, leg.departure, icon, getStationText(trip.legs[i - 2], leg))
                }
            }
            i += 1
            allPoints.addAll(points)
        }
        centerOnTrip(map, allPoints)
    }

    private fun calculatePath(leg: Leg) {
        if (leg.path == null) leg.path = ArrayList()

        if (leg.departure != null && leg.departure.hasLocation()) {
            leg.path.add(Point(leg.departure.lat, leg.departure.lon))
        }

        if (leg is Public) {
            leg.intermediateStops?.filter {
                it.location != null && it.location.hasLocation()
            }?.forEach {
                leg.path.add(Point(it.location.lat, it.location.lon))
            }
        }

        if (leg.arrival != null && leg.arrival.hasLocation()) {
            leg.path.add(Point(leg.arrival.lat, leg.arrival.lon))
        }
    }

    @ColorInt
    private fun getBackgroundColor(leg: Leg): Int {
        if (leg is Public) {
            val line = leg.line
            return if (line?.style != null && line.style!!.backgroundColor != 0) {
                line.style!!.backgroundColor
            } else {
                ContextCompat.getColor(context, R.color.accent)
            }
        }
        return ContextCompat.getColor(context, R.color.walking)
    }

    @ColorInt
    private fun getForegroundColor(leg: Leg): Int {
        if (leg is Public) {
            val line = leg.line
            return if (line?.style != null && line.style!!.foregroundColor != 0) {
                line.style!!.foregroundColor
            } else {
                ContextCompat.getColor(context, android.R.color.white)
            }
        }
        return ContextCompat.getColor(context, android.R.color.black)
    }

    private fun markLocation(map: MapboxMap, location: Location, icon: Icon, text: String) {
        if (!location.hasLocation()) return
        val position = LatLng(location.latAsDouble, location.lonAsDouble)
        map.addMarker(MarkerOptions()
                .icon(icon)
                .position(position)
                .title(location.uniqueShortName())
                .snippet(text)
        )
    }

    private fun getMarkerIcon(type: MarkerType, backgroundColor: Int, foregroundColor: Int): Icon {
        // Get Drawable
        val drawable: Drawable
        if (type == MarkerType.STOP) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_trip_stop) ?: throw RuntimeException()
            drawable.mutate().setColorFilter(backgroundColor, PorterDuff.Mode.SRC_IN)
        } else {
            val res: Int = when (type) {
                MarkerType.BEGIN -> R.drawable.ic_marker_trip_begin
                MarkerType.CHANGE -> R.drawable.ic_marker_trip_change
                MarkerType.END -> R.drawable.ic_marker_trip_end
                MarkerType.WALK -> R.drawable.ic_marker_trip_walk
                else -> throw IllegalArgumentException()
            }
            drawable = ContextCompat.getDrawable(context, res) as LayerDrawable
            drawable.getDrawable(0).mutate().setColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY)
            drawable.getDrawable(1).mutate().setColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN)
        }
        return drawable.toIcon()
    }

    private fun getStopText(stop: Stop): String {
        var text = ""
        stop.getArrivalTime(false)?.let {
            text += "${context.getString(R.string.trip_arr)}: ${getTime(context, it)}"
        }
        stop.getDepartureTime(false)?.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${getTime(context, it)}"
        }
        return text
    }

    private fun getStationText(leg: Public, type: MarkerType): String {
        return when (type) {
            MarkerType.BEGIN -> leg.getDepartureTime(false)?.let {
                "${context.getString(R.string.trip_dep)}: ${getTime(context, it)}"
            }
            MarkerType.END -> leg.getArrivalTime(false)?.let {
                "${context.getString(R.string.trip_arr)}: ${getTime(context, it)}"
            }
            else -> throw IllegalArgumentException()
        } ?: ""
    }

    private fun getStationText(leg1: Leg, leg2: Leg): String {
        var text = ""
        leg1.arrivalTime?.let {
            text += "${context.getString(R.string.trip_arr)}: ${getTime(context, it)}"
        }
        leg2.departureTime?.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${getTime(context, it)}"
        }
        return text
    }

    private fun centerOnTrip(map: MapboxMap, points: List<LatLng>) {
        val latLngBounds = LatLngBounds.Builder().includes(points).build()
        val padding = context.resources.getDimensionPixelSize(R.dimen.mapPadding)
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, padding))
    }

    private fun Drawable.toIcon(): Icon {
        val bitmap = createBitmap(intrinsicWidth, intrinsicHeight, ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return iconFactory.fromBitmap(bitmap)
    }

}
