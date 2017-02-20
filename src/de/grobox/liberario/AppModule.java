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

import dagger.Module;
import dagger.Provides;
import de.grobox.liberario.favorites.locations.FavoriteLocationManager;
import de.grobox.liberario.favorites.trips.FavoriteTripManager;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.settings.SettingsManager;

@Module
class AppModule {

	private final TransportrApplication application;

	AppModule(TransportrApplication application) {
		this.application = application;
	}

	@Provides
	@Singleton
	SettingsManager provideSettingsManager() {
		return new SettingsManager(application.getApplicationContext());
	}

	@Provides
	@Singleton
	TransportNetworkManager provideTransportNetworkManager(SettingsManager settingsManager, FavoriteLocationManager favoriteLocationManager, FavoriteTripManager favoriteTripManager) {
		TransportNetworkManager manager = new TransportNetworkManager(settingsManager);
		manager.addOnTransportNetworkChangedListener(favoriteLocationManager);
		manager.addOnTransportNetworkChangedListener(favoriteTripManager);
		return manager;
	}

	@Provides
	@Singleton
	FavoriteLocationManager provideFavoriteLocationManager(FavoriteTripManager favoriteTripManager) {
		return new FavoriteLocationManager(application.getApplicationContext(), favoriteTripManager);
	}

	@Provides
	@Singleton
	FavoriteTripManager provideFavoriteTripManager() {
		return new FavoriteTripManager(application.getApplicationContext());
	}

}
