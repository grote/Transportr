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

package de.grobox.transportr.trips.detail

import androidx.lifecycle.MutableLiveData
import androidx.annotation.WorkerThread
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.trips.TripQuery
import de.grobox.transportr.utils.SingleLiveEvent
import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.dto.QueryTripsResult
import de.schildbach.pte.dto.QueryTripsResult.Status.OK
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.TripOptions
import java.util.*

internal class TripReloader(
        private val networkProvider: NetworkProvider,
        private val settingsManager: SettingsManager,
        private val query: TripQuery,
        private val trip: MutableLiveData<Trip>,
        private val errorString: String,
        private val tripReloadError: SingleLiveEvent<String>) {

    fun reload() {
        // use a new date slightly earlier to avoid missing the right trip
        val newDate = Date()
        newDate.time = query.date.time - 5000

        Thread {
            try {
                val queryTripsResult = networkProvider.queryTrips(
                    query.from.location, query.via?.location, query.to.location, newDate, true,
                    TripOptions(query.products, settingsManager.optimize, settingsManager.walkSpeed, null, null)
                )
                if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                    onTripReloaded(queryTripsResult)
                } else {
                    tripReloadError.postValue(errorString + "\n" + queryTripsResult.status.name)
                }
            } catch (e: Exception) {
                tripReloadError.postValue(errorString + "\n" + e.toString())
            }
        }.start()
    }

    @WorkerThread
    private fun onTripReloaded(result: QueryTripsResult) {
        val oldTrip = this.trip.value ?: throw IllegalStateException()

        for (newTrip in result.trips) {
            if (oldTrip.isTheSame(newTrip)) {
                trip.postValue(newTrip)
                return
            }
        }
        tripReloadError.postValue(errorString)
    }

    private fun Trip.isTheSame(newTrip: Trip): Boolean {
        // we can not rely on the trip ID as it is too generic with some providers
        if (numChanges != newTrip.numChanges) return false
        if (legs.size != newTrip.legs.size) return false
        if (getPlannedDuration() != newTrip.getPlannedDuration()) return false
        if (firstPublicLeg?.getDepartureTime(true) != newTrip.firstPublicLeg?.getDepartureTime(true)) return false
        if (lastPublicLeg?.getArrivalTime(true) != newTrip.lastPublicLeg?.getArrivalTime(true)) return false
        if (firstPublicLeg?.line?.label != newTrip.firstPublicLeg?.line?.label) return false
        if (lastPublicLeg?.line?.label != newTrip.lastPublicLeg?.line?.label) return false
        if (firstPublicLeg == null && firstDepartureTime != newTrip.firstDepartureTime) return false
        if (lastPublicLeg == null && lastArrivalTime != newTrip.lastArrivalTime) return false
        return true
    }

    private fun Trip.getPlannedDuration(): Long {
        val first = firstPublicLeg?.getDepartureTime(true) ?: firstDepartureTime
        val last = lastPublicLeg?.getDepartureTime(true) ?: lastArrivalTime
        return last.time - first.time
    }

}
