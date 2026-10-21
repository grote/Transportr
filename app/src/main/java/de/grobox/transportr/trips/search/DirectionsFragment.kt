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

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
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
import de.grobox.transportr.utils.DateUtils.formatDate
import de.grobox.transportr.utils.DateUtils.formatRelativeTime
import de.grobox.transportr.utils.DateUtils.formatTime
import de.grobox.transportr.utils.DateUtils.isNow
import de.grobox.transportr.utils.DateUtils.isToday
import de.schildbach.pte.dto.Product
import java.util.*
import javax.annotation.ParametersAreNonnullByDefault
import javax.inject.Inject
import de.grobox.transportr.databinding.FragmentDirectionsFormBinding

@ParametersAreNonnullByDefault
class DirectionsFragment : TransportrFragment() {

    @Inject
    internal lateinit var settingsManager: SettingsManager
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DirectionsViewModel
    private lateinit var viewBinding: FragmentDirectionsFormBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentDirectionsFormBinding.inflate(inflater, container, false)
        component.inject(this)

        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(DirectionsViewModel::class.java)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(viewBinding.toolbar)

        val network = viewModel.transportNetwork.value ?: throw IllegalStateException()
        viewBinding.fromLocation.setTransportNetwork(network)
        viewBinding.viaLocation.setTransportNetwork(network)
        viewBinding.toLocation.setTransportNetwork(network)

        viewBinding.fromLocation.type = FROM
        viewBinding.viaLocation.type = VIA
        viewBinding.toLocation.type = TO

        viewBinding.fromLocation.setLocationViewListener(viewModel)
        viewBinding.viaLocation.setLocationViewListener(viewModel)
        viewBinding.toLocation.setLocationViewListener(viewModel)

