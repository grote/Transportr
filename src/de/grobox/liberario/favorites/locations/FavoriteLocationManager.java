/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.liberario.favorites.locations;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.AbstractManager;
import de.grobox.liberario.data.SpecialLocationDb;
import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.LocationRepository;
import de.grobox.liberario.favorites.trips.FavoriteTripManager;
import de.grobox.liberario.locations.WrapLocation;

import static de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType.TO;
import static de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType.VIA;

@ParametersAreNonnullByDefault
public class FavoriteLocationManager extends AbstractManager {

	private final Context context;
	private final LocationRepository locationRepository;
	private final FavoriteTripManager favoriteTripManager;

	private @Nullable HomeLocation home;
	private @Nullable WrapLocation work;
	private @Nullable List<FavoriteLocation> favoriteLocations;
	private List<FavoriteLocationsLoadedListener> favoriteLocationsLoadedListeners = new ArrayList<>();

	@Inject
	public FavoriteLocationManager(Context context, LocationRepository locationRepository, FavoriteTripManager favoriteTripManager) {
		this.context = context;
		this.locationRepository = locationRepository;
		this.favoriteTripManager = favoriteTripManager;
		observeHome();
		loadWork();
		observeLocations();
	}

	@Nullable
	public HomeLocation getHome() {
		return home;
	}

	private void observeHome() {
		locationRepository.getHomeLocation().observeForever(newHomeLocation -> {
			if (newHomeLocation == null) return;
			Log.w("TEST", "OBSERVED A NEW HOME LOCATION: " + newHomeLocation.toString());
			home = newHomeLocation;
		});
	}

	public void setHome(final WrapLocation home) {
		locationRepository.setHomeLocation(home);
	}

	@Nullable
	public WrapLocation getWork() {
		return work;
	}

	private void loadWork() {
		runOnBackgroundThread(() -> setLoadedWork(SpecialLocationDb.getWork(context)));
	}

	private void setLoadedWork(@Nullable final WrapLocation work) {
		runOnUiThread(() -> FavoriteLocationManager.this.work = work);
	}

	public void setWork(final WrapLocation work) {
		// TODO
		this.work = work;
	}

	/**
	 * Returns a copy of cached favorite locations.
	 */
	@Nullable
	public List<WrapLocation> getLocations(FavLocationType sort) {
		if (favoriteLocations == null) return null;
		List<WrapLocation> list = new ArrayList<>();
		List<FavoriteLocation> tmpList = new ArrayList<>(favoriteLocations);

		if (sort == FROM) {
			Collections.sort(tmpList, FavoriteLocation.FromComparator);
		} else if (sort == VIA) {
			Collections.sort(tmpList, FavoriteLocation.ViaComparator);
		} else if (sort == TO) {
			Collections.sort(tmpList, FavoriteLocation.ToComparator);
		}
		list.addAll(tmpList);
		return list;
	}

	@UiThread
	public void addLocation(final WrapLocation location) {
		final FavLocationType type = FROM;  // TODO
		locationRepository.addFavoriteLocation(location, type);
	}

	private void observeLocations() {
		locationRepository.getFavoriteLocations().observeForever(locations -> {
			if (locations == null) return;
			Log.w("TEST", "OBSERVED NEW FAV LOCATIONS: " + locations.toString());
			favoriteLocations = locations;
			for (FavoriteLocationsLoadedListener l : favoriteLocationsLoadedListeners) {
				if (l != null) l.onFavoriteLocationsLoaded();
			}
			favoriteLocationsLoadedListeners.clear();
		});
	}

	public interface FavoriteLocationsLoadedListener {
		void onFavoriteLocationsLoaded();
	}

}
