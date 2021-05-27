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

package de.grobox.transportr.data.searches

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.annotation.WorkerThread
import de.grobox.transportr.AbstractManager
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.LocationDao
import de.grobox.transportr.favorites.trips.FavoriteTripItem
import de.grobox.transportr.networks.TransportNetworkManager
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.LocationType.COORD
import java.util.*
import javax.inject.Inject

class SearchesRepository @Inject constructor(
        private val searchesDao: SearchesDao,
        private val locationDao: LocationDao,
        transportNetworkManager: TransportNetworkManager) : AbstractManager() {

    private val networkId: LiveData<NetworkId> = transportNetworkManager.networkId
    private val favoriteTripItems: MediatorLiveData<List<FavoriteTripItem>> = MediatorLiveData()

    val favoriteTrips: LiveData<List<FavoriteTripItem>>
        get() = favoriteTripItems

    init {
        val storedSearches = Transformations.switchMap<NetworkId, List<StoredSearch>>(networkId, searchesDao::getStoredSearches)
        this.favoriteTripItems.addSource(storedSearches, this::fetchFavoriteTrips)
    }

    private fun fetchFavoriteTrips(storedSearches: List<StoredSearch>?) {
        if (storedSearches == null) return
        runOnBackgroundThread {
            val favoriteTrips = ArrayList<FavoriteTripItem>()
            for (storedSearch in storedSearches) {
                val from = locationDao.getFavoriteLocation(storedSearch.fromId)
                val via = storedSearch.viaId?.let { locationDao.getFavoriteLocation(it) }
                val to = locationDao.getFavoriteLocation(storedSearch.toId)
                if (from == null || to == null) throw RuntimeException("Start or Destination was null")
                val item = FavoriteTripItem(storedSearch, from, via, to)
                favoriteTrips.add(item)
            }
            favoriteTripItems.postValue(favoriteTrips)
        }
    }

    @WorkerThread
    fun storeSearch(from: FavoriteLocation?, via: FavoriteLocation?, to: FavoriteLocation?): Long {
        if (from == null || to == null) return 0L
        if (from.type == COORD || via != null && via.type == COORD || to.type == COORD) throw IllegalStateException("COORD made it through")
        if (from.uid == 0L || to.uid == 0L) throw IllegalStateException("From or To wasn't saved properly :(")

        // try to find existing stored search
        var storedSearch = searchesDao.getStoredSearch(networkId.value!!, from.uid, via?.uid, to.uid)
        if (storedSearch == null) {
            // no search was found, so create a new one
            storedSearch = StoredSearch(networkId.value!!, from, via, to)
        }
        return searchesDao.storeSearch(storedSearch)
    }

    @WorkerThread
    fun isFavorite(uid: Long): Boolean {
        return searchesDao.isFavorite(uid)
    }

    fun updateFavoriteState(item: FavoriteTripItem) {
        updateFavoriteState(item.uid, item.isFavorite)
    }

    fun updateFavoriteState(uid: Long, isFavorite: Boolean) {
        if (uid == 0L) throw IllegalArgumentException()
        runOnBackgroundThread { searchesDao.setFavorite(uid, isFavorite) }
    }

    fun removeSearch(item: FavoriteTripItem) {
        if (item.getUid() == 0L) throw IllegalArgumentException()
        runOnBackgroundThread { searchesDao.delete(item) }
    }

}
