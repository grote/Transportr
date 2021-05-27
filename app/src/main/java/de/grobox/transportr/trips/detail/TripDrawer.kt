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


import android.content.Context
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.PorterDuff.Mode.MULTIPLY
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.PolylineOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.grobox.transportr.R
import de.grobox.transportr.map.MapDrawer
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.hasLocation
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Point
import de.schildbach.pte.dto.Stop
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.Trip.*
import java.util.*

internal class TripDrawer(context: Context) : MapDrawer(context) {

    private enum class MarkerType {
        BEGIN, CHANGE, STOP, END, WALK
    }

    fun draw(map: MapboxMap, trip: Trip, zoom: Boolean) {
        // draw leg path first, so it is always at the bottom
        var i = 1
        val builder = LatLngBounds.Builder()
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
            builder.includes(points)
        }
        if (zoom) {
            zoomToBounds(map, builder, false)
        }
    }

    private fun calculatePath(leg: Leg) {
        if (leg.path == null) leg.path = ArrayList()

        if (leg.departure != null && leg.departure.hasLocation()) {
            leg.path.add(Point.fromDouble(leg.departure.latAsDouble, leg.departure.lonAsDouble))
        }

        if (leg is Public) {
            leg.intermediateStops?.filter {
                it.location != null && it.location.hasLocation()
            }?.forEach {
                leg.path.add(Point.fromDouble(it.location.latAsDouble, it.location.lonAsDouble))
            }
        }

        if (leg.arrival != null && leg.arrival.hasLocation()) {
            leg.path.add(Point.fromDouble(leg.arrival.latAsDouble, leg.arrival.lonAsDouble))
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
        markLocation(map, location, icon, location.uniqueShortName(), text)
    }

    private fun getMarkerIcon(type: MarkerType, backgroundColor: Int, foregroundColor: Int): Icon {
        // Get Drawable
        val drawable: Drawable
        if (type == MarkerType.STOP) {
            drawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_trip_stop) ?: throw RuntimeException()
            drawable.mutate().setColorFilter(backgroundColor, SRC_IN)
        } else {
            val res: Int = when (type) {
                MarkerType.BEGIN -> R.drawable.ic_marker_trip_begin
                MarkerType.CHANGE -> R.drawable.ic_marker_trip_change
                MarkerType.END -> R.drawable.ic_marker_trip_end
                MarkerType.WALK -> R.drawable.ic_marker_trip_walk
                else -> throw IllegalArgumentException()
            }
            drawable = ContextCompat.getDrawable(context, res) as LayerDrawable
            drawable.getDrawable(0).mutate().setColorFilter(backgroundColor, MULTIPLY)
            drawable.getDrawable(1).mutate().setColorFilter(foregroundColor, SRC_IN)
        }
        return drawable.toIcon()
    }

    private fun getStopText(stop: Stop): String {
        var text = ""
        stop.getArrivalTime(false)?.let {
            text += "${context.getString(R.string.trip_arr)}: ${formatTime(context, it)}"
        }
        stop.getDepartureTime(false)?.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${formatTime(context, it)}"
        }
        return text
    }

    private fun getStationText(leg: Public, type: MarkerType): String {
        return when (type) {
            MarkerType.BEGIN -> leg.getDepartureTime(false)?.let {
                "${context.getString(R.string.trip_dep)}: ${formatTime(context, it)}"
            }
            MarkerType.END -> leg.getArrivalTime(false)?.let {
                "${context.getString(R.string.trip_arr)}: ${formatTime(context, it)}"
            }
            else -> throw IllegalArgumentException()
        } ?: ""
    }

    private fun getStationText(leg1: Leg, leg2: Leg): String {
        var text = ""
        leg1.arrivalTime?.let {
            text += "${context.getString(R.string.trip_arr)}: ${formatTime(context, it)}"
        }
        leg2.departureTime?.let {
            if (text.isNotEmpty()) text += "\n"
            text += "${context.getString(R.string.trip_dep)}: ${formatTime(context, it)}"
        }
        return text
    }

}
