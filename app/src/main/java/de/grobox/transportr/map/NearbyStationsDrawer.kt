package de.grobox.transportr.map

import android.content.Context
import android.support.v4.content.ContextCompat
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.grobox.transportr.R
import de.grobox.transportr.locations.WrapLocation
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.Product
import java.util.*


internal class NearbyStationsDrawer(context: Context) : MapDrawer(context) {

    private val nearbyLocations = HashMap<Marker, Location>()

    fun draw(map: MapboxMap, nearbyStations: List<Location>) {
        val builder = LatLngBounds.Builder()
        for (location in nearbyStations) {
            if (!location.hasLocation()) continue
            if (nearbyLocations.containsValue(location)) continue
            val marker = markLocation(map, location, getIconForProduct(location.products), location.uniqueShortName())
            marker?.let {
                nearbyLocations.put(marker, location)
                builder.include(marker.position)
            }
        }
        zoomToBounds(map, builder, true)
    }

    fun getClickedNearbyStation(marker: Marker): WrapLocation? {
        if (nearbyLocations.containsKey(marker)) {
            return WrapLocation(nearbyLocations[marker])
        }
        return null
    }

    fun reset() {
        nearbyLocations.clear()
    }

    private fun getIconForProduct(p: Set<Product>?): Icon {
        val res = when (p?.iterator()?.next()) {
            Product.HIGH_SPEED_TRAIN -> R.drawable.product_high_speed_train_marker
            Product.REGIONAL_TRAIN -> R.drawable.product_regional_train_marker
            Product.SUBURBAN_TRAIN -> R.drawable.product_suburban_train_marker
            Product.SUBWAY -> R.drawable.product_subway_marker
            Product.TRAM -> R.drawable.product_tram_marker
            Product.BUS -> R.drawable.product_bus_marker
            Product.FERRY -> R.drawable.product_ferry_marker
            Product.CABLECAR -> R.drawable.product_cablecar_marker
            Product.ON_DEMAND -> R.drawable.product_on_demand_marker
            null -> R.drawable.product_bus_marker
        }
        val drawable = ContextCompat.getDrawable(context, res) ?: throw RuntimeException()
        return drawable.toIcon()
    }

}
