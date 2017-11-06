package de.grobox.transportr.data.searches

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.Transformations
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

    fun storeSearch(uid: Long, from: FavoriteLocation?, via: FavoriteLocation?, to: FavoriteLocation?) {
        if (from == null || to == null) return
        if (from.type == COORD || via != null && via.type == COORD || to.type == COORD) throw IllegalStateException("COORD made it through")
        if (from.uid == 0L || to.uid == 0L) throw IllegalStateException("From or To wasn't saved properly :(")

        runOnBackgroundThread {
            if (uid != 0L) {
                searchesDao.updateStoredSearch(uid, Date())
            } else {
                // try to find existing stored search
                var storedSearch = searchesDao.getStoredSearch(networkId.value!!, from.uid, via?.uid, to.uid)
                if (storedSearch == null) {
                    // no search was found, so create a new one
                    storedSearch = StoredSearch(networkId.value!!, from, via, to)
                }
                searchesDao.storeSearch(storedSearch)
            }
        }
    }

    fun updateFavoriteState(item: FavoriteTripItem) {
        if (item.getUid() == 0L) throw IllegalArgumentException()
        runOnBackgroundThread { searchesDao.setFavorite(item.getUid(), item.isFavorite) }
    }

    fun removeSearch(item: FavoriteTripItem) {
        if (item.getUid() == 0L) throw IllegalArgumentException()
        runOnBackgroundThread { searchesDao.delete(item) }
    }

}
