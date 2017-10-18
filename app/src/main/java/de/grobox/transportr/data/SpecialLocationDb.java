package de.grobox.transportr.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.locations.WrapLocation;
import de.schildbach.pte.dto.Location;

import static de.grobox.transportr.data.DbHelper.getLocation;
import static de.grobox.transportr.settings.Preferences.getNetwork;

@Deprecated
@ParametersAreNonnullByDefault
public class SpecialLocationDb {

	@Nullable
	private static WrapLocation getSpecialLocation(Context context, String table) {
		Location location = null;

		String network = getNetwork(context);
		if(network == null) return null;

		DbHelper mDbHelper = new DbHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		Cursor c = db.query(
				table,                      // The table to query
				null,                       // The columns to return (null == all)
				"network = ?",              // The columns for the WHERE clause
				new String[] { network }, // The values for the WHERE clause
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		while(c.moveToNext()) {
			location = getLocation(c);
		}

		c.close();
		db.close();

		if (location == null) return null;
		return new WrapLocation(location);
	}

	private static void setSpecialLocation(Context context, String table, Location location) {
		DbHelper mDbHelper = new DbHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// prepare values for new location
		ContentValues values = new ContentValues();
		values.put("network", getNetwork(context));
		values.put("type", location.type.name());
		values.put("id", location.id);
		values.put("lat", location.lat);
		values.put("lon", location.lon);
		values.put("place", location.place);
		values.put("name", location.name);

		// check if a previous home location was set
		Cursor c = db.query(
				table,   // The table to query
				new String[] { "_id" },     // The columns to return (null == all)
				"network = ?",              // The columns for the WHERE clause
				new String[] { getNetwork(context) }, // The values for the WHERE clause
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		if(c.getCount() > 0) {
			// found previous home location, so update entry
			db.update(table, values, "network = ?", new String[] { getNetwork(context) });
		} else {
			// no previous home location found, so insert new entry
			db.insert(table, null, values);
		}

		c.close();
		db.close();
	}

	@Nullable
	public static WrapLocation getHome(Context context) {
		return getSpecialLocation(context, DbHelper.TABLE_HOME_LOCS);
	}

	public static void setHome(Context context, Location home) {
		setSpecialLocation(context, DbHelper.TABLE_HOME_LOCS, home);
	}

	@Nullable
	public static WrapLocation getWork(Context context) {
		return getSpecialLocation(context, DbHelper.TABLE_WORK_LOCS);
	}

	public static void setWork(Context context, WrapLocation home) {
		setSpecialLocation(context, DbHelper.TABLE_WORK_LOCS, home.getLocation());
	}

}
