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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.annotation.VisibleForTesting
import de.grobox.transportr.settings.SettingsManager
import de.schildbach.pte.NetworkId
import javax.inject.Inject

class TransportNetworkManager @Inject
constructor(private val settingsManager: SettingsManager) {

    val transportNetwork = MutableLiveData<TransportNetwork>()
    val networkId: LiveData<NetworkId> = Transformations.map<TransportNetwork, NetworkId>(transportNetwork, { it.id })
    private var transportNetwork2: TransportNetwork? = null
    private var transportNetwork3: TransportNetwork? = null

    init {
        val network = loadTransportNetwork(1)
        network?.let { transportNetwork.value = network }

        transportNetwork2 = loadTransportNetwork(2)
        transportNetwork3 = loadTransportNetwork(3)
    }

    private fun loadTransportNetwork(i: Int): TransportNetwork? {
        val networkId = settingsManager.getNetworkId(i)
        return getTransportNetworkByNetworkId(networkId)
    }

    fun getTransportNetwork(i: Int): TransportNetwork? {
        return if (i == 0 || i == 1) {
            transportNetwork.value
        } else if (i == 2) {
            transportNetwork2
        } else if (i == 3) {
            transportNetwork3
        } else {
            throw IllegalArgumentException()
        }
    }

    fun setTransportNetwork(network: TransportNetwork) {
        // check if same network was selected again
        if (network == transportNetwork.value) return
        settingsManager.setNetworkId(network.id)


        // move 2nd network to 3rd if existing and not re-selected
        if (this.transportNetwork2 != null && this.transportNetwork2 != network) {
            this.transportNetwork3 = this.transportNetwork2
        }
        // swap remaining networks
        this.transportNetwork2 = this.transportNetwork.value
        this.transportNetwork.value = network
    }

    fun getTransportNetworkByNetworkId(networkId: NetworkId?): TransportNetwork? {
        return if (networkId == null) null else getTransportNetwork(networkId)
    }

    @VisibleForTesting
    fun clearTransportNetwork() {
        transportNetwork.value = null
    }

}
