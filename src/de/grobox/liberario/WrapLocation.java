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

import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

public class WrapLocation {

	private Location loc;

	public WrapLocation(Location loc) {
		this.loc = loc;
	}

	public Location getLocation() {
		return loc;
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) {
			return true;
		}
		if(o instanceof WrapLocation) {
			WrapLocation wLoc = (WrapLocation) o;
			if(getLocation() == null) {
				return wLoc.getLocation() == null;
			}
			return getLocation().equals(wLoc.getLocation());
		}
		return false;
	}

	@Override
	public String toString() {
		if(loc == null) return "";

		if(loc.id != null) {
			// we do not have a context here, so we can not get proper names
			if(loc.id.equals(LocationAdapter.HOME)) {
				return null;
			} else if(loc.id.equals(LocationAdapter.GPS)) {
				return null;
			} else if(loc.id.equals(LocationAdapter.MAP)) {
				return null;
			}
		}
		return TransportrUtils.getFullLocName(getLocation());
	}

}
