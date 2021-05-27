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

package de.grobox.transportr.favorites.locations

import android.content.DialogInterface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mikepenz.materialize.util.KeyboardUtil
import de.grobox.transportr.AppComponent
import de.grobox.transportr.R
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.favorites.trips.FavoriteTripListener
import de.grobox.transportr.locations.LocationView
import de.grobox.transportr.locations.LocationsViewModel
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.settings.SettingsManager
import javax.annotation.ParametersAreNonnullByDefault
import javax.inject.Inject

@ParametersAreNonnullByDefault
abstract class SpecialLocationFragment : DialogFragment(), LocationView.LocationViewListener {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    internal lateinit var settingsManager: SettingsManager

    protected lateinit var viewModel: LocationsViewModel
    var listener: FavoriteTripListener? = null

    private lateinit var loc: LocationView

    @get:StringRes
    protected abstract val hint: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inject((activity!!.application as TransportrApplication).component)
        setStyle(
            STYLE_NO_TITLE,
            R.style.SetHomeDialogTheme
        )
    }

    protected abstract fun inject(component: AppComponent)
    protected abstract fun viewModel(): LocationsViewModel  // a method to init when activity has been created

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_special_location, container)

        // Initialize LocationView
        loc = v.findViewById(R.id.location_input)
        loc.setHint(hint)
        loc.setLocationViewListener(this)

        // Get view model and observe data
        viewModel = viewModel().apply {
            transportNetwork.observe(viewLifecycleOwner, Observer {
                    transportNetwork -> transportNetwork?.let { loc.setTransportNetwork(it) }
            })
            locations.observe(viewLifecycleOwner, Observer { favoriteLocations ->
                favoriteLocations?.let {
                    loc.setFavoriteLocations(it)
                    loc.post { loc.onClick() }  // don't know why this only works when posted
                }
            })
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dialog?.setCanceledOnTouchOutside(true)

        // set width to match parent and show keyboard
        val window = dialog?.window
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            window.setGravity(Gravity.TOP)
            window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        KeyboardUtil.hideKeyboard(activity)
    }

    override fun onLocationItemClick(loc: WrapLocation, type: FavLocationType) {
        onSpecialLocationSet(loc)
        dialog?.cancel()
    }

    protected abstract fun onSpecialLocationSet(location: WrapLocation)

    override fun onLocationCleared(type: FavLocationType) {}

}
