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

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.util.Comparator;
import java.util.Set;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.WrapLocation;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

@Entity(
		tableName = "locations",
		indices = {
				@Index("networkId"),
				@Index("id"),
				@Index(value = {"networkId", "id"}, unique = true)
		}
)
public class FavoriteLocation extends StoredLocation {

	public enum FavLocationType {FROM, VIA, TO}

	private int fromCount;
	private int viaCount;
	private int toCount;

	public FavoriteLocation(long uid, @Nullable NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon,
	                        @Nullable String place, @Nullable String name, @Nullable Set<Product> products, int fromCount, int viaCount,
	                        int toCount) {
		super(uid, networkId, type, id, lat, lon, place, name, products);
		this.fromCount = fromCount;
		this.viaCount = viaCount;
		this.toCount = toCount;
	}

	@Ignore
	public FavoriteLocation(long uid, NetworkId networkId, WrapLocation l) {
		super(uid, networkId, l);
		this.fromCount = 0;
		this.viaCount = 0;
		this.toCount = 0;
	}

	@Ignore
	public FavoriteLocation(NetworkId networkId, WrapLocation l) {
		super(networkId, l);
		this.fromCount = 0;
		this.viaCount = 0;
		this.toCount = 0;
	}

	@Ignore
	public FavoriteLocation(NetworkId networkId, Location l) {
		super(networkId, l);
		this.fromCount = 0;
		this.viaCount = 0;
		this.toCount = 0;
	}

	@Override
	@DrawableRes
	public int getDrawable() {
		switch (type) {
			case ADDRESS:
				return R.drawable.ic_location_address_fav;
			case POI:
				return R.drawable.ic_location_poi_fav;
			case STATION:
				return R.drawable.ic_location_station_fav;
			default:
				return R.drawable.ic_location;
		}
	}

	int getFromCount() {
		return fromCount;
	}

	int getViaCount() {
		return viaCount;
	}

	int getToCount() {
		return toCount;
	}

	public void add(FavLocationType type) {
		switch (type) {
			case FROM:
				fromCount++;
				return;
			case VIA:
				viaCount++;
				return;
			case TO:
				toCount++;
		}
	}

	@Override
	public String toString() {
		return super.toString() + "[" + fromCount + "]";
	}

	public static final Comparator<FavoriteLocation> FromComparator = (loc1, loc2) -> loc2.getFromCount() - loc1.getFromCount();

	public static final Comparator<FavoriteLocation> ViaComparator = (loc1, loc2) -> loc2.getViaCount() - loc1.getViaCount();

	public static final Comparator<FavoriteLocation> ToComparator = (loc1, loc2) -> loc2.getToCount() - loc1.getToCount();

}
