package de.grobox.liberario.trips.search;

import android.arch.lifecycle.ViewModelProviders;

import de.grobox.liberario.favorites.locations.HomePickerDialogFragment;
import de.grobox.liberario.favorites.locations.WorkPickerDialogFragment;
import de.grobox.liberario.favorites.trips.FavoriteTripsFragment;
import de.grobox.liberario.favorites.trips.SavedSearchesViewModel;
import de.grobox.liberario.locations.LocationsViewModel;


public class SavedSearchesFragment extends FavoriteTripsFragment {

	private static final Class<DirectionsViewModel> viewModelClass = DirectionsViewModel.class;

	@Override
	protected SavedSearchesViewModel getViewModel() {
		return ViewModelProviders.of(getActivity(), viewModelFactory).get(viewModelClass);
	}

	@Override
	protected boolean hasTopMargin() {
		return false;
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
