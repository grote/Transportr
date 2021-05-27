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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.util.DisplayMetrics.DENSITY_DEFAULT
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import de.grobox.transportr.R
import de.schildbach.pte.dto.Location
import de.schildbach.pte.dto.LocationType
import de.schildbach.pte.dto.Product
import de.schildbach.pte.dto.Product.*
import java.text.DecimalFormat
import kotlin.math.roundToInt


object TransportrUtils {

    @JvmStatic
    @DrawableRes
    fun getDrawableForProduct(p: Product?): Int = when (p) {
        HIGH_SPEED_TRAIN -> R.drawable.product_high_speed_train
        REGIONAL_TRAIN -> R.drawable.product_regional_train
        SUBURBAN_TRAIN -> R.drawable.product_suburban_train
        SUBWAY -> R.drawable.product_subway
        TRAM -> R.drawable.product_tram
        BUS -> R.drawable.product_bus
        FERRY -> R.drawable.product_ferry
        CABLECAR -> R.drawable.product_cablecar
        ON_DEMAND -> R.drawable.product_on_demand
        null -> R.drawable.product_bus
    }

    @JvmStatic
    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("label", text)
        clipboard.setPrimaryClip(clip)
    }

    @JvmStatic
    fun getLocationName(l: Location): String? {
        return when {
            l.type == LocationType.COORD -> getCoordName(l)
            l.uniqueShortName() != null -> l.uniqueShortName()
            else -> ""
        }
    }

    @JvmStatic
    fun getCoordName(location: Location): String {
        return getCoordName(location.latAsDouble, location.lonAsDouble)
    }

    private fun getCoordName(lat: Double, lon: Double): String {
        val df = DecimalFormat("#.###")
        return df.format(lat) + '/' + df.format(lon)
    }

    /**
     * Returns distance in dp that triggers a refresh in a list.
     */
    @JvmStatic
    fun getDragDistance(context: Context): Int {
        val dragDistance = context.resources.getDimensionPixelOffset(R.dimen.dragToRefreshDistance)
        val density = context.resources.displayMetrics.density
        return (dragDistance / density).toInt()
    }

    @JvmStatic
    fun dpToPx(context: Context, dp: Int): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DENSITY_DEFAULT)).roundToInt()
    }

    @JvmStatic
    fun hasInternet(context: Context): Boolean {
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    // see https://stackoverflow.com/questions/33050999/programmatically-set-text-color-to-primary-android-textview
    fun Context.getColorFromAttr(@AttrRes attrColor: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return ContextCompat.getColor(this, typedValue.run { if (resourceId != 0) resourceId else data })
    }

}

fun Location.hasLocation() = hasCoord() && (latAs1E6 != 0 || lonAs1E6 != 0)
