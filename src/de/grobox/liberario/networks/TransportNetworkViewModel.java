package de.grobox.liberario.networks;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import javax.inject.Inject;

public class TransportNetworkViewModel extends ViewModel {

	private final LiveData<TransportNetwork> transportNetwork;

	@Inject
	public TransportNetworkViewModel(TransportNetworkManager transportNetworkManager) {
		this.transportNetwork = transportNetworkManager.getTransportNetwork();
	}

	public LiveData<TransportNetwork> getTransportNetwork() {
		return transportNetwork;
	}

}
