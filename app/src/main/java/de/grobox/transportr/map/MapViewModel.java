package de.grobox.transportr.map;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.FavoriteLocation;
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
	private final MutableLiveData<LatLngBounds> updatedLiveBounds = new MutableLiveData<>();
	private final MutableLiveData<WrapLocation> selectedLocation = new MutableLiveData<>();
	private final SingleLiveEvent<WrapLocation> findNearbyStations = new SingleLiveEvent<>();
	private final SingleLiveEvent<Boolean> nearbyStationsFound = new SingleLiveEvent<>();

	final SingleLiveEvent<Void> mapClicked = new SingleLiveEvent<>();
	final SingleLiveEvent<Void> markerClicked = new SingleLiveEvent<>();
	final LiveData<LatLngBounds> liveBounds = Transformations.switchMap(getLocations(), this::switchMap);

	@Inject
	MapViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager, LocationRepository locationRepository,
	             SearchesRepository searchesRepository, GpsController gpsController) {
		super(application, transportNetworkManager, locationRepository, searchesRepository);
		this.gpsController = gpsController;
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

	private MutableLiveData<LatLngBounds> switchMap(List<FavoriteLocation> input) {
		if (input == null) {
			updatedLiveBounds.setValue(null);
		} else {
			Set<LatLng> points = new HashSet<>();
			for (FavoriteLocation location : input) {
				if (location.hasLocation()) points.add(location.getLatLng());
			}
			WrapLocation gpsLocation = gpsController.getWrapLocation();
			if (gpsLocation != null && gpsLocation.hasLocation()) points.add(gpsLocation.getLatLng());
			if (points.size() < 2) {
				updatedLiveBounds.setValue(null);
			} else {
				updatedLiveBounds.setValue(new LatLngBounds.Builder().includes(new ArrayList<>(points)).build());
			}
		}
		return updatedLiveBounds;
	}

	void setGeoUri(Uri geoUri) {
		WrapLocation location = getWrapLocation(geoUri.toString());
		if (location != null) {
			selectLocation(location);
		} else {
			Log.w(MapViewModel.class.getSimpleName(), "Invalid geo intent: " + geoUri.toString());
			Toast.makeText(getApplication().getApplicationContext(), R.string.error_geo_intent, Toast.LENGTH_SHORT).show();
		}
	}

	@Nullable
	@VisibleForTesting
	WrapLocation getWrapLocation(String geoUri) {
		Pattern pattern = Pattern.compile("^geo:(-?\\d{1,3}(\\.\\d{1,8})?),(-?\\d{1,3}(\\.\\d{1,8})?).*");
		Matcher matcher = pattern.matcher(geoUri);
		if (matcher.matches()) {
			double lat = Double.valueOf(matcher.group(1));
			double lon = Double.valueOf(matcher.group(3));
			if (lat == 0 && lon == 0) return null;
			return new WrapLocation(new LatLng(lat, lon));
		}
		return null;
	}

}
