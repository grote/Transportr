package de.grobox.transportr.data;

import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.LocationDao;
import de.grobox.transportr.data.searches.SearchesDao;

@Module
public class TestDbModule {

	@Provides
	@Singleton
	Db provideDb(TransportrApplication application) {
		return Room.inMemoryDatabaseBuilder(application.getApplicationContext(), Db.class).build();
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
