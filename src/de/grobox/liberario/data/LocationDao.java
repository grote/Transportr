package de.grobox.liberario.data;

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

	@Query("SELECT * FROM locations WHERE networkId = :networkId")
	List<FavoriteLocation> getFavoriteLocations(NetworkId networkId);

	@Insert(onConflict = REPLACE)
	void addFavoriteLocation(FavoriteLocation location);

	@Query("SELECT COUNT(uid) FROM locations WHERE networkId = :networkId AND type = :type AND id IS :id AND lat = :lat AND lon = :lon AND place IS :place AND name IS :name")
	int exists(NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name);

}
