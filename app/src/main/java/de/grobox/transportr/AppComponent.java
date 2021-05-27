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

import javax.inject.Singleton;

import dagger.Component;
import de.grobox.transportr.data.DbModule;
import de.grobox.transportr.favorites.locations.HomePickerDialogFragment;
import de.grobox.transportr.favorites.locations.WorkPickerDialogFragment;
import de.grobox.transportr.locations.LocationFragment;
import de.grobox.transportr.map.MapActivity;
import de.grobox.transportr.map.MapFragment;
import de.grobox.transportr.settings.SettingsFragment;
import de.grobox.transportr.trips.detail.TripDetailActivity;
import de.grobox.transportr.trips.detail.TripDetailFragment;
import de.grobox.transportr.trips.detail.TripMapFragment;
import de.grobox.transportr.trips.search.DirectionsActivity;
import de.grobox.transportr.trips.search.DirectionsFragment;
import de.grobox.transportr.trips.search.TripsFragment;

@Singleton
@Component(modules = { AppModule.class, DbModule.class })
public interface AppComponent {

	void inject(TransportrActivity activity);
	void inject(MapActivity activity);
	void inject(DirectionsActivity activity);
	void inject(TripDetailActivity activity);

	void inject(MapFragment fragment);
	void inject(LocationFragment fragment);
	void inject(de.grobox.transportr.map.SavedSearchesFragment fragment);
	void inject(de.grobox.transportr.trips.search.SavedSearchesFragment fragment);
	void inject(DirectionsFragment fragment);
	void inject(TripsFragment fragment);
	void inject(TripMapFragment fragment);
	void inject(TripDetailFragment fragment);
	void inject(SettingsFragment fragment);
	void inject(HomePickerDialogFragment fragment);
	void inject(WorkPickerDialogFragment fragment);

}
