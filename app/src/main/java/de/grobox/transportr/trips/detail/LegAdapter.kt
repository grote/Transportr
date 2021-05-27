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

import androidx.recyclerview.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import de.grobox.transportr.R
import de.grobox.transportr.trips.detail.LegViewHolder.LegType
import de.grobox.transportr.trips.detail.LegViewHolder.LegType.*
import de.schildbach.pte.dto.Trip.Leg

internal class LegAdapter internal constructor(
        private val legs: List<Leg>,
        private val listener: LegClickListener,
        private val showLineName: Boolean) : Adapter<LegViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): LegViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.list_item_leg, viewGroup, false)
        return LegViewHolder(v, listener, showLineName)
    }

    override fun onBindViewHolder(ui: LegViewHolder, i: Int) {
        val leg = legs[i]
        ui.bind(leg, getLegType(i))
    }

    override fun getItemCount(): Int {
        return legs.size
    }

    private fun getLegType(position: Int): LegType {
        return when {
            legs.size == 1 -> FIRST_LAST
            position == 0 -> FIRST
            position == legs.size - 1 -> LAST
            else -> MIDDLE
        }
    }

}
