package de.grobox.liberario.locations;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.LocationRepository;
import de.grobox.liberario.data.locations.WorkLocation;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.networks.TransportNetworkViewModel;

public abstract class LocationsViewModel extends TransportNetworkViewModel {

	private final LocationRepository locationRepository;

	private final LiveData<HomeLocation> home;
	private final LiveData<WorkLocation> work;
	private final LiveData<List<FavoriteLocation>> locations;

	public LocationsViewModel(TransportNetworkManager transportNetworkManager, LocationRepository locationRepository) {
		super(transportNetworkManager);
		this.locationRepository = locationRepository;
		this.home = locationRepository.getHomeLocation();
		this.work = locationRepository.getWorkLocation();
		this.locations = locationRepository.getFavoriteLocations();
	}

	public void setHome(WrapLocation wrapLocation) {
		locationRepository.setHomeLocation(wrapLocation);
	}

	public LiveData<HomeLocation> getHome() {
		return home;
	}

	public void setWork(WrapLocation wrapLocation) {
		locationRepository.setWorkLocation(wrapLocation);
	}

	public LiveData<WorkLocation> getWork() {
		return work;
	}

	public LiveData<List<FavoriteLocation>> getLocations() {
		return locations;
	}

	public void clickLocation(WrapLocation location, FavLocationType type) {
		locationRepository.addFavoriteLocation(location, type);
	}

}
