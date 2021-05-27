/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.mapboxsdk.geometry.LatLng;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import de.grobox.transportr.BuildConfig;
import de.grobox.transportr.R;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.locations.LocationFragment;
import de.grobox.transportr.locations.LocationView;
import de.grobox.transportr.locations.LocationView.LocationViewListener;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetwork;
import de.grobox.transportr.ui.TransportrChangeLog;
import de.grobox.transportr.utils.OnboardingBuilder;

import static android.content.Intent.ACTION_VIEW;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.PEEK_HEIGHT_AUTO;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;
import static de.grobox.transportr.trips.search.DirectionsActivity.ACTION_SEARCH;
import static de.grobox.transportr.utils.Constants.WRAP_LOCATION;
import static de.grobox.transportr.utils.IntentUtils.findDirections;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED;

@ParametersAreNonnullByDefault
public class MapActivity extends DrawerActivity implements LocationViewListener {

	@Inject ViewModelProvider.Factory viewModelFactory;

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
					viewModel.setPeekHeight(0);
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
			}
		});

		// get view model and observe data
		viewModel = new ViewModelProvider(this, viewModelFactory).get(MapViewModel.class);
		gpsController = viewModel.getGpsController();
		viewModel.getTransportNetwork().observe(this, this::onTransportNetworkChanged);
		viewModel.getHome().observe(this, homeLocation -> search.setHomeLocation(homeLocation));
		viewModel.getWork().observe(this, workLocation -> search.setWorkLocation(workLocation));
		viewModel.getLocations().observe(this, favoriteLocations -> search.setFavoriteLocations(favoriteLocations));
		viewModel.getMapClicked().observe(this, no -> onMapClicked());
		viewModel.getMarkerClicked().observe(this, no -> onMarkerClicked());
		viewModel.getSelectedLocation().observe(this, this::onLocationSelected);
		viewModel.getSelectedLocationClicked().observe(this, this::onSelectedLocationClicked);
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
			findDirections(MapActivity.this, from, null, to);
		});

		Intent intent = getIntent();
		if (intent != null) onNewIntent(intent);

		if (savedInstanceState == null) {
			showSavedSearches();
			checkAndShowChangelog();
		} else {
			locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag(LocationFragment.TAG);
		}
	}

	private void showSavedSearches() {
		SavedSearchesFragment f = new SavedSearchesFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, f, SavedSearchesFragment.class.getSimpleName())
				.commitNow(); // otherwise takes some time and empty bottomSheet will not be shown
		bottomSheetBehavior.setState(STATE_COLLAPSED);
		viewModel.setPeekHeight(PEEK_HEIGHT_AUTO);
	}

	private void onTransportNetworkChanged(TransportNetwork network) {
		if (transportNetworkInitialized) {
			viewModel.selectLocation(null);
			search.setLocation(null);
			closeDrawer();
			showSavedSearches();
			recreate();
		} else {
			// it didn't really change, this is just the first notification from LiveData Observer
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
		viewModel.selectLocation(null);
		search.postDelayed(() -> { // show dropdown again after it got hidden by hiding the bottom sheet
			search.onClick();
		}, 500);
	}

	private void onLocationSelected(@Nullable WrapLocation loc) {
		if (loc == null) return;

		locationFragment = LocationFragment.newInstance(loc);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.bottomSheet, locationFragment, LocationFragment.TAG)
				.commit(); // takes some time and empty bottomSheet will not be shown
		bottomSheetBehavior.setState(STATE_COLLAPSED);

		// show on-boarding dialog
		if (getSettingsManager().showLocationFragmentOnboarding()) {
			new OnboardingBuilder(this)
					.setTarget(R.id.bottomSheet)
					.setPrimaryText(R.string.onboarding_location_title)
					.setSecondaryText(R.string.onboarding_location_message)
					.setPromptStateChangeListener((prompt, state) -> {
						if (state == STATE_DISMISSED || state == STATE_FOCAL_PRESSED) {
							getSettingsManager().locationFragmentOnboardingShown();
							viewModel.selectedLocationClicked(loc.getLatLng());
						}
					})
					.show();
		}
	}

	private void onSelectedLocationClicked(@Nullable LatLng latLng) {
		if (latLng == null) return;
		bottomSheetBehavior.setState(STATE_EXPANDED);
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

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (intent == null || intent.getAction() == null) return;

		if (intent.getAction().equals(ACTION_VIEW) && intent.getData() != null) {
			viewModel.setGeoUri(intent.getData());
		} else if (intent.getAction().equals(ACTION_SEARCH)) {
			WrapLocation location = (WrapLocation) intent.getSerializableExtra(WRAP_LOCATION);
			viewModel.selectLocation(location);
			viewModel.findNearbyStations(location);
		}
	}

	private void checkAndShowChangelog() {
		TransportrChangeLog cl = new TransportrChangeLog(this, getSettingsManager());
		if (cl.isFirstRun() && !cl.isFirstRunEver()) {
			cl.getLogDialog().show();
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
