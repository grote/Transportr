package de.grobox.transportr.favorites.trips;

import android.arch.lifecycle.LiveData;

import java.util.List;

import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.searches.SearchesRepository;
import de.grobox.transportr.locations.LocationsViewModel;
import de.grobox.transportr.networks.TransportNetworkManager;

public abstract class SavedSearchesViewModel extends LocationsViewModel {

	private final SearchesRepository searchesRepository;

	private final LiveData<List<FavoriteTripItem>> savedSearches;

	protected SavedSearchesViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager,
	                              LocationRepository locationRepository, SearchesRepository searchesRepository) {
		super(application, transportNetworkManager, locationRepository);
		this.searchesRepository = searchesRepository;
		this.savedSearches = searchesRepository.getFavoriteTrips();
	}

	LiveData<List<FavoriteTripItem>> getFavoriteTrips() {
		return savedSearches;
	}

	void updateFavoriteState(FavoriteTripItem item) {
		searchesRepository.updateFavoriteState(item);
	}

	void removeFavoriteTrip(FavoriteTripItem item) {
		searchesRepository.removeSearch(item);
	}

}
