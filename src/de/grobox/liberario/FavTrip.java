/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

import java.io.Serializable;

import de.schildbach.pte.dto.Location;

public class FavTrip implements Serializable, Comparable<FavTrip> {

	private static final long serialVersionUID = 1690558255337614838L;

	private Location from;
	private Location to;
	private Location via;
	private int count;

	public FavTrip(Location from, Location to) {
		this.from = from;
		this.to = to;
		this.count = 0;
	}

	public FavTrip(Location from, Location to, Location via) {
		this.from = from;
		this.to = to;
		this.via = via;
		this.count = 0;
	}

	public Location getFrom() {
		return from;
	}

	public Location getTo() {
		return to;
	}

	public Location getVia() {
		return via;
	}

	public int getCount() {
		return count;
	}

	public void addCount() {
		count += 1;
	}

	@Override
	public boolean equals(Object o)	{
		if(o == this) {
			return true;
		}

		if(o instanceof FavTrip) {
			if(getFrom().equals(((FavTrip) o).getFrom()) &&
					getTo().equals(((FavTrip) o).getTo())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(FavTrip other) {
		return other.getCount() - getCount();
	}

}
