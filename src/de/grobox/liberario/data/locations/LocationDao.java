package de.grobox.liberario.data.locations;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import java.util.List;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.LocationType;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface LocationDao {

	// FavoriteLocation

	@Query("SELECT * FROM locations WHERE networkId = :networkId")
	List<FavoriteLocation> getFavoriteLocations(NetworkId networkId);

	@Insert(onConflict = REPLACE)
	long addFavoriteLocation(FavoriteLocation location);

	@Nullable
	@Query("SELECT * FROM locations WHERE uid = :uid")
	FavoriteLocation getFavoriteLocation(long uid);

	@Nullable
	@Query("SELECT * FROM locations WHERE networkId = :networkId AND type = :type AND id IS :id AND lat = :lat AND lon = :lon AND place IS :place AND name IS :name")
	FavoriteLocation getFavoriteLocation(NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name);

	// HomeLocation

	@Query("SELECT * FROM home_locations WHERE networkId = :networkId")
	LiveData<HomeLocation> getHomeLocation(NetworkId networkId);

	@Insert(onConflict = REPLACE)
	long addHomeLocation(HomeLocation location);

	/* This is just for tests to ensure, there's only ever one home location per network */
	@Query("SELECT COUNT(uid) FROM home_locations WHERE networkId = :networkId")
	int countHomes(NetworkId networkId);

	// WorkLocation

	@Query("SELECT * FROM work_locations WHERE networkId = :networkId")
	LiveData<WorkLocation> getWorkLocation(NetworkId networkId);

	@Insert(onConflict = REPLACE)
	long addWorkLocation(WorkLocation location);

	/* This is just for tests to ensure, there's only ever one home location per network */
	@Query("SELECT COUNT(uid) FROM work_locations WHERE networkId = :networkId")
	int countWorks(NetworkId networkId);

}
