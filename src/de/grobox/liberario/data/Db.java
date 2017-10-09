package de.grobox.liberario.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.LocationDao;
import de.grobox.liberario.data.locations.WorkLocation;
import de.grobox.liberario.data.searches.SearchesDao;
import de.grobox.liberario.data.searches.StoredSearch;


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
