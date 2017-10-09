/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.liberario.networks;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;
import android.util.Log;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.settings.SettingsManager;
import de.schildbach.pte.NetworkId;

@ParametersAreNonnullByDefault
public class TransportNetworkManager {

	private final SettingsManager settingsManager;

	private MutableLiveData<TransportNetwork> transportNetwork = new MutableLiveData<>();
	private @Nullable TransportNetwork transportNetwork2, transportNetwork3;

	@Inject
	public TransportNetworkManager(SettingsManager settingsManager) {
		this.settingsManager = settingsManager;

		TransportNetwork network = loadTransportNetwork(1);
		if (network != null) transportNetwork.setValue(network);

		transportNetwork2 = loadTransportNetwork(2);
		transportNetwork3 = loadTransportNetwork(3);
	}

	@Nullable
	private TransportNetwork loadTransportNetwork(int i) {
		NetworkId networkId = settingsManager.getNetworkId(i);

		if (networkId != null) {
			for (TransportNetwork network : TransportNetworks.networks) {
				if (network.getId().equals(networkId)) return network;
			}
		}
		return null;
	}

	public LiveData<TransportNetwork> getTransportNetwork() {
		return transportNetwork;
	}

	@Nullable
	public TransportNetwork getTransportNetwork(int i) {
		if (i == 0 || i == 1) {
			return transportNetwork.getValue();
		} else if (i == 2) {
			return transportNetwork2;
		} else if (i == 3) {
			return transportNetwork3;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void setTransportNetwork(TransportNetwork transportNetwork) {
		Log.e("TEST", "SET TRANSPORT NETWORK: " + transportNetwork.getId().name());

		// check if same network was selected again
		if (this.transportNetwork != null && transportNetwork.equals(this.transportNetwork.getValue())) return;
		settingsManager.setNetworkId(transportNetwork.getId());

		// move 2nd network to 3rd if existing and not re-selected
		if (this.transportNetwork2 != null && !this.transportNetwork2.equals(transportNetwork)) {
			this.transportNetwork3 = this.transportNetwork2;
		}
		// swap remaining networks
		this.transportNetwork2 = this.transportNetwork.getValue();
		this.transportNetwork.setValue(transportNetwork);
	}

	public interface TransportNetworkChangedListener {
		void onTransportNetworkChanged(TransportNetwork network);
	}

}