        viewModel.home.observe(viewLifecycleOwner, {
            viewBinding.fromLocation.setHomeLocation(it)
            viewBinding.viaLocation.setHomeLocation(it)
            viewBinding.toLocation.setHomeLocation(it)
        })
        viewModel.work.observe(viewLifecycleOwner, {
            viewBinding.fromLocation.setWorkLocation(it)
            viewBinding.viaLocation.setWorkLocation(it)
            viewBinding.toLocation.setWorkLocation(it)
        })
        viewModel.locations.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            viewBinding.fromLocation.setFavoriteLocations(it)
            viewBinding.viaLocation.setFavoriteLocations(it)
            viewBinding.toLocation.setFavoriteLocations(it)
        })
        viewModel.fromLocation.observe(viewLifecycleOwner, {
            viewBinding.fromLocation.setLocation(it)
            if (it != null) viewBinding.toLocation.requestFocus()
            onLocationsChanged()
        })
        viewModel.viaLocation.observe(viewLifecycleOwner, { viewBinding.viaLocation.setLocation(it) })
        viewModel.toLocation.observe(viewLifecycleOwner, {
            viewBinding.toLocation.setLocation(it)
            onLocationsChanged()
        })
        viewModel.viaSupported.observe(viewLifecycleOwner, { viewBinding.viaIcon.visibility = if (it) VISIBLE else GONE })
        viewModel.isDeparture.observe(viewLifecycleOwner, { onIsDepartureChanged(it) })
        viewModel.isExpanded.observe(viewLifecycleOwner, { onViaVisibleChanged(it) })
        viewModel.lastQueryCalendar.observe(viewLifecycleOwner, { onCalendarUpdated(it) })
        viewModel.timeUpdate.observe(viewLifecycleOwner, { onCalendarUpdated(viewModel.lastQueryCalendar.value) })
        viewModel.findGpsLocation.observe(viewLifecycleOwner, { updateGpsLocation(it, refresh = false) })
        viewModel.isFavTrip.observe(viewLifecycleOwner, { onFavStatusChanged(it) })
        viewModel.products.observe(viewLifecycleOwner, { onProductsChanged(it) })

        viewBinding.favIcon.setOnClickListener { viewModel.toggleFavTrip() }

        viewBinding.timeBackground.setOnClickListener {
            if (viewModel.lastQueryCalendar.value == null) throw IllegalStateException()
            val fragment = TimeDateFragment.newInstance(viewModel.lastQueryCalendar.value!!, viewModel.isDeparture.value!!)
            fragment.setTimeDateListener(viewModel)
            fragment.show(activity!!.supportFragmentManager, TimeDateFragment.TAG)
        }
        viewBinding.timeBackground.setOnLongClickListener {
            viewModel.resetCalender()
            true
        }

        viewBinding.productsIcon.setOnClickListener {
            activity?.let { a ->
                ProductDialogFragment().show(a.supportFragmentManager, ProductDialogFragment.TAG)
            }
        }
        viewBinding.swapIcon.setOnClickListener { swapLocations() }

        viewBinding.fromLocation.getGpsButton().setOnClickListener {
            if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            ) {
                Toast.makeText(context, context.getString(R.string.permission_denied_gps), Toast.LENGTH_LONG).show()
            } else {
                updateGpsLocation(FROM, refresh = true)
            }
        }

        viewBinding.viaIcon.setOnClickListener { viewModel.toggleIsExpanded() }

        TooltipCompat.setTooltipText(viewBinding.productsIcon, getString(R.string.action_choose_products))
        TooltipCompat.setTooltipText(viewBinding.swapIcon, getString(R.string.action_switch_locations))
        TooltipCompat.setTooltipText(viewBinding.viaIcon, getString(R.string.action_navigation_expand))
    }

    private fun onLocationsChanged() {
        val fromNotEmpty = this.viewModel.fromLocation.value != null
        val toNotEmpty = this.viewModel.toLocation.value != null
        viewBinding.swapIcon.isEnabled = fromNotEmpty || toNotEmpty
        // If icon disabled, grey it out
        viewBinding.swapIcon.alpha = if (viewBinding.swapIcon.isEnabled) 1.0f else 0.5f
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(DATE, viewModel.lastQueryCalendar.value)
        outState.putBoolean(EXPANDED, viewModel.isExpanded.value ?: false)
        outState.putBoolean(DEPARTURE, viewModel.isDeparture.value ?: true)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && viewModel.trips.value == null) {
            viewModel.setIsExpanded(savedInstanceState.getBoolean(EXPANDED, false))
            viewModel.setIsDeparture(savedInstanceState.getBoolean(DEPARTURE, true))
            viewModel.setFromLocation(viewBinding.fromLocation.getLocation())
            viewModel.setViaLocation(viewBinding.viaLocation.getLocation())
            viewModel.setToLocation(viewBinding.toLocation.getLocation())
            viewModel.onTimeAndDateSet(savedInstanceState.getSerializable(DATE) as Calendar)
        }
        (activity!!.supportFragmentManager.findFragmentByTag(TimeDateFragment.TAG) as? TimeDateFragment)?.setTimeDateListener(viewModel)
    }

    private fun onCalendarUpdated(calendar: Calendar?) {
        if (calendar == null) return
        when {
            isNow(calendar) -> {
                viewBinding.time.setText(R.string.now_small)
                viewBinding.date.visibility = GONE
            }
            isToday(calendar) -> {
                val relTime = formatRelativeTime(context, calendar.time, 30)
                viewBinding.time.text = if (relTime.visibility == VISIBLE) relTime.relativeTime else formatTime(context, calendar.time)
                viewBinding.date.visibility = GONE
            }
            else -> {
                viewBinding.time.text = formatTime(context, calendar.time)
                viewBinding.date.text = formatDate(context, calendar.time)
                viewBinding.date.visibility = VISIBLE
            }
        }
    }

    private fun swapLocations() {
        val toToY = viewBinding.fromCard.y - viewBinding.toCard.y
        val slideUp = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f,
            RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, toToY
        ).apply {
            duration = 400
            fillAfter = true
            isFillEnabled = true
        }

        val fromToY = viewBinding.toCard.y - viewBinding.fromCard.y
        val slideDown = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f,
            RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, fromToY
        ).apply {
            duration = 400
            fillAfter = true
            isFillEnabled = true
        }

        viewBinding.fromCard.startAnimation(slideDown)
        viewBinding.toCard.startAnimation(slideUp)

        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                viewModel.swapFromAndToLocations()

                viewBinding.fromCard.clearAnimation()
                viewBinding.toCard.clearAnimation()

                viewModel.search()
            }
        })
    }

    private fun onIsDepartureChanged(isDeparture: Boolean) {
        viewBinding.departure.text = getString(if (isDeparture) R.string.trip_dep else R.string.trip_arr)
    }

    private fun onViaVisibleChanged(viaVisible: Boolean) {
        viewBinding.viaIcon.setImageResource(if (viaVisible) R.drawable.ic_action_navigation_unfold_less_white else R.drawable.ic_action_navigation_unfold_more_white)
        viewBinding.viaCard.visibility = if (viaVisible) VISIBLE else GONE
    }

    private fun updateGpsLocation(type: FavLocationType?, refresh: Boolean) {
        if (type == null) {
            viewModel.locationLiveData.removeObservers(viewLifecycleOwner)
            viewBinding.fromLocation.clearSearching()
            return
        }
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            return
        }
        viewBinding.fromLocation.setSearching()
        if (!refresh) viewBinding.toLocation.requestFocus()
        var counter = 0
        viewModel.locationLiveData.observe(viewLifecycleOwner) { location ->
            counter++
            //when fragment is started (!refresh) we do not need to wait because the location listener has been running before already
            //when refreshing the first location is old and must not be used
            if (!refresh || counter == 3) {
                viewModel.setFromLocation(location)
                viewModel.search()
                viewModel.locationLiveData.removeObservers(viewLifecycleOwner)
            }
        }
    }

    private fun onFavStatusChanged(isFav: Boolean?) {
        if (isFav == null) {
            viewBinding.favIcon.visibility = INVISIBLE
        } else {
            viewBinding.favIcon.visibility = VISIBLE
            viewBinding.favIcon.setImageResource(if (isFav) R.drawable.ic_action_star else R.drawable.ic_action_star_empty)
            val tooltip = getString(if (isFav) R.string.action_unfav_trip else R.string.action_fav_trip)
            viewBinding.favIcon.contentDescription = tooltip
            TooltipCompat.setTooltipText(viewBinding.favIcon, tooltip)
        }
    }

    private fun onProductsChanged(products: EnumSet<Product>) {
        viewBinding.productsMarked.visibility = if (Product.ALL == products) GONE else VISIBLE
    }

}

