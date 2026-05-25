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

package de.grobox.transportr.locations

import android.content.Context
import android.location.Geocoder
import androidx.annotation.WorkerThread
import com.mapbox.mapboxsdk.geometry.LatLng
import de.grobox.transportr.utils.hasLocation
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType.ADDRESS
import de.schildbach.pte.dto.Point
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread


class ReverseGeocoder(private val context: Context, private val callback: ReverseGeocoderCallback) {

    fun findLocation(location: Location) {
        if (!location.hasLocation()) return
        findLocation(location.latAsDouble, location.lonAsDouble)
    }

    fun findLocation(location: android.location.Location) {
        if (location.latitude == 0.0 && location.latitude == 0.0) return
        findLocation(location.latitude, location.longitude)
    }

    private fun findLocation(lat: Double, lon: Double) {
        if (Geocoder.isPresent()) {
            findLocationWithGeocoder(lat, lon)
        } else {
            findLocationWithOsm(lat, lon)
        }
    }

    private fun findLocationWithGeocoder(lat: Double, lon: Double) {
        thread(start = true) {
            try {
                val geoCoder = Geocoder(context, Locale.getDefault())
                val addresses = geoCoder.getFromLocation(lat, lon, 1)
                if (addresses == null || addresses.size == 0) throw IOException("No results")

                val address = addresses[0]

                var name: String? = address.thoroughfare ?: throw IOException("Empty Address")
                if (address.featureName != null) name += " " + address.featureName
                val place = address.locality

                val point = Point.fromDouble(lat, lon)
                val l = Location(ADDRESS, null, point, place, name)

                callback.onLocationRetrieved(WrapLocation(l))
            } catch (e: IOException) {
                if (e.message != "Service not Available") {
                    e.printStackTrace()
                }
                findLocationWithOsm(lat, lon)
            }
        }
    }

    private fun findLocationWithOsm(lat: Double, lon: Double) {
        val client = OkHttpClient()

        // https://nominatim.openstreetmap.org/reverse?lat=52.5217&lon=13.4324&format=json
        val url = StringBuilder("https://nominatim.openstreetmap.org/reverse?")
        url.append("lat=").append(lat).append("&")
        url.append("lon=").append(lon).append("&")
        url.append("format=json")

        val request = Request.Builder()
            .header("User-Agent", "Transportr (https://transportr.app)")
            .url(url.toString())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback.onLocationRetrieved(getWrapLocation(lat, lon))
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    callback.onLocationRetrieved(getWrapLocation(lat, lon))
                    return
                }

                val result = body.string()
                body.close()

                try {
                    val data = JSONObject(result)
                    val address = data.getJSONObject("address")
                    var name = address.optString("road")
                    if (name.isNotEmpty()) {
                        val number = address.optString("house_number")
                        if (number.isNotEmpty()) name += " $number"
                    } else {
                        name = data.getString("display_name").split(",")[0]
                    }
                    var place = address.optString("city")
                    if (place.isEmpty()) place = address.optString("state")

                    val point = Point.fromDouble(lat, lon)
                    val l = Location(ADDRESS, null, point, place, name)

                    callback.onLocationRetrieved(WrapLocation(l))
                } catch (e: JSONException) {
                    callback.onLocationRetrieved(getWrapLocation(lat, lon))
                    throw IOException(e)
                }
            }
        })
    }

    private fun getWrapLocation(lat: Double, lon: Double): WrapLocation {
        return WrapLocation(LatLng(lat, lon))
    }

    interface ReverseGeocoderCallback {
        @WorkerThread
        fun onLocationRetrieved(location: WrapLocation)
    }

}
