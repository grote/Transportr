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

package de.grobox.liberario;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Trip;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class FavFile {

	static public final String FAV_LOC_FILE = "favs_";
	static public final String FAV_TRIP_FILE = "favs_trip_";

	@SuppressWarnings("unchecked")
	public static List<FavLocation> getFavLocationList(Context context) {
		List<FavLocation> fav_list = new ArrayList<FavLocation>();

		FileInputStream fis = null;
		try {
			fis = context.openFileInput(FAV_LOC_FILE + Preferences.getNetwork(context));
		} catch (FileNotFoundException e) {
			return fav_list;
		}
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(fis);
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		}

		try {
			// TODO take care of unchecked cast
			if(is != null) fav_list = (List<FavLocation>) is.readObject();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		}

		return fav_list;
	}

	public static List<Location> getFavLocationList(Context context, FavLocation.LOC_TYPE sort) {
		List<FavLocation> fav_list = getFavLocationList(context);
		List<Location> list = new ArrayList<Location>();

		if(sort == FavLocation.LOC_TYPE.FROM) {
			Collections.sort(fav_list, FavLocation.FromComparator);
		}
		else if(sort == FavLocation.LOC_TYPE.TO) {
			Collections.sort(fav_list, FavLocation.ToComparator);
		}

		for(final FavLocation loc : fav_list) {
			list.add(loc.getLocation());
		}

		return list;
	}

	public static void setFavLocationList(Context context, List<FavLocation> favList) {
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(FAV_LOC_FILE + Preferences.getNetwork(context), Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(fos);
			os.writeObject(favList);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void resetFavLocationList(final Context context) {
		// show confirmation dialog
		new AlertDialog.Builder(context)
		.setMessage(context.getResources().getString(R.string.clear_favs))
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// actually reset the fav file
				List<FavLocation> fav_list = new ArrayList<FavLocation>();
				setFavLocationList(context, fav_list);
			}
		})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		})
		.show();
	}


	/* FavTrip */

	@SuppressWarnings("unchecked")
	public static List<FavTrip> getFavTripList(Context context) {
		List<FavTrip> fav_list = new ArrayList<FavTrip>();

		FileInputStream fis = null;
		try {
			fis = context.openFileInput(FAV_TRIP_FILE + Preferences.getNetwork(context));
		} catch (FileNotFoundException e) {
			return fav_list;
		}
		ObjectInputStream is = null;
		try {
			is = new ObjectInputStream(fis);
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		}

		try {
			// TODO take care of unchecked cast
			if(is != null) fav_list = (List<FavTrip>) is.readObject();
		} catch (OptionalDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		}
		try {
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return fav_list;
		}

		Collections.sort(fav_list);

		return fav_list;
	}

	public static void useFavTrip(Context context, FavTrip fav) {
		List<FavTrip> fav_list = getFavTripList(context);

		if(fav_list.contains(fav)){
			// increase counter by one for existing trip
			fav_list.get(fav_list.indexOf(fav)).addCount();

			FavFile.setFavTripList(context, fav_list);
		}
	}

	public static void setFavTripList(Context context, List<FavTrip> favList) {
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(FAV_TRIP_FILE + Preferences.getNetwork(context), Context.MODE_PRIVATE);
		} catch (FileNotFoundException e) {	}

		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(fos);
			os.writeObject(favList);
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static boolean isFavTrip(Context context, Trip trip) {
		if(trip == null) {
			return false;
		}

		FavTrip fav = new FavTrip(trip.from, trip.to);

		if(getFavTripList(context).contains(fav)){
			return true;
		}
		return false;
	}

	public static void favTrip(Context context, Trip trip) {
		FavTrip fav = new FavTrip(trip.from, trip.to);
		List<FavTrip> fav_list = getFavTripList(context);

		if(fav_list.contains(fav)){
			// increase counter by one for existing trip
			fav_list.get(fav_list.indexOf(fav)).addCount();
		}
		else {
			// add trip as favorite
			fav_list.add(fav);
		}

		FavFile.setFavTripList(context, fav_list);
	}

	public static void unfavTrip(Context context, Trip trip) {
		FavTrip fav = new FavTrip(trip.from, trip.to);
		List<FavTrip> fav_list = getFavTripList(context);

		if(fav_list.contains(fav)){
			// remove trip from favorites
			fav_list.remove(fav);
		}

		FavFile.setFavTripList(context, fav_list);
	}
}
