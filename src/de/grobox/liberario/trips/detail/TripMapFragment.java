package de.grobox.liberario.trips.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.fragments.TransportrFragment;

@ParametersAreNonnullByDefault
public class TripMapFragment extends TransportrFragment implements OnMapReadyCallback {

	public static final String TAG = TripMapFragment.class.getSimpleName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private TripDetailViewModel viewModel;
	private MapboxMap map;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_trip_map, container, false);
		getComponent().inject(this);

		MapView mapView = v.findViewById(R.id.map);
		mapView.onCreate(savedInstanceState);
		mapView.getMapAsync(this);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(TripDetailViewModel.class);

		return v;
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		map = mapboxMap;
		TripDrawer tripDrawer = new TripDrawer(getContext());
		tripDrawer.draw(map, viewModel.getTrip());

		viewModel.getZoomLocation().observe(this, this::zoom);
		viewModel.getZoomLeg().observe(this, this::zoom);
	}

	private void zoom(@Nullable LatLng latLng) {
		if (latLng == null) return;
		map.easeCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
	}

	private void zoom(@Nullable LatLngBounds bounds) {
		if (bounds == null) return;
		map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, 32));
	}

}
