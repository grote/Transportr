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

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Animation.RELATIVE_TO_SELF
import android.view.animation.TranslateAnimation
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.FROM
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.TO
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType.VIA
import de.grobox.transportr.databinding.FragmentDirectionsFormBinding
import de.grobox.transportr.locations.LocationGpsView
import de.grobox.transportr.locations.LocationView
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

@ParametersAreNonnullByDefault
class DirectionsFragment : TransportrFragment() {

    @Inject
    internal lateinit var settingsManager: SettingsManager
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DirectionsViewModel

    private var _binding: FragmentDirectionsFormBinding? = null
    private val binding get() = _binding!!

    private lateinit var fromLocation: LocationGpsView
    private lateinit var viaLocation: LocationView
    private lateinit var toLocation: LocationView
    private lateinit var fromCard: View
    private lateinit var viaCard: View
    private lateinit var toCard: View
    private lateinit var toolbar: Toolbar
    private lateinit var timeBackground: View
    private lateinit var time: TextView
    private lateinit var date: TextView
    private lateinit var departure: TextView
    private lateinit var favIcon: ImageButton
    private lateinit var productsIcon: View
    private lateinit var productsMarked: View
    private lateinit var swapIcon: View
    private lateinit var viaIcon: ImageButton



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDirectionsFormBinding.inflate(inflater, container, false)
        component.inject(this)

        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(DirectionsViewModel::class.java)
        fromLocation = binding.fromLocation
        viaLocation = binding.viaLocation
        toLocation = binding.toLocation
        fromCard = binding.fromCard
        viaCard = binding.viaCard
        toCard = binding.toCard
        toolbar = binding.toolbar
        timeBackground = binding.timeBackground
        time = binding.time
        date = binding.date
        departure = binding.departure
        favIcon = binding.favIcon
        productsIcon = binding.productsIcon
        productsMarked = binding.productsMarked
        swapIcon = binding.swapIcon
        viaIcon = binding.viaIcon

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        viewModel.home.observe(viewLifecycleOwner, {
            fromLocation.setHomeLocation(it)
            viaLocation.setHomeLocation(it)
            toLocation.setHomeLocation(it)
        })
        viewModel.work.observe(viewLifecycleOwner, {
            fromLocation.setWorkLocation(it)
            viaLocation.setWorkLocation(it)
            toLocation.setWorkLocation(it)
        })
        viewModel.locations.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            fromLocation.setFavoriteLocations(it)
            viaLocation.setFavoriteLocations(it)
            toLocation.setFavoriteLocations(it)
        })
        viewModel.fromLocation.observe(viewLifecycleOwner, {
            fromLocation.setLocation(it)
            if (it != null) toLocation.requestFocus()
            onLocationsChanged()
        })
        viewModel.viaLocation.observe(viewLifecycleOwner, { viaLocation.setLocation(it) })
        viewModel.toLocation.observe(viewLifecycleOwner, {
            toLocation.setLocation(it)
            onLocationsChanged()
        })
        viewModel.viaSupported.observe(viewLifecycleOwner, { viaIcon.visibility = if (it) VISIBLE else GONE })
        viewModel.isDeparture.observe(viewLifecycleOwner, { onIsDepartureChanged(it) })
        viewModel.isExpanded.observe(viewLifecycleOwner, { onViaVisibleChanged(it) })
        viewModel.lastQueryCalendar.observe(viewLifecycleOwner, { onCalendarUpdated(it) })
        viewModel.timeUpdate.observe(viewLifecycleOwner, { onCalendarUpdated(viewModel.lastQueryCalendar.value) })
        viewModel.findGpsLocation.observe(viewLifecycleOwner, { onFindGpsLocation(it) })
        viewModel.isFavTrip.observe(viewLifecycleOwner, { onFavStatusChanged(it) })
        viewModel.products.observe(viewLifecycleOwner, { onProductsChanged(it) })

        favIcon.setOnClickListener { viewModel.toggleFavTrip() }

        timeBackground.setOnClickListener {
            if (viewModel.lastQueryCalendar.value == null) throw IllegalStateException()
            val fragment = TimeDateFragment.newInstance(viewModel.lastQueryCalendar.value!!, viewModel.isDeparture.value!!)
            fragment.setTimeDateListener(viewModel)
            fragment.show(activity!!.supportFragmentManager, TimeDateFragment.TAG)
        }
        timeBackground.setOnLongClickListener {
            viewModel.resetCalender()
            true
        }

        productsIcon.setOnClickListener {
            activity?.let { a ->
                ProductDialogFragment().show(a.supportFragmentManager, ProductDialogFragment.TAG)
            }
        }
        swapIcon.setOnClickListener { swapLocations() }
        viaIcon.setOnClickListener { viewModel.toggleIsExpanded() }

        TooltipCompat.setTooltipText(productsIcon, getString(R.string.action_choose_products))
        TooltipCompat.setTooltipText(swapIcon, getString(R.string.action_switch_locations))
        TooltipCompat.setTooltipText(viaIcon, getString(R.string.action_navigation_expand))
    }

    private fun onLocationsChanged() {
        val fromNotEmpty = this.viewModel.fromLocation.value != null
        val toNotEmpty = this.viewModel.toLocation.value != null
        swapIcon.isEnabled = fromNotEmpty || toNotEmpty
        // If icon disabled, grey it out
        swapIcon.alpha = if (swapIcon.isEnabled) 1.0f else 0.5f
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
            viewModel.setFromLocation(fromLocation.getLocation())
            viewModel.setViaLocation(viaLocation.getLocation())
            viewModel.setToLocation(toLocation.getLocation())
            viewModel.onTimeAndDateSet(savedInstanceState.getSerializable(DATE) as Calendar)
        }
        (activity!!.supportFragmentManager.findFragmentByTag(TimeDateFragment.TAG) as? TimeDateFragment)?.setTimeDateListener(viewModel)
    }

    private fun onCalendarUpdated(calendar: Calendar?) {
        if (calendar == null) return
        when {
            isNow(calendar) -> {
                time.setText(R.string.now_small)
                date.visibility = GONE
            }
            isToday(calendar) -> {
                val relTime = formatRelativeTime(context, calendar.time, 30)
                time.text = if (relTime.visibility == VISIBLE) relTime.relativeTime else formatTime(context, calendar.time)
                date.visibility = GONE
            }
            else -> {
                time.text = formatTime(context, calendar.time)
                date.text = formatDate(context, calendar.time)
                date.visibility = VISIBLE
            }
        }
    }

    private fun swapLocations() {
        val toToY = fromCard.y - toCard.y
        val slideUp = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f,
            RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, toToY
        ).apply {
            duration = 400
            fillAfter = true
            isFillEnabled = true
        }

        val fromToY = toCard.y - fromCard.y
        val slideDown = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f,
            RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, fromToY
        ).apply {
            duration = 400
            fillAfter = true
            isFillEnabled = true
        }

        fromCard.startAnimation(slideDown)
        toCard.startAnimation(slideUp)

        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                viewModel.swapFromAndToLocations()

                fromCard.clearAnimation()
                toCard.clearAnimation()

                viewModel.search()
            }
        })
    }

    private fun onIsDepartureChanged(isDeparture: Boolean) {
        departure.text = getString(if (isDeparture) R.string.trip_dep else R.string.trip_arr)
    }

    private fun onViaVisibleChanged(viaVisible: Boolean) {
        viaIcon?.setImageResource(if (viaVisible) R.drawable.ic_action_navigation_unfold_less_white else R.drawable.ic_action_navigation_unfold_more_white)
        viaCard.visibility = if (viaVisible) VISIBLE else GONE
    }

    private fun onFindGpsLocation(type: FavLocationType?) {
        if (type == null) {
            viewModel.locationLiveData.removeObservers(viewLifecycleOwner)
            fromLocation.clearSearching()
            return
        }
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            return
        }
        fromLocation.setSearching()
        toLocation.requestFocus()
        viewModel.locationLiveData.observe(viewLifecycleOwner) { location ->
            viewModel.setFromLocation(location)
            viewModel.search()
            viewModel.locationLiveData.removeObservers(viewLifecycleOwner)
        }
    }

    private fun onFavStatusChanged(isFav: Boolean?) {
        if (isFav == null) {
            favIcon.visibility = INVISIBLE
        } else {
            favIcon.visibility = VISIBLE
            favIcon.setImageResource(if (isFav) R.drawable.ic_action_star else R.drawable.ic_action_star_empty)
            val tooltip = getString(if (isFav) R.string.action_unfav_trip else R.string.action_fav_trip)
            favIcon.contentDescription = tooltip
            TooltipCompat.setTooltipText(favIcon, tooltip)
        }
    }

    private fun onProductsChanged(products: EnumSet<Product>) {
        productsMarked.visibility = if (Product.ALL == products) GONE else VISIBLE
    }

}

