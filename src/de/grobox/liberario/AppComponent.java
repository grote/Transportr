/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.liberario;

import javax.inject.Singleton;

import dagger.Component;
import de.grobox.liberario.activities.NewMapActivity;
import de.grobox.liberario.departures.DeparturesActivity;
import de.grobox.liberario.favorites.trips.FavoriteTripsFragment;
import de.grobox.liberario.favorites.locations.HomePickerDialogFragment;
import de.grobox.liberario.favorites.locations.WorkPickerDialogFragment;
import de.grobox.liberario.locations.LocationFragment;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.networks.PickTransportNetworkActivity;
import de.grobox.liberario.settings.SettingsFragment;
import de.grobox.liberario.trips.detail.TripDetailActivity;
import de.grobox.liberario.trips.detail.TripDetailFragment;
import de.grobox.liberario.trips.detail.TripMapFragment;
import de.grobox.liberario.trips.search.DirectionsActivity;
import de.grobox.liberario.trips.search.DirectionsFragment;
import de.grobox.liberario.trips.search.TripsFragment;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {

	void inject(NewMapActivity activity);
	void inject(PickTransportNetworkActivity activity);
	void inject(DeparturesActivity activity);
	void inject(DirectionsActivity activity);
	void inject(TripDetailActivity activity);

	void inject(LocationFragment fragment);
	void inject(FavoriteTripsFragment fragment);
	void inject(DirectionsFragment fragment);
	void inject(TripsFragment fragment);
	void inject(TripMapFragment fragment);
	void inject(TripDetailFragment fragment);
	void inject(SettingsFragment fragment);
	void inject(HomePickerDialogFragment fragment);
	void inject(WorkPickerDialogFragment fragment);

	void inject(LocationView view);

}
