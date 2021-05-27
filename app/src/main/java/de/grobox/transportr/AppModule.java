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

import dagger.Module;
import dagger.Provides;
import de.grobox.transportr.data.locations.LocationDao;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.searches.SearchesDao;
import de.grobox.transportr.data.searches.SearchesRepository;
import de.grobox.transportr.map.GpsController;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.settings.SettingsManager;

@Module(includes = ViewModelModule.class)
class AppModule {

	private final TransportrApplication application;

	AppModule(TransportrApplication application) {
		this.application = application;
	}

	@Provides
	TransportrApplication provideTransportrApplication() {
		return application;
	}

	@Provides
	@Singleton
	SettingsManager provideSettingsManager() {
		return new SettingsManager(application.getApplicationContext());
	}

	@Provides
	@Singleton
	TransportNetworkManager provideTransportNetworkManager(SettingsManager settingsManager) {
		return new TransportNetworkManager(settingsManager);
	}

	@Provides
	@Singleton
	LocationRepository locationRepository(LocationDao locationDao, TransportNetworkManager transportNetworkManager) {
		return new LocationRepository(locationDao, transportNetworkManager);
	}

	@Provides
	@Singleton
	SearchesRepository searchesRepository(SearchesDao searchesDao, LocationDao locationDao, TransportNetworkManager transportNetworkManager) {
		return new SearchesRepository(searchesDao, locationDao, transportNetworkManager);
	}

	@Provides
	GpsController gpsController() {
		return new GpsController(application.getApplicationContext());
	}

}
