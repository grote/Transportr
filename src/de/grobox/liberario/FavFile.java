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

import android.content.Context;

public class FavFile {

	static public final String FAV_FILE = "favs";

	@SuppressWarnings("unchecked")
	public static List<FavLocation> getFavList(Context context) {
		List<FavLocation> fav_list = new ArrayList<FavLocation>();

		FileInputStream fis = null;
		try {
			fis = context.openFileInput(FAV_FILE);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		List<FavLocation> fav_list = getFavList(context);
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

	public static void setFavList(Context context, List<FavLocation> favList) {
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(FAV_FILE, Context.MODE_PRIVATE);
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

	public static void resetFavList(Context context) {
		List<FavLocation> fav_list = new ArrayList<FavLocation>();
		setFavList(context, fav_list);
	}

}
