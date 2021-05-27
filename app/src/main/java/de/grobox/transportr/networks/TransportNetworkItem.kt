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
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import android.view.View
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import de.grobox.transportr.R

internal class TransportNetworkItem(val transportNetwork: TransportNetwork, idExtra: Int) :
    AbstractExpandableItem<TransportNetworkItem, TransportNetworkViewHolder, TransportNetworkItem>() {

    private val identifier = transportNetwork.id.ordinal + 10000 * idExtra

    fun getName(context: Context): String {
        return transportNetwork.getName(context)
    }

    @IdRes
    override fun getType(): Int {
        return R.id.list_item_transport_network
    }

    @LayoutRes
    override fun getLayoutRes(): Int {
        return R.layout.list_item_transport_network
    }

    override fun bindView(ui: TransportNetworkViewHolder, payloads: List<Any>) {
        super.bindView(ui, payloads)
        ui.bind(transportNetwork, false)
    }

    override fun getViewHolder(view: View): TransportNetworkViewHolder {
        return TransportNetworkViewHolder(view)
    }

    override fun getIdentifier(): Long {
        return identifier.toLong()
    }

}
