package de.grobox.liberario.data.searches;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.grobox.liberario.AbstractManager;
import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.LocationDao;
import de.grobox.liberario.data.locations.WorkLocation;
import de.grobox.liberario.favorites.trips.FavoriteTripItem;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.schildbach.pte.NetworkId;

public class SearchesRepository extends AbstractManager implements Observer<TransportNetwork> {

	private final SearchesDao searchesDao;
	private final LocationDao locationDao;

	private final LiveData<TransportNetwork> network;
	private final MutableLiveData<List<FavoriteTripItem>> favoriteTripItems = new MutableLiveData<>();

	@Inject
	public SearchesRepository(SearchesDao searchesDao, LocationDao locationDao, TransportNetworkManager transportNetworkManager) {
		this.searchesDao = searchesDao;
		this.locationDao = locationDao;
		this.network = transportNetworkManager.getTransportNetwork();
		this.network.observeForever(this);
	}

	@Override
	public void onChanged(@Nullable TransportNetwork transportNetwork) {
		if (transportNetwork == null) return;
		fetchFavoriteTrips(transportNetwork.getId());
	}

	public LiveData<List<FavoriteTripItem>> getFavoriteTrips() {
		if (favoriteTripItems.getValue() != null) return favoriteTripItems;
		if (network.getValue() == null) return favoriteTripItems;

		fetchFavoriteTrips(network.getValue().getId());
		return favoriteTripItems;
	}

	private void fetchFavoriteTrips(NetworkId networkId) {
		runOnBackgroundThread(() -> {
			List<FavoriteTripItem> favoriteTrips = new ArrayList<>();

			HomeLocation homeLocation = locationDao.getHomeLocation(networkId);
			WorkLocation workLocation = locationDao.getWorkLocation(networkId);
			favoriteTrips.add(new FavoriteTripItem(homeLocation));
			favoriteTrips.add(new FavoriteTripItem(workLocation));

			List<StoredSearch> storedSearches = searchesDao.getStoredSearches(networkId);
			for (StoredSearch storedSearch : storedSearches) {
				FavoriteLocation from = locationDao.getFavoriteLocation(storedSearch.fromId);
				FavoriteLocation via = storedSearch.viaId == null ? null : locationDao.getFavoriteLocation(storedSearch.viaId);
				FavoriteLocation to = locationDao.getFavoriteLocation(storedSearch.toId);
				if (from == null || to == null) throw new RuntimeException("Start or Destination was null");
				FavoriteTripItem item = new FavoriteTripItem(storedSearch, from, via, to);
				favoriteTrips.add(item);
				Log.w("TEST", "ADDING ITEM TO RESULT: " + item.toString());
			}
			favoriteTripItems.postValue(favoriteTrips);
		});
	}

	public void storeSearch(FavoriteTripItem item) {
		runOnBackgroundThread(() -> {
			if (item.getUid() != 0) {
				item.use();
				Log.w("TEST", "STORE SEARCH WITH ID: " + item.toString());
				searchesDao.updateStoredSearch(item.getUid(), item.count, item.lastUsed);
			} else {
				if (network.getValue() == null) return;
				NetworkId networkId = network.getValue().getId();

				FavoriteLocation from = getFavoriteLocation(networkId, item.getFrom());
				FavoriteLocation via = getFavoriteLocation(networkId, item.getVia());
				FavoriteLocation to = getFavoriteLocation(networkId, item.getTo());
				if (from == null || to == null) throw new RuntimeException("Start or Destination was null");

				StoredSearch storedSearch = new StoredSearch(networkId, from, via, to);
				Log.w("TEST", "STORE SEARCH WITHOUT ID: " + storedSearch.toString());
				searchesDao.storeSearch(storedSearch);
			}
		});
	}

	public void updateFavoriteState(FavoriteTripItem item) {
		if (item.getUid() == 0) throw new IllegalArgumentException();
		runOnBackgroundThread(() -> searchesDao.setFavorite(item.getUid(), item.isFavorite()));
	}

	@Nullable
	private FavoriteLocation getFavoriteLocation(NetworkId networkId, @Nullable WrapLocation l) {
		if (l == null) return null;
		return locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name);
	}

}

