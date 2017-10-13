package de.grobox.liberario;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.grobox.liberario.favorites.trips.SavedSearchesViewModel;
import de.grobox.liberario.locations.LocationsViewModel;
import de.grobox.liberario.trips.TripDetailViewModel;
import de.grobox.liberario.trips.search.DirectionsViewModel;

@Module
abstract class ViewModelModule {

	@Binds
	@IntoMap
	@ViewModelKey(LocationsViewModel.class)
	abstract ViewModel bindLocationsViewModel(LocationsViewModel locationsViewModel);

	@Binds
	@IntoMap
	@ViewModelKey(SavedSearchesViewModel.class)
	abstract ViewModel bindSavedSearchesViewModel(SavedSearchesViewModel savedSearchesViewModel);

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