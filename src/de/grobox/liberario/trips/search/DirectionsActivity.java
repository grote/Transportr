package de.grobox.liberario.trips.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;

import java.util.Date;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.grobox.liberario.locations.WrapLocation;

import static de.grobox.liberario.fragments.DirectionsFragment.TASK_BRING_ME_HOME;
import static de.grobox.liberario.locations.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.utils.Constants.DATE;
import static de.grobox.liberario.utils.Constants.FAV_TRIP_UID;
import static de.grobox.liberario.utils.Constants.FROM;
import static de.grobox.liberario.utils.Constants.SEARCH;
import static de.grobox.liberario.utils.Constants.TO;
import static de.grobox.liberario.utils.Constants.VIA;

@ParametersAreNonnullByDefault
public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener {

	private final static String TAG = DirectionsActivity.class.getName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	@Nullable
	private TripsFragment tripsFragment;
	private DirectionsViewModel viewModel;

	private FrameLayout fragmentContainer;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getComponent().inject(this);
		setContentView(R.layout.activity_directions);

		AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
		appBarLayout.addOnOffsetChangedListener(this);

		// get view model and observe data
		viewModel = ViewModelProviders.of(this, viewModelFactory).get(DirectionsViewModel.class);
		viewModel.showTrips().observe(this, v -> showTrips());

		fragmentContainer = findViewById(R.id.fragmentContainer);

		if (savedInstanceState == null) {
			showFavorites();
			processIntent();
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home && isShowingTrips()) {
			showFavorites();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
		if (tripsFragment != null) {
			tripsFragment.setSwipeEnabled(verticalOffset == 0);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		Log.e(TAG, "ON NEW INTENT");

		processIntent();
	}

	private void showFavorites() {
		SavedSearchesFragment f = new SavedSearchesFragment();
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentContainer, f, SavedSearchesFragment.TAG)
				.commit();
	}

	private void showTrips() {
		tripsFragment = new TripsFragment();
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, tripsFragment, TripsFragment.TAG)
				.commit();
		fragmentContainer.requestFocus();
	}

	boolean isShowingTrips() {
		return fragmentIsVisible(TripsFragment.TAG);
	}

	private void processIntent() {
		Intent intent = getIntent();
		if (intent == null) return;

		Log.e(TAG, "PROCESSING NEW INTENT");

		WrapLocation from, via, to;
		boolean search;
		Date date;
		long uid = intent.getLongExtra(FAV_TRIP_UID, 0);
		String special = (String) intent.getSerializableExtra("special");
		if (special != null && special.equals(TASK_BRING_ME_HOME)) {
			from = new WrapLocation(GPS);
			to = viewModel.getHome().getValue();
			search = true;
		} else {
			from = (WrapLocation) intent.getSerializableExtra(FROM);
			to = (WrapLocation) intent.getSerializableExtra(TO);
			search = intent.getBooleanExtra(SEARCH, false);
		}
		via = (WrapLocation) intent.getSerializableExtra(VIA);
		date = (Date) intent.getSerializableExtra(DATE);

		if (search) searchFromTo(uid, from, via, to, date);
		else presetFromTo(uid, from, via, to, date);

		// remove the intent (and clear its action) since it was already processed
		// and should not be processed again
		Log.e(TAG, "SETTIG INTENT NULL");
		setIntent(null);
	}

	private void presetFromTo(long uid, @Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to, @Nullable Date date) {
		viewModel.setFavTripUid(uid);
		if (from != null && from.getWrapType() == GPS) {
			// TODO
//			activateGPS();
			viewModel.setFromLocation(null);
		} else {
			viewModel.setFromLocation(from);
		}
		viewModel.setViaLocation(via);
		viewModel.setToLocation(to);

		if (date != null) {
			viewModel.setDate(date);
		}
	}

	private void searchFromTo(long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to, Date date) {
		presetFromTo(uid, from, via, to, date);
		viewModel.search();
	}

}
