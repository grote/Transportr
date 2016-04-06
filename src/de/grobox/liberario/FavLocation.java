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

import java.io.Serializable;
import java.util.Comparator;

import de.schildbach.pte.dto.Location;

public class FavLocation extends WrapLocation implements Serializable, Comparable<FavLocation> {
	private static final long serialVersionUID = 3542146506031067902L;
	public enum LOC_TYPE { FROM, TO }

	private int from_count;
	private int to_count;

	public FavLocation(Location loc, int from, int to) {
		super(loc);
		from_count = from;
		to_count = to;
	}

	public int getFromCount() {
		return from_count;
	}

	public int getToCount() {
		return to_count;
	}

	@Override
	public boolean equals(Object o)	{
		if(o == this) {
			return true;
		}

		if(o instanceof FavLocation) {
			if(this.getLocation().equals(((FavLocation) o).getLocation())) {
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
	public int compareTo(@NonNull FavLocation other) {
		return (other.getFromCount() + other.getToCount()) - (this.getFromCount() + this.getToCount());
	}

	public static Comparator<FavLocation> FromComparator = new Comparator<FavLocation>() {
		public int compare(FavLocation loc1, FavLocation loc2) {
			return loc2.getFromCount() - loc1.getFromCount();
		}
	};

	public static Comparator<FavLocation> ToComparator = new Comparator<FavLocation>() {
		public int compare(FavLocation loc1, FavLocation loc2) {
			return loc2.getToCount() - loc1.getToCount();
		}
	};

}
