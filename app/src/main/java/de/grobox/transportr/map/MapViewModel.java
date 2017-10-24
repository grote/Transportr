package de.grobox.transportr.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.geometry.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.searches.SearchesRepository;
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.utils.SingleLiveEvent;

@Singleton
public class MapViewModel extends SavedSearchesViewModel {

	private final GpsController gpsController;

	private final MutableLiveData<Integer> peekHeight = new MutableLiveData<>();
	private final MutableLiveData<LatLng> selectedLocationClicked = new MutableLiveData<>();
	private final MutableLiveData<WrapLocation> selectedLocation = new MutableLiveData<>();
	private final SingleLiveEvent<WrapLocation> findNearbyStations = new SingleLiveEvent<>();
	private final SingleLiveEvent<Boolean> nearbyStationsFound = new SingleLiveEvent<>();

	final SingleLiveEvent<Void> mapClicked = new SingleLiveEvent<>();
	final SingleLiveEvent<Void> markerClicked = new SingleLiveEvent<>();

	@Inject
	MapViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager, LocationRepository locationRepository,
	             SearchesRepository searchesRepository) {
		super(application, transportNetworkManager, locationRepository, searchesRepository);
		gpsController = new GpsController(application.getApplicationContext());
	}

	GpsController getGpsController() {
		return gpsController;
	}

	LiveData<Integer> getPeekHeight() {
		return peekHeight;
	}

	public void setPeekHeight(int peekHeight) {
		this.peekHeight.setValue(peekHeight);
	}

	LiveData<LatLng> getSelectedLocationClicked() {
		return selectedLocationClicked;
	}

	public void selectedLocationClicked(LatLng latLng) {
		selectedLocationClicked.setValue(latLng);
	}

	void selectLocation(@Nullable WrapLocation location) {
		selectedLocation.setValue(location);
	}

	LiveData<WrapLocation> getSelectedLocation() {
		return selectedLocation;
	}

	public void findNearbyStations(WrapLocation location) {
		findNearbyStations.setValue(location);
	}

	LiveData<WrapLocation> getFindNearbyStations() {
		return findNearbyStations;
	}

	void setNearbyStationsFound(boolean found) {
		nearbyStationsFound.setValue(found);
	}

	public LiveData<Boolean> nearbyStationsFound() {
		return nearbyStationsFound;
	}

}
