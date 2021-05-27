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

import java.util.Set;

import androidx.annotation.Nullable;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import de.grobox.transportr.locations.WrapLocation;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

public abstract class StoredLocation extends WrapLocation {

	@PrimaryKey(autoGenerate = true)
	private final long uid;
	private final NetworkId networkId;

	StoredLocation(long uid, NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name, @Nullable Set<Product> products) {
		super(type, id, lat, lon, place, name, products);
		this.uid = uid;
		this.networkId = networkId;
	}

	@Ignore
	StoredLocation(long uid, NetworkId networkId, WrapLocation l) {
		this(uid, networkId, l.type, l.id, l.lat, l.lon, l.place, l.name, l.products);
	}

	@Ignore
	StoredLocation(NetworkId networkId, WrapLocation l) {
		this(0, networkId, l);
	}

	@Ignore
	StoredLocation(NetworkId networkId, Location l) {
		this(0, networkId, l.type, l.id, l.hasCoord() ? l.getLatAs1E6() : 0, l.hasCoord() ? l.getLonAs1E6() : 0, l.place, l.name, l.products);
	}

	public long getUid() {
		return uid;
	}

	public NetworkId getNetworkId() {
		return networkId;
	}

}
