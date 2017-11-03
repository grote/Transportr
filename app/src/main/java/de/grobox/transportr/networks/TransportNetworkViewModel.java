package de.grobox.transportr.networks;

import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import de.grobox.transportr.TransportrApplication;

public abstract class TransportNetworkViewModel extends AndroidViewModel {

	private final LiveData<TransportNetwork> transportNetwork;

	protected TransportNetworkViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager) {
		super(application);
		this.transportNetwork = transportNetworkManager.getTransportNetwork();
	}

	public LiveData<TransportNetwork> getTransportNetwork() {
		return transportNetwork;
	}

}
