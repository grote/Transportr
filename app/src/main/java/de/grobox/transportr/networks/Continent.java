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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import de.grobox.transportr.R;

@Immutable
@ParametersAreNonnullByDefault
enum Continent implements ParentRegion {

	EUROPE(R.string.np_continent_europe, R.drawable.continent_europe),
	AFRICA(R.string.np_continent_africa, R.drawable.continent_africa),
	NORTH_AMERICA(R.string.np_continent_north_america, R.drawable.continent_north_america),
	CENTRAL_AMERICA(R.string.np_continent_central_america, R.drawable.continent_central_america),
	SOUTH_AMERICA(R.string.np_continent_south_america, R.drawable.continent_south_america),
	ASIA(R.string.np_continent_asia, R.drawable.continent_asia),
	OCEANIA(R.string.np_continent_oceania, R.drawable.continent_oceania);


	private final @StringRes int name;
	private final @DrawableRes int contour;
	private final List<Region> subRegions = new ArrayList<>();

	Continent(@StringRes int name, @DrawableRes int contour) {
		this.name = name;
		this.contour = contour;
	}

	@Override
	@StringRes
	public int getName() {
		return name;
	}

	@DrawableRes
	public int getContour() {
		return contour;
	}

	@Override
	public String getName(Context context) {
		return context.getString(name);
	}

	@Override
	public void addSubRegion(Region subRegion) {
		subRegions.add(subRegion);
	}

	@NonNull
	@Override
	public List<Region> getSubRegions() {
		return subRegions;
	}

}
