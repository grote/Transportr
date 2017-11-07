package de.grobox.transportr.trips.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.view.MenuItem;
import android.widget.FrameLayout;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrActivity;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.trips.search.SavedSearchesFragment.HomePickerFragment;
import de.grobox.transportr.trips.search.SavedSearchesFragment.WorkPickerFragment;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static de.grobox.transportr.locations.WrapLocation.WrapType.GPS;
import static de.grobox.transportr.utils.Constants.FROM;
import static de.grobox.transportr.utils.Constants.SEARCH;
import static de.grobox.transportr.utils.Constants.TO;
import static de.grobox.transportr.utils.Constants.VIA;

@ParametersAreNonnullByDefault
public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener {

	public final static String INTENT_URI_HOME = "transportr://home";
	public final static String INTENT_URI_WORK = "transportr://work";

	@Inject ViewModelProvider.Factory viewModelFactory;

	private DirectionsViewModel viewModel;
	private FrameLayout fragmentContainer;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getComponent().inject(this);
		setContentView(R.layout.activity_directions);

		// get view model and observe data
		viewModel = ViewModelProviders.of(this, viewModelFactory).get(DirectionsViewModel.class);
		viewModel.showTrips.observe(this, v -> showTrips());

		if (viewModel.showWhenLocked()) {
			//noinspection deprecation
			getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);
		}

		AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
		appBarLayout.addOnOffsetChangedListener(this);

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
		if (verticalOffset == 0) {
			viewModel.topSwipeEnabled.setValue(true);
		} else {
			Boolean enabled = viewModel.topSwipeEnabled.getValue();
			if (enabled != null && enabled) viewModel.topSwipeEnabled.setValue(false);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		processIntent();
	}

	private void showFavorites() {
		viewModel.isFavTrip().setValue(null);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentContainer, new SavedSearchesFragment(), SavedSearchesFragment.TAG)
				.commit();
	}

	private void showTrips() {
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, new TripsFragment(), TripsFragment.TAG)
				.commit();
		fragmentContainer.requestFocus();
	}

	private boolean isShowingTrips() {
		return fragmentIsVisible(TripsFragment.TAG);
	}

	private void processIntent() {
		Intent intent = getIntent();
		if (intent == null) return;

		WrapLocation from, via, to;
		boolean search;
		String data = intent.getDataString();
		if (data != null) {
			from = new WrapLocation(GPS);
			search = true;
			switch (data) {
				case INTENT_URI_HOME:
					to = viewModel.getHome().getValue();
					if (to == null) new HomePickerFragment().show(getSupportFragmentManager(), HomePickerFragment.TAG);
					break;
				case INTENT_URI_WORK:
					to = viewModel.getWork().getValue();
					if (to == null) new WorkPickerFragment().show(getSupportFragmentManager(), WorkPickerFragment.TAG);
					break;
				default:
					throw new IllegalArgumentException();
			}
		} else {
			from = (WrapLocation) intent.getSerializableExtra(FROM);
			to = (WrapLocation) intent.getSerializableExtra(TO);
			search = intent.getBooleanExtra(SEARCH, false);
		}
		via = (WrapLocation) intent.getSerializableExtra(VIA);

		if (search) searchFromTo(from, via, to);
		else presetFromTo(from, via, to);

		// remove the intent (and clear its action) since it was already processed
		// and should not be processed again
		setIntent(null);
	}

	private void presetFromTo(@Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to) {
		if (from == null || from.getWrapType() == GPS) {
			viewModel.setFromLocation(null);
			viewModel.findGpsLocation.setValue(FavLocationType.FROM);
		} else {
			viewModel.setFromLocation(from);
		}
		viewModel.setViaLocation(via);
		viewModel.setToLocation(to);
	}

	private void searchFromTo(WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to) {
		presetFromTo(from, via, to);
		viewModel.search();
	}

}
