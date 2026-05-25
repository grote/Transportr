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
import de.grobox.transportr.databinding.FragmentDirectionsFormBinding
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
import org.jspecify.annotations.NullMarked
import javax.inject.Inject

@NullMarked
class DirectionsFragment : TransportrFragment() {

    private var _binding: FragmentDirectionsFormBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @Inject
    internal lateinit var settingsManager: SettingsManager
    @Inject
    internal lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: DirectionsViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDirectionsFormBinding.inflate(inflater, container, false)
        component.inject(this)

        viewModel = ViewModelProvider(activity!!, viewModelFactory).get(DirectionsViewModel::class.java)

        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar(binding.toolbar)

        val network = viewModel.transportNetwork.value ?: throw IllegalStateException()
        binding.fromLocation.setTransportNetwork(network)
        binding.viaLocation.setTransportNetwork(network)
        binding.toLocation.setTransportNetwork(network)

        binding.fromLocation.type = FROM
        binding.viaLocation.type = VIA
        binding.toLocation.type = TO

        binding.fromLocation.setLocationViewListener(viewModel)
        binding.viaLocation.setLocationViewListener(viewModel)
        binding.toLocation.setLocationViewListener(viewModel)

        viewModel.home.observe(viewLifecycleOwner, {
            binding.fromLocation.setHomeLocation(it)
            binding.viaLocation.setHomeLocation(it)
            binding.toLocation.setHomeLocation(it)
        })
        viewModel.work.observe(viewLifecycleOwner, {
            binding.fromLocation.setWorkLocation(it)
            binding.viaLocation.setWorkLocation(it)
            binding.toLocation.setWorkLocation(it)
        })
        viewModel.locations.observe(viewLifecycleOwner, {
            if (it == null) return@observe
            binding.fromLocation.setFavoriteLocations(it)
            binding.viaLocation.setFavoriteLocations(it)
            binding.toLocation.setFavoriteLocations(it)
        })
        viewModel.fromLocation.observe(viewLifecycleOwner, {
            binding.fromLocation.setLocation(it)
            if (it != null) binding.toLocation.requestFocus()
            onLocationsChanged()
        })
        viewModel.viaLocation.observe(viewLifecycleOwner, {
            binding.viaLocation.setLocation(it)
        })
        viewModel.toLocation.observe(viewLifecycleOwner, {
            binding.toLocation.setLocation(it)
            onLocationsChanged()
        })
        viewModel.viaSupported.observe(viewLifecycleOwner, { binding.viaIcon.visibility = if (it) VISIBLE else GONE })
        viewModel.isDeparture.observe(viewLifecycleOwner, { onIsDepartureChanged(it) })
        viewModel.isExpanded.observe(viewLifecycleOwner, { onViaVisibleChanged(it) })
        viewModel.lastQueryCalendar.observe(viewLifecycleOwner, { onCalendarUpdated(it) })
        viewModel.timeUpdate.observe(viewLifecycleOwner, { onCalendarUpdated(viewModel.lastQueryCalendar.value) })
        viewModel.findGpsLocation.observe(viewLifecycleOwner, { updateGpsLocation(it, refresh = false) })
        viewModel.isFavTrip.observe(viewLifecycleOwner, { onFavStatusChanged(it) })
        viewModel.products.observe(viewLifecycleOwner, { onProductsChanged(it) })

        binding.favIcon.setOnClickListener { viewModel.toggleFavTrip() }

        binding.timeBackground.setOnClickListener {
            if (viewModel.lastQueryCalendar.value == null) throw IllegalStateException()
            val fragment = TimeDateFragment.newInstance(viewModel.lastQueryCalendar.value!!, viewModel.isDeparture.value!!)
            fragment.setTimeDateListener(viewModel)
            fragment.show(activity!!.supportFragmentManager, TimeDateFragment.TAG)
        }
        binding.timeBackground.setOnLongClickListener {
            viewModel.resetCalender()
            true
        }

        binding.productsIcon.setOnClickListener {
            activity?.let { a ->
                ProductDialogFragment().show(a.supportFragmentManager, ProductDialogFragment.TAG)
            }
        }
        binding.swapIcon.setOnClickListener { swapLocations() }

