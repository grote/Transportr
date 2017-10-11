package de.grobox.liberario.favorites.trips;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import de.grobox.liberario.data.locations.LocationRepository;
import de.grobox.liberario.data.searches.SearchesRepository;
import de.grobox.liberario.locations.LocationsViewModel;
import de.grobox.liberario.networks.TransportNetworkManager;

public class SavedSearchesViewModel extends LocationsViewModel {

	private final SearchesRepository searchesRepository;

	private final LiveData<List<FavoriteTripItem>> savedSearches;

	@Inject
	SavedSearchesViewModel(TransportNetworkManager transportNetworkManager, LocationRepository locationRepository, SearchesRepository searchesRepository) {
		super(transportNetworkManager, locationRepository);
		this.searchesRepository = searchesRepository;
		this.savedSearches = searchesRepository.getFavoriteTrips();
	}

	LiveData<List<FavoriteTripItem>> getFavoriteTrips() {
		return savedSearches;
	}

	public void useFavoriteTripItem(FavoriteTripItem item) {
		Log.w(this.getClass().getName(), "USE FAV TRIP ITEM");
		searchesRepository.storeSearch(item);
	}

	void updateFavoriteState(FavoriteTripItem item) {
		searchesRepository.updateFavoriteState(item);
	}

}
