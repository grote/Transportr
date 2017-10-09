package de.grobox.liberario.favorites.trips;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import de.grobox.liberario.data.searches.SearchesRepository;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.networks.TransportNetworkViewModel;

public class SavedSearchesViewModel extends TransportNetworkViewModel {

	private final SearchesRepository searchesRepository;

	private final LiveData<List<FavoriteTripItem>> savedSearches;

	@Inject
	SavedSearchesViewModel(TransportNetworkManager transportNetworkManager, SearchesRepository searchesRepository) {
		super(transportNetworkManager);
		this.searchesRepository = searchesRepository;
		this.savedSearches = searchesRepository.getFavoriteTrips();
	}

	public LiveData<List<FavoriteTripItem>> getFavoriteTrips() {
		Log.w(this.getClass().getName(), "GET FAV TRIPS");
		return savedSearches;
	}

	public void useFavoriteTripItem(FavoriteTripItem item) {
		Log.w(this.getClass().getName(), "USE FAV TRIP ITEM");
		searchesRepository.storeSearch(item);
	}

	public void updateFavoriteState(FavoriteTripItem item) {
		Log.w(this.getClass().getName(), "UPDATE FAV STATE OF TRIP ITEM " + (item.isFavorite() ? "TO FAV" : "TO RECENT"));
		searchesRepository.updateFavoriteState(item);
	}

}
