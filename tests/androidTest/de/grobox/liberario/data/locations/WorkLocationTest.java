package de.grobox.liberario.data.locations;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import de.grobox.liberario.data.DbTest;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;

import static de.schildbach.pte.NetworkId.DB;
import static de.schildbach.pte.dto.LocationType.ADDRESS;
import static de.schildbach.pte.dto.LocationType.STATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

public class WorkLocationTest extends DbTest {

	private LocationDao dao;
	@Mock private Observer<WorkLocation> observer;

	@Before
	public void createDb() {
		super.createDb();
		dao = db.locationDao();
	}

	@Test
	public void insertWorkLocation() throws Exception {
		LiveData<WorkLocation> workLocationLive = dao.getWorkLocation(DB);
		workLocationLive.observeForever(observer);

		// no home location should exist
		assertNull(workLocationLive.getValue());

		// create a complete station location
		Location location = new Location(STATION, "stationId", 23, 42, "place", "name", Product.ALL);
		WorkLocation insert = new WorkLocation(DB, location);
		long uid1 = dao.addWorkLocation(insert);
		verify(observer).onChanged(insert);

		// assert that location has been inserted and retrieved properly
		WorkLocation workLocation = workLocationLive.getValue();
		assertNotNull(workLocation);
		assertEquals(uid1, workLocation.getUid());
		assertEquals(DB, workLocation.getNetworkId());
		assertEquals(location.type, workLocation.type);
		assertEquals(location.id, workLocation.id);
		assertEquals(location.lat, workLocation.lat);
		assertEquals(location.lon, workLocation.lon);
		assertEquals(location.place, workLocation.place);
		assertEquals(location.name, workLocation.name);
		assertEquals(location.products, workLocation.products);

		// create a different home location
		location = new Location(ADDRESS, null, 1337, 0, "place2", "name2", null);
		dao.addWorkLocation(new WorkLocation(DB, location));
		verify(observer).onChanged(insert);

		// assert that old home location has been replaced properly
		assertEquals(1, dao.countWorks(DB));
		workLocation = workLocationLive.getValue();
		assertNotNull(workLocation);
		assertEquals(DB, workLocation.getNetworkId());
		assertEquals(location.type, workLocation.type);
		assertEquals(location.id, workLocation.id);
		assertEquals(location.lat, workLocation.lat);
		assertEquals(location.lon, workLocation.lon);
		assertEquals(location.place, workLocation.place);
		assertEquals(location.name, workLocation.name);
		assertEquals(location.products, workLocation.products);
	}

}
