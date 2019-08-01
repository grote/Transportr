/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast

import javax.inject.Inject

import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.trips.detail.TripUtils.getStandardFare
import de.grobox.transportr.trips.detail.TripUtils.hasFare
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState
import de.grobox.transportr.utils.TransportrUtils.getColorFromAttr
import de.schildbach.pte.dto.Trip

import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import de.grobox.transportr.trips.detail.TripDetailViewModel.SheetState.*
import de.grobox.transportr.trips.detail.TripUtils.intoCalendar
import de.grobox.transportr.trips.detail.TripUtils.share
import de.grobox.transportr.utils.DateUtils.*

class TripDetailFragment : TransportrFragment(), Toolbar.OnMenuItemClickListener {

    companion object {
        val TAG = TripDetailFragment::class.java.simpleName
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: TripDetailViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var list: RecyclerView
    private lateinit var bottomBar: View
    private lateinit var fromTime: TextView
    private lateinit var from: TextView
    private lateinit var toTime: TextView
    private lateinit var to: TextView
    private lateinit var duration: TextView
    private lateinit var price: TextView
    private lateinit var topBar: View
    private lateinit var fromTimeRel: TextView
    private lateinit var durationTop: TextView
    private lateinit var priceTop: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_trip_detail, container, false)
        setHasOptionsMenu(true)
        component.inject(this)

        toolbar = v.findViewById(R.id.toolbar)
        list = v.findViewById(R.id.list)
        bottomBar = v.findViewById(R.id.bottomBar)
        fromTime = bottomBar.findViewById(R.id.fromTime)
        from = bottomBar.findViewById(R.id.from)
        toTime = bottomBar.findViewById(R.id.toTime)
        to = bottomBar.findViewById(R.id.to)
        duration = bottomBar.findViewById(R.id.duration)
        price = bottomBar.findViewById(R.id.price)
        topBar = v.findViewById(R.id.topBar)
        fromTimeRel = topBar.findViewById(R.id.fromTimeRel)
        durationTop = topBar.findViewById(R.id.durationTop)
        priceTop = topBar.findViewById(R.id.priceTop)

        toolbar.setNavigationOnClickListener { _ -> onToolbarClose() }
        toolbar.setOnMenuItemClickListener(this)
        list.layoutManager = LinearLayoutManager(context)
        bottomBar.setOnClickListener { _ -> onBottomBarClick() }

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(TripDetailViewModel::class.java)
        viewModel.getTrip().observe(this, Observer<Trip> { this.onTripChanged(it) })
        viewModel.sheetState.observe(this, Observer<SheetState> { this.onSheetStateChanged(it) })

        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val toolbarMenu = toolbar.menu
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

        val reloadMenuItem = toolbar.menu.findItem(R.id.action_reload)
        if (reloadMenuItem != null) reloadMenuItem.actionView = null

        val network = viewModel.transportNetwork.value
        val showLineName = network != null && network.hasGoodLineNames()
        val adapter = LegAdapter(trip.legs, viewModel, showLineName)
        list.adapter = adapter

        fromTime.text = getTime(context, trip.firstDepartureTime)
        setRelativeDepartureTime(fromTimeRel, trip.firstDepartureTime)
        from.text = trip.from.uniqueShortName()
        toTime.text = getTime(context, trip.lastArrivalTime)
        to.text = trip.to.uniqueShortName()
        duration.text = getDuration(trip.duration)
        durationTop.text = getString(R.string.total_time, getDuration(trip.duration))
        price.visibility = if (trip.hasFare()) VISIBLE else GONE
        price.text = trip.getStandardFare()
        priceTop.visibility = if (trip.hasFare()) VISIBLE else GONE
        priceTop.text = trip.getStandardFare()
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
                toolbar.visibility = GONE
                topBar.visibility = GONE
                bottomBar.visibility = VISIBLE
            }
            MIDDLE -> {
                toolbar.visibility = GONE
                topBar.visibility = VISIBLE
                topBar.setBackgroundColor(context.getColorFromAttr(R.attr.colorPrimary))
                fromTimeRel.setTextColor(getColor(context, R.color.md_white_1000))
                durationTop.setTextColor(getColor(context, R.color.md_white_1000))
                priceTop.setTextColor(getColor(context, R.color.md_white_1000))
                bottomBar.visibility = GONE
            }
            EXPANDED -> {
                toolbar.visibility = VISIBLE
                topBar.visibility = VISIBLE
                topBar.setBackgroundColor(context.getColorFromAttr(android.R.attr.colorBackground))
                fromTimeRel.setTextColor(context.getColorFromAttr(android.R.attr.textColorPrimary))
                durationTop.setTextColor(context.getColorFromAttr(android.R.attr.textColorSecondary))
                priceTop.setTextColor(context.getColorFromAttr(android.R.attr.textColorSecondary))
                bottomBar.visibility = GONE
            }
        }
    }

    private fun onTripReloadError(error: String?) {
        toolbar.menu.findItem(R.id.action_reload).actionView = null
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
    }

}
