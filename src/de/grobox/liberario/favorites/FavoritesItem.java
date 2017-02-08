/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

package de.grobox.liberario.favorites;

import android.support.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.Date;

import javax.annotation.ParametersAreNonnullByDefault;

import de.schildbach.pte.dto.Location;

import static com.google.common.base.Preconditions.checkArgument;
import static de.grobox.liberario.favorites.FavoritesType.HOME;
import static de.grobox.liberario.favorites.FavoritesType.TRIP;
import static de.grobox.liberario.favorites.FavoritesType.WORK;

@ParametersAreNonnullByDefault
public class FavoritesItem implements Comparable<FavoritesItem> {

	private final FavoritesType type;
	@Nullable
	private final Location from, via, to;
	private final int count;
	private Date lastUsed;
	private boolean favorite;

	public FavoritesItem(Location from, @Nullable Location via, Location to) {
		this(from, via, to, false);
	}

	public FavoritesItem(Location from, @Nullable Location via, Location to, boolean favorite) {
		this(from, via, to, 1, new Date(), favorite);
	}

	public FavoritesItem(Location from, @Nullable Location via, Location to, int count, Date lastUsed, boolean favorite) {
		this.type = TRIP;
		this.from = from;
		this.via = via;
		this.to = to;
		this.count = count;
		this.lastUsed = lastUsed;
		this.favorite = favorite;
	}

	FavoritesItem(FavoritesType type, @Nullable Location to) {
		checkArgument(type == HOME || type == WORK, "This constructor can only be used for HOME and WORK");
		this.type = type;
		this.from = null;
		this.via = null;
		this.to = to;
		this.count = 0;
		this.lastUsed = new Date();
		this.favorite = false;
	}

	FavoritesType getType() {
		return type;
	}

	@Nullable
	public Location getFrom() {
		return from;
	}

	@Nullable
	public Location getVia() {
		return via;
	}

	@Nullable
	public Location getTo() {
		return to;
	}

	public int getCount() {
		return count;
	}

	public boolean isFavorite() {
		return favorite;
	}

	void setFavorite(boolean favorite) {
		this.favorite = favorite;
	}

	@Override
	public int compareTo(FavoritesItem i) {
		if (type == HOME) return -1;
		if (type == WORK && i.type != HOME) return -1;
		if (favorite && !i.favorite) return -1;
		if (favorite) {
			if (count == i.count) return lastUsed.compareTo(i.lastUsed);
			return count > i.count ? -1 : 1;
		} else {
			return lastUsed.compareTo(i.lastUsed);
		}
	}

	@Override
	@SuppressWarnings("RedundantIfStatement")
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof FavoritesItem))
			return false;
		final FavoritesItem other = (FavoritesItem) o;
		if (!Objects.equal(this.type, other.type))
			return false;
		if (!Objects.equal(this.from, other.from))
			return false;
		if (!Objects.equal(this.to, other.to))
			return false;
		if (!Objects.equal(this.via, other.via))
			return false;
		return true;
	}

	@SuppressWarnings("RedundantIfStatement")
	boolean equalsAllFields(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof FavoritesItem))
			return false;
		final FavoritesItem other = (FavoritesItem) o;
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

}
