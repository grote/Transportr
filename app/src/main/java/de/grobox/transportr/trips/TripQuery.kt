package de.grobox.transportr.trips

import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.locations.WrapLocation
import de.schildbach.pte.dto.Product
import java.util.*

class TripQuery internal constructor(
        val uid: Long,
        val from: WrapLocation, val via: WrapLocation?, val to: WrapLocation,
        val date: Date,
        departure: Boolean?,
        products: Set<Product>?) {

    val departure = departure ?: true
    val products: Set<Product> = products ?: EnumSet.allOf(Product::class.java)

    fun toFavoriteTripItem(): FavoriteTripItem {
        return FavoriteTripItem(uid, from, via, to)
    }

}
