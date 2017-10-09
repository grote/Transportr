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

package de.grobox.liberario.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.grobox.liberario.FavoriteLocation;
import de.grobox.liberario.locations.WrapLocation;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

import static de.grobox.liberario.data.DbHelper.getLocation;
import static de.grobox.liberario.settings.Preferences.getNetwork;

@Deprecated
public class LocationDb {

	public static List<FavoriteLocation> getFavLocationList(Context context) {
		List<FavoriteLocation> fav_list = new ArrayList<>();
		String network = getNetwork(context);
		if(network == null) return fav_list;

		DbHelper mDbHelper = new DbHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(
			DbHelper.TABLE_FAV_LOCS,    // The table to query
			null,                       // The columns to return (null == all)
			"network = ?",              // The columns for the WHERE clause
			new String[] { network }, // The values for the WHERE clause
			null,   // don't group the rows
			null,   // don't filter by row groups
			null    // The sort order
		);

		while(c.moveToNext()) {
			Location loc = getLocation(c);
			FavoriteLocation fav_loc = new FavoriteLocation(loc, c.getInt(c.getColumnIndex("from_count")), c.getInt(c.getColumnIndex("via_count")), c.getInt(c.getColumnIndex("to_count")));
			fav_list.add(fav_loc);
		}

		c.close();
		db.close();

		return fav_list;
	}

	public static List<WrapLocation> getFavLocationList(Context context, FavoriteLocation.FavLocationType sort) {
		List<FavoriteLocation> fav_list = getFavLocationList(context);
		List<WrapLocation> list = new ArrayList<>();

		if (sort == FavoriteLocation.FavLocationType.FROM) {
			Collections.sort(fav_list, FavoriteLocation.FromComparator);
		} else if (sort == FavoriteLocation.FavLocationType.VIA) {
			Collections.sort(fav_list, FavoriteLocation.ViaComparator);
		} else if (sort == FavoriteLocation.FavLocationType.TO) {
			Collections.sort(fav_list, FavoriteLocation.ToComparator);
		}
		for (FavoriteLocation loc : fav_list) {
			list.add(loc);
		}
		return list;
	}

	public static void updateFavLocation(Context context, Location loc, FavoriteLocation.FavLocationType favLocationType) {
		if(loc == null || loc.type == LocationType.COORD || loc.type == LocationType.ANY) {
			// don't store GPS or ANY locations (includes ambiguous locations)
			return;
		}

		String whereClause;
		String[] whereArgs;

		if(loc.hasId()) {
			// use location id to identify location
			whereClause = "network = ? AND id = ?";
			whereArgs = new String[] { getNetwork(context), loc.id };
		} else {
			// use other values to identify location
			String lat = String.valueOf(loc.lat);
			String lon = String.valueOf(loc.lon);
			String place = loc.place == null ? "" : loc.place;
			String name = loc.name == null ? "" : loc.name;

			whereClause = "network = ? AND type = ? AND lat = ? AND lon = ? AND place = ? AND name = ?";
			whereArgs = new String[] { getNetwork(context), loc.type.name(), lat, lon, place, name };
		}

		DbHelper mDbHelper = new DbHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// get location that needs to be updated from database
		Cursor c = db.query(
				DbHelper.TABLE_FAV_LOCS,    // The table to query
				new String[] {"_id", "from_count", "via_count", "to_count"},
				whereClause,
				whereArgs,
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);
		ContentValues values = new ContentValues();

		if(c.moveToFirst()) {
			// increase counter by one for existing location
			if(favLocationType == FavoriteLocation.FavLocationType.FROM) {
				values.put("from_count", c.getInt(c.getColumnIndex("from_count")) + 1);
			}
			else if(favLocationType == FavoriteLocation.FavLocationType.VIA) {
				values.put("via_count", c.getInt(c.getColumnIndex("via_count")) + 1);
			}
			else if(favLocationType == FavoriteLocation.FavLocationType.TO) {
				values.put("to_count", c.getInt(c.getColumnIndex("to_count")) + 1);
			}
			db.update(DbHelper.TABLE_FAV_LOCS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
		}
		else {
			// add new favorite location
			values.put("network", getNetwork(context));
			values.put("type", loc.type.name());
			values.put("id", loc.id);
			values.put("lat", loc.lat);
			values.put("lon", loc.lon);
			values.put("place", loc.place);
			values.put("name", loc.name);

			// set counter to one
			if(favLocationType == FavoriteLocation.FavLocationType.FROM) {
				values.put("from_count", 1);
				values.put("via_count", 0);
				values.put("to_count", 0);
			}
			else if(favLocationType == FavoriteLocation.FavLocationType.VIA) {
				values.put("from_count", 0);
				values.put("via_count", 1);
				values.put("to_count", 0);
			}
			else if(favLocationType == FavoriteLocation.FavLocationType.TO) {
				values.put("from_count", 0);
				values.put("via_count", 0);
				values.put("to_count", 1);
			}

			db.insert(DbHelper.TABLE_FAV_LOCS, null, values);
		}

		c.close();
		db.close();
	}

}
