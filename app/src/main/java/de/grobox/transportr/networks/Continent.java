/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.networks;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;

@ParametersAreNonnullByDefault
enum Continent implements Region {

	EUROPE(R.string.np_continent_europe),
	AFRICA(R.string.np_continent_africa),
	NORTH_AMERICA(R.string.np_continent_north_america),
	CENTRAL_AMERICA(R.string.np_continent_central_america),
	SOUTH_AMERICA(R.string.np_continent_south_america),
	ASIA(R.string.np_continent_asia),
	OCEANIA(R.string.np_continent_oceania);


	private final @StringRes int name;

	Continent(@StringRes int name) {
		this.name = name;
	}

	@StringRes
	public int getName() {
		return name;
	}

	@Override
	public String getName(Context context) {
		return context.getString(name);
	}

}
