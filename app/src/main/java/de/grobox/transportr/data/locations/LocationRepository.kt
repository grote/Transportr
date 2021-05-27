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

package de.grobox.transportr.data.locations


import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.annotation.WorkerThread
import de.grobox.transportr.AbstractManager
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.locations.WrapLocation.WrapType.NORMAL
import de.grobox.transportr.networks.TransportNetworkManager
import de.schildbach.pte.NetworkId
import de.schildbach.pte.dto.LocationType.ADDRESS
import de.schildbach.pte.dto.LocationType.COORD
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject
constructor(private val locationDao: LocationDao, transportNetworkManager: TransportNetworkManager) : AbstractManager() {

    private val networkId: LiveData<NetworkId> = transportNetworkManager.networkId
    val homeLocation: LiveData<HomeLocation> = Transformations.switchMap(networkId, locationDao::getHomeLocation)
    val workLocation: LiveData<WorkLocation> = Transformations.switchMap(networkId, locationDao::getWorkLocation)
    val favoriteLocations: LiveData<List<FavoriteLocation>> = Transformations.switchMap(networkId, locationDao::getFavoriteLocations)

    fun setHomeLocation(location: WrapLocation) {
        runOnBackgroundThread {
            // add also as favorite location if it doesn't exist already
            val favoriteLocation = getFavoriteLocation(networkId.value, location)
            if (favoriteLocation == null) locationDao.addFavoriteLocation(FavoriteLocation(networkId.value, location))

            locationDao.addHomeLocation(HomeLocation(networkId.value!!, location))
        }
    }

    fun setWorkLocation(location: WrapLocation) {
        runOnBackgroundThread {
            // add also as favorite location if it doesn't exist already
            val favoriteLocation = getFavoriteLocation(networkId.value, location)
            if (favoriteLocation == null) locationDao.addFavoriteLocation(FavoriteLocation(networkId.value, location))

            locationDao.addWorkLocation(WorkLocation(networkId.value!!, location))
        }
    }

    @WorkerThread
    fun addFavoriteLocation(wrapLocation: WrapLocation, type: FavLocationType): FavoriteLocation? {
        if (wrapLocation.type == COORD || wrapLocation.wrapType != NORMAL) return null

        val favoriteLocation = if (wrapLocation is FavoriteLocation) {
            wrapLocation
        } else {
            val location = findExistingLocation(wrapLocation)
            location as? FavoriteLocation ?: FavoriteLocation(networkId.value, wrapLocation)
        }
        favoriteLocation.add(type)

        return if (favoriteLocation.uid != 0L) {
            locationDao.addFavoriteLocation(favoriteLocation)
            favoriteLocation
        } else {
            val existingLocation = getFavoriteLocation(networkId.value, favoriteLocation)
            if (existingLocation != null) {
                existingLocation.add(type)
                locationDao.addFavoriteLocation(existingLocation)
                existingLocation
            } else {
                val uid = locationDao.addFavoriteLocation(favoriteLocation)
                FavoriteLocation(uid, networkId.value, favoriteLocation)
            }
        }
    }

    @WorkerThread
    private fun getFavoriteLocation(networkId: NetworkId?, l: WrapLocation?): FavoriteLocation? {
        return if (l == null) null else locationDao.getFavoriteLocation(networkId, l.type, l.id, l.lat, l.lon, l.place, l.name)
    }

    /**
     * This checks existing ADDRESS Locations for geographic proximity
     * before adding the given location as a favorite.
     * The idea is too prevent duplicates of addresses with slightly different coordinates.
     *
     * @return The given {@link WrapLocation} or if found, the existing one
     */
    private fun findExistingLocation(location: WrapLocation): WrapLocation {
        favoriteLocations.value?.filter {
            it.type == ADDRESS && it.name != null && it.name == location.name && it.isSamePlace(location)
        }?.forEach { return it }
        return location
    }

}
