package de.grobox.liberario.data;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Product;

import static de.schildbach.pte.NetworkId.BVG;
import static de.schildbach.pte.NetworkId.DB;
import static de.schildbach.pte.dto.LocationType.ADDRESS;
import static de.schildbach.pte.dto.LocationType.ANY;
import static de.schildbach.pte.dto.LocationType.POI;
import static de.schildbach.pte.dto.LocationType.STATION;
import static org.junit.Assert.assertEquals;

public class LocationDaoTest extends DbTest {

	private LocationDao dao;

	@Before
	public void createDb() {
		super.createDb();
		dao = db.locationDao();
	}

	@Test
	public void insertFavoriteLocation() throws Exception {
		// no locations should exist
		assertEquals(0, dao.getFavoriteLocations(DB).size());

		// create a complete station location
		Location loc1 = new Location(STATION, "stationId", 23, 42, "place", "name", Product.ALL);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));

		// assert that location has been inserted and retrieved properly
		List<FavoriteLocation> locations1 = dao.getFavoriteLocations(DB);
		assertEquals(1, locations1.size());
		FavoriteLocation f1 = locations1.get(0);
		assertEquals(1, f1.getUid());
		assertEquals(DB, f1.networkId);
		assertEquals(loc1.type, f1.type);
		assertEquals(loc1.id, f1.id);
		assertEquals(loc1.lat, f1.lat);
		assertEquals(loc1.lon, f1.lon);
		assertEquals(loc1.place, f1.place);
		assertEquals(loc1.name, f1.name);
		assertEquals(loc1.products, f1.products);

		// insert a second location in a different network
		Location loc2 = new Location(ANY, null, 1337, 0, null, null, Product.fromCodes("ISB".toCharArray()));
		dao.addFavoriteLocation(new FavoriteLocation(BVG, loc2));

		// assert that location has been inserted and retrieved properly
		List<FavoriteLocation> locations2 = dao.getFavoriteLocations(BVG);
		assertEquals(1, locations2.size());
		FavoriteLocation f2 = locations2.get(0);
		assertEquals(2, f2.getUid());
		assertEquals(BVG, f2.networkId);
		assertEquals(loc2.type, f2.type);
		assertEquals(loc2.id, f2.id);
		assertEquals(loc2.lat, f2.lat);
		assertEquals(loc2.lon, f2.lon);
		assertEquals(loc2.place, f2.place);
		assertEquals(loc2.name, f2.name);
		assertEquals(loc2.products, f2.products);
	}

	@Test
	public void replaceFavoriteLocation() throws Exception {
		// create a complete station location
		Location loc1 = new Location(STATION, "stationId", 23, 42, "place", "name", Product.ALL);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));

		// retrieve favorite location
		List<FavoriteLocation> locations1 = dao.getFavoriteLocations(DB);
		assertEquals(1, locations1.size());
		FavoriteLocation f1 = locations1.get(0);

		// change the favorite location and replace it in the DB
		f1.place = "new place";
		f1.name = "new name";
		f1.products = null;
		dao.addFavoriteLocation(f1);

		// retrieve favorite location again
		List<FavoriteLocation> locations2 = dao.getFavoriteLocations(DB);
		assertEquals(1, locations2.size());
		FavoriteLocation f2 = locations2.get(0);

		// assert that same location was retrieved and data changed
		assertEquals(f1.getUid(), f2.getUid());
		assertEquals(f1.place, f2.place);
		assertEquals(f1.name, f2.name);
		assertEquals(f1.products, f2.products);
	}

	@Test
	public void twoLocationsWithoutId() throws Exception {
		Location loc1 = new Location(ADDRESS, null, 23, 42, null, "name1", null);
		Location loc2 = new Location(ADDRESS, null, 0, 0, null, "name2", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc2));
		assertEquals(2, dao.getFavoriteLocations(DB).size());
	}

	@Test
	public void twoLocationsWithSameId() throws Exception {
		Location loc1 = new Location(ADDRESS, "test", 23, 42, null, "name1", null);
		Location loc2 = new Location(ADDRESS, "test", 0, 0, null, "name2", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc2));

		// second location should override first one and don't create a new one
		List<FavoriteLocation> locations = dao.getFavoriteLocations(DB);
		assertEquals(1, locations.size());
		FavoriteLocation f = locations.get(0);
		assertEquals(loc2.lat, f.lat);
		assertEquals(loc2.lon, f.lon);
		assertEquals(loc2.name, f.name);
	}

	@Test
	public void twoLocationsWithSameIdDifferentNetworks() throws Exception {
		Location loc1 = new Location(ADDRESS, "test", 23, 42, null, "name1", null);
		Location loc2 = new Location(ADDRESS, "test", 0, 0, null, "name2", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));
		dao.addFavoriteLocation(new FavoriteLocation(BVG, loc2));

		// second location should not override first one
		List<FavoriteLocation> locations = dao.getFavoriteLocations(DB);
		assertEquals(1, dao.getFavoriteLocations(DB).size());
		assertEquals(1, dao.getFavoriteLocations(BVG).size());
	}

	@Test
	public void locationExists() throws Exception {
		// insert a minimal location
		Location loc1 = new Location(ANY, null, 0, 0, null, null, null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));

		// assert the exists check works
		assertEquals(1, dao.exists(DB, loc1.type, loc1.id, loc1.lat, loc1.lon, loc1.place, loc1.name));
		assertEquals(0, dao.exists(DB, ADDRESS, loc1.id, loc1.lat, loc1.lon, loc1.place, loc1.name));
		assertEquals(0, dao.exists(DB, loc1.type, "id", loc1.lat, loc1.lon, loc1.place, loc1.name));
		assertEquals(0, dao.exists(DB, loc1.type, loc1.id, 1, loc1.lon, loc1.place, loc1.name));
		assertEquals(0, dao.exists(DB, loc1.type, loc1.id, loc1.lat, 1, loc1.place, loc1.name));
		assertEquals(0, dao.exists(DB, loc1.type, loc1.id, loc1.lat, loc1.lon, "place", loc1.name));
		assertEquals(0, dao.exists(DB, loc1.type, loc1.id, loc1.lat, loc1.lon, loc1.place, "name"));

		// insert a maximal location
		Location loc2 = new Location(STATION, "id", 1, 1, "place", "name", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc2));

		// assert the exists check works
		assertEquals(1, dao.exists(DB, loc2.type, loc2.id, loc2.lat, loc2.lon, loc2.place, loc2.name));
		assertEquals(0, dao.exists(DB, POI, loc2.id, loc2.lat, loc2.lon, loc2.place, loc2.name));
		assertEquals(0, dao.exists(DB, loc2.type, "oid", loc2.lat, loc2.lon, loc2.place, loc2.name));
		assertEquals(0, dao.exists(DB, loc2.type, loc2.id, 42, loc2.lon, loc2.place, loc2.name));
		assertEquals(0, dao.exists(DB, loc2.type, loc2.id, loc2.lat, 42, loc2.place, loc2.name));
		assertEquals(0, dao.exists(DB, loc2.type, loc2.id, loc2.lat, loc2.lon, "oplace", loc2.name));
		assertEquals(0, dao.exists(DB, loc2.type, loc2.id, loc2.lat, loc2.lon, loc2.place, "oname"));
	}

}
