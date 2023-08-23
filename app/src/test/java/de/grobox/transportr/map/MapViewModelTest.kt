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


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.networks.TransportNetworkManager
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class MapViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val application: TransportrApplication = Mockito.mock(TransportrApplication::class.java)
    private val transportNetworkManager: TransportNetworkManager = Mockito.mock(TransportNetworkManager::class.java)
    private val locationRepository: LocationRepository = Mockito.mock(LocationRepository::class.java)
    private val searchesRepository: SearchesRepository = Mockito.mock(SearchesRepository::class.java)
    private val positionController: PositionController = Mockito.mock(PositionController::class.java)

    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        val state = MutableLiveData<PositionController.PositionState>()
        Mockito.`when`(positionController.positionState).thenReturn(state)
        viewModel = MapViewModel(application, transportNetworkManager, locationRepository, searchesRepository, positionController)
    }

    @Test
    fun transportNetworkDidNotChangeInitially() {
        assertFalse(viewModel.transportNetworkWasChanged)
    }

}