        // todo: how to access gpsButton here?
        binding.fromLocation.ui.gps.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            ) {
                Toast.makeText(context,context.getString(R.string.permission_denied_gps), Toast.LENGTH_LONG).show()
            } else {
                updateGpsLocation(FROM, refresh = true)
            }
        }

        binding.viaIcon.setOnClickListener { viewModel.toggleIsExpanded() }

        TooltipCompat.setTooltipText(binding.productsIcon, getString(R.string.action_choose_products))
        TooltipCompat.setTooltipText(binding.swapIcon, getString(R.string.action_switch_locations))
        TooltipCompat.setTooltipText(binding.viaIcon, getString(R.string.action_navigation_expand))
    }

    private fun onLocationsChanged() {
        val fromNotEmpty = this.viewModel.fromLocation.value != null
        val toNotEmpty = this.viewModel.toLocation.value != null
        binding.swapIcon.isEnabled = fromNotEmpty || toNotEmpty
        // If icon disabled, grey it out
        binding.swapIcon.alpha = if (binding.swapIcon.isEnabled) 1.0f else 0.5f
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
            viewModel.setFromLocation(binding.fromLocation.getLocation())
            viewModel.setViaLocation(binding.viaLocation.getLocation())
            viewModel.setToLocation(binding.toLocation.getLocation())
            viewModel.onTimeAndDateSet(savedInstanceState.getSerializable(DATE) as Calendar)
        }
        (activity!!.supportFragmentManager.findFragmentByTag(TimeDateFragment.TAG) as? TimeDateFragment)?.setTimeDateListener(viewModel)
    }

    private fun onCalendarUpdated(calendar: Calendar?) {
        if (calendar == null) return
        when {
            isNow(calendar) -> {
                binding.time.setText(R.string.now_small)
                binding.date.visibility = GONE
            }
            isToday(calendar) -> {
                val relTime = formatRelativeTime(context, calendar.time, 30)
                binding.time.text = if (relTime.visibility == VISIBLE) relTime.relativeTime else formatTime(context, calendar.time)
                binding.date.visibility = GONE
            }
            else -> {
                binding.time.text = formatTime(context, calendar.time)
                binding.date.text = formatDate(context, calendar.time)
                binding.date.visibility = VISIBLE
            }
        }
    }

    private fun swapLocations() {
        val toToY = binding.fromCard.y - binding.toCard.y
        val slideUp = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f,
            RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, toToY
        ).apply {
            duration = 400
            fillAfter = true
            isFillEnabled = true
        }

        val fromToY = binding.toCard.y - binding.fromCard.y
        val slideDown = TranslateAnimation(
            RELATIVE_TO_SELF, 0.0f, RELATIVE_TO_SELF, 0.0f,
            RELATIVE_TO_SELF, 0.0f, Animation.ABSOLUTE, fromToY
        ).apply {
            duration = 400
            fillAfter = true
            isFillEnabled = true
        }

        binding.fromCard.startAnimation(slideDown)
        binding.toCard.startAnimation(slideUp)

        slideUp.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                viewModel.swapFromAndToLocations()

                binding.fromCard.clearAnimation()
                binding.toCard.clearAnimation()

                viewModel.search()
            }
        })
    }

    private fun onIsDepartureChanged(isDeparture: Boolean) {
        binding.departure.text = getString(if (isDeparture) R.string.trip_dep else R.string.trip_arr)
    }

    private fun onViaVisibleChanged(viaVisible: Boolean) {
        binding.viaIcon?.setImageResource(if (viaVisible) R.drawable.ic_action_navigation_unfold_less_white else R.drawable.ic_action_navigation_unfold_more_white)
        binding.viaCard.visibility = if (viaVisible) VISIBLE else GONE
    }

    private fun updateGpsLocation(type: FavLocationType?, refresh: Boolean) {
        if (type == null) {
            viewModel.locationLiveData.removeObservers(viewLifecycleOwner)
            binding.fromLocation.clearSearching()
            return
        }
        if (ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) {
            return
        }
        binding.fromLocation.setSearching()
        if (!refresh) binding.toLocation.requestFocus()
        var counter = 0
        viewModel.locationLiveData.observe(viewLifecycleOwner) { location ->
            counter ++
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
            binding.favIcon.visibility = INVISIBLE
        } else {
            binding.favIcon.visibility = VISIBLE
            binding.favIcon.setImageResource(if (isFav) R.drawable.ic_action_star else R.drawable.ic_action_star_empty)
            val tooltip = getString(if (isFav) R.string.action_unfav_trip else R.string.action_fav_trip)
            binding.favIcon.contentDescription = tooltip
            TooltipCompat.setTooltipText(binding.favIcon, tooltip)
        }
    }

    private fun onProductsChanged(products: EnumSet<Product>) {
        binding.productsMarked.visibility = if (Product.ALL == products) GONE else VISIBLE
    }

}

