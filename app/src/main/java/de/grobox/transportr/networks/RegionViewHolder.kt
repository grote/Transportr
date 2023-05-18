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

import android.os.Build
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.networks.TransportNetwork.Status.ALPHA
import de.grobox.transportr.networks.TransportNetwork.Status.STABLE

internal abstract class RegionViewHolder<in Reg : Region>(v: View) : RecyclerView.ViewHolder(v) {
    protected val name: TextView = v.findViewById(R.id.name)

    open fun bind(region: Reg, expanded: Boolean) {
        name.text = region.getName(name.context)
    }
}

internal class TransportNetworkViewHolder(v: View) : RegionViewHolder<TransportNetwork>(v) {
    private val logo: ImageView = v.findViewById(R.id.logo)
    private val desc: TextView = v.findViewById(R.id.desc)
    private val status: TextView = v.findViewById(R.id.status)

    override fun bind(region: TransportNetwork, expanded: Boolean) {
        super.bind(region, expanded)
        logo.setImageResource(region.logo)
        desc.text = region.getDescription(desc.context)
        if (region.status == STABLE) {
            status.visibility = GONE
        } else {
            if (region.status == ALPHA) {
                status.text = status.context.getString(R.string.alpha)
            } else {
                status.text = status.context.getString(R.string.beta)
            }
            status.visibility = VISIBLE
        }
    }
}

internal abstract class ExpandableRegionViewHolder<in Reg : Region>(v: View) : RegionViewHolder<Reg>(v) {
    private val chevron: ImageView = v.findViewById(R.id.chevron)

    override fun bind(region: Reg, expanded: Boolean) {
        super.bind(region, expanded)
        if (expanded)
            chevron.rotation = 0f
        else
            chevron.rotation = 180f
    }
}

internal class CountryViewHolder(v: View) : ExpandableRegionViewHolder<Country>(v) {
    private val flag: TextView = v.findViewById(R.id.flag)

    override fun bind(region: Country, expanded: Boolean) {
        super.bind(region, expanded)
        flag.text = region.flag
        flag.visibility = VISIBLE
    }
}

internal class ContinentViewHolder(v: View) : ExpandableRegionViewHolder<Continent>(v) {
    private val contour: ImageView = v.findViewById(R.id.contour)

    override fun bind(region: Continent, expanded: Boolean) {
        super.bind(region, expanded)
        contour.setImageResource(region.contour)
    }
}
