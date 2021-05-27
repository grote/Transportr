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

package de.grobox.transportr.locations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import de.grobox.transportr.R
import de.grobox.transportr.ui.LineView
import de.schildbach.pte.dto.Line


internal class LineAdapter : RecyclerView.Adapter<LineViewHolder>() {

    private var lines: List<Line> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_line, parent, false) as LineView
        return LineViewHolder(v)
    }

    override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        holder.bind(lines[position])

    }

    override fun getItemCount() = lines.size

    fun swapLines(linesToSwap: List<Line>) {
        lines = linesToSwap
        notifyDataSetChanged()
    }

}

internal class LineViewHolder(private val lineView: LineView) : RecyclerView.ViewHolder(lineView) {

    fun bind(line: Line) = lineView.setLine(line)

}
