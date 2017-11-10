/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.map;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMap.OnMarkerClickListener;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.NearbyLocationsLoader;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetwork;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static de.grobox.transportr.utils.Constants.LOADER_NEARBY_STATIONS;
import static de.schildbach.pte.dto.LocationType.STATION;
import static de.schildbach.pte.dto.NearbyLocationsResult.Status.OK;

@ParametersAreNonnullByDefault
public class MapFragment extends GpsMapFragment implements LoaderCallbacks<NearbyLocationsResult>, OnMarkerClickListener {

	@Inject ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;

	private @Nullable Marker selectedLocationMarker;
	private NearbyStationsDrawer nearbyStationsDrawer;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		getComponent().inject(this);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(MapViewModel.class);
		viewModel.getTransportNetwork().observe(this, this::onTransportNetworkChanged);
		gpsController = viewModel.getGpsController();

		nearbyStationsDrawer = new NearbyStationsDrawer(getContext());

		if (savedInstanceState == null && viewModel.getTransportNetwork().getValue() != null) {
			requestPermission();
		}

		return v;
	}

	@Override
	@LayoutRes
	protected int getLayout() {
		return R.layout.fragment_map;
	}

	@Override
	public void onMapReady(MapboxMap mapboxMap) {
		super.onMapReady(mapboxMap);

		Location location = new Location(STATION, "fake");
		Bundle args = NearbyLocationsLoader.getBundle(location, 0);
		getActivity().getSupportLoaderManager().initLoader(LOADER_NEARBY_STATIONS, args, this);

		map.setOnMapClickListener(point -> viewModel.getMapClicked().call());
		map.setOnMapLongClickListener(point -> viewModel.selectLocation(new WrapLocation(point)));
		map.setOnMarkerClickListener(this);

		// observe map related data
		viewModel.isFreshStart().observe(this, this::onFreshStart);
		viewModel.getSelectedLocation().observe(this, this::onLocationSelected);
		viewModel.getSelectedLocationClicked().observe(this, this::onSelectedLocationClicked);
		viewModel.getFindNearbyStations().observe(this, this::findNearbyStations);
	}

	private void onFreshStart(@Nullable Boolean isFreshStart) {
		if (isFreshStart != null && !isFreshStart) return;
		// zoom to favorite locations or only current location, if no favorites exist
		viewModel.getLiveBounds().observe(this, bounds -> {
			if (bounds != null) {
				zoomToBounds(bounds);
				viewModel.getLiveBounds().removeObservers(this);
			} else if (map.getMyLocation() != null) {
				zoomToMyLocation();
			}
		});
		viewModel.isFreshStart().setValue(false);
	}

	@Override
	public boolean onMarkerClick(@NonNull Marker marker) {
		if (marker.equals(selectedLocationMarker)) {
			viewModel.getMarkerClicked().call();
			return true;
		}
		WrapLocation wrapLocation = nearbyStationsDrawer.getClickedNearbyStation(marker);
		if (wrapLocation != null) {
			viewModel.selectLocation(wrapLocation);
			return true;
		}
		return false;
	}

	private void onTransportNetworkChanged(@Nullable TransportNetwork network) {
		requestPermission();
		if (network != null && map != null) {
			// stop observing fresh start, so we get only updated when activity was recreated
			viewModel.isFreshStart().removeObservers(this);
			viewModel.isFreshStart().setValue(true);
			// prevent loader from re-adding nearby stations
			getActivity().getSupportLoaderManager().destroyLoader(LOADER_NEARBY_STATIONS);
		}
	}

	private void onLocationSelected(@Nullable WrapLocation location) {
		if (location == null) return;
		LatLng latLng = location.getLatLng();
		addMarker(latLng);
		animateTo(latLng, LOCATION_ZOOM);
		viewModel.clearSelectedLocation();
	}

	private void onSelectedLocationClicked(@Nullable LatLng latLng) {
		if (latLng == null) return;
		animateTo(latLng, LOCATION_ZOOM);
	}

	private void addMarker(LatLng latLng) {
		if (selectedLocationMarker != null) map.removeMarker(selectedLocationMarker);
		selectedLocationMarker = map.addMarker(new MarkerOptions().position(latLng));
	}

	private void zoomToMyLocation() {
		if (map.getMyLocation() == null) return;
		LatLng coords = new LatLng(map.getMyLocation().getLatitude(), map.getMyLocation().getLongitude());
		CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coords, LOCATION_ZOOM);
		map.moveCamera(update);
	}

	private void findNearbyStations(WrapLocation location) {
		Bundle args = NearbyLocationsLoader.getBundle(location.getLocation(), 1000);
		getActivity().getSupportLoaderManager().restartLoader(LOADER_NEARBY_STATIONS, args, this).forceLoad();
	}

	/* Nearby Stations Loader */

	@Override
	public Loader<NearbyLocationsResult> onCreateLoader(int id, Bundle args) {
		return new NearbyLocationsLoader(getContext(), viewModel.getTransportNetwork().getValue(), args);
	}

	@Override
	public void onLoadFinished(Loader<NearbyLocationsResult> loader, @Nullable NearbyLocationsResult result) {
		if (result != null && result.status == OK && result.locations != null && result.locations.size() > 0) {
			nearbyStationsDrawer.draw(map, result.locations);
		} else {
			Toast.makeText(getContext(), R.string.error_find_nearby_stations, Toast.LENGTH_SHORT).show();
		}
		viewModel.setNearbyStationsFound(true);
	}

	@Override
	public void onLoaderReset(Loader<NearbyLocationsResult> loader) {
		nearbyStationsDrawer.reset();
	}

}
