package de.grobox.liberario.favorites.trips;

import android.animation.ObjectAnimator;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.WorkLocation;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.ui.LceAnimator;

import static android.support.v7.util.SortedList.INVALID_POSITION;
import static de.grobox.liberario.utils.TransportrUtils.findDirections;

@ParametersAreNonnullByDefault
public class FavoriteTripsFragment extends TransportrFragment implements FavoriteTripListener {

	public static final String TAG = FavoriteTripsFragment.class.getName();
	private static final String TOP_MARGIN = "topMargin";

	@Inject	ViewModelProvider.Factory viewModelFactory;
	private SavedSearchesViewModel viewModel;

	private ProgressBar progressBar;
	private RecyclerView list;
	private FavoriteTripAdapter adapter;
	private boolean listAlreadyUpdated = false;

	public static FavoriteTripsFragment newInstance(boolean topMargin) {
		FavoriteTripsFragment f = new FavoriteTripsFragment();
		Bundle args = new Bundle();
		args.putBoolean(TOP_MARGIN, topMargin);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_favorites, container, false);
		getComponent().inject(this);

		progressBar = v.findViewById(R.id.progressBar);

		list = v.findViewById(R.id.favorites);
		adapter = new FavoriteTripAdapter(this);
		list.setHasFixedSize(false);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(getContext()));

		viewModel = ViewModelProviders.of(this, viewModelFactory).get(SavedSearchesViewModel.class);
		viewModel.getHome().observe(this, this::onHomeLocationChanged);
		viewModel.getWork().observe(this, this::onWorkLocationChanged);
		viewModel.getFavoriteTrips().observe(this, this::onFavoriteTripsChanged);

		if (!getArguments().getBoolean(TOP_MARGIN)) {
			FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) list.getLayoutParams();
			params.topMargin = 0;
			list.setLayoutParams(params);
		}
		return v;
	}

	private void onHomeLocationChanged(@Nullable HomeLocation home) {
		FavoriteTripItem oldHome = adapter.getHome();
		FavoriteTripItem newHome = new FavoriteTripItem(home);
		if (oldHome == null) {
			adapter.add(newHome);
		} else {
			onSpecialLocationChanged(oldHome, newHome);
		}
	}

	private void onWorkLocationChanged(@Nullable WorkLocation work) {
		FavoriteTripItem oldWork = adapter.getWork();
		FavoriteTripItem newWork = new FavoriteTripItem(work);
		if (oldWork == null) {
			adapter.add(newWork);
		} else {
			onSpecialLocationChanged(oldWork, newWork);
		}
	}

	private void onSpecialLocationChanged(FavoriteTripItem oldItem, FavoriteTripItem newItem) {
		int position = adapter.findItemPosition(oldItem);
		if (position == INVALID_POSITION) return;

		// animate the new location in from right to left
		RecyclerView.ViewHolder viewHolder = list.findViewHolderForAdapterPosition(position);
		if (viewHolder != null) {  // is null when activity gets recreated
			View view = viewHolder.itemView;
			ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth(), 0).start();
		}
		adapter.updateItem(position, newItem);
	}

	private void onFavoriteTripsChanged(List<FavoriteTripItem> trips) {
		// duplicate detection does not work, so we manage list updates ourselves, no need to reload
		if (listAlreadyUpdated) {
			// be ready for the next update
			listAlreadyUpdated = false;
			return;
		}
		LceAnimator.showContent(progressBar, list, null);
		adapter.swap(trips);
	}

	@Override
	public void onFavoriteChanged(FavoriteTripItem item, boolean isFavorite) {
		item.setFavorite(isFavorite);
		int position = adapter.findItemPosition(item);
		if (position != INVALID_POSITION) {
			adapter.updateItem(position, item);
		}
		listAlreadyUpdated = true;
		viewModel.updateFavoriteState(item);
	}

	@Override
	public void changeHome() {
		HomePickerDialogFragment f = HomePickerDialogFragment.newInstance();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), HomePickerDialogFragment.TAG);
	}

	@Override
	public void changeWork() {
		WorkPickerDialogFragment f = WorkPickerDialogFragment.newInstance();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), WorkPickerDialogFragment.TAG);
	}

	@Override
	public void onFavoriteClicked(FavoriteTripItem item) {
		if (item.getType() == FavoriteTripType.HOME) {
			if (item.getTo() == null) {
				changeHome();
			} else {
				findDirections(getContext(), item.getUid(), item.getFrom(), item.getVia(), item.getTo());
			}
		} else if (item.getType() == FavoriteTripType.WORK) {
			if (item.getTo() == null) {
				changeWork();
			} else {
				findDirections(getContext(), item.getUid(), item.getFrom(), item.getVia(), item.getTo());
			}
		} else if (item.getType() == FavoriteTripType.TRIP) {
			if (item.getFrom() == null || item.getTo() == null) throw new IllegalArgumentException();
			findDirections(getContext(), item.getUid(), item.getFrom(), item.getVia(), item.getTo());
		} else {
			throw new IllegalArgumentException();
		}
	}

}
