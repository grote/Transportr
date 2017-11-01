package de.grobox.transportr.trips.detail;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.fragments.TransportrFragment;
import de.grobox.transportr.networks.TransportNetwork;
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState;
import de.schildbach.pte.dto.Trip;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.BOTTOM;
import static de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.MIDDLE;
import static de.grobox.transportr.utils.DateUtils.getDuration;
import static de.grobox.transportr.utils.DateUtils.getTime;
import static de.grobox.transportr.utils.TransportrUtils.intoCalendar;
import static de.grobox.transportr.utils.TransportrUtils.share;

@ParametersAreNonnullByDefault
public class TripDetailFragment extends TransportrFragment implements Toolbar.OnMenuItemClickListener {

	public static final String TAG = TripDetailFragment.class.getSimpleName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private TripDetailViewModel viewModel;
	private Toolbar toolbar;
	private RecyclerView list;
	private View bottomBar;
	private TextView fromTime, from, toTime, to, duration;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_trip_detail, container, false);
		setHasOptionsMenu(true);
		getComponent().inject(this);

		toolbar = v.findViewById(R.id.toolbar);
		list = v.findViewById(R.id.list);
		bottomBar = v.findViewById(R.id.bottomBar);
		fromTime = bottomBar.findViewById(R.id.fromTime);
		from = bottomBar.findViewById(R.id.from);
		toTime = bottomBar.findViewById(R.id.toTime);
		to = bottomBar.findViewById(R.id.to);
		duration = bottomBar.findViewById(R.id.duration);

		toolbar.setNavigationOnClickListener(view -> onToolbarClose());
		toolbar.setOnMenuItemClickListener(this);
		list.setLayoutManager(new LinearLayoutManager(getContext()));
		bottomBar.setOnClickListener(view -> onBottomBarClick());

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(TripDetailViewModel.class);
		viewModel.getTrip().observe(this, this::onTripChanged);
		viewModel.sheetState.observe(this, this::onSheetStateChanged);

		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Menu toolbarMenu = toolbar.getMenu();
		inflater.inflate(R.menu.trip_details, toolbarMenu);
		viewModel.tripReloadError.observe(this, this::onTripReloadError);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_reload:
				item.setActionView(R.layout.actionbar_progress_actionview);
				viewModel.reloadTrip();
				return true;
			case R.id.action_share:
				share(getContext(), viewModel.getTrip().getValue());
				return true;
			case R.id.action_calendar:
				intoCalendar(getContext(), viewModel.getTrip().getValue());
				return true;
			default:
				return false;
		}
	}

	private void onTripChanged(@Nullable Trip trip) {
		if (trip == null) return;

		MenuItem reloadMenuItem = toolbar.getMenu().findItem(R.id.action_reload);
		if (reloadMenuItem != null) reloadMenuItem.setActionView(null);

		TransportNetwork network = viewModel.getTransportNetwork().getValue();
		boolean showLineName = network != null && network.hasGoodLineNames();
		LegAdapter adapter = new LegAdapter(trip.legs, viewModel, showLineName);
		list.setAdapter(adapter);

		fromTime.setText(getTime(getContext(), trip.getFirstDepartureTime()));
		from.setText(trip.from.uniqueShortName());
		toTime.setText(getTime(getContext(), trip.getLastArrivalTime()));
		to.setText(trip.to.uniqueShortName());
		duration.setText(getDuration(trip.getDuration()));
	}

	private void onToolbarClose() {
		viewModel.sheetState.setValue(BOTTOM);
	}

	private void onBottomBarClick() {
		viewModel.sheetState.setValue(MIDDLE);
	}

	private void onSheetStateChanged(@Nullable SheetState sheetState) {
		if (sheetState == null) return;

		if (sheetState == BOTTOM) {
			toolbar.setVisibility(GONE);
			bottomBar.setVisibility(VISIBLE);
		} else if (sheetState == MIDDLE) {
			toolbar.setVisibility(GONE);
			bottomBar.setVisibility(GONE);
		} else {
			toolbar.setVisibility(VISIBLE);
			bottomBar.setVisibility(GONE);
		}
	}

	private void onTripReloadError(@Nullable String error) {
		toolbar.getMenu().findItem(R.id.action_reload).setActionView(null);
		Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
	}

}
