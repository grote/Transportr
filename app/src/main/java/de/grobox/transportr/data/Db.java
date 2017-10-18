package de.grobox.transportr.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

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
