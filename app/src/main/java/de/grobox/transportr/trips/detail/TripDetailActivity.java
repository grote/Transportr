package de.grobox.transportr.trips.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;
import android.widget.FrameLayout;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.activities.TransportrActivity;
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState;
import de.grobox.transportr.ui.ThreeStateBottomSheetBehavior;
import de.schildbach.pte.dto.Trip;

import static android.support.design.widget.BottomSheetBehavior.STATE_COLLAPSED;
import static android.support.design.widget.BottomSheetBehavior.STATE_EXPANDED;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.BOTTOM;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.EXPANDED;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.MIDDLE;

@ParametersAreNonnullByDefault
public class TripDetailActivity extends TransportrActivity {

	public static final String TRIP = "de.schildbach.pte.dto.Trip";

	@Inject	ViewModelProvider.Factory viewModelFactory;

	private ThreeStateBottomSheetBehavior bottomSheetBehavior;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getComponent().inject(this);

		TripDetailViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(TripDetailViewModel.class);
		Trip trip = (Trip) getIntent().getSerializableExtra(TRIP);
		viewModel.setTrip(trip);

		setContentView(R.layout.activity_trip_detail);
		setUpCustomToolbar(true);

		FrameLayout bottomContainer = findViewById(R.id.bottomContainer);
		bottomSheetBehavior = ThreeStateBottomSheetBehavior.from(bottomContainer);
		bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == STATE_COLLAPSED) {
					if (bottomSheetBehavior.isMiddle()) {
						viewModel.sheetState.setValue(MIDDLE);
					} else if (bottomSheetBehavior.isBottom()) {
						viewModel.sheetState.setValue(BOTTOM);
					}
				} else if (newState == STATE_EXPANDED) {
					viewModel.sheetState.setValue(EXPANDED);
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {
			}
		});
		viewModel.sheetState.observe(this, this::onSheetStateChanged);

		if (savedInstanceState == null) {
			viewModel.sheetState.setValue(MIDDLE);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.topContainer, new TripMapFragment(), TripMapFragment.TAG)
					.add(R.id.bottomContainer, new TripDetailFragment(), TripDetailFragment.TAG)
					.commit();
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
