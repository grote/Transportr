package de.grobox.transportr.map;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;
import de.grobox.transportr.fragments.TransportrFragment;

@ParametersAreNonnullByDefault
abstract public class BaseMapFragment extends TransportrFragment implements OnMapReadyCallback {

	protected MapView mapView;
	protected MapboxMap map;
	protected boolean isFreshStart;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(getLayout(), container, false);
		mapView = v.findViewById(R.id.map);

		isFreshStart = savedInstanceState == null;

		return v;
	}

	protected abstract @LayoutRes int getLayout();

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mapView.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		mapView.onStart();
		mapView.getMapAsync(this);
	}

	@Override
	@CallSuper
	public void onMapReady(MapboxMap mapboxMap) {
		map = mapboxMap;
	}

	@Override
	public void onResume() {
		super.onResume();
		mapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mapView.onPause();
		isFreshStart = false;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		mapView.onSaveInstanceState(outState);
	}

	@Override
	public void onStop() {
		super.onStop();
		mapView.onStop();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mapView.onDestroy();
	}

}
