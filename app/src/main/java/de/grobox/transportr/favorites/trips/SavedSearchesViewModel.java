package de.grobox.transportr.favorites.trips;

import android.arch.lifecycle.LiveData;

import java.util.List;

import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.searches.SearchesRepository;
import de.grobox.transportr.locations.LocationsViewModel;
import de.grobox.transportr.networks.TransportNetworkManager;

public abstract class SavedSearchesViewModel extends LocationsViewModel {

	protected final SearchesRepository searchesRepository;

	private final LiveData<List<FavoriteTripItem>> savedSearches;

	public SavedSearchesViewModel(TransportNetworkManager transportNetworkManager, LocationRepository locationRepository, SearchesRepository searchesRepository) {
		super(transportNetworkManager, locationRepository);
		this.searchesRepository = searchesRepository;
		this.savedSearches = searchesRepository.getFavoriteTrips();
	}

	LiveData<List<FavoriteTripItem>> getFavoriteTrips() {
		return savedSearches;
	}

	void updateFavoriteState(FavoriteTripItem item) {
		searchesRepository.updateFavoriteState(item);
	}

}
