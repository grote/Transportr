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

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.trips.detail.TripDetailActivity
import de.grobox.transportr.trips.search.TripAdapter.OnTripClickListener
import de.grobox.transportr.trips.search.TripsRepository.QueryMoreState
import de.grobox.transportr.ui.LceAnimator
import de.grobox.transportr.utils.Linkify
import de.grobox.transportr.utils.TransportrUtils.getDragDistance
import de.schildbach.pte.dto.Trip
import kotlinx.android.synthetic.main.fragment_trips.*
import java.util.regex.Pattern
import javax.annotation.ParametersAreNonnullByDefault
import javax.inject.Inject

class TripsFragment : TransportrFragment(), OnRefreshListener, OnTripClickListener {
    
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private lateinit var viewModel: DirectionsViewModel
    
    private val adapter = TripAdapter(this)
    private var topSwipingEnabled = false
    private var queryMoreDirection = SwipyRefreshLayoutDirection.BOTH

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_trips, container, false)
        component.inject(this)

        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(DirectionsViewModel::class.java)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Progress Bar and Error View
        errorButton.setOnClickListener { viewModel.search() }

        // Swipe to Refresh
        swipe.let {
            it.setColorSchemeColors(R.color.accent)
            it.setDistanceToTriggerSync(getDragDistance(context))
            it.setOnRefreshListener(this)
        }

        val layoutManager = LinearLayoutManager(context)
        list.layoutManager = layoutManager
        list.setHasFixedSize(false)
        viewModel.topSwipeEnabled.observe(viewLifecycleOwner, { enabled -> onSwipeEnabledChanged(enabled) })
        viewModel.queryMoreState.observe(viewLifecycleOwner, { state -> updateSwipeState(state) })
        viewModel.trips.observe(viewLifecycleOwner, { trips -> onTripsLoaded(trips) })
        viewModel.queryError.observe(viewLifecycleOwner, { error -> onError(error) })
        viewModel.queryPTEError.observe(viewLifecycleOwner, { error -> onPTEError(error) })
        viewModel.queryMoreError.observe(viewLifecycleOwner, { error -> onMoreError(error) })
        viewModel.timeUpdate.observe(viewLifecycleOwner, { adapter.notifyDataSetChanged() })
        adapter.setHasStableIds(false)
        list.adapter = adapter
        LceAnimator.showLoading(progressBar, list, errorLayout)
    }

    override fun onRefresh(direction: SwipyRefreshLayoutDirection) {
        queryMoreDirection = direction
        val later = queryMoreDirection == SwipyRefreshLayoutDirection.BOTTOM
        viewModel.searchMore(later)
    }

    private fun onSwipeEnabledChanged(enabled: Boolean) {
        if (!swipe.isRefreshing && enabled != topSwipingEnabled) {
            updateSwipeState(viewModel.queryMoreState.value)
        }
        topSwipingEnabled = enabled
    }

    private fun updateSwipeState(state: QueryMoreState?) {
        val topEnabled = viewModel.topSwipeEnabled.value
        if (topEnabled == null || state == null) return
        if (state === QueryMoreState.NONE || (!topEnabled && state === QueryMoreState.EARLIER)) {
            swipe.isEnabled = false
        } else {
            swipe.isEnabled = true
            swipe.direction = when {
                state === QueryMoreState.EARLIER -> SwipyRefreshLayoutDirection.TOP
                state === QueryMoreState.LATER -> SwipyRefreshLayoutDirection.BOTTOM
                !topEnabled && state === QueryMoreState.BOTH -> SwipyRefreshLayoutDirection.BOTTOM
                else -> SwipyRefreshLayoutDirection.BOTH
            }
        }
    }

    private fun onTripsLoaded(trips: Set<Trip>?) {
        if (trips == null) return
        val oldCount = adapter.itemCount
        adapter.addAll(trips)
        if (oldCount > 0) {
            swipe.isRefreshing = false
            list.smoothScrollBy(0, if (queryMoreDirection == SwipyRefreshLayoutDirection.BOTTOM) 200 else -200)
        } else {
            LceAnimator.showContent(progressBar, list, errorLayout)
        }
    }

    private fun onError(error: String?) {
        if (error == null) return
        errorText.text = error
        LceAnimator.showErrorView(progressBar, list, errorLayout)
    }

    @SuppressLint("SetTextI18n")
    private fun onPTEError(error: Pair<String, String>?) {
        if (error == null) return
        errorText.text = "${error.first}\n\n${getString(R.string.trip_error_pte, "public-transport-enabler")}"
        val pteMatcher = Pattern.compile("public-transport-enabler")
        Linkify.addLinks(errorText, pteMatcher, error.second)
        LceAnimator.showErrorView(progressBar, list, errorLayout)
    }

    private fun onMoreError(error: String?) {
        if (error == null) return
        swipe.isRefreshing = false
        Toast.makeText(context, error, Toast.LENGTH_LONG).show()
    }

    override fun onClick(trip: Trip) {
        startActivity(
            Intent(context, TripDetailActivity::class.java).apply {
                putExtra(TripDetailActivity.TRIP, trip)
                // unfortunately, PTE does not save these locations reliably in the Trip object
                putExtra(TripDetailActivity.FROM, viewModel.fromLocation.value)
                putExtra(TripDetailActivity.VIA, viewModel.viaLocation.value)
                putExtra(TripDetailActivity.TO, viewModel.toLocation.value)
            }
        )
    }

    companion object {
        val TAG = TripsFragment::class.java.name
    }
}