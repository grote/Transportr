/*    Transportr
 *    Copyright (C) 2013-2014 Torsten Grote
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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

@Deprecated
class DbHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "liberario.db";
	private static final int DB_VERSION = 5;

	static final String TABLE_FAV_LOCS = "fav_locations";
	static final String TABLE_FAVORITE_TRIPS = "recent_trips";
	static final String TABLE_HOME_LOCS = "home_locations";
	static final String TABLE_WORK_LOCS = "work_locations";

	private static final String NETWORK = "network STRING NOT NULL";

	private static final String LOCATION =
			"type STRING NOT NULL, " +
					"id STRING, " +
					"lat INTEGER NOT NULL DEFAULT 0, " +
					"lon INTEGER NOT NULL DEFAULT 0, " +
					"place TEXT, " +
					"name TEXT";

	private static final String CREATE_TABLE_FAV_LOCS =
			"CREATE TABLE " + TABLE_FAV_LOCS + " (" +
					"_id INTEGER PRIMARY KEY, " +
					NETWORK + ", " +
					LOCATION + ", " +
					"from_count INTEGER NOT NULL DEFAULT 0, " +
					"via_count INTEGER NOT NULL DEFAULT 0, " +
					"to_count INTEGER NOT NULL DEFAULT 0" +
					" )";

	private static final String CREATE_TABLE_RECENT_TRIPS =
			"CREATE TABLE " + TABLE_FAVORITE_TRIPS + " (" +
					"_id INTEGER PRIMARY KEY, " +
					NETWORK + ", " +
					"from_loc INTEGER NOT NULL, " +
					"via_loc INTEGER DEFAULT NULL, " +
					"to_loc INTEGER NOT NULL, " +
					"count INTEGER NOT NULL DEFAULT 0, " +
					"last_used DATETIME, " +
					"is_favourite INTEGER NOT NULL" +
					" )";

	private static final String CREATE_TABLE_HOME_LOCS =
			"CREATE TABLE " + TABLE_HOME_LOCS + " (" +
					"_id INTEGER PRIMARY KEY, " +
					NETWORK + ", " +
					LOCATION + ", " +
					"UNIQUE(network)" +
					" )";

	private static final String CREATE_TABLE_WORK_LOCS =
			"CREATE TABLE " + TABLE_WORK_LOCS + " (" +
					"_id INTEGER PRIMARY KEY, " +
					NETWORK + ", " +
					LOCATION + ", " +
					"UNIQUE(network)" +
					" )";

	DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_FAV_LOCS);
		db.execSQL(CREATE_TABLE_RECENT_TRIPS);
		db.execSQL(CREATE_TABLE_HOME_LOCS);
		db.execSQL(CREATE_TABLE_WORK_LOCS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		int upgradeTo = oldVersion + 1;
		while (upgradeTo <= newVersion) {
			switch (upgradeTo) {
				case 2:
					db.execSQL("ALTER TABLE fav_trips ADD COLUMN last_used DATETIME");
					break;
				case 3:
					db.execSQL("ALTER TABLE fav_trips RENAME TO " + TABLE_FAVORITE_TRIPS);
					db.execSQL("ALTER TABLE " + TABLE_FAVORITE_TRIPS + " ADD COLUMN is_favourite INTEGER");
					break;
				case 4:
					db.execSQL("ALTER TABLE " + TABLE_FAV_LOCS + " ADD COLUMN via_count INTEGER NOT NULL DEFAULT 0");
					db.execSQL("ALTER TABLE " + TABLE_FAVORITE_TRIPS + " ADD COLUMN via_loc INTEGER DEFAULT NULL");
					break;
				case 5:
					db.execSQL(CREATE_TABLE_WORK_LOCS);
					break;
			}
			upgradeTo++;
		}
	}

	@Nullable
	public static Location getLocation(Cursor c, String pre) {
		if (c.isNull(c.getColumnIndex("pre_type".replace("pre_", pre)))) return null;

		return new Location(
				LocationType.valueOf(c.getString(c.getColumnIndex("pre_type".replace("pre_", pre)))),
				c.getString(c.getColumnIndex("pre_id".replace("pre_", pre))),
				c.getInt(c.getColumnIndex("pre_lat".replace("pre_", pre))),
				c.getInt(c.getColumnIndex("pre_lon".replace("pre_", pre))),
				c.getString(c.getColumnIndex("pre_place".replace("pre_", pre))),
				c.getString(c.getColumnIndex("pre_name".replace("pre_", pre)))
		);
	}

	public static Location getLocation(Cursor c) {
		return getLocation(c, "");
	}

	static int getLocationId(SQLiteDatabase db, Location loc, String network) {
		String whereClause;
		String[] whereArgs;

		if (loc == null) {
			return -1;
		} else if (loc.hasId()) {
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
				DbHelper.TABLE_FAV_LOCS,    // The table to query
				new String[] { "_id" },
				whereClause,
				whereArgs,
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		if (c.moveToFirst()) {
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
