package de.grobox.transportr.locations

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import de.grobox.transportr.R
import de.grobox.transportr.ui.LineView
import de.schildbach.pte.dto.Line


internal class LineAdapter() : RecyclerView.Adapter<LineViewHolder>() {

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
