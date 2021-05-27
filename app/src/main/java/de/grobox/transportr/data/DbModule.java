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

package de.grobox.transportr.data;

import androidx.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.LocationDao;
import de.grobox.transportr.data.searches.SearchesDao;

@Module
public class DbModule {

	@Provides
	@Singleton
	Db provideDb(TransportrApplication application) {
		return Room.databaseBuilder(application.getApplicationContext(), Db.class, Db.DATABASE_NAME).build();
	}

	@Provides
	@Singleton
	LocationDao locationDao(Db db) {
		return db.locationDao();
	}

	@Provides
	@Singleton
	SearchesDao searchesDao(Db db) {
		return db.searchesDao();
	}

}
