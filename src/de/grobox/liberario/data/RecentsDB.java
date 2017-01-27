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

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.WrapLocation;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

import static de.grobox.liberario.Preferences.getNetwork;
import static de.grobox.liberario.data.DBHelper.getLocation;

public class RecentsDB {

	/* FavLocation */

	public static List<FavLocation> getFavLocationList(Context context) {
		List<FavLocation> fav_list = new ArrayList<>();
		String network = getNetwork(context);
		if(network == null) return fav_list;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(
			DBHelper.TABLE_FAV_LOCS,    // The table to query
			null,                       // The columns to return (null == all)
			"network = ?",              // The columns for the WHERE clause
			new String[] { network }, // The values for the WHERE clause
			null,   // don't group the rows
			null,   // don't filter by row groups
			null    // The sort order
		);

		while(c.moveToNext()) {
			Location loc = getLocation(c);
			FavLocation fav_loc = new FavLocation(loc, c.getInt(c.getColumnIndex("from_count")), c.getInt(c.getColumnIndex("via_count")), c.getInt(c.getColumnIndex("to_count")));
			fav_list.add(fav_loc);
		}

		c.close();
		db.close();

		return fav_list;
	}

	public static List<WrapLocation> getFavLocationList(Context context, FavLocation.LOC_TYPE sort, boolean onlyIDs) {
		List<FavLocation> fav_list = getFavLocationList(context);
		List<WrapLocation> list = new ArrayList<>();

		if(sort == FavLocation.LOC_TYPE.FROM) {
			Collections.sort(fav_list, FavLocation.FromComparator);
		}
		else if(sort == FavLocation.LOC_TYPE.VIA) {
			Collections.sort(fav_list, FavLocation.ViaComparator);
		}
		else if(sort == FavLocation.LOC_TYPE.TO) {
			Collections.sort(fav_list, FavLocation.ToComparator);
		}

		for(final FavLocation loc : fav_list) {
			if(!onlyIDs || loc.getLocation().hasId()) {
				list.add(loc);
			}
		}

		return list;
	}

	public static void updateFavLocation(Context context, Location loc, FavLocation.LOC_TYPE loc_type) {
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

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// get location that needs to be updated from database
		Cursor c = db.query(
				DBHelper.TABLE_FAV_LOCS,    // The table to query
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
			if(loc_type == FavLocation.LOC_TYPE.FROM) {
				values.put("from_count", c.getInt(c.getColumnIndex("from_count")) + 1);
			}
			else if(loc_type == FavLocation.LOC_TYPE.VIA) {
				values.put("via_count", c.getInt(c.getColumnIndex("via_count")) + 1);
			}
			else if(loc_type == FavLocation.LOC_TYPE.TO) {
				values.put("to_count", c.getInt(c.getColumnIndex("to_count")) + 1);
			}
			db.update(DBHelper.TABLE_FAV_LOCS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
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
			if(loc_type == FavLocation.LOC_TYPE.FROM) {
				values.put("from_count", 1);
				values.put("via_count", 0);
				values.put("to_count", 0);
			}
			else if(loc_type == FavLocation.LOC_TYPE.VIA) {
				values.put("from_count", 0);
				values.put("via_count", 1);
				values.put("to_count", 0);
			}
			else if(loc_type == FavLocation.LOC_TYPE.TO) {
				values.put("from_count", 0);
				values.put("via_count", 0);
				values.put("to_count", 1);
			}

			db.insert(DBHelper.TABLE_FAV_LOCS, null, values);
		}

		c.close();
		db.close();
	}

	/* Home */

	public static Location getHome(Context context) {
		Location home = null;

		String network = getNetwork(context);
		if(network == null) return null;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(
				DBHelper.TABLE_HOME_LOCS,   // The table to query
				null,                       // The columns to return (null == all)
				"network = ?",              // The columns for the WHERE clause
				new String[] { network }, // The values for the WHERE clause
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		while(c.moveToNext()) {
			home = getLocation(c);
		}

		c.close();
		db.close();

		return home;
	}

	public static void setHome(Context context, Location home) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// prepare values for new home location
		ContentValues values = new ContentValues();
		values.put("network", getNetwork(context));
		values.put("type", home.type.name());
		values.put("id", home.id);
		values.put("lat", home.lat);
		values.put("lon", home.lon);
		values.put("place", home.place);
		values.put("name", home.name);

		// check if a previous home location was set
		Cursor c = db.query(
			DBHelper.TABLE_HOME_LOCS,   // The table to query
			new String[] { "_id" },     // The columns to return (null == all)
			"network = ?",              // The columns for the WHERE clause
			new String[] { getNetwork(context) }, // The values for the WHERE clause
			null,   // don't group the rows
			null,   // don't filter by row groups
			null    // The sort order
		);

		if(c.getCount() > 0) {
			// found previous home location, so update entry
			db.update(DBHelper.TABLE_HOME_LOCS, values, "network = ?", new String[] { getNetwork(context) });
		} else {
			// no previous home location found, so insert new entry
			db.insert(DBHelper.TABLE_HOME_LOCS, null, values);
		}

		c.close();
		db.close();
	}

}
