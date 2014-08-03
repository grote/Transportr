/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.FavTrip;
import de.grobox.liberario.Preferences;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

public class FavDB {

	/* FavLocation */

	public static List<FavLocation> getFavLocationList(Context context) {
		List<FavLocation> fav_list = new ArrayList<FavLocation>();

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(
			DBHelper.TABLE_FAV_LOCS,    // The table to query
			null,                       // The columns to return (null == all)
			"network = ?",              // The columns for the WHERE clause
			new String[] { Preferences.getNetwork(context) }, // The values for the WHERE clause
			null,   // don't group the rows
			null,   // don't filter by row groups
			null    // The sort order
		);

		while(c.moveToNext()) {
			Location loc = getLocation(c);
			FavLocation fav_loc = new FavLocation(loc, c.getInt(c.getColumnIndex("from_count")), c.getInt(c.getColumnIndex("to_count")));
			fav_list.add(fav_loc);
		}

		c.close();
		db.close();

		return fav_list;
	}

	public static List<Location> getFavLocationList(Context context, FavLocation.LOC_TYPE sort, boolean onlyIDs) {
		List<FavLocation> fav_list = getFavLocationList(context);
		List<Location> list = new ArrayList<Location>();

		if(sort == FavLocation.LOC_TYPE.FROM) {
			Collections.sort(fav_list, FavLocation.FromComparator);
		}
		else if(sort == FavLocation.LOC_TYPE.TO) {
			Collections.sort(fav_list, FavLocation.ToComparator);
		}

		for(final FavLocation loc : fav_list) {
			if(!onlyIDs || loc.getLocation().hasId()) {
				list.add(loc.getLocation());
			}
		}

		return list;
	}

	public static void updateFavLocation(Context context, Location loc, FavLocation.LOC_TYPE loc_type) {
		// don't save locations with no id
		if(!loc.hasId()) return;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// get location that needs to be updated from database
		Cursor c = db.query(
				DBHelper.TABLE_FAV_LOCS,    // The table to query
				new String[] { "_id", "from_count", "to_count" },
				"network = ? AND id = ?",
				new String[] { Preferences.getNetwork(context), loc.id },
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
			else if(loc_type == FavLocation.LOC_TYPE.TO) {
				values.put("to_count", c.getInt(c.getColumnIndex("to_count")) + 1);
			}
			db.update(DBHelper.TABLE_FAV_LOCS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
		}
		else {
			// add new favorite location
			values.put("network", Preferences.getNetwork(context));
			values.put("type", loc.type.name());
			values.put("id", loc.id);
			values.put("lat", loc.lat);
			values.put("lon", loc.lon);
			values.put("place", loc.place);
			values.put("name", loc.name);

			// set counter to one
			if(loc_type == FavLocation.LOC_TYPE.FROM) {
				values.put("from_count", 1);
				values.put("to_count", 0);
			}
			else if(loc_type == FavLocation.LOC_TYPE.TO) {
				values.put("from_count", 0);
				values.put("to_count", 1);
			}

			db.insert(DBHelper.TABLE_FAV_LOCS, null, values);
		}

		c.close();
		db.close();
	}


	/* FavTrip */

	public static List<FavTrip> getFavTripList(Context context) {
		List<FavTrip> fav_list = new ArrayList<FavTrip>();

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		final String FAV_TRIPS =
			"SELECT f.count, " +
			"l1.type AS from_type, l1.id AS from_id, l1.lat AS from_lat, l1.lon AS from_lon, l1.place AS from_place, l1.name AS from_name, " +
			"l2.type AS to_type,   l2.id AS to_id,   l2.lat AS to_lat,   l2.lon AS to_lon,   l2.place AS to_place,   l2.name AS to_name " +
			"FROM " + DBHelper.TABLE_FAV_TRIPS + " f " +
			"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l1 ON f.from_loc = l1._id " +
			"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l2 ON f.to_loc = l2._id " +
			"WHERE f.network = ? " +
			"ORDER BY f.count DESC";

		Cursor c = db.rawQuery(FAV_TRIPS, new String[]{ Preferences.getNetwork(context) });

		while(c.moveToNext()) {
			Location from = getLocation(c, "from_");
			Location to = getLocation(c, "to_");
			FavTrip trip = new FavTrip(from, to, c.getInt(c.getColumnIndex("count")));
			fav_list.add(trip);
		}

		c.close();
		db.close();

		return fav_list;
	}

	public static void updateFavTrip(Context context, FavTrip fav) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, fav.getFrom(), Preferences.getNetwork(context));
		int to_id = getLocationId(db, fav.getTo(), Preferences.getNetwork(context));

