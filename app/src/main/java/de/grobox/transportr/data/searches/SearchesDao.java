package de.grobox.transportr.data.searches;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

import de.schildbach.pte.NetworkId;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SearchesDao {

	@Query("SELECT * FROM searches WHERE networkId = :networkId")
	LiveData<List<StoredSearch>> getStoredSearches(NetworkId networkId);

	@Nullable
	@Query("SELECT * FROM searches WHERE networkId = :networkId AND from_id = :fromId AND via_id IS :viaId AND to_id = :toId")
	StoredSearch getStoredSearch(NetworkId networkId, long fromId, @Nullable Long viaId, long toId);

	@Insert(onConflict = REPLACE)
	long storeSearch(StoredSearch storedSearch);

	@Query("UPDATE searches SET count = count + 1, lastUsed = :lastUsed WHERE uid = :uid")
	void updateStoredSearch(long uid, Date lastUsed);

	@Query("SELECT favorite FROM searches WHERE uid = :uid")
	boolean isFavorite(long uid);

	@Query("UPDATE searches SET favorite = :favorite WHERE uid = :uid")
	void setFavorite(long uid, boolean favorite);

	@Delete
	void delete(StoredSearch storedSearch);

}
