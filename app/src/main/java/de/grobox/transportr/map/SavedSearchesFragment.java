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

package de.grobox.transportr.map;

import android.arch.lifecycle.ViewModelProviders;

import de.grobox.transportr.favorites.locations.HomePickerDialogFragment;
import de.grobox.transportr.favorites.locations.WorkPickerDialogFragment;
import de.grobox.transportr.favorites.trips.FavoriteTripsFragment;
import de.grobox.transportr.favorites.trips.SavedSearchesViewModel;
import de.grobox.transportr.locations.LocationsViewModel;


public class SavedSearchesFragment extends FavoriteTripsFragment {

	private static final Class<MapViewModel> viewModelClass = MapViewModel.class;

	@Override
	protected SavedSearchesViewModel getViewModel() {
		return ViewModelProviders.of(getActivity(), viewModelFactory).get(viewModelClass);
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
