package de.grobox.liberario.data.searches;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.Date;
import java.util.List;

import de.schildbach.pte.NetworkId;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

@Dao
public interface SearchesDao {

	@Query("SELECT * FROM searches WHERE networkId = :networkId")
	LiveData<List<StoredSearch>> getStoredSearches(NetworkId networkId);

	@Insert(onConflict = REPLACE)
	long storeSearch(StoredSearch storedSearch);

	@Query("UPDATE searches SET count = count + 1, lastUsed = :lastUsed WHERE uid = :uid")
	void updateStoredSearch(long uid, Date lastUsed);

	@Query("UPDATE searches SET favorite = :favorite WHERE uid = :uid")
	void setFavorite(long uid, boolean favorite);

}
