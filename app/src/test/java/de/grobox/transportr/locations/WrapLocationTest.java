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

package de.grobox.transportr.locations;

import org.junit.Test;

import de.schildbach.pte.dto.Location;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class WrapLocationTest {

	@Test
	public void isSamePlace() {
		WrapLocation location1 = new WrapLocation(Location.coord(48850000, 2360000));
		WrapLocation location2 = new WrapLocation(Location.coord(48850300, 2360300));
		WrapLocation location3 = new WrapLocation(Location.coord(488500301, 23600301));

		assertTrue(location1.isSamePlace(location1));
		assertTrue(location1.isSamePlace(location2));
		assertTrue(location2.isSamePlace(location1));

		assertFalse(location1.isSamePlace(location3));
		assertFalse(location3.isSamePlace(location1));

		assertFalse(location1.isSamePlace(null));
	}

}
