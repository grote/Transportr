package de.grobox.transportr.trips.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.map.BaseMapFragment;
import de.schildbach.pte.dto.Trip;

@ParametersAreNonnullByDefault
public class TripMapFragment extends BaseMapFragment implements OnMapReadyCallback {

	public static final String TAG = TripMapFragment.class.getSimpleName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private TripDetailViewModel viewModel;
	private int mapPadding;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		getComponent().inject(this);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(TripDetailViewModel.class);
		mapPadding = getResources().getDimensionPixelSize(R.dimen.mapPadding);

		return v;
	}

	@Override
	@LayoutRes
	protected int getLayout() {
		return R.layout.fragment_trip_map;
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		super.onMapReady(mapboxMap);

		// set padding, so everything gets centered in top half of screen
		DisplayMetrics metrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int topPadding = mapPadding / 2;
		int bottomPadding = (mapView.getHeight() / 4);
		map.setPadding(0, topPadding, 0, bottomPadding);

		viewModel.getTrip().observe(this, this::onTripChanged);
		viewModel.getZoomLocation().observe(this, this::zoom);
		viewModel.getZoomLeg().observe(this, this::zoom);
	}

	private void onTripChanged(@Nullable Trip trip) {
		if (trip == null) return;

		TripDrawer tripDrawer = new TripDrawer(getContext());
		tripDrawer.draw(map, trip);
	}

	private void zoom(@Nullable LatLng latLng) {
		if (latLng == null) return;
		map.easeCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
	}

	private void zoom(@Nullable LatLngBounds bounds) {
		if (bounds == null) return;
		map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, mapPadding));
	}

}
