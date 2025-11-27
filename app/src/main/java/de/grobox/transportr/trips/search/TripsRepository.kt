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

package de.grobox.transportr.trips.search


import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Pair
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.R
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.*
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.networks.TransportNetworkManager_Factory
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.trips.TripQuery
import de.grobox.transportr.utils.SingleLiveEvent
import de.grobox.transportr.utils.TransportrUtils
import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.dto.QueryTripsContext
import de.schildbach.pte.dto.QueryTripsResult
import de.schildbach.pte.dto.QueryTripsResult.Status.*
import de.schildbach.pte.dto.Trip
import de.schildbach.pte.dto.TripOptions
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

class TripsRepository(
        private val ctx: Context,
        private val networkProvider: NetworkProvider,
        private val settingsManager: SettingsManager,
        private val locationRepository: LocationRepository,
        private val searchesRepository: SearchesRepository) {

    companion object {
        private val TAG = TripsRepository::class.java.simpleName
    }

    enum class QueryMoreState { EARLIER, LATER, BOTH, NONE }

    val trips = MutableLiveData<Set<Trip>>()
    val queryMoreState = MutableLiveData<QueryMoreState>()
    val queryError = SingleLiveEvent<String>()
    val queryPTEError = SingleLiveEvent<Pair<String, String>>()
    val queryMoreError = SingleLiveEvent<String>()
    val isFavTrip = MutableLiveData<Boolean>()

    private var uid: Long = 0L
    private var queryTripsLaterContext: QueryTripsContext? = null
    private var queryTripsEarlierContext: QueryTripsContext? = null
    private var queryTripsTask = Thread()

    init {
        queryMoreState.value = QueryMoreState.NONE
    }

    private fun clearState() {
        trips.value = null
        queryMoreState.value = QueryMoreState.NONE
        queryTripsLaterContext = null
        queryTripsEarlierContext = null
        isFavTrip.value = null
        uid = 0L
    }

    fun search(query: TripQuery) {
        // reset current data
        clearState()

        Log.i(TAG, "From: " + query.from.location)
        Log.i(TAG, "Via: " + (if (query.via == null) "null" else query.via.location))
        Log.i(TAG, "To: " + query.to.location)
        Log.i(TAG, "Date: " + query.date)
        Log.i(TAG, "Departure: " + query.departure)
        Log.i(TAG, "Products: " + query.products)
        Log.i(TAG, "Optimize for: " + settingsManager.optimize)
        Log.i(TAG, "Walk Speed: " + settingsManager.walkSpeed)

        if (queryTripsTask.isAlive && !queryTripsTask.isInterrupted) {
            queryTripsTask.interrupt()
        }
        queryTripsTask = thread(true) { queryTrips(query) }
    }

    @WorkerThread
    private fun queryTrips(query: TripQuery) {
        try {
            val queryTripsResult = networkProvider.queryTrips(
                query.from.location, if (query.via == null) null else query.via.location, query.to.location,
                query.date, query.departure,
                TripOptions(query.products, settingsManager.optimize, settingsManager.walkSpeed, null, null)
            )
            if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                // deliver result first, so UI can get updated
                onQueryTripsResultReceived(queryTripsResult, null)
                // store locations (needed for references in stored search)
                val from = locationRepository.addFavoriteLocation(query.from, FROM)
                val via = query.via?.let { locationRepository.addFavoriteLocation(it, VIA) }
                val to = locationRepository.addFavoriteLocation(query.to, TO)
                // store search query
                uid = searchesRepository.storeSearch(from, via, to)
                // set fav status
                isFavTrip.postValue(searchesRepository.isFavorite(uid))
            } else {
                PTEError(queryTripsResult.status.name, queryTripsResult.getError(), query)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is InterruptedIOException && e !is SocketTimeoutException) {
                // return, because this thread was interrupted
            } else if (!TransportrUtils.hasInternet(ctx)) {
                queryError.postValue(ctx.getString(R.string.error_no_internet))
            } else if (e is SocketTimeoutException) {
                queryError.postValue(ctx.getString(R.string.error_connection_failure))
            } else {
                val errorBuilder = StringBuilder("$e\n${e.stackTrace[0]}\n${e.stackTrace[1]}\n${e.stackTrace[2]}")
                e.cause?.let { errorBuilder.append("\nCause: ${it.stackTrace[0]}\n${it.stackTrace[1]}\n${it.stackTrace[2]}") }
                PTEError(e.toString(), errorBuilder.toString(), query)
            }
        }
    }

    fun searchMore(later: Boolean) {
        if (later && !queryTripsLaterContext!!.canQueryLater()) throw IllegalStateException("Can not query later")
        if (!later && !queryTripsEarlierContext!!.canQueryEarlier()) throw IllegalStateException("Can not query earlier")

        val queryTripsContext = if (later) queryTripsLaterContext!! else queryTripsEarlierContext!!

        Log.i(TAG, "QueryTripsContext: " + queryTripsContext.toString())
        Log.i(TAG, "Later: $later")

        thread(true) {
            try {
                val queryTripsResult = networkProvider.queryMoreTrips(queryTripsContext, later)
                if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                    onQueryTripsResultReceived(queryTripsResult, later)
                } else {
                    queryMoreError.postValue(queryTripsResult.getError())
                }
            } catch (e: Exception) {
                queryMoreError.postValue(e.toString())
            }
        }
    }

    private fun onQueryTripsResultReceived(queryTripsResult: QueryTripsResult, later: Boolean?) {
        Handler(Looper.getMainLooper()).post {
            if (later == null || !later) queryTripsEarlierContext = queryTripsResult.context
            if (later == null || later) queryTripsLaterContext = queryTripsResult.context
            queryMoreState.value = getQueryMoreStateFromContext()

            val oldTrips = trips.value?.let { HashSet(it) } ?: HashSet()
            oldTrips.addAll(queryTripsResult.trips)
            trips.value = oldTrips
        }
    }

    private fun getQueryMoreStateFromContext() =
        if (queryTripsEarlierContext?.canQueryEarlier() == true && queryTripsLaterContext?.canQueryLater() == true) {
            QueryMoreState.BOTH
        } else if (queryTripsEarlierContext?.canQueryEarlier() == true) {
            QueryMoreState.EARLIER
        } else if (queryTripsLaterContext?.canQueryLater() == true) {
            QueryMoreState.LATER
        } else {
            QueryMoreState.NONE
        }

    private fun QueryTripsResult.getError(): String = when (status) {
        AMBIGUOUS -> ctx.getString(R.string.trip_error_ambiguous)
        TOO_CLOSE -> ctx.getString(R.string.trip_error_too_close)
        UNKNOWN_FROM -> ctx.getString(R.string.trip_error_unknown_from)
        UNKNOWN_VIA -> ctx.getString(R.string.trip_error_unknown_via)
        UNKNOWN_TO -> ctx.getString(R.string.trip_error_unknown_to)
        UNKNOWN_LOCATION -> ctx.getString(R.string.trip_error_unknown_from)
        UNRESOLVABLE_ADDRESS -> ctx.getString(R.string.trip_error_unresolvable_address)
        NO_TRIPS -> ctx.getString(R.string.trip_error_no_trips)
        INVALID_DATE -> ctx.getString(R.string.trip_error_invalid_date)
        SERVICE_DOWN -> ctx.getString(R.string.trip_error_service_down)
        OK -> throw IllegalArgumentException()
        null -> throw IllegalStateException()
    }

    private fun PTEError(errorShort: String, error: String, query: TripQuery) {
        val title = StringBuilder()
            .append(networkProvider.id().name)
            .append(": ")
            .append(errorShort)
        val body = StringBuilder()
            .appendLine("### Query")
            .appendLine("- NetworkId: `${networkProvider.id().name}`")
            .appendLine("- From: `${query.from.location}`")
            .appendLine("- Via: `${if (query.via == null) "null" else query.via.location}`")
            .appendLine("- To: `${query.to.location}`")
            .appendLine("- Date: `${query.date}`")
            .appendLine("- Departure: `${query.departure}`")
            .appendLine("- Products: `${query.products}`")
            .appendLine("- Optimize for: `${settingsManager.optimize}`")
            .appendLine("- Walk Speed: `${settingsManager.walkSpeed}`")
            .appendLine()
            .appendLine("### Error")
            .appendLine("```")
            .appendLine(error)
            .appendLine("```")
            .appendLine()
            .appendLine("### Additional information")
            .appendLine("[Please modify this part]")

        val uri = Uri.Builder()
            .scheme("https")
            .authority("github.com")
            .appendPath("schildbach")
            .appendPath("public-transport-enabler")
            .appendPath("issues")
            .appendPath("new")
            .appendQueryParameter("title", title.toString())
            .appendQueryParameter("body", body.toString())
        queryPTEError.postValue(Pair(error, uri.build().toString()))
    }

    fun toggleFavState() {
        val oldFavState = isFavTrip.value
        if (uid == 0L || oldFavState == null) throw IllegalStateException()
        searchesRepository.updateFavoriteState(uid, !oldFavState)
        isFavTrip.value = !oldFavState
    }

}
