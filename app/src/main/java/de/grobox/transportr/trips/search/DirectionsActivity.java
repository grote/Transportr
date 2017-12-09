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

package de.grobox.transportr.trips.search;

import java.util.List;

import android.arch.lifecycle.LiveData;
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
import de.grobox.transportr.favorites.trips.FavoriteTripItem;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.trips.search.SavedSearchesFragment.HomePickerFragment;
import de.grobox.transportr.trips.search.SavedSearchesFragment.WorkPickerFragment;

import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static de.grobox.transportr.locations.WrapLocation.WrapType.GPS;
import static de.grobox.transportr.utils.Constants.FROM;
import static de.grobox.transportr.utils.Constants.FAV_TRIP_UID;
import static de.grobox.transportr.utils.Constants.SEARCH;
import static de.grobox.transportr.utils.Constants.TO;
import static de.grobox.transportr.utils.Constants.VIA;

@ParametersAreNonnullByDefault
public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener {

	public final static String INTENT_URI_HOME = "transportr://home";
	public final static String INTENT_URI_WORK = "transportr://work";
	public final static String INTENT_URI_FAVORITE = "transportr://favorite";

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
				case INTENT_URI_FAVORITE:
					long uid = intent.getLongExtra(FAV_TRIP_UID, 0);

					LiveData<List<FavoriteTripItem>> tripsLiveData;
					tripsLiveData = viewModel.getFavoriteTrips();
					tripsLiveData.observe(this, trips -> {
						for (FavoriteTripItem trip : trips) {
							if (trip.getUid() == uid) {
								searchFromTo(trip.getFrom(), trip.getVia(), trip.getTo());
								tripsLiveData.removeObservers(this); // everything is done
								break;
							}
						}
					});
					setIntent(null);
					return; // will be finished in viewmodel's loaded trips
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
			viewModel.findGpsLocation.setValue(null);
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
