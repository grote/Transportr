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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Set;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.WrapLocation;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

@Entity(
		tableName = "home_locations",
		indices = {
				@Index(value = {"networkId"}, unique = true)
		}
)
public class HomeLocation extends StoredLocation {

	public HomeLocation(long uid, @Nullable NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name, @Nullable Set<Product> products) {
		super(uid, networkId, type, id, lat, lon, place, name, products);
	}

	@Ignore
	public HomeLocation(@NonNull NetworkId networkId, WrapLocation l) {
		super(networkId, l);
	}

	@Ignore
	public HomeLocation(@NonNull NetworkId networkId, Location l) {
		super(networkId, l);
	}

	@Override
	@DrawableRes
	public int getDrawable() {
		return R.drawable.ic_action_home;
	}

}
