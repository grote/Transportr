package de.grobox.liberario.locations;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import java.util.List;

import javax.inject.Inject;

import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.LocationRepository;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.networks.TransportNetworkViewModel;

public class LocationsViewModel extends TransportNetworkViewModel {

	private final LocationRepository locationRepository;

	private final LiveData<HomeLocation> home;
	private final LiveData<List<FavoriteLocation>> locations;

	@Inject
	public LocationsViewModel(TransportNetworkManager transportNetworkManager, LocationRepository locationRepository) {
		super(transportNetworkManager);
		this.locationRepository = locationRepository;
		this.home = locationRepository.getHomeLocation();
		this.locations = locationRepository.getFavoriteLocations();
	}

	public LiveData<HomeLocation> getHome() {
		Log.w(this.getClass().getName(), "GET HOME");
		return home;
	}

	public void setHome(WrapLocation wrapLocation) {
		locationRepository.setHomeLocation(wrapLocation);
	}

	public LiveData<List<FavoriteLocation>> getLocations() {
		Log.w(this.getClass().getName(), "GET LOCATIONS");
		return locations;
	}

	public void clickLocation(WrapLocation location, FavLocationType type) {
		Log.w(this.getClass().getName(), "CLICK LOCATION");
		locationRepository.addFavoriteLocation(location, type);
	}

}
