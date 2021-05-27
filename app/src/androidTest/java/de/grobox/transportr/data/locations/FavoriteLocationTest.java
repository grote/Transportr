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

package de.grobox.transportr.data.locations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.grobox.transportr.data.DbTest;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Product;

import static de.schildbach.pte.NetworkId.BVG;
import static de.schildbach.pte.NetworkId.DB;
import static de.schildbach.pte.dto.LocationType.ADDRESS;
import static de.schildbach.pte.dto.LocationType.ANY;
import static de.schildbach.pte.dto.LocationType.POI;
import static de.schildbach.pte.dto.LocationType.STATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class FavoriteLocationTest extends DbTest {

	private LocationDao dao;

	@Before
	@Override
	public void createDb() throws Exception {
		super.createDb();
		dao = db.locationDao();
	}

	@Test
	public void insertFavoriteLocation() throws Exception {
		// no locations should exist
		assertNotNull(getValue(dao.getFavoriteLocations(DB)));

		// create a complete station location
		Location loc1 = new Location(STATION, "stationId", Point.from1E6(23, 42), "place", "name", Product.ALL);
		long uid1 = dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));

		// assert that location has been inserted and retrieved properly
		List<FavoriteLocation> locations1 = getValue(dao.getFavoriteLocations(DB));
		assertEquals(1, locations1.size());
		FavoriteLocation f1 = locations1.get(0);
		assertEquals(uid1, f1.getUid());
		assertEquals(DB, f1.getNetworkId());
		assertEquals(loc1.type, f1.type);
		assertEquals(loc1.id, f1.id);
		assertEquals(loc1.getLatAs1E6(), f1.lat);
		assertEquals(loc1.getLonAs1E6(), f1.lon);
		assertEquals(loc1.place, f1.place);
		assertEquals(loc1.name, f1.name);
		assertEquals(loc1.products, f1.products);

		// insert a second location in a different network
		Location loc2 = new Location(ANY, null, Point.from1E6(1337, 0), null, null, Product.fromCodes("ISB".toCharArray()));
		long uid2 = dao.addFavoriteLocation(new FavoriteLocation(BVG, loc2));

		// assert that location has been inserted and retrieved properly
		List<FavoriteLocation> locations2 = getValue(dao.getFavoriteLocations(BVG));
		assertEquals(1, locations2.size());
		FavoriteLocation f2 = locations2.get(0);
		assertEquals(uid2, f2.getUid());
		assertEquals(BVG, f2.getNetworkId());
		assertEquals(loc2.type, f2.type);
		assertEquals(loc2.id, f2.id);
		assertEquals(loc2.getLatAs1E6(), f2.lat);
		assertEquals(loc2.getLonAs1E6(), f2.lon);
		assertEquals(loc2.place, f2.place);
		assertEquals(loc2.name, f2.name);
		assertEquals(loc2.products, f2.products);
	}

	@Test
	public void replaceFavoriteLocation() throws Exception {
		// create a complete station location
		Location loc1 = new Location(STATION, "stationId", Point.from1E6(23, 42), "place", "name", Product.ALL);
		long uid1 = dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));

		// retrieve favorite location
		List<FavoriteLocation> locations1 = getValue(dao.getFavoriteLocations(DB));
		assertEquals(1, locations1.size());
		FavoriteLocation f1 = locations1.get(0);
		assertEquals(uid1, f1.getUid());

		// change the favorite location and replace it in the DB
		f1.place = "new place";
		f1.name = "new name";
		f1.products = null;
		uid1 = dao.addFavoriteLocation(f1);
		assertEquals(uid1, f1.getUid());

		// retrieve favorite location again
		List<FavoriteLocation> locations2 = getValue(dao.getFavoriteLocations(DB));
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
		Location loc1 = new Location(ADDRESS, null, Point.from1E6(23, 42), null, "name1", null);
		Location loc2 = new Location(ADDRESS, null, Point.from1E6(0, 0), null, "name2", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc2));
		assertEquals(2, getValue(dao.getFavoriteLocations(DB)).size());
	}

	@Test
	public void twoLocationsWithSameId() throws Exception {
		Location loc1 = new Location(ADDRESS, "test", Point.from1E6(23, 42), null, "name1", null);
		Location loc2 = new Location(ADDRESS, "test", Point.from1E6(0, 0), null, "name2", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc2));

		// second location should override first one and don't create a new one
		List<FavoriteLocation> locations = getValue(dao.getFavoriteLocations(DB));
		assertEquals(1, locations.size());
		FavoriteLocation f = locations.get(0);
		assertEquals(loc2.getLatAs1E6(), f.lat);
		assertEquals(loc2.getLonAs1E6(), f.lon);
		assertEquals(loc2.name, f.name);
	}

	@Test
	public void twoLocationsWithSameIdDifferentNetworks() throws Exception {
		Location loc1 = new Location(ADDRESS, "test", Point.from1E6(23, 42), null, "name1", null);
		Location loc2 = new Location(ADDRESS, "test", Point.from1E6(0, 0), null, "name2", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));
		dao.addFavoriteLocation(new FavoriteLocation(BVG, loc2));

		// second location should not override first one
		assertEquals(1, getValue(dao.getFavoriteLocations(DB)).size());
		assertEquals(1, getValue(dao.getFavoriteLocations(BVG)).size());
	}

	@Test
	public void getFavoriteLocationByUid() throws Exception {
		// insert a minimal location
		Location l1 = new Location(STATION, "id", Point.from1E6(1, 1), "place", "name", null);
		FavoriteLocation f1 = new FavoriteLocation(DB, l1);
		long uid = dao.addFavoriteLocation(f1);

		// retrieve by UID
		FavoriteLocation f2 = dao.getFavoriteLocation(uid);

		// assert that retrieval worked
		assertNotNull(f2);
		assertEquals(uid, f2.getUid());
		assertEquals(DB, f2.getNetworkId());
		assertEquals(l1.type, f2.type);
		assertEquals(l1.id, f2.id);
		assertEquals(l1.getLatAs1E6(), f2.lat);
		assertEquals(l1.getLonAs1E6(), f2.lon);
		assertEquals(l1.place, f2.place);
		assertEquals(l1.name, f2.name);
		assertEquals(l1.products, f2.products);
	}

	@Test
	public void getFavoriteLocationByValues() {
		// insert a minimal location
		Location loc1 = new Location(ANY, null, Point.from1E6(0, 0), null, null, null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc1));

		// assert the exists check works
		assertNotNull(dao.getFavoriteLocation(DB, loc1.type, loc1.id, loc1.getLatAs1E6(), loc1.getLonAs1E6(), loc1.place, loc1.name));
		assertNull(dao.getFavoriteLocation(DB, ADDRESS, loc1.id, loc1.getLatAs1E6(), loc1.getLonAs1E6(), loc1.place, loc1.name));
		assertNull(dao.getFavoriteLocation(DB, loc1.type, "id", loc1.getLatAs1E6(), loc1.getLonAs1E6(), loc1.place, loc1.name));
		assertNull(dao.getFavoriteLocation(DB, loc1.type, loc1.id, 1, loc1.getLonAs1E6(), loc1.place, loc1.name));
		assertNull(dao.getFavoriteLocation(DB, loc1.type, loc1.id, loc1.getLatAs1E6(), 1, loc1.place, loc1.name));
		assertNull(dao.getFavoriteLocation(DB, loc1.type, loc1.id, loc1.getLatAs1E6(), loc1.getLonAs1E6(), "place", loc1.name));
		assertNull(dao.getFavoriteLocation(DB, loc1.type, loc1.id, loc1.getLatAs1E6(), loc1.getLonAs1E6(), loc1.place, "name"));

		// insert a maximal location
		Location loc2 = new Location(STATION, "id", Point.from1E6(1, 1), "place", "name", null);
		dao.addFavoriteLocation(new FavoriteLocation(DB, loc2));

		// assert the exists check works
		assertNotNull(dao.getFavoriteLocation(DB, loc2.type, loc2.id, loc2.getLatAs1E6(), loc2.getLonAs1E6(), loc2.place, loc2.name));
		assertNull(dao.getFavoriteLocation(DB, POI, loc2.id, loc2.getLatAs1E6(), loc2.getLonAs1E6(), loc2.place, loc2.name));
		assertNull(dao.getFavoriteLocation(DB, loc2.type, "oid", loc2.getLatAs1E6(), loc2.getLonAs1E6(), loc2.place, loc2.name));
		assertNull(dao.getFavoriteLocation(DB, loc2.type, loc2.id, 42, loc2.getLonAs1E6(), loc2.place, loc2.name));
		assertNull(dao.getFavoriteLocation(DB, loc2.type, loc2.id, loc2.getLatAs1E6(), 42, loc2.place, loc2.name));
		assertNull(dao.getFavoriteLocation(DB, loc2.type, loc2.id, loc2.getLatAs1E6(), loc2.getLonAs1E6(), "oplace", loc2.name));
		assertNull(dao.getFavoriteLocation(DB, loc2.type, loc2.id, loc2.getLatAs1E6(), loc2.getLonAs1E6(), loc2.place, "oname"));
	}

}
