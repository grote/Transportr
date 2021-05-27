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

package de.grobox.transportr.trips.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import de.grobox.transportr.R;
import de.grobox.transportr.TransportrActivity;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState;
import de.grobox.transportr.ui.ThreeStateBottomSheetBehavior;
import de.grobox.transportr.utils.OnboardingBuilder;
import de.schildbach.pte.dto.Trip;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.BOTTOM;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.EXPANDED;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.MIDDLE;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_DISMISSED;
import static uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt.STATE_FOCAL_PRESSED;

@ParametersAreNonnullByDefault
public class TripDetailActivity extends TransportrActivity {

	public static final String TRIP = "de.schildbach.pte.dto.Trip";
	public static final String FROM = "de.schildbach.pte.dto.Trip.from";
	public static final String VIA = "de.schildbach.pte.dto.Trip.via";
	public static final String TO = "de.schildbach.pte.dto.Trip.to";

	@Inject ViewModelProvider.Factory viewModelFactory;

	private ThreeStateBottomSheetBehavior bottomSheetBehavior;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getComponent().inject(this);

		TripDetailViewModel viewModel = new ViewModelProvider(this, viewModelFactory).get(TripDetailViewModel.class);
		Intent intent = getIntent();
		Trip trip = (Trip) intent.getSerializableExtra(TRIP);
		WrapLocation from = (WrapLocation) intent.getSerializableExtra(FROM);
		WrapLocation via = (WrapLocation) intent.getSerializableExtra(VIA);
		WrapLocation to = (WrapLocation) intent.getSerializableExtra(TO);
		viewModel.setTrip(trip);
		viewModel.setFrom(from);
		viewModel.setVia(via);
		viewModel.setTo(to);

		if (viewModel.showWhenLocked()) {
			getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);
		}

		setContentView(R.layout.activity_trip_detail);
		setUpCustomToolbar(true);

		FrameLayout bottomContainer = findViewById(R.id.bottomContainer);
		bottomSheetBehavior = ThreeStateBottomSheetBehavior.from(bottomContainer);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == STATE_COLLAPSED) {
					if (bottomSheetBehavior.isMiddle()) {
						viewModel.getSheetState().setValue(MIDDLE);
					} else if (bottomSheetBehavior.isBottom()) {
						viewModel.getSheetState().setValue(BOTTOM);
					}
				} else if (newState == STATE_EXPANDED) {
					viewModel.getSheetState().setValue(EXPANDED);
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
			}
		});
		viewModel.getSheetState().observe(this, this::onSheetStateChanged);

		if (savedInstanceState == null) {
			viewModel.getSheetState().setValue(MIDDLE);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.topContainer, new TripMapFragment(), TripMapFragment.TAG)
					.add(R.id.bottomContainer, new TripDetailFragment(), TripDetailFragment.Companion.getTAG())
					.commit();

			showOnboarding();
		}
	}

	private void showOnboarding() {
		if (getSettingsManager().showTripDetailFragmentOnboarding()) {
			new OnboardingBuilder(this)
					.setTarget(R.id.bottomContainer)
					.setPrimaryText(R.string.onboarding_location_title)
					.setSecondaryText(R.string.onboarding_location_message)
					.setPromptStateChangeListener((prompt, state) -> {
						if (state == STATE_DISMISSED || state == STATE_FOCAL_PRESSED) {
							getSettingsManager().tripDetailOnboardingShown();
							bottomSheetBehavior.setState(STATE_EXPANDED);
						}
					})
					.show();
		}
	}

	private void onSheetStateChanged(@Nullable SheetState sheetState) {
		if (sheetState == null) return;
		switch (sheetState) {
			case BOTTOM:
				bottomSheetBehavior.setBottom();
				bottomSheetBehavior.setState(STATE_COLLAPSED);
				break;
			case MIDDLE:
				bottomSheetBehavior.setHideable(true);  // ensures it can be swiped down
				bottomSheetBehavior.setMiddle();
				bottomSheetBehavior.setState(STATE_COLLAPSED);
				break;
		}
	}

}
