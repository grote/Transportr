package de.grobox.transportr.trips.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetwork;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.networks.TransportNetworkViewModel;
import de.grobox.transportr.settings.SettingsManager;
import de.grobox.transportr.trips.TripQuery;
import de.grobox.transportr.utils.SingleLiveEvent;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;

import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.MIDDLE;

public class TripDetailViewModel extends TransportNetworkViewModel implements LegClickListener {

	enum SheetState {BOTTOM, MIDDLE, EXPANDED}

	private final SettingsManager settingsManager;

	private final MutableLiveData<Trip> trip = new MutableLiveData<>();
	private final MutableLiveData<LatLngBounds> zoomLeg = new MutableLiveData<>();
	private final MutableLiveData<LatLng> zoomLocation = new MutableLiveData<>();

	final SingleLiveEvent<String> tripReloadError = new SingleLiveEvent<>();
	final MutableLiveData<SheetState> sheetState = new MutableLiveData<>();

	@Nullable WrapLocation from, via, to;

	@Inject
	TripDetailViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager, SettingsManager settingsManager) {
		super(application, transportNetworkManager);
		this.settingsManager = settingsManager;
	}

	boolean showWhenLocked() {
		return settingsManager.showWhenLocked();
	}

	public LiveData<Trip> getTrip() {
		return trip;
	}

	public void setTrip(Trip trip) {
		this.trip.setValue(trip);
	}

	@Override
	public void onLegClick(Leg leg) {
		if (leg.path == null || leg.path.size() == 0) return;

		List<LatLng> latLngs = new ArrayList<>(leg.path.size());
		for (Point point : leg.path) {
			latLngs.add(new LatLng(point.getLatAsDouble(), point.getLonAsDouble()));
		}
		LatLngBounds bounds = new LatLngBounds.Builder().includes(latLngs).build();
		zoomLeg.setValue(bounds);
		sheetState.setValue(MIDDLE);
	}

	@Override
	public void onLocationClick(Location location) {
		if (!location.hasLocation()) return;
		LatLng latLng = new LatLng(location.getLatAsDouble(), location.getLonAsDouble());
		zoomLocation.setValue(latLng);
		sheetState.setValue(MIDDLE);
	}

	LiveData<LatLng> getZoomLocation() {
		return zoomLocation;
	}

	LiveData<LatLngBounds> getZoomLeg() {
		return zoomLeg;
	}

	void reloadTrip() {
		TransportNetwork network = getTransportNetwork().getValue();
		if (network == null) throw new IllegalStateException();

		Trip oldTrip = trip.getValue();
		if (oldTrip == null) throw new IllegalStateException();

		if (from == null || to == null) throw new IllegalStateException();

		String errorString = getApplication().getString(R.string.error_trip_refresh_failed);
		TripQuery query = new TripQuery(0, from, via, to, oldTrip.getFirstDepartureTime(), true, oldTrip.products());
		new TripReloader(network.getNetworkProvider(), settingsManager, query, trip, errorString, tripReloadError)
				.reload();
	}

}
