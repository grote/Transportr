package de.grobox.liberario.trips.search;

import javax.inject.Inject;

import de.grobox.liberario.data.locations.LocationRepository;
import de.grobox.liberario.locations.LocationsViewModel;
import de.grobox.liberario.networks.TransportNetworkManager;

public class DirectionsViewModel extends LocationsViewModel {

	@Inject
	DirectionsViewModel(TransportNetworkManager transportNetworkManager, LocationRepository locationRepository) {
		super(transportNetworkManager, locationRepository);
	}

}
