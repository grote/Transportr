package de.grobox.liberario.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;


@Database(version = 1, entities = {FavoriteLocation.class})
@TypeConverters(Converters.class)
abstract class Db extends RoomDatabase {

	abstract public LocationDao locationDao();

}
