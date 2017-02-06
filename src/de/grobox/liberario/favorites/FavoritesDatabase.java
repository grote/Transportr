package de.grobox.liberario.favorites;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.grobox.liberario.data.DBHelper;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

import static de.grobox.liberario.settings.Preferences.getNetwork;
import static de.grobox.liberario.data.DBHelper.getLocation;
import static de.grobox.liberario.data.DBHelper.getLocationId;

public class FavoritesDatabase {

	public static List<FavoritesItem> getFavoriteTripList(Context context) {
		List<FavoritesItem> list = new ArrayList<>();

		// when the app starts for the first time, no network is selected
		if (getNetwork(context) == null) return list;

		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		String RECENT_TRIPS =
				"SELECT r.count, r.last_used, r.is_favourite, " +
						"l1.type AS from_type, l1.id AS from_id, l1.lat AS from_lat, l1.lon AS from_lon, l1.place AS from_place, l1.name AS from_name, " +
						"l2.type AS to_type,   l2.id AS to_id,   l2.lat AS to_lat,   l2.lon AS to_lon,   l2.place AS to_place,   l2.name AS to_name, " +
						"l3.type AS via_type,  l3.id AS via_id,  l3.lat AS via_lat,  l3.lon AS via_lon,  l3.place AS via_place,  l3.name AS via_name " +
						"FROM " + DBHelper.TABLE_FAVORITE_TRIPS + " r " +
						"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l1 ON r.from_loc = l1._id " +
						"INNER JOIN " + DBHelper.TABLE_FAV_LOCS + " l2 ON r.to_loc = l2._id " +
						"LEFT  JOIN " + DBHelper.TABLE_FAV_LOCS + " l3 ON r.via_loc = l3._id " +
						"WHERE r.network = ?";

		Cursor c = db.rawQuery(RECENT_TRIPS, new String[] { getNetwork(context) });

		while (c.moveToNext()) {
			list.add(getTrip(c));
		}
		c.close();
		db.close();
		return list;
	}

	@SuppressLint("SimpleDateFormat")
	private static FavoritesItem getTrip(Cursor c) {
		Location from = getLocation(c, "from_");
		Location via = getLocation(c, "via_");
		Location to = getLocation(c, "to_");
		String lastUsed = c.getString(c.getColumnIndex("last_used"));
		Date date;
		try {
			date = lastUsed == null ? new Date() : (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(lastUsed);
		} catch (ParseException e) {
			e.printStackTrace();
			date = new Date();
		}
		return new FavoritesItem(from, via, to, c.getInt(c.getColumnIndex("count")),
				date, c.getInt(c.getColumnIndex("is_favourite")) > 0);
	}

	public static void updateFavoriteTrip(Context context, FavoritesItem trip) {
		if (trip.getFrom() == null || trip.getTo() == null) {
			// this should never happen, but well...
			return;
		}
		if (trip.getFrom().type == LocationType.COORD) {
			// don't store GPS locations
			return;
		}
		if ((trip.getFrom().type == LocationType.ANY) ||
				(trip.getVia() != null && trip.getVia().type == LocationType.ANY) ||
				(trip.getTo().type == LocationType.ANY)) {
			// don't store trips with ANY locations (includes ambiguous locations)
			return;
		}
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, trip.getFrom(), getNetwork(context));
		int via_id = getLocationId(db, trip.getVia(), getNetwork(context));
		int to_id = getLocationId(db, trip.getTo(), getNetwork(context));

		if (from_id < 0 || to_id < 0) {
			db.close();
			return;
		}
		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		// try to find a trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_FAVORITE_TRIPS,    // The table to query
				new String[] { "_id", "count" },
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { getNetwork(context), from, to } :
						new String[] { getNetwork(context), from, via, to }
				),
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);
		ContentValues values = new ContentValues();

		if (c.moveToFirst()) {
			// increase counter by one for existing recent trip
			values.put("count", c.getInt(c.getColumnIndex("count")) + 1);

			// update last_used time
			@SuppressLint("SimpleDateFormat")
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put("last_used", df.format(Calendar.getInstance().getTime()));

			db.update(DBHelper.TABLE_FAVORITE_TRIPS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
		} else {
			// add new favorite trip trip database
			values.put("network", getNetwork(context));
			values.put("from_loc", from_id);
			if (via_id < 0) values.putNull("via_loc");
			else values.put("via_loc", via_id);
			values.put("to_loc", to_id);
			values.put("count", 1);
			values.put("is_favourite", 0);

			// insert current time as last_used
			@SuppressLint("SimpleDateFormat")
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			values.put("last_used", df.format(Calendar.getInstance().getTime()));

			db.insert(DBHelper.TABLE_FAVORITE_TRIPS, null, values);
		}
		c.close();
		db.close();
	}

	public static void toggleFavoriteTrip(Context context, FavoritesItem recent) {
		if (recent.getFrom() == null || recent.getTo() == null) {
			// this should never happen, but well...
			return;
		}
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), getNetwork(context));

		if (from_id < 0 || to_id < 0) {
			db.close();
			return;
		}
		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		// try to find a recent trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_FAVORITE_TRIPS,    // The table to query
				new String[] { "_id", "count" },
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { getNetwork(context), from, to } :
						new String[] { getNetwork(context), from, via, to }
				),
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);

		if (c.moveToFirst()) {
			ContentValues values = new ContentValues();
			values.put("is_favourite", recent.isFavorite() ? 0 : 1); // Toggle
			db.update(DBHelper.TABLE_FAVORITE_TRIPS, values, "_id = ?", new String[] { c.getString(c.getColumnIndex("_id")) });
		}
		c.close();
		db.close();
	}

	public static boolean isFavoriteTrip(Context context, FavoritesItem recent) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), getNetwork(context));

		if (from_id < 0 || to_id < 0) {
			db.close();
			return false;
		}
		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		// try to find a recent trip with these locations
		Cursor c = db.query(
				DBHelper.TABLE_FAVORITE_TRIPS,    // The table to query
				new String[] { "_id", "is_favourite" },
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { getNetwork(context), from, to } :
						new String[] { getNetwork(context), from, via, to }
				),
				null,   // don't group the rows
				null,   // don't filter by row groups
				null    // The sort order
		);
		boolean is_fav = false;

		if (c.moveToFirst()) {
			if (c.getInt(c.getColumnIndex("is_favourite")) > 0) {
				is_fav = true;
			}
		}
		c.close();
		db.close();
		return is_fav;
	}

	public static void deleteFavoriteTrip(Context context, FavoritesItem recent) {
		DBHelper mDbHelper = new DBHelper(context);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		int from_id = getLocationId(db, recent.getFrom(), getNetwork(context));
		int via_id = getLocationId(db, recent.getVia(), getNetwork(context));
		int to_id = getLocationId(db, recent.getTo(), getNetwork(context));

		if (from_id < 0 || to_id < 0) {
			db.close();
			return;
		}

		String from = String.valueOf(from_id);
		String via = String.valueOf(via_id);
		String to = String.valueOf(to_id);

		db.delete(DBHelper.TABLE_FAVORITE_TRIPS,
				(via_id < 0 ?
						"network = ? AND from_loc = ? AND via_loc IS NULL AND to_loc = ?" :
						"network = ? AND from_loc = ? AND via_loc = ? AND to_loc = ?"
				),
				(via_id < 0 ?
						new String[] { getNetwork(context), from, to } :
						new String[] { getNetwork(context), from, via, to }
				));
		db.close();
	}

}
