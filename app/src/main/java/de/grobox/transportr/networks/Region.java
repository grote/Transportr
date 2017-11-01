/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

	EUROPE(R.string.np_region_europe, "\uD83C\uDDEA\uD83C\uDDFA"),
	GERMANY(R.string.np_region_germany, "\uD83C\uDDE9\uD83C\uDDEA"),
	AUSTRIA(R.string.np_region_austria, "\uD83C\uDDE6\uD83C\uDDF9"),
	LIECHTENSTEIN(R.string.np_region_liechtenstein, "\uD83C\uDDF1\uD83C\uDDEE"),
	SWITZERLAND(R.string.np_region_switzerland, "\uD83C\uDDE8\uD83C\uDDED"),
	BELGIUM(R.string.np_region_belgium, "\uD83C\uDDE7\uD83C\uDDEA"),
	LUXEMBOURG(R.string.np_region_luxembourg, "\uD83C\uDDF1\uD83C\uDDFA"),
	NETHERLANDS(R.string.np_region_netherlands, "\uD83C\uDDF3\uD83C\uDDF1"),
	DENMARK(R.string.np_region_denmark, "\uD83C\uDDE9\uD83C\uDDF0"),
	SWEDEN(R.string.np_region_sweden, "\uD83C\uDDF8\uD83C\uDDEA"),
	NORWAY(R.string.np_region_norway, "\uD83C\uDDF3\uD83C\uDDF4"),
	FINLAND(R.string.np_region_finland, "\uD83C\uDDEB\uD83C\uDDEE"),
	GREAT_BRITAIN(R.string.np_region_gb, "\uD83C\uDDEC\uD83C\uDDE7"),
	IRELAND(R.string.np_region_ireland, "\uD83C\uDDEE\uD83C\uDDEA"),
	ITALY(R.string.np_region_italy, "\uD83C\uDDEE\uD83C\uDDF9"),
	POLAND(R.string.np_region_poland, "\uD83C\uDDF5\uD83C\uDDF1"),
	UAE(R.string.np_region_uae, "\uD83C\uDDE6\uD83C\uDDEA"),
	USA(R.string.np_region_usa, "\uD83C\uDDFA\uD83C\uDDF8"),
	AUSTRALIA(R.string.np_region_australia, "\uD83C\uDDE6\uD83C\uDDFA"),
	FRANCE(R.string.np_region_france, "\uD83C\uDDEB\uD83C\uDDF7"),
	NEW_ZEALAND(R.string.np_region_nz, "\uD83C\uDDF3\uD83C\uDDFF"),
	SPAIN(R.string.np_region_spain, "\uD83C\uDDEA\uD83C\uDDF8"),
	BRAZIL(R.string.np_region_br, "\uD83C\uDDE7\uD83C\uDDF7"),
	CANADA(R.string.np_region_canada, "\uD83C\uDDE8\uD83C\uDDE6");

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
