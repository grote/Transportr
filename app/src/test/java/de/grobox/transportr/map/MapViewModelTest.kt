package de.grobox.transportr.map


import com.mapbox.mapboxsdk.geometry.LatLng
import de.grobox.transportr.TransportrApplication
import de.grobox.transportr.data.locations.LocationRepository
import de.grobox.transportr.data.searches.SearchesRepository
import de.grobox.transportr.locations.WrapLocation
import de.grobox.transportr.networks.TransportNetworkManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class MapViewModelTest {

    private val application: TransportrApplication = Mockito.mock(TransportrApplication::class.java)
    private val transportNetworkManager: TransportNetworkManager = Mockito.mock(TransportNetworkManager::class.java)
    private val locationRepository: LocationRepository = Mockito.mock(LocationRepository::class.java)
    private val searchesRepository: SearchesRepository = Mockito.mock(SearchesRepository::class.java)
    private val gpsController: GpsController = Mockito.mock(GpsController::class.java)

    private val viewModel = MapViewModel(application, transportNetworkManager, locationRepository, searchesRepository, gpsController)

    @Test
    fun getWrapLocation() {
        assertEquals(get(1.0, 1.0), viewModel.getWrapLocation("geo:1,1"))
        assertEquals(get(-90.0, -126.0), viewModel.getWrapLocation("geo:-90,-126"))
        assertEquals(get(3.14159265, -3.14159265), viewModel.getWrapLocation("geo:3.14159265,-3.14159265?z=20"))
        assertEquals(get(-48.123, 126.0), viewModel.getWrapLocation("geo:-48.123,126(label)"))
        assertEquals(get(90.0, -126.0), viewModel.getWrapLocation("geo:90,-126?q=my+street+address"))

        assertNull(viewModel.getWrapLocation("geo:90"))
        assertNull(viewModel.getWrapLocation("geo:90,"))
        assertNull(viewModel.getWrapLocation("geo:90,.23"))
        assertNull(viewModel.getWrapLocation("geo:,23"))
        assertNull(viewModel.getWrapLocation("geo:0,0"))
    }

    private operator fun get(lat: Double, lon: Double): WrapLocation {
        return WrapLocation(LatLng(lat, lon))
    }

}
