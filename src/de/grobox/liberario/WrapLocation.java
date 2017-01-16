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

import android.support.annotation.Nullable;

import java.io.Serializable;

import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

import static com.google.common.base.Preconditions.checkArgument;
import static de.grobox.liberario.WrapLocation.WrapType.NORMAL;
import static de.schildbach.pte.dto.LocationType.ANY;

public class WrapLocation implements Serializable {

	public enum WrapType { NORMAL, HOME, GPS, MAP }

	private Location loc;
	private WrapType type;

	public WrapLocation(Location loc) {
		this(loc, NORMAL);
	}

	public WrapLocation(WrapType type) {
		this(new Location(ANY, null), type);
		checkArgument(type != NORMAL, "Type can't be normal");
	}

	public WrapLocation(Location loc, WrapType type) {
		this.loc = loc;
		this.type = type;
	}

	public Location getLocation() {
		return loc;
	}

	@Nullable
	public String getId() {
		if (loc == null) return null;
		return loc.id;
	}

	public WrapType getType() {
		return type;
	}

	@Nullable
	public String getName() {
		if (loc == null) return null;
		return TransportrUtils.getLocationName(loc);
	}

	public boolean hasId() {
		return loc != null && loc.hasId();
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
		return TransportrUtils.getFullLocName(getLocation());
	}

}
