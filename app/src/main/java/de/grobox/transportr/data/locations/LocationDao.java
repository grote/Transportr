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

package de.grobox.transportr.data.locations;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.LocationType;

import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface LocationDao {

	// FavoriteLocation

	@Query("SELECT * FROM locations WHERE networkId = :networkId")
	LiveData<List<FavoriteLocation>> getFavoriteLocations(NetworkId networkId);

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
