/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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
