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
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;

@ParametersAreNonnullByDefault
enum Country implements ParentRegion {

	GERMANY(R.string.np_region_germany, "🇩🇪", Continent.EUROPE),
	AUSTRIA(R.string.np_region_austria, "🇦🇹", Continent.EUROPE),
	LIECHTENSTEIN(R.string.np_region_liechtenstein, "🇱🇮", Continent.EUROPE),
	SWITZERLAND(R.string.np_region_switzerland, "🇨🇭", Continent.EUROPE),
	LUXEMBOURG(R.string.np_region_luxembourg, "🇱🇺", Continent.EUROPE),
	NETHERLANDS(R.string.np_region_netherlands, "🇳🇱", Continent.EUROPE),
	DENMARK(R.string.np_region_denmark, "🇩🇰", Continent.EUROPE),
	SWEDEN(R.string.np_region_sweden, "🇸🇪", Continent.EUROPE),
	NORWAY(R.string.np_region_norway, "🇳🇴", Continent.EUROPE),
	FINLAND(R.string.np_region_finland, "🇫🇮", Continent.EUROPE),
	FRANCE(R.string.np_region_france, "🇫🇷", Continent.EUROPE),
	GREAT_BRITAIN(R.string.np_region_gb, "🇬🇧", Continent.EUROPE),
	IRELAND(R.string.np_region_ireland, "🇮🇪", Continent.EUROPE),
	POLAND(R.string.np_region_poland, "🇵🇱", Continent.EUROPE),
	UAE(R.string.np_region_uae, "🇦🇪", Continent.ASIA),
	AUSTRALIA(R.string.np_region_australia, "🇦🇺", Continent.OCEANIA),
	CANADA(R.string.np_region_canada, "🇨🇦", Continent.NORTH_AMERICA),
	USA(R.string.np_region_usa, "🇺🇸", Continent.NORTH_AMERICA);

	private final @StringRes int name;
	private final String flag;
	private final Continent continent;
	private List<Region> subRegions;

	Country(@StringRes int name, String flag, Continent continent) {
		this.name = name;
		this.flag = flag;
		this.continent = continent;
		this.continent.addSubRegion(this);
		this.subRegions = new ArrayList<>();
	}

	@Override
	@StringRes
	public int getName() {
		return name;
	}

	@Override
	public String getName(Context context) {
		return context.getString(name);
	}

	public String getFlag() {
		return flag;
	}

	public Continent getContinent() {
		return continent;
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
