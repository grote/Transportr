package de.grobox.transportr.data.locations;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.grobox.transportr.data.DbTest;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;

import static de.schildbach.pte.NetworkId.DB;
import static de.schildbach.pte.dto.LocationType.ADDRESS;
import static de.schildbach.pte.dto.LocationType.STATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class HomeLocationTest extends DbTest {

	private LocationDao dao;

	@Before
	public void createDb() throws Exception {
		super.createDb();
		dao = db.locationDao();
	}

	@Test
	public void insertHomeLocation() throws Exception {
		// no home location should exist
		assertNull(getValue(dao.getHomeLocation(DB)));

		// create a complete station location
		Location location = new Location(STATION, "stationId", 23, 42, "place", "name", Product.ALL);
		long uid1 = dao.addHomeLocation(new HomeLocation(DB, location));

		// assert that location has been inserted and retrieved properly
		HomeLocation homeLocation = getValue(dao.getHomeLocation(DB));
		assertNotNull(homeLocation);
		assertEquals(uid1, homeLocation.getUid());
		assertEquals(DB, homeLocation.getNetworkId());
		assertEquals(location.type, homeLocation.type);
		assertEquals(location.id, homeLocation.id);
		assertEquals(location.lat, homeLocation.lat);
		assertEquals(location.lon, homeLocation.lon);
		assertEquals(location.place, homeLocation.place);
		assertEquals(location.name, homeLocation.name);
		assertEquals(location.products, homeLocation.products);

		// create a different home location
		location = new Location(ADDRESS, null, 1337, 0, "place2", "name2", null);
		dao.addHomeLocation(new HomeLocation(DB, location));

		// assert that old home location has been replaced properly
		assertEquals(1, dao.countHomes(DB));
		homeLocation = getValue(dao.getHomeLocation(DB));
		assertNotNull(homeLocation);
		assertEquals(DB, homeLocation.getNetworkId());
		assertEquals(location.type, homeLocation.type);
		assertEquals(location.id, homeLocation.id);
		assertEquals(location.lat, homeLocation.lat);
		assertEquals(location.lon, homeLocation.lon);
		assertEquals(location.place, homeLocation.place);
		assertEquals(location.name, homeLocation.name);
		assertEquals(location.products, homeLocation.products);
	}

}
