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
import de.grobox.transportr.databinding.FragmentTripDetailBinding
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
import javax.inject.Inject

class TripDetailFragment : TransportrFragment(), Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentTripDetailBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        val TAG = TripDetailFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TripDetailViewModel

    private val timeUpdater: CountDownTimer = object : CountDownTimer(Long.MAX_VALUE, 1000 * 30) {
        override fun onTick(millisUntilFinished: Long) {
            viewModel.getTrip().value?.let {
                formatRelativeTime(binding.fromTimeRel.context, it.firstDepartureTime).let {
                    binding.fromTimeRel.apply {
                        text = it.relativeTime
                        visibility = it.visibility
                    }
                }
            }
        }

        override fun onFinish() {}
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentTripDetailBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        component.inject(this)

        binding.toolbar.setNavigationOnClickListener { _ -> onToolbarClose() }
        binding.toolbar.setOnMenuItemClickListener(this)
        binding.list.layoutManager = LinearLayoutManager(context)
        binding.bottomBar.setOnClickListener { _ -> onBottomBarClick() }

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
        val toolbarMenu = binding.toolbar.menu
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

        val reloadMenuItem = binding.toolbar.menu.findItem(R.id.action_reload)
        if (reloadMenuItem != null) reloadMenuItem.actionView = null

        val network = viewModel.transportNetwork.value
        val showLineName = network != null && network.hasGoodLineNames()
        val adapter = LegAdapter(trip.legs, viewModel, showLineName)
        binding.list.adapter = adapter

        binding.fromTime.text = formatTime(context, trip.firstDepartureTime)
        formatRelativeTime(binding.fromTimeRel.context, trip.firstDepartureTime).let {
            binding.fromTimeRel.apply {
                text = it.relativeTime
                visibility = it.visibility
            }
        }
        binding.from.text = trip.from.uniqueShortName()
        binding.toTime.text = formatTime(context, trip.lastArrivalTime)
        binding.to.text = trip.to.uniqueShortName()
        binding.duration.text = formatDuration(trip.duration)
        binding.durationTop.text = getString(R.string.total_time, formatDuration(trip.duration))
        binding.price.visibility = if (trip.hasFare()) VISIBLE else GONE
        binding.price.text = trip.getStandardFare()
        binding.priceTop.visibility = if (trip.hasFare()) VISIBLE else GONE
        binding.priceTop.text = trip.getStandardFare()
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
                binding.toolbar.visibility = GONE
                binding.topBar.visibility = GONE
                binding.bottomBar.visibility = VISIBLE
            }
            MIDDLE -> {
                binding.toolbar.visibility = GONE
                binding.topBar.visibility = VISIBLE
                binding.topBar.setBackgroundColor(context.getColorFromAttr(R.attr.colorPrimary))
                binding.fromTimeRel.setTextColor(getColor(context, R.color.md_white_1000))
                binding.durationTop.setTextColor(getColor(context, R.color.md_white_1000))
                binding.priceTop.setTextColor(getColor(context, R.color.md_white_1000))
                binding.bottomBar.visibility = GONE
            }
            EXPANDED -> {
                binding.toolbar.visibility = VISIBLE
                binding.topBar.visibility = VISIBLE
                binding.topBar.setBackgroundColor(context.getColorFromAttr(android.R.attr.colorBackground))
                binding.fromTimeRel.setTextColor(context.getColorFromAttr(android.R.attr.textColorPrimary))
                binding.durationTop.setTextColor(context.getColorFromAttr(android.R.attr.textColorSecondary))
                binding.priceTop.setTextColor(context.getColorFromAttr(android.R.attr.textColorSecondary))
                binding.bottomBar.visibility = GONE
            }
        }
    }

    private fun onTripReloadError(error: String?) {
        binding.toolbar.menu.findItem(R.id.action_reload).actionView = null
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

}
