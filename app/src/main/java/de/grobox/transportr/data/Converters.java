/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.data;


import androidx.room.TypeConverter;
import androidx.annotation.Nullable;

import java.util.Date;
import java.util.Set;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

public class Converters {

	@TypeConverter
	public static String fromNetworkId(@Nullable NetworkId networkId) {
		if (networkId == null) return null;
		return networkId.name();
	}

	@Nullable
	@TypeConverter
	public static NetworkId toNetworkId(String network) {
		try {
			return NetworkId.valueOf(network);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@TypeConverter
	public static String fromLocationType(LocationType locationType) {
		return locationType.name();
	}

	@TypeConverter
	public static LocationType toLocationType(String type) {
		try {
			return LocationType.valueOf(type);
		} catch (IllegalArgumentException e) {
			return LocationType.ANY;
		}
	}

	@Nullable
	@TypeConverter
	public static String fromProducts(Set<Product> products) {
		if (products == null) return null;
		return String.valueOf(Product.toCodes(products));
	}

	@TypeConverter
	public static Set<Product> toProducts(@Nullable String codes) {
		if (codes == null) return null;
		return Product.fromCodes(codes.toCharArray());
	}

	@TypeConverter
	public static Date toDate(Long value) {
		return value == null ? null : new Date(value);
	}

	@TypeConverter
	public static Long fromDate(Date date) {
		return date == null ? null : date.getTime();
	}

}
