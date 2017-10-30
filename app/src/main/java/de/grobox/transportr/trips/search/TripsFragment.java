package de.grobox.transportr.trips.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.fragments.TransportrFragment;
import de.grobox.transportr.trips.detail.TripDetailActivity;
import de.grobox.transportr.trips.search.TripAdapter.OnTripClickListener;
import de.grobox.transportr.trips.search.TripsRepository.QueryMoreState;
import de.grobox.transportr.ui.LceAnimator;
import de.schildbach.pte.dto.Trip;

import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTH;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTTOM;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.TOP;
import static de.grobox.transportr.trips.detail.TripDetailActivity.TRIP;
import static de.grobox.transportr.utils.TransportrUtils.getDragDistance;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
		viewModel.getQueryMoreState().observe(this, this::onQueryMoreStateChanged);
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
	public void onRefresh(SwipyRefreshLayoutDirection direction) {
		queryMoreDirection = direction;
		boolean later = queryMoreDirection == BOTTOM;
		viewModel.searchMore(later);
	}

	public void onSwipeEnabledChanged(boolean enabled) {
		if (!swipe.isRefreshing() && enabled != topSwipingEnabled) {
			updateSwipeState();
		}
		topSwipingEnabled = enabled;
	}

	private void onQueryMoreStateChanged(@Nullable QueryMoreState state) {
		updateSwipeState();
	}

	private void updateSwipeState() {
		Boolean topEnabled = viewModel.topSwipeEnabled.getValue();
		QueryMoreState state = viewModel.getQueryMoreState().getValue();
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
		errorText.setText(error);
		LceAnimator.showErrorView(progressBar, list, errorLayout);
	}

	private void onMoreError(@Nullable String error) {
		if (error == null) return;
		swipe.setRefreshing(false);
		Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onClick(Trip trip) {
		Log.e("TEST", trip.toString());

		Intent i = new Intent(getContext(), TripDetailActivity.class);
		i.putExtra(TRIP, trip);
//		i.putExtra("de.schildbach.pte.dto.Trip.from", from);
//		i.putExtra("de.schildbach.pte.dto.Trip.to", to);
//		i.putExtra("de.schildbach.pte.dto.Trip.products", trip.products().toArray());
		startActivity(i);
	}

}
