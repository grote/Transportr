package de.grobox.liberario.trips.search;

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
import android.widget.Toast;

import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.Set;

import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.trips.TripDetailActivity;
import de.grobox.liberario.trips.search.TripAdapter.OnTripClickListener;
import de.grobox.liberario.ui.LceAnimator;
import de.schildbach.pte.dto.Trip;

import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout.LARGE;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTH;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTTOM;
import static de.grobox.liberario.trips.TripDetailActivity.TRIP;
import static de.grobox.liberario.utils.TransportrUtils.getDragDistance;

public class TripsFragment extends TransportrFragment implements OnRefreshListener, OnTripClickListener {

	final static String TAG = TripsFragment.class.getName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private DirectionsViewModel viewModel;
	private ProgressBar progressBar;
	private SwipyRefreshLayout swipe;
	private RecyclerView list;
	private TripAdapter adapter;

	private SwipyRefreshLayoutDirection queryMoreDirection = BOTH;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_trips, container, false);
		getComponent().inject(this);

		// Progress Bar
		progressBar = v.findViewById(R.id.progressBar);

		// Swipe to Refresh
		swipe = v.findViewById(R.id.swipe);
		swipe.setColorSchemeResources(R.color.accent);
		swipe.setProgressBackgroundColor(R.color.cardview_dark_background);
		swipe.setSize(LARGE);
		swipe.setDistanceToTriggerSync(getDragDistance(getContext()));
		swipe.setOnRefreshListener(this);

		list = v.findViewById(R.id.list);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		list.setLayoutManager(layoutManager);
		list.setHasFixedSize(false);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(DirectionsViewModel.class);
		viewModel.getTrips().observe(this, this::onTripsLoaded);
		viewModel.getQueryError().observe(this, this::onError);
		viewModel.getQueryMoreError().observe(this, this::onMoreError);

		adapter = new TripAdapter(this);
		adapter.setHasStableIds(false);
		list.setAdapter(adapter);

		LceAnimator.showLoading(progressBar, list, null);

		return v;
	}

	public void setSwipeEnabled(boolean enabled) {
		if (swipe != null) {
			if (swipe.isRefreshing()) return;
			if (enabled && swipe.getDirection() == BOTH) return;
			if (!enabled && swipe.getDirection() == BOTTOM) return;
			swipe.setDirection(enabled ? BOTH : BOTTOM);
		}
	}

	@Override
	public void onRefresh(SwipyRefreshLayoutDirection direction) {
		queryMoreDirection = direction;
		boolean later = queryMoreDirection == BOTTOM;
		viewModel.searchMore(later);
	}

	private void onTripsLoaded(@Nullable Set<Trip> trips) {
		if (trips == null) return;

		int oldCount = adapter.getItemCount();
		adapter.addAll(trips);

		if (oldCount > 0) {
			swipe.setRefreshing(false);
			list.smoothScrollBy(0, queryMoreDirection == BOTTOM ? 200 : -200);
		} else {
			LceAnimator.showContent(progressBar, list, null);
		}
	}

	private void onError(@Nullable String error) {
		if (error == null) return;
		Log.e(TAG, "RECEIVED NEW QUERY ERROR: " + error);
		Toast.makeText(getContext(), "Query Error: " + error, Toast.LENGTH_LONG).show();
		LceAnimator.showContent(progressBar, list, null);
//		LceAnimator.showErrorView(progressBar, list, errorView);
	}

	private void onMoreError(@Nullable String error) {
		if (error == null) return;
		Log.e(TAG, "RECEIVED NEW MORE ERROR: " + error);
		swipe.setRefreshing(false);
		Toast.makeText(getContext(), "More Error: " + error, Toast.LENGTH_LONG).show();
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
