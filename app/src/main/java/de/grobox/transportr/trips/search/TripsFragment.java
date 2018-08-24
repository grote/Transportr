/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrFragment;
import de.grobox.transportr.trips.detail.TripDetailActivity;
import de.grobox.transportr.trips.search.TripAdapter.OnTripClickListener;
import de.grobox.transportr.trips.search.TripsRepository.QueryMoreState;
import de.grobox.transportr.ui.LceAnimator;
import de.schildbach.pte.dto.Trip;

import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTH;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTTOM;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.TOP;
import static de.grobox.transportr.trips.detail.TripDetailActivity.FROM;
import static de.grobox.transportr.trips.detail.TripDetailActivity.TO;
import static de.grobox.transportr.trips.detail.TripDetailActivity.TRIP;
import static de.grobox.transportr.trips.detail.TripDetailActivity.VIA;
import static de.grobox.transportr.utils.TransportrUtils.getDragDistance;

@ParametersAreNonnullByDefault
public class TripsFragment extends TransportrFragment implements OnRefreshListener, OnTripClickListener {

	final static String TAG = TripsFragment.class.getName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private DirectionsViewModel viewModel;
	private ProgressBar progressBar;
	private View errorLayout;
	private TextView errorText;
	private SwipyRefreshLayout swipe;
	private RecyclerView list;
	private TripAdapter adapter;

	private boolean topSwipingEnabled = false;
	private SwipyRefreshLayoutDirection queryMoreDirection = BOTH;
	private final CountDownTimer listUpdateTimer = new CountDownTimer(Long.MAX_VALUE, 1000 * 30) {
		@Override
		public void onTick(long millisUntilFinished) {
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onFinish() {
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_trips, container, false);
		getComponent().inject(this);

		// Progress Bar and Error View
		progressBar = v.findViewById(R.id.progressBar);
		errorLayout = v.findViewById(R.id.errorLayout);
		errorText = errorLayout.findViewById(R.id.errorText);
		errorLayout.findViewById(R.id.errorButton).setOnClickListener(view -> viewModel.search());

		// Swipe to Refresh
		swipe = v.findViewById(R.id.swipe);
		swipe.setColorSchemeResources(R.color.accent);
		swipe.setDistanceToTriggerSync(getDragDistance(getContext()));
		swipe.setOnRefreshListener(this);

		list = v.findViewById(R.id.list);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		list.setLayoutManager(layoutManager);
		list.setHasFixedSize(false);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(DirectionsViewModel.class);
		viewModel.topSwipeEnabled.observe(this, this::onSwipeEnabledChanged);
		viewModel.getQueryMoreState().observe(this, this::updateSwipeState);
		viewModel.getTrips().observe(this, this::onTripsLoaded);
		viewModel.getQueryError().observe(this, this::onError);
		viewModel.getQueryMoreError().observe(this, this::onMoreError);

		adapter = new TripAdapter(this);
		adapter.setHasStableIds(false);
		list.setAdapter(adapter);

		LceAnimator.showLoading(progressBar, list, errorLayout);

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		listUpdateTimer.start();
	}

	@Override
	public void onStop() {
		super.onStop();
		listUpdateTimer.cancel();
	}

	@Override
	public void onRefresh(SwipyRefreshLayoutDirection direction) {
		queryMoreDirection = direction;
		boolean later = queryMoreDirection == BOTTOM;
		viewModel.searchMore(later);
	}

	private void onSwipeEnabledChanged(boolean enabled) {
		if (!swipe.isRefreshing() && enabled != topSwipingEnabled) {
			updateSwipeState(viewModel.getQueryMoreState().getValue());
		}
		topSwipingEnabled = enabled;
	}

	private void updateSwipeState(@Nullable QueryMoreState state) {
		Boolean topEnabled = viewModel.topSwipeEnabled.getValue();
		if (topEnabled == null || state == null) return;

		if (state == QueryMoreState.NONE) {
			swipe.setEnabled(false);
		} else if (!topEnabled && state == QueryMoreState.EARLIER) {
			swipe.setEnabled(false);
		} else {
			swipe.setEnabled(true);
			if (state == QueryMoreState.EARLIER) swipe.setDirection(TOP);
			else if (state == QueryMoreState.LATER) swipe.setDirection(BOTTOM);
			else if (!topEnabled && state == QueryMoreState.BOTH) swipe.setDirection(BOTTOM);
			else swipe.setDirection(BOTH);
		}
	}

	private void onTripsLoaded(@Nullable Set<Trip> trips) {
		if (trips == null) return;

		int oldCount = adapter.getItemCount();
		adapter.addAll(trips);

		if (oldCount > 0) {
			swipe.setRefreshing(false);
			list.smoothScrollBy(0, queryMoreDirection == BOTTOM ? 200 : -200);
		} else {
			LceAnimator.showContent(progressBar, list, errorLayout);
		}
	}

	private void onError(@Nullable String error) {
		if (error == null) return;
		errorText.setText(error + "\n\n" + getString(R.string.trip_error_pte));
		Pattern pteMatcher = Pattern.compile("public-transport-enabler");
		Linkify.addLinks(errorText, pteMatcher, "https://github.com/schildbach/public-transport-enabler/issues");
		LceAnimator.showErrorView(progressBar, list, errorLayout);
	}

	private void onMoreError(@Nullable String error) {
		if (error == null) return;
		swipe.setRefreshing(false);
		Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(Trip trip) {
		Intent i = new Intent(getContext(), TripDetailActivity.class);
		i.putExtra(TRIP, trip);
		// unfortunately, PTE does not save these locations reliably in the Trip object
		i.putExtra(FROM, viewModel.getFromLocation().getValue());
		i.putExtra(VIA, viewModel.getViaLocation().getValue());
		i.putExtra(TO, viewModel.getToLocation().getValue());
		startActivity(i);
	}

}
