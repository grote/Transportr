package de.grobox.transportr.locations;

import android.arch.lifecycle.LiveData;

import java.util.List;

import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.FavoriteLocation;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.locations.WorkLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.networks.TransportNetworkViewModel;

import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.schildbach.pte.dto.LocationType.ADDRESS;

public abstract class LocationsViewModel extends TransportNetworkViewModel {

	private final LocationRepository locationRepository;

	private final LiveData<HomeLocation> home;
	private final LiveData<WorkLocation> work;
	private final LiveData<List<FavoriteLocation>> locations;

	public LocationsViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager, LocationRepository locationRepository) {
		super(application, transportNetworkManager);
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

	public void useLocation(WrapLocation location, FavLocationType type) {
		locationRepository.addFavoriteLocation(location, type);
	}

	/**
	 * This checks existing {@link ADDRESS} Locations for geographic proximity
	 * before adding the given location as a favorite.
	 * The idea is too prevent duplicates of addresses with slightly different coordinates.
	 *
	 * @return The given {@link WrapLocation} or if found, the existing one
	 */
	public WrapLocation addFavoriteIfNotExists(WrapLocation location) {
		if (locations.getValue() != null) {
			for (FavoriteLocation fav : locations.getValue()) {
				if (fav.type == ADDRESS && fav.name != null && fav.name.equals(location.name) && fav.isSamePlace(location)) {
					return fav;
				}
			}
		}
		useLocation(location, FROM);
		return location;
	}

}
