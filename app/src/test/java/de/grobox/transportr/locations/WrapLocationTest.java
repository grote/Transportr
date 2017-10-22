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
