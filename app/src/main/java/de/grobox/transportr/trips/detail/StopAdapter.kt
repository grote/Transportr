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

package de.grobox.transportr.trips.detail

import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import de.grobox.transportr.R
import de.grobox.transportr.trips.detail.LegViewHolder.Companion.DEFAULT_LINE_COLOR
import de.schildbach.pte.dto.Stop

internal class StopAdapter internal constructor(private val listener: LegClickListener) : Adapter<StopViewHolder>() {

    private var stops: List<Stop>? = null
    @ColorInt private var color: Int = DEFAULT_LINE_COLOR

    internal fun changeDate(stops: List<Stop>, @ColorInt color: Int) {
        this.stops = stops
        this.color = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): StopViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_stop, viewGroup, false)
        return StopViewHolder(v, listener)
    }

    override fun onBindViewHolder(ui: StopViewHolder, i: Int) {
        stops?.let {
            val stop = it[i]
            ui.bind(stop, color)
        } ?: throw IllegalStateException()
    }

    override fun getItemCount(): Int {
        return stops?.size ?: 0
    }

}
