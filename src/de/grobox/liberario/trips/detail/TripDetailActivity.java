package de.grobox.liberario.trips.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.schildbach.pte.dto.Trip;

@ParametersAreNonnullByDefault
public class TripDetailActivity extends TransportrActivity {

	public static final String TRIP = "de.schildbach.pte.dto.Trip";

	@Inject	ViewModelProvider.Factory viewModelFactory;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getComponent().inject(this);

		TripDetailViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(TripDetailViewModel.class);
		Trip trip = (Trip) getIntent().getSerializableExtra(TRIP);
		viewModel.setTrip(trip);

		setContentView(R.layout.activity_trip_detail);
		setUpCustomToolbar(true);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.topContainer, new TripMapFragment(), TripMapFragment.TAG)
					.add(R.id.bottomContainer, new TripDetailFragment(), TripDetailFragment.TAG)
					.commit();
		}

		AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
		AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
		behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
			@Override
			public boolean canDrag(AppBarLayout appBarLayout) {
				return false;
			}
		});
		params.setBehavior(behavior);
	}

}
