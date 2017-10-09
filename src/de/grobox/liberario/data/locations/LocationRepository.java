package de.grobox.liberario.data.locations;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.grobox.liberario.AbstractManager;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.schildbach.pte.NetworkId;

import static de.grobox.liberario.locations.WrapLocation.WrapType.NORMAL;

@Singleton
public class LocationRepository extends AbstractManager implements Observer<TransportNetwork> {

	private final LocationDao locationDao;

	private final LiveData<TransportNetwork> network;
	private final MutableLiveData<HomeLocation> home = new MutableLiveData<>();
	private final MutableLiveData<WorkLocation> work = new MutableLiveData<>();
	private final MutableLiveData<List<FavoriteLocation>> locations = new MutableLiveData<>();

	@Inject
	public LocationRepository(LocationDao locationDao, TransportNetworkManager transportNetworkManager) {
		this.locationDao = locationDao;
		this.network = transportNetworkManager.getTransportNetwork();
		this.network.observeForever(this);
	}

	@Override
	public void onChanged(@Nullable TransportNetwork transportNetwork) {
		if (transportNetwork == null) return;
		fetchFavoriteLocations(transportNetwork.getId());
		fetchHomeLocation(transportNetwork.getId());
	}

	public LiveData<HomeLocation> getHomeLocation() {
		if (network.getValue() == null) return home;
		if (home.getValue() != null) return home;

		fetchHomeLocation(network.getValue().getId());
		return home;
	}

	private void fetchHomeLocation(NetworkId networkId) {
		runOnBackgroundThread(() -> {
			HomeLocation homeLocation = locationDao.getHomeLocation(networkId);
			home.postValue(homeLocation);
		});
	}

	public void setHomeLocation(WrapLocation location) {
		if (network.getValue() == null) return;

		NetworkId networkId = network.getValue().getId();
		runOnBackgroundThread(() -> {
			// add also as favorite location if it doesn't exist already
			FavoriteLocation favoriteLocation = getFavoriteLocation(networkId, location);
			if (favoriteLocation == null) locationDao.addFavoriteLocation(new FavoriteLocation(networkId, location));

			locationDao.addHomeLocation(new HomeLocation(networkId, location));
//			home.postValue(homeLocation);  // TODO necessary?
		});
	}

	public LiveData<WorkLocation> getWorkLocation() {
		if (network.getValue() == null) return work;
		if (work.getValue() != null) return work;

		fetchWorkLocation(network.getValue().getId());
		return work;
	}

	private void fetchWorkLocation(NetworkId networkId) {
		runOnBackgroundThread(() -> {
			WorkLocation workLocation = locationDao.getWorkLocation(networkId);
			work.postValue(workLocation);
		});
	}

	public void setWorkLocation(WrapLocation location) {
		if (network.getValue() == null) return;

		NetworkId networkId = network.getValue().getId();
		runOnBackgroundThread(() -> {
			// add also as favorite location if it doesn't exist already
			FavoriteLocation favoriteLocation = getFavoriteLocation(networkId, location);
			if (favoriteLocation == null) locationDao.addFavoriteLocation(new FavoriteLocation(networkId, location));

			locationDao.addWorkLocation(new WorkLocation(networkId, location));
		});
	}

	@Nullable
	private FavoriteLocation getFavoriteLocation(NetworkId networkId, @Nullable WrapLocation l) {
		if (l == null) return null;
		return locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name);
	}

	public LiveData<List<FavoriteLocation>> getFavoriteLocations() {
		if (network.getValue() == null) return locations;
		if (locations.getValue() != null) return locations;

		fetchFavoriteLocations(network.getValue().getId());
		return locations;
	}

	private void fetchFavoriteLocations(NetworkId networkId) {
		runOnBackgroundThread(() -> {
			List<FavoriteLocation> favoriteLocations = locationDao.getFavoriteLocations(networkId);
			locations.postValue(favoriteLocations);
		});
	}

	public void addFavoriteLocation(WrapLocation wrapLocation, FavoriteLocation.FavLocationType type) {
		FavoriteLocation favoriteLocation;
		if (wrapLocation instanceof FavoriteLocation) {
			favoriteLocation = (FavoriteLocation) wrapLocation;
			favoriteLocation.add(type);
		} else if (network.getValue() != null && wrapLocation.getWrapType() == NORMAL) {
			favoriteLocation = new FavoriteLocation(network.getValue().getId(), wrapLocation);
		} else {
			// nothing for us to do here
			return;
		}
		runOnBackgroundThread(() -> {
			locationDao.addFavoriteLocation(favoriteLocation);
		});
	}

}
