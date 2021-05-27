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

package de.grobox.transportr;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.grobox.transportr.map.MapViewModel;
import de.grobox.transportr.trips.detail.TripDetailViewModel;
import de.grobox.transportr.trips.search.DirectionsViewModel;

@Module
public abstract class ViewModelModule {

	@Binds
	@IntoMap
	@ViewModelKey(MapViewModel.class)
	abstract ViewModel bindMapViewModel(MapViewModel mapViewModel);

	@Binds
	@IntoMap
	@ViewModelKey(DirectionsViewModel.class)
	abstract ViewModel bindDirectionsViewModel(DirectionsViewModel directionsViewModel);

	@Binds
	@IntoMap
	@ViewModelKey(TripDetailViewModel.class)
	abstract ViewModel bindTripDetailViewModel(TripDetailViewModel tripDetailViewModel);

	@Binds
	abstract ViewModelProvider.Factory bindViewModelFactory(ViewModelFactory factory);

}
