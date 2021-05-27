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
import javax.annotation.concurrent.Immutable

@Immutable
internal class Continent constructor(
    @param:StringRes @field:StringRes @get:StringRes val name: Int,
    @param:DrawableRes @field:DrawableRes @get:DrawableRes val contour: Int,
    internal val countries: List<Country>
) : Region {

    override fun getName(context: Context): String {
        return context.getString(name)
    }

    fun getItem(context: Context): ContinentItem {
        return ContinentItem(this, context)
    }

    fun getSubItems(context: Context): List<CountryItem> {
        val sortedCountries = countries.sortedWith(Country.Comparator(context))
        return List(sortedCountries.size, { sortedCountries[it].getItem() })
    }

    fun getTransportNetworks(): Set<TransportNetwork> {
        return countries.flatMap { it.networks }.toSet()
    }

}
