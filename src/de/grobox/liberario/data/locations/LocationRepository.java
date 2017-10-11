package de.grobox.liberario.data.locations;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.grobox.liberario.AbstractManager;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.schildbach.pte.NetworkId;

@Singleton
public class LocationRepository extends AbstractManager {

	private final LocationDao locationDao;

	private final LiveData<NetworkId> networkId;
	private final LiveData<HomeLocation> home;
	private final LiveData<WorkLocation> work;
	private final LiveData<List<FavoriteLocation>> locations;

	@Inject
	public LocationRepository(LocationDao locationDao, TransportNetworkManager transportNetworkManager) {
		this.locationDao = locationDao;
		this.networkId = transportNetworkManager.getNetworkId();
		this.home = Transformations.switchMap(networkId, this::getHomeLocation);
		this.work = Transformations.switchMap(networkId, this::getWorkLocation);
		this.locations = Transformations.switchMap(networkId, this::getFavoriteLocations);
	}

	private LiveData<HomeLocation> getHomeLocation(NetworkId id) {
		return locationDao.getHomeLocation(id);
	}

	public LiveData<HomeLocation> getHomeLocation() {
		return home;
	}

	public void setHomeLocation(WrapLocation location) {
		if (networkId.getValue() == null) return;

		runOnBackgroundThread(() -> {
			// add also as favorite location if it doesn't exist already
			FavoriteLocation favoriteLocation = getFavoriteLocation(networkId.getValue(), location);
			if (favoriteLocation == null) locationDao.addFavoriteLocation(new FavoriteLocation(networkId.getValue(), location));

			locationDao.addHomeLocation(new HomeLocation(networkId.getValue(), location));
		});
	}

	private LiveData<WorkLocation> getWorkLocation(NetworkId id) {
		return locationDao.getWorkLocation(id);
	}

	public LiveData<WorkLocation> getWorkLocation() {
		return work;
	}

	public void setWorkLocation(WrapLocation location) {
		if (networkId.getValue() == null) return;

		runOnBackgroundThread(() -> {
			// add also as favorite location if it doesn't exist already
			FavoriteLocation favoriteLocation = getFavoriteLocation(networkId.getValue(), location);
			if (favoriteLocation == null) locationDao.addFavoriteLocation(new FavoriteLocation(networkId.getValue(), location));

			locationDao.addWorkLocation(new WorkLocation(networkId.getValue(), location));
		});
	}

	private LiveData<List<FavoriteLocation>> getFavoriteLocations(NetworkId networkId) {
		return locationDao.getFavoriteLocations(networkId);
	}

	@Nullable
	private FavoriteLocation getFavoriteLocation(NetworkId networkId, @Nullable WrapLocation l) {
		if (l == null) return null;
		return locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name);
	}

	public LiveData<List<FavoriteLocation>> getFavoriteLocations() {
		return locations;
	}

	public void addFavoriteLocation(WrapLocation wrapLocation, FavoriteLocation.FavLocationType type) {
		FavoriteLocation favoriteLocation;
		if (wrapLocation instanceof FavoriteLocation) {
			favoriteLocation = (FavoriteLocation) wrapLocation;
			favoriteLocation.add(type);
		} else if (!(wrapLocation instanceof StoredLocation)) {  // no home or work location please
			favoriteLocation = new FavoriteLocation(networkId.getValue(), wrapLocation);
		} else {
			// nothing for us to do here
			return;
		}
		runOnBackgroundThread(() -> locationDao.addFavoriteLocation(favoriteLocation));
	}

}
