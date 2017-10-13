package de.grobox.liberario.trips;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.networks.TransportNetwork;

@ParametersAreNonnullByDefault
public class TripDetailFragment extends TransportrFragment {

	public static final String TAG = TripDetailFragment.class.getSimpleName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	private TripDetailViewModel viewModel;
	private RecyclerView list;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		list = (RecyclerView) inflater.inflate(R.layout.fragment_trip_detail, container, false);
		getComponent().inject(this);

		viewModel = ViewModelProviders.of(getActivity(), viewModelFactory).get(TripDetailViewModel.class);

		TransportNetwork network = viewModel.getTransportNetwork().getValue();
		boolean showLineName = network != null && network.hasGoodLineNames();

		LegAdapter adapter = new LegAdapter(viewModel.getTrip().legs, viewModel, showLineName);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(getContext()));

		return list;
	}

}
