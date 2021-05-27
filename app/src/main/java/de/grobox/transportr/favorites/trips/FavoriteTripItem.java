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

package de.grobox.transportr.favorites.trips;

import androidx.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.Date;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.data.locations.WorkLocation;
import de.grobox.transportr.data.searches.StoredSearch;
import de.grobox.transportr.locations.WrapLocation;

import static de.grobox.transportr.favorites.trips.FavoriteTripType.HOME;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.TRIP;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.WORK;
import static de.grobox.transportr.locations.WrapLocation.WrapType.GPS;

@ParametersAreNonnullByDefault
public class FavoriteTripItem extends StoredSearch implements Comparable<FavoriteTripItem> {

	private final FavoriteTripType type;
	private final WrapLocation from;
	private final @Nullable WrapLocation via, to;

	public FavoriteTripItem(long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to) {
		this.uid = uid;
		this.type = TRIP;
		this.from = from;
		this.via = via;
		this.to = to;
	}

	public FavoriteTripItem(StoredSearch storedSearch, WrapLocation from, @Nullable WrapLocation via, WrapLocation to) {
		super(storedSearch);
		this.type = TRIP;
		this.from = from;
		this.via = via;
		this.to = to;
	}

	public FavoriteTripItem(@Nullable HomeLocation to) {
		this.type = HOME;
		this.from = new WrapLocation(GPS);
		this.via = null;
		this.to = to;
		this.count = 1;
		this.lastUsed = new Date();
		this.favorite = false;
	}

	public FavoriteTripItem(@Nullable WorkLocation to) {
		this.type = WORK;
		this.from = new WrapLocation(GPS);
		this.via = null;
		this.to = to;
		this.count = 1;
		this.lastUsed = new Date();
		this.favorite = false;
	}

	FavoriteTripType getType() {
		return type;
	}

	public WrapLocation getFrom() {
		return from;
	}

	@Nullable
	public WrapLocation getVia() {
		return via;
	}

	@Nullable
	public WrapLocation getTo() {
		return to;
	}

	@Override
	public int compareTo(FavoriteTripItem i) {
		if (equals(i)) return 0;
		if (type == HOME) return -1;
		if (i.type == HOME) return 1;
		if (type == WORK) return -1;
		if (i.type == WORK) return 1;
		if (favorite && !i.favorite) return -1;
		if (!favorite && i.favorite) return 1;
		if (favorite) {
			if (count == i.count) return lastUsed.compareTo(i.lastUsed);
			return count > i.count ? -1 : 1;
		} else {
			return lastUsed.compareTo(i.lastUsed) * -1;
		}
	}

	@Override
	@SuppressWarnings("SimplifiableIfStatement")
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof FavoriteTripItem))
			return false;
		FavoriteTripItem item = (FavoriteTripItem) o;
		return uid == item.uid && type == item.type;
	}

	@SuppressWarnings("RedundantIfStatement")
	boolean equalsAllFields(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof FavoriteTripItem))
			return false;
		final FavoriteTripItem other = (FavoriteTripItem) o;
		if (!Objects.equal(this.type, other.type))
			return false;
		if (!Objects.equal(this.from, other.from))
			return false;
		if (!Objects.equal(this.to, other.to))
			return false;
		if (!Objects.equal(this.via, other.via))
			return false;
		if (this.count != other.count)
			return false;
		if (this.favorite != other.favorite)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ID[" + uid + "] " + (favorite ? "F " : "R ") +
				from.getName() + " -> " + (to == null ? type.name() : to.getName()) + " [" + count + "]";
	}

}
