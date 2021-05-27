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
import androidx.annotation.StringRes
import javax.annotation.concurrent.Immutable

@Immutable
internal class Country constructor(
    @param:StringRes @field:StringRes @get:StringRes val name: Int,
    val flag: String,
    val sticky: Boolean = false,
    val networks: List<TransportNetwork>
) : Region {

    override fun getName(context: Context): String {
        return context.getString(name)
    }

    fun getItem(): CountryItem {
        return CountryItem(this)
    }

    fun getSubItems(): List<TransportNetworkItem> {
        return List(networks.size, { networks[it].getItem() })
    }

    internal class Comparator constructor(private val context: Context) : java.util.Comparator<Country> {
        override fun compare(c1: Country, c2: Country): Int {
            if (c1.sticky && !c2.sticky) return -1
            if (!c1.sticky && c2.sticky) return 1
            return c1.getName(context).compareTo(c2.getName(context))
        }
    }

}