		// try to find a fav trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_FAV_TRIPS,    // The table to query
				new String[] { "_id", "count" },
				"network = ? AND from_loc = ? AND to_loc = ?",
				new String[] { Preferences.getNetwork(context), String.valueOf(from_id), String.valueOf(to_id) },
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		ContentValues values = new ContentValues();

		if(c.moveToFirst()) {
			// increase counter by one for existing fav trip
			values.put("count", c.getInt(c.getColumnIndex("count")) + 1);

			db.update(DBHelper.TABLE_FAV_TRIPS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
		}
		else {
			// add new fav trip trip database
			values.put("network", Preferences.getNetwork(context));
			values.put("from_loc", from_id);
			values.put("to_loc", to_id);
			values.put("count", 1);

			db.insert(DBHelper.TABLE_FAV_TRIPS, null, values);
		}

		c.close();
		db.close();
	}

	public static boolean isFavTrip(Context context, FavTrip fav) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		String from_id = String.valueOf(getLocationId(db, fav.getFrom(), Preferences.getNetwork(context)));
		String to_id = String.valueOf(getLocationId(db, fav.getTo(), Preferences.getNetwork(context)));

		// try to find a fav trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_FAV_TRIPS,    // The table to query
				new String[] { "_id", "count" },
				"network = ? AND from_loc = ? AND to_loc = ?",
				new String[] { Preferences.getNetwork(context), String.valueOf(from_id), String.valueOf(to_id) },
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		return c.moveToFirst();
	}

	public static void unfavTrip(Context context, FavTrip fav) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		String from_id = String.valueOf(getLocationId(db, fav.getFrom(), Preferences.getNetwork(context)));
		String to_id = String.valueOf(getLocationId(db, fav.getTo(), Preferences.getNetwork(context)));

		db.delete(DBHelper.TABLE_FAV_TRIPS, "network = ? AND from_loc = ? AND to_loc =? ", new String[] { Preferences.getNetwork(context), from_id, to_id });

		db.close();
	}


	/* Home */

	public static Location getHome(Context context) {
		Location home = null;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(
				DBHelper.TABLE_HOME_LOCS,   // The table to query
				null,                       // The columns to return (null == all)
				"network = ?",              // The columns for the WHERE clause
				new String[] { Preferences.getNetwork(context) }, // The values for the WHERE clause
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
		values.put("network", Preferences.getNetwork(context));
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
			new String[] { Preferences.getNetwork(context) }, // The values for the WHERE clause
			null,   // don't group the rows
			null,   // don't filter by row groups
			null    // The sort order
		);

		if(c.getCount() > 0) {
			// found previous home location, so update entry
			db.update(DBHelper.TABLE_HOME_LOCS, values, "network = ?", new String[] { Preferences.getNetwork(context) });
		} else {
			// no previous home location found, so insert new entry
			db.insert(DBHelper.TABLE_HOME_LOCS, null, values);
		}

		db.close();
	}

	private static Location getLocation(Cursor c) {
		return getLocation(c, "");
	}

	private static Location getLocation(Cursor c, String pre) {
		return new Location(
			LocationType.valueOf(c.getString(c.getColumnIndex("pre_type".replace("pre_", pre)))),
			c.getString(c.getColumnIndex("pre_id".replace("pre_", pre))),
			c.getInt(c.getColumnIndex("pre_lat".replace("pre_", pre))),
			c.getInt(c.getColumnIndex("pre_lon".replace("pre_", pre))),
			c.getString(c.getColumnIndex("pre_place".replace("pre_", pre))),
			c.getString(c.getColumnIndex("pre_name".replace("pre_", pre)))
		);
	}

	private static int getLocationId(SQLiteDatabase db, Location loc, String network) {
		// get from location ID from database
		Cursor c = db.query(
				DBHelper.TABLE_FAV_LOCS,    // The table to query
				new String[] { "_id" },
				"network = ? AND id = ?",
				new String[] { network, loc.id },
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		if(c.moveToFirst()) {
			int loc_id = c.getInt(c.getColumnIndex("_id"));
			c.close();
			return loc_id;
		} else {
			return 0;
		}
	}
}
