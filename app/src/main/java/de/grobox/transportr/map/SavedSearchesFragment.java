package de.grobox.transportr.map;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;

import de.grobox.transportr.favorites.locations.HomePickerDialogFragment;
import de.grobox.transportr.favorites.locations.WorkPickerDialogFragment;
import de.grobox.transportr.favorites.trips.FavoriteTripItem;
import de.grobox.transportr.favorites.trips.FavoriteTripsFragment;
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel;
import de.grobox.transportr.locations.LocationsViewModel;
import de.grobox.transportr.locations.WrapLocation;

import static de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM;


public class SavedSearchesFragment extends FavoriteTripsFragment {

	private static final Class<MapViewModel> viewModelClass = MapViewModel.class;

	@Override
	protected SavedSearchesViewModel getViewModel() {
		return ViewModelProviders.of(getActivity(), viewModelFactory).get(viewModelClass);
	}

	@Override
	protected boolean hasTopMargin() {
		return true;
	}

	@Override
	protected WrapLocation getCurrentFrom(@NonNull FavoriteTripItem item) {
		WrapLocation location = ((MapViewModel) viewModel).getGpsController().getWrapLocation();
		if (location != null) {
			return viewModel.addFavoriteIfNotExists(location, FROM); // save before this gets used in a search
		}
		return super.getCurrentFrom(item);
	}

	@Override
	protected HomePickerDialogFragment getHomePickerDialogFragment() {
		return new HomePickerFragment();
	}

	@Override
	protected WorkPickerDialogFragment getWorkPickerDialogFragment() {
		return new WorkPickerFragment();
	}

	public static class HomePickerFragment extends HomePickerDialogFragment {
		@Override
		protected LocationsViewModel getViewModel() {
			return ViewModelProviders.of(getActivity(), viewModelFactory).get(viewModelClass);
		}
	}

	public static class WorkPickerFragment extends WorkPickerDialogFragment {
		@Override
		protected LocationsViewModel getViewModel() {
			return ViewModelProviders.of(getActivity(), viewModelFactory).get(viewModelClass);
		}
	}

}
