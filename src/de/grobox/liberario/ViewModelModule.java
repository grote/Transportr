package de.grobox.liberario;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import de.grobox.liberario.map.MapViewModel;
import de.grobox.liberario.trips.detail.TripDetailViewModel;
import de.grobox.liberario.trips.search.DirectionsViewModel;

@Module
abstract class ViewModelModule {

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