/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetBehavior.BottomSheetCallback;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.BuildConfig;
import de.grobox.transportr.R;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.locations.LocationFragment;
import de.grobox.transportr.locations.LocationView;
import de.grobox.transportr.locations.LocationView.LocationViewListener;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.PickTransportNetworkActivity;
import de.grobox.transportr.networks.TransportNetwork;

import static android.support.design.widget.BottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.transportr.locations.WrapLocation.WrapType.NORMAL;
import static de.grobox.transportr.networks.PickTransportNetworkActivity.FORCE_NETWORK_SELECTION;
import static de.grobox.transportr.utils.TransportrUtils.findDirections;

@ParametersAreNonnullByDefault
public class MapActivity extends DrawerActivity implements LocationViewListener {

	@Inject	ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;
	private GpsController gpsController;
	private LocationView search;
	private BottomSheetBehavior bottomSheetBehavior;

	private @Nullable LocationFragment locationFragment;
	private boolean transportNetworkInitialized = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (BuildConfig.DEBUG) enableStrictMode();
		getComponent().inject(this);
		ensureTransportNetworkSelected();
		setContentView(R.layout.activity_map);
		setupDrawer(savedInstanceState);

		View menu = findViewById(R.id.menu);
		menu.setOnClickListener(view -> openDrawer());

		search = findViewById(R.id.search);
		search.setLocationViewListener(this);

		View bottomSheet = findViewById(R.id.bottomSheet);
		bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == STATE_HIDDEN) {
					search.clearLocation();
					search.reset();
					bottomSheetBehavior.setPeekHeight(0);
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
			}
		});

		// get view model and observe data
		viewModel = ViewModelProviders.of(this, viewModelFactory).get(MapViewModel.class);
		gpsController = viewModel.getGpsController();
		viewModel.getTransportNetwork().observe(this, this::onTransportNetworkChanged);
		viewModel.getHome().observe(this, homeLocation -> search.setHomeLocation(homeLocation));
		viewModel.getWork().observe(this, workLocation -> search.setWorkLocation(workLocation));
		viewModel.getLocations().observe(this, favoriteLocations -> search.setFavoriteLocations(favoriteLocations));
		viewModel.mapClicked.observe(this, no -> onMapClicked());
		viewModel.markerClicked.observe(this, no -> onMarkerClicked());
		viewModel.getSelectedLocation().observe(this, this::onLocationSelected);
		viewModel.getPeekHeight().observe(this, height -> {
			if (height != null) bottomSheetBehavior.setPeekHeight(height);
		});

		FloatingActionButton directionsFab = findViewById(R.id.directionsFab);
		directionsFab.setOnClickListener(view -> {
			WrapLocation from = gpsController.getWrapLocation();
			WrapLocation to = null;
			if (locationFragment != null && locationFragmentVisible()) {
				to = locationFragment.getLocation();
			}
			if (from != null && from.getWrapType() == NORMAL) {
				from = viewModel.addFavoriteIfNotExists(from, FROM);
			}
			findDirections(MapActivity.this, 0, from, null, to, null, true);
		});

		if (savedInstanceState == null) {
			SavedSearchesFragment f = new SavedSearchesFragment();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, f, SavedSearchesFragment.TAG)
					.commitNow(); // otherwise takes some time and empty bottomSheet will not be shown
			bottomSheetBehavior.setPeekHeight(PEEK_HEIGHT_AUTO);
			bottomSheetBehavior.setState(STATE_COLLAPSED);
		} else {
			locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag(LocationFragment.TAG);
		}
	}

	private void onTransportNetworkChanged(TransportNetwork network) {
		if (transportNetworkInitialized) {
			search.setLocation(null);
			closeDrawer();
			recreate();
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.bottomSheet, new SavedSearchesFragment(), SavedSearchesFragment.TAG)
					.commit();
		} else {
			search.setTransportNetwork(network);
			transportNetworkInitialized = true;
		}
	}

	@Override
	public void onLocationItemClick(final WrapLocation loc, FavLocationType type) {
		viewModel.selectLocation(loc);
	}

	@Override
	public void onLocationCleared(FavLocationType type) {
		bottomSheetBehavior.setState(STATE_HIDDEN);
	}

	private void onLocationSelected(WrapLocation loc) {
		viewModel.addFavoriteIfNotExists(loc, FROM);

		locationFragment = LocationFragment.newInstance(loc);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, locationFragment, LocationFragment.TAG)
				.commit(); // takes some time and empty bottomSheet will not be shown
		bottomSheetBehavior.setState(STATE_COLLAPSED);
	}

	private void onMapClicked() {
		search.clearFocus();  // also hides soft keyboard
	}

	private void onMarkerClicked() {
		if (locationFragment != null) search.setLocation(locationFragment.getLocation());
		bottomSheetBehavior.setState(STATE_COLLAPSED);
	}

	private boolean locationFragmentVisible() {
		return locationFragment != null && locationFragment.isVisible() && bottomSheetBehavior.getState() != STATE_HIDDEN;
	}

	private void ensureTransportNetworkSelected() {
		TransportNetwork network = manager.getTransportNetwork().getValue();
		if (network == null) {
			Intent intent = new Intent(this, PickTransportNetworkActivity.class);
			intent.putExtra(FORCE_NETWORK_SELECTION, true);
			startActivity(intent);
		}
	}

	private void enableStrictMode() {
		ThreadPolicy.Builder threadPolicy = new ThreadPolicy.Builder();
		threadPolicy.detectAll();
		threadPolicy.penaltyLog();
		StrictMode.setThreadPolicy(threadPolicy.build());

		VmPolicy.Builder vmPolicy = new VmPolicy.Builder();
		vmPolicy.detectAll();
		vmPolicy.penaltyLog();
		StrictMode.setVmPolicy(vmPolicy.build());
	}

}
