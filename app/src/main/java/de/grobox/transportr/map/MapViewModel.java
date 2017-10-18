package de.grobox.transportr.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.mapbox.mapboxsdk.geometry.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.searches.SearchesRepository;
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.utils.SingleLiveEvent;

@Singleton
public class MapViewModel extends SavedSearchesViewModel {

	private final MutableLiveData<Integer> peekHeight = new MutableLiveData<>();
	private final SingleLiveEvent<LatLng> zoomTo = new SingleLiveEvent<>();
	private final SingleLiveEvent<WrapLocation> findNearbyStations = new SingleLiveEvent<>();

	@Inject
	MapViewModel(TransportNetworkManager transportNetworkManager, LocationRepository locationRepository, SearchesRepository searchesRepository) {
		super(transportNetworkManager, locationRepository, searchesRepository);
	}

	LiveData<Integer> getPeekHeight() {
		return peekHeight;
	}

	public void setPeekHeight(int peekHeight) {
		this.peekHeight.setValue(peekHeight);
	}

	LiveData<LatLng> getZoomTo() {
		return zoomTo;
	}

	public void zoomTo(LatLng latLng) {
		zoomTo.setValue(latLng);
	}

	LiveData<WrapLocation> getFindNearbyStations() {
		return findNearbyStations;
	}

	public void findNearbyStations(WrapLocation location) {
		findNearbyStations.setValue(location);
	}

}
