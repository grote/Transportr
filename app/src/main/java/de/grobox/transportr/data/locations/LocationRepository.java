package de.grobox.transportr.data.locations;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.grobox.transportr.AbstractManager;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.schildbach.pte.NetworkId;

import static de.schildbach.pte.dto.LocationType.COORD;

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

	public LiveData<List<FavoriteLocation>> getFavoriteLocations() {
		return locations;
	}

	public void addFavoriteLocation(WrapLocation wrapLocation, FavLocationType type) {
		if (wrapLocation.type == COORD) return;

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
		runOnBackgroundThread(() -> {
			FavoriteLocation locationToStore = favoriteLocation;
			if (favoriteLocation.getUid() == 0) {
				FavoriteLocation existingLocation = getFavoriteLocation(networkId.getValue(), wrapLocation);
				if (existingLocation != null) {
					locationToStore = existingLocation;
					locationToStore.add(type);
				}
			}
			locationDao.addFavoriteLocation(locationToStore);
		});
	}

	@WorkerThread
	private LiveData<List<FavoriteLocation>> getFavoriteLocations(NetworkId networkId) {
		return locationDao.getFavoriteLocations(networkId);
	}

	@Nullable
	@WorkerThread
	private FavoriteLocation getFavoriteLocation(NetworkId networkId, @Nullable WrapLocation l) {
		if (l == null) return null;
		return locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name);
	}

}
