package de.grobox.transportr.map


import android.arch.core.executor.testing.InstantTaskExecutorRule
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.networks.TransportNetworkManager
import org.junit.Assert.assertTrue
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
    private val gpsController: GpsController = Mockito.mock(GpsController::class.java)

    private lateinit var viewModel: MapViewModel

    @Before
    fun setUp() {
        viewModel = MapViewModel(application, transportNetworkManager, locationRepository, searchesRepository, gpsController)
    }

    @Test
    fun freshStart() {
        val isFreshStart = viewModel.isFreshStart.value as Boolean
        assertTrue(isFreshStart)
    }

}
