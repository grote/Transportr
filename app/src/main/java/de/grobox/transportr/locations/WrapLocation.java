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

import com.google.common.base.Strings;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.util.Set;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.room.Ignore;
import de.grobox.transportr.R;
import de.grobox.transportr.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Product;

import static com.google.common.base.Preconditions.checkArgument;
import static de.grobox.transportr.locations.WrapLocation.WrapType.NORMAL;
import static de.schildbach.pte.dto.LocationType.ANY;
import static de.schildbach.pte.dto.LocationType.COORD;

public class WrapLocation implements Serializable {

	public enum WrapType {NORMAL, GPS}

	@Ignore
	private final WrapType wrapType;

	public final LocationType type;
	public @Nullable String id;
	public int lat, lon;
	public @Nullable String place;
	public @Nullable String name;
	public @Nullable Set<Product> products;

	public WrapLocation(LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name, @Nullable Set<Product> products) {
		this.wrapType = NORMAL;
		this.type = type;
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.place = place;
		this.name = name;
		this.products = products;
	}

	public WrapLocation(Location l) {
		this(l.type, l.id, l.hasCoord() ? l.getLatAs1E6() : 0, l.hasCoord() ? l.getLonAs1E6() : 0, l.place, l.name, l.products);
	}

	public WrapLocation(WrapType wrapType) {
		checkArgument(wrapType != NORMAL, "Type can't be normal");
		this.wrapType = wrapType;
		this.type = ANY;
	}

	public WrapLocation(LatLng latLng) {
		this.wrapType = NORMAL;
		this.type = COORD;
		this.lat = (int) (latLng.getLatitude() * 1E6);
		this.lon = (int) (latLng.getLongitude() * 1E6);
		checkArgument(lat != 0 || lon != 0, "Null Island is not a valid location");
	}

	public WrapType getWrapType() {
		return wrapType;
	}

	public Location getLocation() {
		Point point = Point.from1E6(lat, lon);
		if (type == LocationType.ANY && id != null) {
			return new Location(type, null, point, place, name, products);
		}
		return new Location(type, id, point, place, name, products);
	}

	@Nullable
	public String getId() {
		return id;
	}

	public boolean hasId() {
		return !Strings.isNullOrEmpty(id);
	}

	public final boolean hasLocation() {
		return lat != 0 || lon != 0;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (o instanceof WrapLocation) {
			WrapLocation wLoc = (WrapLocation) o;
			if (getLocation() == null) {
				return wLoc.getLocation() == null;
			}
			return getLocation().equals(wLoc.getLocation());
		}
		return false;
	}

	@DrawableRes
	public int getDrawable() {
		switch (wrapType) {
			case GPS:
				return R.drawable.ic_gps;
			case NORMAL:
				switch (type) {
					case ADDRESS:
						return R.drawable.ic_location_address;
					case POI:
						return R.drawable.ic_action_about;
					case STATION:
						return R.drawable.ic_location_station;
					case COORD:
						return R.drawable.ic_gps;
				}
		}
		return R.drawable.ic_location;
	}

	public String getName() {
		// FIXME improve
		if (type.equals(LocationType.COORD)) {
			return TransportrUtils.getCoordName(getLocation());
		} else if (getLocation().uniqueShortName() != null) {
			return getLocation().uniqueShortName();
		} else if (hasId()) {
			return id;
		} else {
			return "";
		}
	}

	String getFullName() {
		if (name != null) {
			return place == null ? name : name + ", " + place;
		} else {
			return getName();
		}
	}

	public LatLng getLatLng() {
		return new LatLng(lat / 1E6, lon / 1E6);
	}

	public boolean isSamePlace(@Nullable WrapLocation other) {
		return other != null && isSamePlaceInt(other.lat, other.lon);
	}

	public boolean isSamePlace(double otherLat, double otherLon) {
		return isSamePlaceInt((int) (otherLat * 1E6), (int) (otherLon * 1E6));
	}

	private boolean isSamePlaceInt(int otherLat, int otherLon) {
		return (lat / 1000 == otherLat / 1000) && (lon / 1000 == otherLon / 1000);
	}

	@Override
	public String toString() {
		return getFullName();
	}

}
