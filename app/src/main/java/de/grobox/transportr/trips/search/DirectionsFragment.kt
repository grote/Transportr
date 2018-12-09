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

package de.grobox.transportr.trips.search

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.view.animation.Animation
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.TranslateAnimation
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.TO
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.VIA
import de.grobox.transportr.settings.SettingsManager
import de.grobox.transportr.ui.TimeDateFragment
import de.grobox.transportr.utils.Constants.DATE
import de.grobox.transportr.utils.Constants.DEPARTURE
import de.grobox.transportr.utils.Constants.EXPANDED
import de.grobox.transportr.utils.DateUtils
import de.grobox.transportr.utils.DateUtils.*
import kotlinx.android.synthetic.main.fragment_directions_form.*
import java.util.*
import javax.annotation.ParametersAreNonnullByDefault
import javax.inject.Inject

@ParametersAreNonnullByDefault
class DirectionsFragment : TransportrFragment() {

    @Inject
    internal lateinit var settingsManager: SettingsManager
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DirectionsViewModel
    private var expandItem: MenuItem? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_directions_form, container, false)
        component.inject(this)

        setHasOptionsMenu(true)

        viewModel = ViewModelProviders.of(activity!!, viewModelFactory).get(DirectionsViewModel::class.java)

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(toolbar)

        val network = viewModel.transportNetwork.value ?: throw IllegalStateException()
        fromLocation.setTransportNetwork(network)
        viaLocation.setTransportNetwork(network)
        toLocation.setTransportNetwork(network)

        fromLocation.type = FROM
        viaLocation.type = VIA
        toLocation.type = TO

        fromLocation.setLocationViewListener(viewModel)
        viaLocation.setLocationViewListener(viewModel)
        toLocation.setLocationViewListener(viewModel)

        viewModel.home.observe(this, Observer { homeLocation ->
            fromLocation.setHomeLocation(homeLocation)
            viaLocation.setHomeLocation(homeLocation)
            toLocation.setHomeLocation(homeLocation)
        })
        viewModel.work.observe(this, Observer { workLocation ->
            fromLocation.setWorkLocation(workLocation)
            viaLocation.setWorkLocation(workLocation)
            toLocation.setWorkLocation(workLocation)
        })
        viewModel.locations.observe(this, Observer { favoriteLocations ->
            if (favoriteLocations == null) return@Observer
            fromLocation.setFavoriteLocations(favoriteLocations)
            viaLocation.setFavoriteLocations(favoriteLocations)
            toLocation.setFavoriteLocations(favoriteLocations)
        })
        viewModel.fromLocation.observe(this, Observer { location ->
            fromLocation.setLocation(location)
            if (location != null) toLocation.requestFocus()
        })
        viewModel.viaLocation.observe(this, Observer { location -> viaLocation.setLocation(location) })
        viewModel.toLocation.observe(this, Observer { location -> toLocation.setLocation(location) })
        viewModel.isDeparture.observe(this, Observer<Boolean> { this.onIsDepartureChanged(it) })
        viewModel.isExpanded.observe(this, Observer<Boolean> { this.onViaVisibleChanged(it) })
        viewModel.calendar.observe(this, Observer { this.onCalendarUpdated(it) })
        viewModel.findGpsLocation.observe(this, Observer { this.onFindGpsLocation(it) })
        viewModel.isFavTrip.observe(this, Observer { this.onFavStatusChanged(it) })

        favIcon.visibility = VISIBLE
        favIcon.setOnClickListener { viewModel.toggleFavTrip() }

        departureIcon.setOnClickListener { viewModel.toggleDeparture() }

        val onTimeClickListener = OnClickListener {
            if (viewModel.calendar.value == null) throw IllegalStateException()
            val fragment = TimeDateFragment.newInstance(viewModel.calendar.value!!)
            fragment.setTimeDateListener(viewModel)
            fragment.show(activity!!.supportFragmentManager, TimeDateFragment.TAG)
        }
        val onTimeLongClickListener = OnLongClickListener {
            viewModel.resetCalender()
            true
        }

        timeIcon.setOnClickListener(onTimeClickListener)
        date.setOnClickListener(onTimeClickListener)
        time.setOnClickListener(onTimeClickListener)

        timeIcon.setOnLongClickListener(onTimeLongClickListener)
        date.setOnLongClickListener(onTimeLongClickListener)
        time.setOnLongClickListener(onTimeLongClickListener)

        productsIcon.setOnClickListener {
            activity?.let { a ->
                ProductDialogFragment().show(a.supportFragmentManager, ProductDialogFragment.TAG)
            }
        }
        swapIcon.setOnClickListener { swapLocations() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(DATE, viewModel.calendar.value)
        outState.putBoolean(EXPANDED, viewModel.isExpanded.value ?: false)
        outState.putBoolean(DEPARTURE, viewModel.isDeparture.value ?: true)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && viewModel.trips.value == null) {
            viewModel.setIsExpanded(savedInstanceState.getBoolean(EXPANDED, false))
            viewModel.setIsDeparture(savedInstanceState.getBoolean(DEPARTURE, true))
            viewModel.setFromLocation(fromLocation.getLocation())
            viewModel.setViaLocation(viaLocation.getLocation())
            viewModel.setToLocation(toLocation.getLocation())
            viewModel.onTimeAndDateSet(savedInstanceState.getSerializable(DATE) as Calendar)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.directions, menu)
        expandItem = menu.findItem(R.id.action_navigation_expand)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_navigation_expand -> {
                viewModel.setIsExpanded(!item.isChecked)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onCalendarUpdated(calendar: Calendar?) {
        if (calendar == null) return
        when {
            isNow(calendar) -> {
                time.setText(R.string.now)
                date.visibility = GONE
            }
            DateUtils.isToday(calendar) -> {
                time.text = getTime(context, calendar.time)
                date.visibility = GONE
            }
            else -> {
                time.text = getTime(context, calendar.time)
                date.text = getDate(context, calendar.time)
                date.visibility = VISIBLE
            }
        }
    }

    private fun swapLocations() {
        val toToY = fromCard.y - toCard.y
        val slideUp = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF,
            0.0f, Animation.ABSOLUTE, toToY
        )
        slideUp.duration = 400
        slideUp.fillAfter = true
        slideUp.isFillEnabled = true

        val fromToY = toCard.y - fromCard.y
        val slideDown = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF,
            0.0f, Animation.ABSOLUTE, fromToY
        )
        slideDown.duration = 400
        slideDown.fillAfter = true
        slideDown.isFillEnabled = true

        fromCard.startAnimation(slideDown)
        toCard.startAnimation(slideUp)

        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                // swap location objects
                val tmp = toLocation.getLocation()
                if (fromLocation.isSearching) {
                    viewModel.findGpsLocation.setValue(null)
                    // TODO: GPS currently only supports from location, so don't swap it for now
                    viewModel.setToLocation(null)
                } else {
                    viewModel.setToLocation(fromLocation.getLocation())
                }
                viewModel.setFromLocation(tmp)

                fromCard.clearAnimation()
                toCard.clearAnimation()

                viewModel.search()
            }
        })
    }

    private fun onIsDepartureChanged(isDeparture: Boolean) {
        departureIcon.setImageResource(if (isDeparture) R.drawable.ic_trip_departure else R.drawable.ic_trip_arrival)
    }

    private fun onViaVisibleChanged(viaVisible: Boolean) {
        expandItem?.isChecked = viaVisible
        expandItem?.setIcon(if (viaVisible) R.drawable.ic_action_navigation_unfold_less_white else R.drawable.ic_action_navigation_unfold_more_white)
        viaCard.visibility = if (viaVisible) VISIBLE else GONE
    }

    private fun onFindGpsLocation(type: FavLocationType?) {
        if (type == null) {
            viewModel.locationLiveData.removeObservers(this@DirectionsFragment)
            fromLocation.clearSearching()
            return
        }
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            return
        }
        fromLocation.setSearching()
        toLocation.requestFocus()
        viewModel.locationLiveData.observe(this, Observer { location ->
            viewModel.setFromLocation(location)
            viewModel.search()
            viewModel.locationLiveData.removeObservers(this@DirectionsFragment)
        })
    }

    private fun onFavStatusChanged(isFav: Boolean?) {
        if (isFav == null) {
            favIcon.visibility = INVISIBLE
        } else {
            favIcon.visibility = VISIBLE
            if (isFav) {
                favIcon.setImageResource(R.drawable.ic_action_star)
            } else {
                favIcon.setImageResource(R.drawable.ic_action_star_empty)
            }
        }
    }

}
