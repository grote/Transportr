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

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import de.grobox.transportr.data.locations.FavoriteLocation;
import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.data.locations.LocationDao;
import de.grobox.transportr.data.locations.WorkLocation;
import de.grobox.transportr.data.searches.SearchesDao;
import de.grobox.transportr.data.searches.StoredSearch;


@Database(
		version = 1,
		entities = {
				FavoriteLocation.class,
				HomeLocation.class,
				WorkLocation.class,
				StoredSearch.class
		}
)
@TypeConverters(Converters.class)
public abstract class Db extends RoomDatabase {

	public static final String DATABASE_NAME = "transportr.db";

	abstract public LocationDao locationDao();

	abstract public SearchesDao searchesDao();

}
