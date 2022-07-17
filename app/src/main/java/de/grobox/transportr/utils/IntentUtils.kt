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

package de.grobox.transportr.utils


import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.mapbox.mapboxsdk.geometry.LatLng
import de.grobox.transportr.R
import de.grobox.transportr.departures.DeparturesActivity
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.MapActivity
import de.grobox.transportr.trips.search.DirectionsActivity
import de.grobox.transportr.trips.search.DirectionsActivity.Companion.ACTION_PRE_FILL
import de.grobox.transportr.trips.search.DirectionsActivity.Companion.ACTION_SEARCH
import de.grobox.transportr.utils.Constants.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.regex.Pattern


object IntentUtils {

    @JvmStatic
    @JvmOverloads
    fun findDirections(
        context: Context,
        from: WrapLocation?,
        via: WrapLocation?,
        to: WrapLocation?,
        search: Boolean = true,
        clearTop: Boolean = false
    ) {
        val intent = Intent(context, DirectionsActivity::class.java)
        if (clearTop) intent.flags = FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (search) intent.action = ACTION_SEARCH
        else intent.action = ACTION_PRE_FILL
        intent.putExtra(FROM, from)
        intent.putExtra(VIA, via)
        intent.putExtra(TO, to)
        context.startActivity(intent)
    }

    @JvmStatic
    @JvmOverloads
    fun presetDirections(context: Context, from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, clearTop: Boolean = false) {
        findDirections(context, from, via, to, false, clearTop)
    }

    @JvmStatic
    fun findDepartures(context: Context, location: WrapLocation) {
        val intent = Intent(context, DeparturesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(WRAP_LOCATION, location)
        context.startActivity(intent)
    }

    @JvmStatic
    fun findNearbyStations(context: Context, location: WrapLocation) {
        val intent = Intent(context, MapActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.action = ACTION_SEARCH
        intent.putExtra(WRAP_LOCATION, location)
        context.startActivity(intent)
    }

    @JvmStatic
    fun startGeoIntent(context: Context, loc: WrapLocation) {
        val uri1 = "geo:0,0?q=${loc.latLng.latitude},${loc.latLng.longitude}"
        val uri2 = try {
            "(" + URLEncoder.encode(loc.getName(), "utf-8") + ")"
        } catch (e: UnsupportedEncodingException) {
            "(" + loc.getName() + ")"
        }
        val geo = Uri.parse(uri1 + uri2)

        // show station on external map
        val intent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            data = geo
        }
        val intentChooser = Intent.createChooser(intent,  context.getString(R.string.show_location_in))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            // exclude Transportr from list on Android >= 7
            intentChooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS, arrayOf(ComponentName(context, MapActivity::class.java)))
        try {
            Log.d(context.javaClass.simpleName, "Starting geo intent: $geo")
            context.startActivity(intentChooser)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, context.getString(R.string.error_no_map), Toast.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    fun getWrapLocation(geoUri: String): WrapLocation? {
        val pattern = Pattern.compile("^geo:(0,0\\?q=)?(-?\\d{1,3}(\\.\\d{1,8})?),(-?\\d{1,3}(\\.\\d{1,8})?).*")
        val matcher = pattern.matcher(geoUri)
        if (matcher.matches()) {
            val lat: Double = matcher.group(2)!!.toDouble()
            val lon: Double = matcher.group(4)!!.toDouble()
            return if (lat == 0.0 && lon == 0.0) null else WrapLocation(LatLng(lat, lon))
        }
        return null
    }

}
