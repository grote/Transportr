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
enum Region {

	EUROPE(R.string.np_region_europe, "🇪🇺"),
	GERMANY(R.string.np_region_germany, "🇩🇪"),
	AUSTRIA(R.string.np_region_austria, "🇦🇹"),
	LIECHTENSTEIN(R.string.np_region_liechtenstein, "🇱🇮"),
	SWITZERLAND(R.string.np_region_switzerland, "🇨🇭"),
	BELGIUM(R.string.np_region_belgium, "🇧🇪"),
	LUXEMBOURG(R.string.np_region_luxembourg, "🇱🇺"),
	NETHERLANDS(R.string.np_region_netherlands, "🇳🇱"),
	DENMARK(R.string.np_region_denmark, "🇩🇰"),
	SWEDEN(R.string.np_region_sweden, "🇸🇪"),
	NORWAY(R.string.np_region_norway, "🇳🇴"),
	FINLAND(R.string.np_region_finland, "🇫🇮"),
	GREAT_BRITAIN(R.string.np_region_gb, "🇬🇧"),
	IRELAND(R.string.np_region_ireland, "🇮🇪"),
	ITALY(R.string.np_region_italy, "🇮🇹"),
	POLAND(R.string.np_region_poland, "🇵🇱"),
	UAE(R.string.np_region_uae, "🇦🇪"),
	USA(R.string.np_region_usa, "🇺🇸"),
	AUSTRALIA(R.string.np_region_australia, "🇦🇺"),
	FRANCE(R.string.np_region_france, "🇫🇷"),
	NEW_ZEALAND(R.string.np_region_nz, "🇳🇿"),
	SPAIN(R.string.np_region_spain, "🇪🇸"),
	BRAZIL(R.string.np_region_br, "🇧🇷"),
	CANADA(R.string.np_region_canada, "🇨🇦"),
	COSTA_RICA(R.string.np_region_costa_rica, "🇨🇷"),
	AFRICA(R.string.np_region_africa, "🌍"),
	NICARAGUA(R.string.np_region_nicaragua, "🇳🇮");

	private final @StringRes int name;
	private final @Nullable String flag;

	Region(@StringRes int name, @Nullable String flag) {
		this.name = name;
		this.flag = flag;
	}

	@StringRes
	public int getName() {
		return name;
	}

	public String getName(Context context) {
		return context.getString(name);
	}

	@Nullable
	public String getFlag() {
		return flag;
	}

}
