/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.favorites.trips;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.grobox.transportr.R;
import de.grobox.transportr.TransportrFragment;
import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.data.locations.WorkLocation;
import de.grobox.transportr.favorites.locations.HomePickerDialogFragment;
import de.grobox.transportr.favorites.locations.WorkPickerDialogFragment;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.ui.LceAnimator;

import static androidx.recyclerview.widget.SortedList.INVALID_POSITION;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.HOME;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.TRIP;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.WORK;
import static de.grobox.transportr.utils.IntentUtils.findDirections;

@ParametersAreNonnullByDefault
public abstract class FavoriteTripsFragment<VM extends SavedSearchesViewModel> extends TransportrFragment implements FavoriteTripListener {

	@Inject protected ViewModelProvider.Factory viewModelFactory;
	protected VM viewModel;

	private ProgressBar progressBar;
	private RecyclerView list;
	private FavoriteTripAdapter adapter;
	private boolean listAlreadyUpdated = false;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_favorites, container, false);

		progressBar = v.findViewById(R.id.progressBar);

		list = v.findViewById(R.id.favorites);
		adapter = new FavoriteTripAdapter(this);
		list.setHasFixedSize(false);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(getContext()));

		viewModel = getViewModel();
		viewModel.getHome().observe(getViewLifecycleOwner(), this::onHomeLocationChanged);
		viewModel.getWork().observe(getViewLifecycleOwner(), this::onWorkLocationChanged);
		viewModel.getFavoriteTrips().observe(getViewLifecycleOwner(), this::onFavoriteTripsChanged);

		return v;
	}

	abstract protected VM getViewModel();

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
		HomePickerDialogFragment f = getHomePickerDialogFragment();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), HomePickerDialogFragment.class.getSimpleName());
	}

	protected abstract HomePickerDialogFragment getHomePickerDialogFragment();

	@Override
	public void changeWork() {
		WorkPickerDialogFragment f = getWorkPickerDialogFragment();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), WorkPickerDialogFragment.class.getSimpleName());
	}

	protected abstract WorkPickerDialogFragment getWorkPickerDialogFragment();

	@Override
	public void onFavoriteClicked(FavoriteTripItem item) {
		if (item.getType() == HOME) {
			if (item.getTo() == null) {
				changeHome();
			} else {
				onSpecialLocationClicked(item.getTo());
			}
		} else if (item.getType() == WORK) {
			if (item.getTo() == null) {
				changeWork();
			} else {
				onSpecialLocationClicked(item.getTo());
			}
		} else if (item.getType() == TRIP) {
			if (item.getFrom() == null || item.getTo() == null) throw new IllegalArgumentException();
			findDirections(getContext(), item.getFrom(), item.getVia(), item.getTo(), true, true);
		} else {
			throw new IllegalArgumentException();
		}
	}

	protected abstract void onSpecialLocationClicked(@NonNull WrapLocation location);

	@Override
	public void onFavoriteDeleted(FavoriteTripItem item) {
		adapter.remove(item);
		listAlreadyUpdated = true;
		viewModel.removeFavoriteTrip(item);
	}

}
