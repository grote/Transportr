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

package de.grobox.liberario;

import android.support.annotation.NonNull;

import java.util.Comparator;

import de.grobox.liberario.locations.WrapLocation;
import de.schildbach.pte.dto.Location;

public class FavoriteLocation extends WrapLocation implements Comparable<FavoriteLocation> {

	public enum FavLocationType {FROM, VIA, TO}

	private int from_count;
	private int via_count;
	private int to_count;

	public FavoriteLocation(Location loc, int from, int via, int to) {
		super(loc);
		from_count = from;
		via_count = via;
		to_count = to;
	}

	public void add(FavLocationType type) {
		switch (type) {
			case FROM:
				from_count++;
				break;
			case VIA:
				via_count++;
				break;
			case TO:
				to_count++;
				break;
		}
	}

	private int getFromCount() {
		return from_count;
	}

	private int getViaCount() {
		return via_count;
	}

	private int getToCount() {
		return to_count;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}

		if (o instanceof FavoriteLocation) {
			if (this.getLocation().equals(((FavoriteLocation) o).getLocation())) {
				return true;
			}
			return false;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getLocation().hashCode();
	}

	@Override
	public int compareTo(@NonNull FavoriteLocation other) {
		return (other.getFromCount() + other.getToCount()) - (this.getFromCount() + this.getToCount());
	}

	public static Comparator<FavoriteLocation> FromComparator = new Comparator<FavoriteLocation>() {
		public int compare(FavoriteLocation loc1, FavoriteLocation loc2) {
			return loc2.getFromCount() - loc1.getFromCount();
		}
	};

	public static Comparator<FavoriteLocation> ViaComparator = new Comparator<FavoriteLocation>() {
		public int compare(FavoriteLocation loc1, FavoriteLocation loc2) {
			return loc2.getViaCount() - loc1.getViaCount();
		}
	};

	public static Comparator<FavoriteLocation> ToComparator = new Comparator<FavoriteLocation>() {
		public int compare(FavoriteLocation loc1, FavoriteLocation loc2) {
			return loc2.getToCount() - loc1.getToCount();
		}
	};

}
