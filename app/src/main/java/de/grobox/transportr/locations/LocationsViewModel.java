package de.grobox.transportr.locations;

import android.arch.lifecycle.LiveData;

import java.util.List;

import de.grobox.transportr.data.locations.FavoriteLocation;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.locations.WorkLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.networks.TransportNetworkViewModel;

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
