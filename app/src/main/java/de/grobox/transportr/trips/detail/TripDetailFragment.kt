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

import android.os.Bundle
import android.os.CountDownTimer
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.*
import de.grobox.transportr.trips.detail.TripUtils.getStandardFare
import de.grobox.transportr.trips.detail.TripUtils.hasFare
import de.grobox.transportr.trips.detail.TripUtils.intoCalendar
import de.grobox.transportr.trips.detail.TripUtils.share
import de.grobox.transportr.utils.DateUtils.formatDuration
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.DateUtils.formatRelativeTime
import de.grobox.transportr.utils.TransportrUtils.getColorFromAttr
import de.schildbach.pte.dto.Trip
import de.grobox.transportr.databinding.FragmentTripDetailBinding
import javax.inject.Inject

class TripDetailFragment : TransportrFragment(), Toolbar.OnMenuItemClickListener {

    companion object {
        val TAG = TripDetailFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TripDetailViewModel
    private lateinit var viewBinding: FragmentTripDetailBinding

    private val timeUpdater: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000 * 30) {
        override fun onTick(millisUntilFinished: Long) {
            val fromTimeRel = viewBinding.fromTimeRel
            viewModel.getTrip().value?.let {
                formatRelativeTime(fromTimeRel.context, it.firstDepartureTime).let {
                    fromTimeRel.apply {
                        text = it.relativeTime
                        visibility = it.visibility
                    }
                }
            }
        }

        override fun onFinish() {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentTripDetailBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        component.inject(this)

        viewBinding.toolbar.setNavigationOnClickListener { _ -> onToolbarClose() }
        viewBinding.toolbar.setOnMenuItemClickListener(this)
        viewBinding.list.layoutManager = LinearLayoutManager(context)
        viewBinding.bottomBar.setOnClickListener { _ -> onBottomBarClick() }

        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(TripDetailViewModel::class.java)
        viewModel.getTrip().observe(viewLifecycleOwner, Observer<Trip> { this.onTripChanged(it) })
        viewModel.sheetState.observe(viewLifecycleOwner, Observer<SheetState> { this.onSheetStateChanged(it) })
    }

    override fun onStart() {
        super.onStart()
        timeUpdater.start()
    }

    override fun onStop() {
        super.onStop()
        timeUpdater.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val toolbarMenu = viewBinding.toolbar.menu
        inflater.inflate(R.menu.trip_details, toolbarMenu)
        viewModel.tripReloadError.observe(this, Observer<String> { this.onTripReloadError(it) })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reload -> {
                item.setActionView(R.layout.actionbar_progress_actionview)
                viewModel.reloadTrip()
                return true
            }
            R.id.action_share -> {
                share(context, viewModel.getTrip().value)
                return true
            }
            R.id.action_calendar -> {
                intoCalendar(context, viewModel.getTrip().value)
                return true
            }
            else -> return false
        }
    }

    private fun onTripChanged(trip: Trip?) {
        if (trip == null) return

        val reloadMenuItem = viewBinding.toolbar.menu.findItem(R.id.action_reload)
        if (reloadMenuItem != null) reloadMenuItem.actionView = null

        val network = viewModel.transportNetwork.value
        val showLineName = network != null && network.hasGoodLineNames()
        val adapter = LegAdapter(trip.legs, viewModel, showLineName)
        viewBinding.list.adapter = adapter

        viewBinding.fromTime.text = formatTime(context, trip.firstDepartureTime)
        formatRelativeTime(viewBinding.fromTimeRel.context, trip.firstDepartureTime).let {
            viewBinding.fromTimeRel.apply {
                text = it.relativeTime
                visibility = it.visibility
            }
        }
        viewBinding.from.text = trip.from.uniqueShortName()
        viewBinding.toTime.text = formatTime(context, trip.lastArrivalTime)
        viewBinding.to.text = trip.to.uniqueShortName()
        viewBinding.duration.text = formatDuration(trip.duration)
        viewBinding.durationTop.text = getString(R.string.total_time, formatDuration(trip.duration))
        viewBinding.price.visibility = if (trip.hasFare()) VISIBLE else GONE
        viewBinding.price.text = trip.getStandardFare()
        viewBinding.priceTop.visibility = if (trip.hasFare()) VISIBLE else GONE
        viewBinding.priceTop.text = trip.getStandardFare()
    }

    private fun onToolbarClose() {
        viewModel.sheetState.value = BOTTOM
    }

    private fun onBottomBarClick() {
        viewModel.sheetState.value = MIDDLE
    }

    private fun onSheetStateChanged(sheetState: SheetState?) {
        when (sheetState) {
            null -> return
            BOTTOM -> {
                viewBinding.toolbar.visibility = GONE
                viewBinding.topBar.visibility = GONE
                viewBinding.bottomBar.visibility = VISIBLE
            }
            MIDDLE -> {
                viewBinding.toolbar.visibility = GONE
                viewBinding.topBar.visibility = VISIBLE
                viewBinding.topBar.setBackgroundColor(context.getColorFromAttr(R.attr.colorPrimary))
                viewBinding.fromTimeRel.setTextColor(getColor(context, R.color.md_white_1000))
                viewBinding.durationTop.setTextColor(getColor(context, R.color.md_white_1000))
                viewBinding.priceTop.setTextColor(getColor(context, R.color.md_white_1000))
                viewBinding.bottomBar.visibility = GONE
            }
            EXPANDED -> {
                viewBinding.toolbar.visibility = VISIBLE
                viewBinding.topBar.visibility = VISIBLE
                viewBinding.topBar.setBackgroundColor(context.getColorFromAttr(android.R.attr.colorBackground))
                viewBinding.fromTimeRel.setTextColor(context.getColorFromAttr(android.R.attr.textColorPrimary))
                viewBinding.durationTop.setTextColor(context.getColorFromAttr(android.R.attr.textColorSecondary))
                viewBinding. priceTop.setTextColor(context.getColorFromAttr(android.R.attr.textColorSecondary))
                viewBinding.bottomBar.visibility = GONE
            }
        }
    }

    private fun onTripReloadError(error: String?) {
        viewBinding.toolbar.menu.findItem(R.id.action_reload).actionView = null
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

}
