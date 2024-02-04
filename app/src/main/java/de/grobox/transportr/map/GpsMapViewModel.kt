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

package de.grobox.transportr.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.map.GpsMapViewModel.GpsFabState
import de.grobox.transportr.map.PositionController.PositionState

internal interface GpsMapViewModel {

    val positionController: PositionController

    //todo: change to LiveData
    val isCameraTracking: MutableLiveData<Boolean>
    val isPositionStale: MutableLiveData<Boolean>
    val gpsFabState: LiveData<GpsFabState>

    enum class GpsFabState {
        DISABLED,
        ENABLED,
        TRACKING
    }
}

internal class GpsMapViewModelImpl(override val positionController: PositionController) : GpsMapViewModel {
    override val isCameraTracking = MutableLiveData<Boolean>()
    override val isPositionStale = MutableLiveData<Boolean>()
    override val gpsFabState = MediatorLiveData<GpsFabState>().apply {
        var state = PositionState.DISABLED
        var isTracking = false
        var isStale = false
        fun update() {
            value = when {
                state == PositionState.DENIED || state == PositionState.DISABLED || isStale -> GpsFabState.DISABLED
                state == PositionState.ENABLED && !isTracking -> GpsFabState.ENABLED
                state == PositionState.ENABLED && isTracking -> GpsFabState.TRACKING
                else -> value
            }
        }
        addSource(positionController.positionState) {
            state = it
            update()
        }
        addSource(isCameraTracking) {
            isTracking = it
            update()
        }
        addSource(isPositionStale) {
            isStale = it
            update()
        }
    }
}