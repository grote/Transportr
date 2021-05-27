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

package de.grobox.transportr.data.searches;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.List;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import de.grobox.transportr.data.DbTest;
import de.grobox.transportr.data.locations.FavoriteLocation;
import de.grobox.transportr.data.locations.LocationDao;
import de.schildbach.pte.dto.Location;

import static de.schildbach.pte.NetworkId.DB;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StoredSearchTest extends DbTest {

	private SearchesDao dao;

	private FavoriteLocation f1, f2, f3;

	@Before
	@Override
	public void createDb() throws Exception {
		super.createDb();
		dao = db.searchesDao();
		LocationDao locationDao = db.locationDao();

		// create simple locations
		FavoriteLocation fTmp1 = new FavoriteLocation(DB, Location.coord(1, 1));
		FavoriteLocation fTmp2 = new FavoriteLocation(DB, Location.coord(2, 2));
		FavoriteLocation fTmp3 = new FavoriteLocation(DB, Location.coord(3, 3));
		long uid1 = locationDao.addFavoriteLocation(fTmp1);
		long uid2 = locationDao.addFavoriteLocation(fTmp2);
		long uid3 = locationDao.addFavoriteLocation(fTmp3);
		List<FavoriteLocation> locations = getValue(locationDao.getFavoriteLocations(DB));
		assertEquals(3, locations.size());
		f1 = locations.get(0);
		f2 = locations.get(1);
		f3 = locations.get(2);
		assertEquals(uid1, f1.getUid());
		assertEquals(uid2, f2.getUid());
		assertEquals(uid3, f3.getUid());
	}

	@Test
	public void getStoredSearches() throws Exception {
		// no stored searches should exist in empty DB
		assertEquals(0, getValue(dao.getStoredSearches(DB)).size());

		// store a new search
		StoredSearch madeSearch = new StoredSearch(DB, f1, null, f3);
		long uid1 = dao.storeSearch(madeSearch);

		// assert that search was stored and retrieved properly
		List<StoredSearch> storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(1, storedSearches.size());
		StoredSearch storedSearch = storedSearches.get(0);
		assertEquals(uid1, storedSearch.getUid());
		assertEquals(madeSearch.getNetworkId(), storedSearch.getNetworkId());
		assertEquals(madeSearch.fromId, storedSearch.fromId);
		assertEquals(madeSearch.viaId, storedSearch.viaId);
		assertEquals(madeSearch.toId, storedSearch.toId);
		assertEquals(madeSearch.count, storedSearch.count);
		assertEquals(madeSearch.lastUsed, storedSearch.lastUsed);
		assertEquals(madeSearch.favorite, storedSearch.favorite);

		// store a new search, this time with via location
		madeSearch = new StoredSearch(DB, f3, f2, f1);
		long uid2 = dao.storeSearch(madeSearch);

		// assert that search was stored and retrieved properly
		storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(2, storedSearches.size());
		storedSearch = storedSearches.get(1);
		assertEquals(uid2, storedSearch.getUid());
		assertEquals(madeSearch.fromId, storedSearch.fromId);
		assertEquals(madeSearch.viaId, storedSearch.viaId);
		assertEquals(madeSearch.toId, storedSearch.toId);
	}

	@Test
	public void getStoredSearch() throws Exception {
		// no stored searches should exist in empty DB
		assertNull(dao.getStoredSearch(DB, 0, null, 0));

		// store a new search
		StoredSearch madeSearch = new StoredSearch(DB, f1, null, f3);
		long uid1 = dao.storeSearch(madeSearch);

		// assert that search was stored and retrieved properly
		StoredSearch storedSearch = dao.getStoredSearch(DB, f1.getUid(), null, f3.getUid());
		assertNotNull(storedSearch);
		assertEquals(uid1, storedSearch.getUid());
		assertEquals(DB, storedSearch.getNetworkId());
		assertEquals(f1.getUid(), storedSearch.fromId);
		assertEquals(null, storedSearch.viaId);
		assertEquals(f3.getUid(), storedSearch.toId);

		// store a search with via
		madeSearch = new StoredSearch(DB, f1, f2, f3);
		long uid2 = dao.storeSearch(madeSearch);

		// assert that search was stored and retrieved properly
		storedSearch = dao.getStoredSearch(DB, f1.getUid(), f2.getUid(), f3.getUid());
		assertNotNull(storedSearch);
		assertEquals(uid2, storedSearch.getUid());
		assertEquals(DB, storedSearch.getNetworkId());
		assertEquals(f1.getUid(), storedSearch.fromId);
		assertNotNull(storedSearch.viaId);
		assertEquals(f2.getUid(), (long) storedSearch.viaId);
		assertEquals(f3.getUid(), storedSearch.toId);
	}

	@Test
	public void increaseCount() throws Exception {
		// store a new search, should have count of 1
		StoredSearch madeSearch = new StoredSearch(DB, f3, null, f1);
		assertEquals(1, madeSearch.count);
		long uid = dao.storeSearch(madeSearch);

		// retrieve it again, so we get it with an UID which is required for the update
		List<StoredSearch> storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(1, storedSearches.size());
		StoredSearch storedSearch = storedSearches.get(0);
		assertEquals(uid, storedSearch.getUid());

		// update date and store it with increased count
		Date newDate = new Date();
		dao.updateStoredSearch(storedSearch.uid, newDate);

		// retrieve the stored search and assert count got increased
		storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(1, storedSearches.size());
		assertEquals(2, storedSearches.get(0).count);
		assertEquals(newDate, storedSearches.get(0).lastUsed);
	}

	@Test
	public void updatedSearch() throws Exception {
		// store a new search, should have count of 1
		StoredSearch madeSearch1 = new StoredSearch(DB, f1, null, f2);
		assertEquals(1, madeSearch1.count);
		long uid1 = dao.storeSearch(madeSearch1);

		// store another search that should stay unchanged
		StoredSearch madeSearch2 = new StoredSearch(DB, f3, f2, f1);
		dao.storeSearch(madeSearch2);

		// increase count by one and update lastUsed date
		Date lastUsed = new Date();
		dao.updateStoredSearch(uid1, lastUsed);

		// retrieve the stored search and assert count got increased
		List<StoredSearch> storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(2, storedSearches.size());
		assertEquals(madeSearch1.count + 1, storedSearches.get(0).count);
		assertEquals(lastUsed, storedSearches.get(0).lastUsed);

		// other search remained was unchanged
		assertEquals(madeSearch2.count, storedSearches.get(1).count);
		assertEquals(madeSearch2.lastUsed, storedSearches.get(1).lastUsed);
	}

	@Test
	public void setFavorite() throws Exception {
		// store a new search, should not be a favorite
		StoredSearch madeSearch1 = new StoredSearch(DB, f1, null, f2);
		assertFalse(madeSearch1.favorite);
		long uid1 = dao.storeSearch(madeSearch1);

		// store another search that should stay unchanged
		StoredSearch madeSearch2 = new StoredSearch(DB, f3, f2, f1);
		dao.storeSearch(madeSearch2);

		// make it a favorite
		dao.setFavorite(uid1, true);

		// retrieve the stored search and assert it is now a favorite
		List<StoredSearch> storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(2, storedSearches.size());
		assertTrue(storedSearches.get(0).favorite);

		// other search remained was unchanged
		assertFalse(madeSearch2.favorite);

		// make it a regular saved search again
		dao.setFavorite(uid1, false);
		assertFalse(getValue(dao.getStoredSearches(DB)).get(0).favorite);
	}

	@Test
	public void delete() throws Exception {
		// store a new search
		StoredSearch madeSearch = new StoredSearch(DB, f1, null, f3);
		dao.storeSearch(madeSearch);

		// get and remove search
		List<StoredSearch> storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(1, storedSearches.size());
		StoredSearch storedSearch = storedSearches.get(0);
		dao.delete(storedSearch);
		storedSearches = getValue(dao.getStoredSearches(DB));
		assertEquals(0, storedSearches.size());
	}

}
