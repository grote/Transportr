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

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.WrapLocation;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

public class RecentsDB {

	/* FavLocation */

	public static List<FavLocation> getFavLocationList(Context context) {
		List<FavLocation> fav_list = new ArrayList<>();
		String network = Preferences.getNetwork(context);
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
			// don't store GPS or ANY locations
			return;
		}

		if(loc.id != null && loc.id.equals("IS_AMBIGUOUS")) {
			// don't store ambiguous locations
			return;
		}

		String whereClause;
		String[] whereArgs;

		if(loc.hasId()) {
			// use location id to identify location
			whereClause = "network = ? AND id = ?";
			whereArgs = new String[] { Preferences.getNetwork(context), loc.id };
		} else {
			// use other values to identify location
			String lat = String.valueOf(loc.lat);
			String lon = String.valueOf(loc.lon);
			String place = loc.place == null ? "" : loc.place;
			String name = loc.name == null ? "" : loc.name;

			whereClause = "network = ? AND type = ? AND lat = ? AND lon = ? AND place = ? AND name = ?";
			whereArgs = new String[] { Preferences.getNetwork(context), loc.type.name(), lat, lon, place, name };
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


	/* RecentTrip */

	public static List<RecentTrip> getRecentTripList(Context context, final boolean sort_count) {
		List<RecentTrip> recent_list = new ArrayList<>();

		// when the app starts for the first time, no network is selected
		if(Preferences.getNetwork(context) == null)  return recent_list;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		final String RECENT_TRIPS =
			"SELECT r.count, r.last_used, r.is_favourite, " +
			"l1.type AS from_type, l1.id AS from_id, l1.lat AS from_lat, l1.lon AS from_lon, l1.place AS from_place, l1.name AS from_name, " +
			"l2.type AS to_type,   l2.id AS to_id,   l2.lat AS to_lat,   l2.lon AS to_lon,   l2.place AS to_place,   l2.name AS to_name, " +
			"l3.type AS via_type,  l3.id AS via_id,  l3.lat AS via_lat,  l3.lon AS via_lon,  l3.place AS via_place,  l3.name AS via_name " +
			"FROM " + DBHelper.TABLE_RECENT_TRIPS + " r " +
			"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l1 ON r.from_loc = l1._id " +
			"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l2 ON r.to_loc = l2._id " +
			"LEFT  JOIN " + DBHelper.TABLE_FAV_LOCS + " l3 ON r.via_loc = l3._id " +
			"WHERE r.network = ? " +
			"ORDER BY " + (sort_count ? "r.count" : "r.last_used") + " DESC";

		Cursor c = db.rawQuery(RECENT_TRIPS, new String[]{ Preferences.getNetwork(context) });

		while(c.moveToNext()) {
			Location from = getLocation(c, "from_");
			Location via = getLocation(c, "via_");
			Location to = getLocation(c, "to_");
			RecentTrip trip = new RecentTrip(from, via, to, c.getInt(c.getColumnIndex("count")),
					c.getString(c.getColumnIndex("last_used")), c.getInt(c.getColumnIndex("is_favourite")) > 0);
			recent_list.add(trip);
		}

		c.close();
		db.close();

		return recent_list;
	}

	public static List<RecentTrip> getFavouriteTripList(Context context) {
		List<RecentTrip> favourite_list = new ArrayList<>();

		// when the app starts for the first time, no network is selected
		if(Preferences.getNetwork(context) == null)  return favourite_list;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		final String FAVOURITE_TRIPS =
			"SELECT r.count, r.last_used, r.is_favourite, " +
			"l1.type AS from_type, l1.id AS from_id, l1.lat AS from_lat, l1.lon AS from_lon, l1.place AS from_place, l1.name AS from_name, " +
			"l2.type AS to_type,   l2.id AS to_id,   l2.lat AS to_lat,   l2.lon AS to_lon,   l2.place AS to_place,   l2.name AS to_name, " +
			"l3.type AS via_type,  l3.id AS via_id,  l3.lat AS via_lat,  l3.lon AS via_lon,  l3.place AS via_place,  l3.name AS via_name " +
			"FROM " + DBHelper.TABLE_RECENT_TRIPS + " r " +
			"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l1 ON r.from_loc = l1._id " +
			"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l2 ON r.to_loc = l2._id " +
			"LEFT  JOIN " + DBHelper.TABLE_FAV_LOCS + " l3 ON r.via_loc = l3._id " +
			"WHERE r.network = ? AND r.is_favourite > 0 " +
			"ORDER BY r.count DESC";

		Cursor c = db.rawQuery(FAVOURITE_TRIPS, new String[]{ Preferences.getNetwork(context) });

		while(c.moveToNext()) {
			Location from = getLocation(c, "from_");
			Location via = getLocation(c, "via_");
			Location to = getLocation(c, "to_");
			RecentTrip trip = new RecentTrip(from, via, to, c.getInt(c.getColumnIndex("count")),
					c.getString(c.getColumnIndex("last_used")), c.getInt(c.getColumnIndex("is_favourite")) > 0);
			favourite_list.add(trip);
		}

		c.close();
		db.close();

		return favourite_list;
	}

	public static void updateRecentTrip(Context context, RecentTrip recent) {
		if(recent.getFrom() == null || recent.getTo() == null) {
			// this should never happen, but well...
			return;
		}

		if(recent.getFrom().type == LocationType.COORD) {
			// don't store GPS locations
			return;
		}

		if( (recent.getFrom().id != null && recent.getFrom().id.equals("IS_AMBIGUOUS")) ||
				(recent.getVia() != null && recent.getVia().id != null && recent.getVia().id.equals("IS_AMBIGUOUS")) ||
				(recent.getTo().id != null && recent.getTo().id.equals("IS_AMBIGUOUS")) ) {
			// don't store trips with ambiguous locations
			return;
		}

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), Preferences.getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), Preferences.getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), Preferences.getNetwork(context));

		if(from_id < 0 || to_id < 0) {
			db.close();
			return;
		}

		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		// try to find a recent trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_RECENT_TRIPS,    // The table to query
				new String[] { "_id", "count" },
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { Preferences.getNetwork(context), from, to } :
						new String[] { Preferences.getNetwork(context), from, via, to }
				),
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		ContentValues values = new ContentValues();

		if(c.moveToFirst()) {
			// increase counter by one for existing recent trip
			values.put("count", c.getInt(c.getColumnIndex("count")) + 1);

			// update last_used time
			@SuppressLint("SimpleDateFormat")
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put("last_used", df.format(Calendar.getInstance().getTime()));

			db.update(DBHelper.TABLE_RECENT_TRIPS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
		}
		else {
			// add new recent trip trip database
			values.put("network", Preferences.getNetwork(context));
			values.put("from_loc", from_id);
			if(via_id < 0) values.putNull("via_loc");
			else values.put("via_loc", via_id);
			values.put("to_loc", to_id);
			values.put("count", 1);
			values.put("is_favourite", 0);

			// insert current time as last_used
			@SuppressLint("SimpleDateFormat")
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put("last_used", df.format(Calendar.getInstance().getTime()));

			db.insert(DBHelper.TABLE_RECENT_TRIPS, null, values);
		}

		c.close();
		db.close();
	}

	public static void toggleFavouriteTrip(Context context, RecentTrip recent) {
		if(recent.getFrom() == null || recent.getTo() == null) {
			// this should never happen, but well...
			return;
		}

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), Preferences.getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), Preferences.getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), Preferences.getNetwork(context));

		if(from_id < 0 || to_id < 0) {
			db.close();
			return;
		}

		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		// try to find a recent trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_RECENT_TRIPS,    // The table to query
				new String[] { "_id", "count" },
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { Preferences.getNetwork(context), from, to } :
						new String[] { Preferences.getNetwork(context), from, via, to }
				),
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		if(c.moveToFirst()) {
			ContentValues values = new ContentValues();
			values.put("is_favourite", recent.isFavourite() ? 0 : 1); // Toggle
			db.update(DBHelper.TABLE_RECENT_TRIPS, values, "_id = ?", new String[]{c.getString(c.getColumnIndex("_id")) });
		}

		c.close();
		db.close();
	}

	public static boolean isFavedRecentTrip(Context context, RecentTrip recent) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), Preferences.getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), Preferences.getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), Preferences.getNetwork(context));

		if(from_id < 0 || to_id < 0) {
			db.close();
			return false;
		}

		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		// try to find a recent trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_RECENT_TRIPS,    // The table to query
				new String[] { "_id", "is_favourite" },
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { Preferences.getNetwork(context), from, to } :
						new String[] { Preferences.getNetwork(context), from, via, to }
				),
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		boolean is_fav = false;

		if(c.moveToFirst()) {
			if(c.getInt(c.getColumnIndex("is_favourite")) > 0) {
				is_fav = true;
			}
		}

		c.close();
		db.close();

		return is_fav;
	}

	public static void deleteRecentTrip(Context context, RecentTrip recent) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), Preferences.getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), Preferences.getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), Preferences.getNetwork(context));

		if(from_id < 0 || to_id < 0) {
			db.close();
			return;
		}

		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		db.delete(DBHelper.TABLE_RECENT_TRIPS,
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { Preferences.getNetwork(context), from, to } :
						new String[] { Preferences.getNetwork(context), from, via, to }
				));

		db.close();
	}


	/* Home */

	public static Location getHome(Context context) {
		Location home = null;

		String network = Preferences.getNetwork(context);
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

		c.close();
		db.close();
	}

	private static Location getLocation(Cursor c) {
		return getLocation(c, "");
	}

	private static Location getLocation(Cursor c, String pre) {
		if(c.isNull(c.getColumnIndex("pre_type".replace("pre_", pre)))) return null;

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
		String whereClause;
		String[] whereArgs;

		if(loc == null) {
			return -1;
		} else if(loc.hasId()) {
			whereClause = "network = ? AND id = ?";
			whereArgs = new String[] { network, loc.id };
		} else {
			// location has no ID, so use complicated way of assembling query
			String lat = String.valueOf(loc.lat);
			String lon = String.valueOf(loc.lon);
			String place = loc.place == null ? "" : loc.place;
			String name = loc.name == null ? "" : loc.name;

			whereClause = "network = ? AND type = ? AND lat = ? AND lon = ? AND place = ? AND name = ?";
			whereArgs = new String[] { network, loc.type.name(), lat, lon, place, name };
		}

		// get from location ID from database
		Cursor c = db.query(
				DBHelper.TABLE_FAV_LOCS,    // The table to query
				new String[] { "_id" },
				whereClause,
				whereArgs,
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		if(c.moveToFirst()) {
			int loc_id = c.getInt(c.getColumnIndex("_id"));
			c.close();
			Log.d("getLocationId", "Found location: " + loc.toString());
			return loc_id;
		} else {
			c.close();
			Log.d("getLocationId", "Could not find location: " + loc.toString());
			return -1;
		}
	}
}
