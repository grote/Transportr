package de.grobox.transportr.trips.search


import android.arch.lifecycle.MutableLiveData
import android.os.Handler
import android.os.Looper
import android.util.Log
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.utils.SingleLiveEvent
import de.schildbach.pte.NetworkProvider
import de.schildbach.pte.dto.QueryTripsContext
import de.schildbach.pte.dto.QueryTripsResult
import de.schildbach.pte.dto.QueryTripsResult.Status.OK
import de.schildbach.pte.dto.Trip

internal class TripsRepository(
        private val networkProvider: NetworkProvider,
        private val settingsManager: SettingsManager,
        private val searchesRepository: SearchesRepository) {

    companion object {
        private val TAG = TripsRepository::class.java.simpleName
    }

    enum class QueryMoreState { EARLIER, LATER, BOTH, NONE }

    val trips = MutableLiveData<Set<Trip>>()
    val queryMoreState = MutableLiveData<QueryMoreState>()
    val queryError = SingleLiveEvent<String>()
    val queryMoreError = SingleLiveEvent<String>()

    private var queryTripsContext: QueryTripsContext? = null

    init {
        queryMoreState.value = QueryMoreState.NONE
    }

    private fun clearState() {
        trips.value = null
        queryMoreState.value = QueryMoreState.NONE
        queryTripsContext = null
    }

    fun search(query: TripQuery) {
        clearState()  // reset current data

        Log.i(TAG, "From: " + query.from.location)
        Log.i(TAG, "Via: " + if (query.via == null) "null" else query.via.location)
        Log.i(TAG, "To: " + query.to.location)
        Log.i(TAG, "Date: " + query.date)
        Log.i(TAG, "Departure: " + query.departure)
        Log.i(TAG, "Products: " + query.products)
        Log.i(TAG, "Optimize for: " + settingsManager.optimize)
        Log.i(TAG, "Walk Speed: " + settingsManager.walkSpeed)

        Thread {
            try {
                val queryTripsResult = networkProvider.queryTrips(
                        query.from.location, if (query.via == null) null else query.via.location, query.to.location,
                        query.date, query.departure, query.products, settingsManager.optimize, settingsManager.walkSpeed,
                        null, null)
                if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                    searchesRepository.storeSearch(query.toFavoriteTripItem())
                    onQueryTripsResultReceived(queryTripsResult)
                } else {
                    queryError.postValue(queryTripsResult.status.name)
                }
            } catch (e: Exception) {
                queryError.postValue(e.toString())
            }
        }.start()
    }

    fun searchMore(later: Boolean) {
        if (queryTripsContext == null) throw IllegalStateException("No query context")
        if (later && !queryTripsContext!!.canQueryLater()) throw IllegalStateException("Can not query later")
        if (!later && !queryTripsContext!!.canQueryEarlier()) throw IllegalStateException("Can not query earlier")

        Log.i(TAG, "QueryTripsContext: " + queryTripsContext!!.toString())
        Log.i(TAG, "Later: " + later)

        Thread {
            try {
                val queryTripsResult = networkProvider.queryMoreTrips(queryTripsContext, later)
                if (queryTripsResult.status == OK && queryTripsResult.trips.size > 0) {
                    onQueryTripsResultReceived(queryTripsResult)
                } else {
                    queryMoreError.postValue(queryTripsResult.status.name)
                }
            } catch (e: Exception) {
                queryMoreError.postValue(e.toString())
            }
        }.start()
    }

    private fun onQueryTripsResultReceived(queryTripsResult: QueryTripsResult) {
        Handler(Looper.getMainLooper()).post({
            queryTripsContext = queryTripsResult.context
            queryMoreState.value = getQueryMoreStateFromContext(queryTripsContext)

            val oldTrips = trips.value?.let { HashSet(it) } ?: HashSet()
            oldTrips.addAll(queryTripsResult.trips)
            trips.value = oldTrips
        })
    }

    private fun getQueryMoreStateFromContext(context: QueryTripsContext?): QueryMoreState = context?.let {
        if (it.canQueryEarlier() && it.canQueryLater()) {
            QueryMoreState.BOTH
        } else if (it.canQueryEarlier()) {
            QueryMoreState.EARLIER
        } else if (it.canQueryLater()) {
            QueryMoreState.LATER
        } else {
            QueryMoreState.NONE
        }
        QueryMoreState.BOTH
    } ?: QueryMoreState.NONE

}
