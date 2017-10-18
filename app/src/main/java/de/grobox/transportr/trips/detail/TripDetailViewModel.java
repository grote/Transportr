package de.grobox.transportr.trips.detail;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.networks.TransportNetworkViewModel;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Leg;

public class TripDetailViewModel extends TransportNetworkViewModel implements LegClickListener {

	private Trip trip;
	private MutableLiveData<LatLngBounds> zoomLeg = new MutableLiveData<>();
	private MutableLiveData<LatLng> zoomLocation = new MutableLiveData<>();

	@Inject
	TripDetailViewModel(TransportNetworkManager transportNetworkManager) {
		super(transportNetworkManager);
	}

	public Trip getTrip() {
		return trip;
	}

	public void setTrip(Trip trip) {
		this.trip = trip;
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
	}

	@Override
	public void onLocationClick(Location location) {
		if (!location.hasLocation()) return;
		LatLng latLng = new LatLng(location.getLatAsDouble(), location.getLonAsDouble());
		zoomLocation.setValue(latLng);
	}

	LiveData<LatLng> getZoomLocation() {
		return zoomLocation;
	}

	LiveData<LatLngBounds> getZoomLeg() {
		return zoomLeg;
	}

}
