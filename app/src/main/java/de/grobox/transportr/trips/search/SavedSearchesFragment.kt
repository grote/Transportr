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

import androidx.lifecycle.ViewModelProvider
import de.grobox.transportr.favorites.locations.HomePickerDialogFragment
import de.grobox.transportr.favorites.locations.WorkPickerDialogFragment
import de.grobox.transportr.favorites.trips.FavoriteTripsFragment
import de.grobox.transportr.locations.LocationsViewModel
import de.grobox.transportr.locations.WrapLocation


class SavedSearchesFragment : FavoriteTripsFragment<DirectionsViewModel>() {

    companion object {
        private val viewModelClass = DirectionsViewModel::class.java
    }

    override fun getViewModel(): DirectionsViewModel {
        component.inject(this)
        return ViewModelProvider(activity!!, viewModelFactory).get(viewModelClass)
    }

    override fun getHomePickerDialogFragment(): HomePickerDialogFragment {
        return HomePickerFragment()
    }

    override fun getWorkPickerDialogFragment(): WorkPickerDialogFragment {
        return WorkPickerFragment()
    }

    override fun onSpecialLocationClicked(location: WrapLocation) {
        viewModel.setToLocation(location)
        viewModel.search()
    }

    class HomePickerFragment : HomePickerDialogFragment() {
        override fun viewModel(): LocationsViewModel {
            return ViewModelProvider(activity!!, viewModelFactory).get(viewModelClass)
        }
    }

    class WorkPickerFragment : WorkPickerDialogFragment() {
        override fun viewModel(): LocationsViewModel {
            return ViewModelProvider(activity!!, viewModelFactory).get(viewModelClass)
        }
    }

}
