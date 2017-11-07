package de.grobox.transportr.utils


import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SEARCH
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.net.Uri
import android.util.Log
import de.grobox.transportr.departures.DeparturesActivity
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.map.MapActivity
import de.grobox.transportr.trips.search.DirectionsActivity
import de.grobox.transportr.utils.Constants.*
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object IntentUtils {

    @JvmStatic
    @JvmOverloads
    fun findDirections(context: Context, from: WrapLocation?, via: WrapLocation?, to: WrapLocation?, search: Boolean = true, clearTop: Boolean = false) {
        val intent = Intent(context, DirectionsActivity::class.java)
        if (clearTop) intent.flags = FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra(FROM, from)
        intent.putExtra(VIA, via)
        intent.putExtra(TO, to)
        intent.putExtra(SEARCH, search)
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
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = geo
        if (intent.resolveActivity(context.packageManager) != null) {
            Log.d(context.javaClass.simpleName, "Starting geo intent: " + geo.toString())
            context.startActivity(intent)
        }
    }

}
