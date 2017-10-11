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

public class HomeLocationTest extends DbTest {

	private LocationDao dao;
	@Mock private Observer<HomeLocation> observer;

	@Before
	public void createDb() {
		super.createDb();
		dao = db.locationDao();
	}

	@Test
	public void insertHomeLocation() throws Exception {
		LiveData<HomeLocation> homeLocationLive = dao.getHomeLocation(DB);
		homeLocationLive.observeForever(observer);

		// no home location should exist
		assertNull(homeLocationLive.getValue());

		// create a complete station location
		Location location = new Location(STATION, "stationId", 23, 42, "place", "name", Product.ALL);
		HomeLocation insert = new HomeLocation(DB, location);
		long uid1 = dao.addHomeLocation(insert);
		verify(observer).onChanged(insert);

		// assert that location has been inserted and retrieved properly
		HomeLocation homeLocation = homeLocationLive.getValue();
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
		insert = new HomeLocation(DB, location);
		dao.addHomeLocation(new HomeLocation(DB, location));
		verify(observer).onChanged(insert);

		// assert that old home location has been replaced properly
		assertEquals(1, dao.countHomes(DB));
		homeLocation = homeLocationLive.getValue();
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
