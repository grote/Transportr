package de.grobox.transportr.data.searches;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import de.grobox.transportr.AbstractManager;
import de.grobox.transportr.data.locations.FavoriteLocation;
import de.grobox.transportr.data.locations.LocationDao;
import de.grobox.transportr.favorites.trips.FavoriteTripItem;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.schildbach.pte.NetworkId;

import static de.schildbach.pte.dto.LocationType.COORD;

public class SearchesRepository extends AbstractManager {

	private final SearchesDao searchesDao;
	private final LocationDao locationDao;

	private final LiveData<NetworkId> networkId;
	private final MediatorLiveData<List<FavoriteTripItem>> favoriteTripItems;

	@Inject
	public SearchesRepository(SearchesDao searchesDao, LocationDao locationDao, TransportNetworkManager transportNetworkManager) {
		this.searchesDao = searchesDao;
		this.locationDao = locationDao;
		this.networkId = transportNetworkManager.getNetworkId();
		LiveData<List<StoredSearch>> storedSearches = Transformations.switchMap(networkId, this::getStoredSearches);
		this.favoriteTripItems = new MediatorLiveData<>();
		this.favoriteTripItems.addSource(storedSearches, this::fetchFavoriteTrips);
	}

	private LiveData<List<StoredSearch>> getStoredSearches(NetworkId id) {
		return searchesDao.getStoredSearches(id);
	}

	private void fetchFavoriteTrips(List<StoredSearch> storedSearches) {
		runOnBackgroundThread(() -> {
			List<FavoriteTripItem> favoriteTrips = new ArrayList<>();
			for (StoredSearch storedSearch : storedSearches) {
				FavoriteLocation from = locationDao.getFavoriteLocation(storedSearch.fromId);
				FavoriteLocation via = storedSearch.viaId == null ? null : locationDao.getFavoriteLocation(storedSearch.viaId);
				FavoriteLocation to = locationDao.getFavoriteLocation(storedSearch.toId);
				if (from == null || to == null) throw new RuntimeException("Start or Destination was null");
				FavoriteTripItem item = new FavoriteTripItem(storedSearch, from, via, to);
				favoriteTrips.add(item);
			}
			favoriteTripItems.postValue(favoriteTrips);
		});
	}

	public LiveData<List<FavoriteTripItem>> getFavoriteTrips() {
		return favoriteTripItems;
	}

	public void storeSearch(FavoriteTripItem item) {
		if (item.getFrom().type == COORD || (item.getTo() != null && item.getTo().type == COORD)) return;
		runOnBackgroundThread(() -> {
			if (item.getUid() != 0) {
				searchesDao.updateStoredSearch(item.getUid(), new Date());
			} else {
				if (networkId.getValue() == null) return;
				NetworkId networkId = this.networkId.getValue();

				FavoriteLocation from = getFavoriteLocation(networkId, item.getFrom());
				FavoriteLocation via = getFavoriteLocation(networkId, item.getVia());
				FavoriteLocation to = getFavoriteLocation(networkId, item.getTo());
				if (from == null || to == null) throw new RuntimeException("Start or Destination wasn't saved");

				// try to find existing stored search
				StoredSearch storedSearch = searchesDao.getStoredSearch(networkId, from.getUid(), via == null ? null : via.getUid(), to.getUid());
				if (storedSearch == null) {
					// no search was found, so create a new one
					storedSearch = new StoredSearch(networkId, from, via, to);
				}
				searchesDao.storeSearch(storedSearch);
			}
		});
	}

	public void updateFavoriteState(FavoriteTripItem item) {
		if (item.getUid() == 0) throw new IllegalArgumentException();
		runOnBackgroundThread(() -> searchesDao.setFavorite(item.getUid(), item.isFavorite()));
	}

	public void removeSearch(FavoriteTripItem item) {
		if (item.getUid() == 0) throw new IllegalArgumentException();
		runOnBackgroundThread(() -> searchesDao.delete(item));
	}

	@Nullable
	private FavoriteLocation getFavoriteLocation(NetworkId networkId, @Nullable WrapLocation l) {
		if (l == null) return null;
		return locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name);
	}

}
