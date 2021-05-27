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

package de.grobox.transportr.networks

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.google.common.base.Preconditions.checkArgument
import de.grobox.transportr.R
import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider
import java.lang.ref.SoftReference
import javax.annotation.concurrent.Immutable

@Immutable
data class TransportNetwork internal constructor(
    val id: NetworkId,
    @field:StringRes private val name: Int = 0,
    @field:StringRes private val description: Int,
    @field:StringRes private val agencies: Int = 0,
    val status: Status = Status.STABLE,
    @field:DrawableRes @get:DrawableRes val logo: Int = R.drawable.network_placeholder,
    private val goodLineNames: Boolean = false,
    private val itemIdExtra: Int = 0,
    private val factory: () -> NetworkProvider
) : Region {

    enum class Status {
        ALPHA, BETA, STABLE
    }

    val networkProvider: NetworkProvider by lazy { networkProviderRef.get() ?: getNetworkProviderReference().get()!! }
    private val networkProviderRef by lazy { getNetworkProviderReference() }
    private fun getNetworkProviderReference() = SoftReference<NetworkProvider>(factory.invoke())

    init {
        checkArgument(description != 0 || agencies != 0)
    }

    override fun getName(context: Context): String {
        return if (name == 0) {
            id.name
        } else {
            context.getString(name)
        }
    }

    fun getDescription(context: Context): String? {
        return if (description != 0 && agencies != 0) {
            context.getString(description) + " (" + context.getString(agencies) + ")"
        } else if (description != 0) {
            context.getString(description)
        } else if (agencies != 0) {
            context.getString(agencies)
        } else {
            throw IllegalArgumentException()
        }
    }

    fun hasGoodLineNames(): Boolean {
        return goodLineNames
    }

    internal fun getItem(): TransportNetworkItem {
        return TransportNetworkItem(this, itemIdExtra)
    }

}
