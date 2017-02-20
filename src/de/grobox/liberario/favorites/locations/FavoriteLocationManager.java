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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.AbstractManager;
import de.grobox.liberario.data.LocationDb;
import de.grobox.liberario.data.SpecialLocationDb;
import de.grobox.liberario.favorites.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.favorites.trips.FavoriteTripManager;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager.TransportNetworkChangedListener;
import de.schildbach.pte.dto.Location;

import static de.grobox.liberario.data.LocationDb.updateFavLocation;
import static de.grobox.liberario.favorites.locations.FavoriteLocation.FavLocationType.FROM;

@ParametersAreNonnullByDefault
public class FavoriteLocationManager extends AbstractManager implements TransportNetworkChangedListener {

	private final Context context;
	private final FavoriteTripManager favoriteTripManager;

	private @Nullable Location home, work;
	private @Nullable List<FavoriteLocation> favoriteLocations;
	private List<FavoriteLocationsLoadedListener> favoriteLocationsLoadedListeners = new ArrayList<>();

	@Inject
	public FavoriteLocationManager(Context context, FavoriteTripManager favoriteTripManager) {
		this.context = context;
		this.favoriteTripManager = favoriteTripManager;
		loadHome();
		loadWork();
		loadLocations();
	}

	@Nullable
	public Location getHome() {
		return home;
	}

	private void loadHome() {
		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				setLoadedHome(SpecialLocationDb.getHome(context));
			}
		});
	}

	private void setLoadedHome(@Nullable final Location home) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FavoriteLocationManager.this.home = home;
			}
		});
	}

	public void setHome(final Location home) {
		this.home = home;
		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				SpecialLocationDb.setHome(context, home);
				favoriteTripManager.loadTrips();
			}
		});
	}

	@Nullable
	public Location getWork() {
		return work;
	}

	private void loadWork() {
		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				setLoadedWork(SpecialLocationDb.getWork(context));
			}
		});
	}

	private void setLoadedWork(@Nullable final Location work) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FavoriteLocationManager.this.work = work;
			}
		});
	}

	public void setWork(final Location work) {
		this.work = work;
		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				SpecialLocationDb.setWork(context, work);
				favoriteTripManager.loadTrips();
			}
		});
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
		} else if (sort == FavLocationType.VIA) {
			Collections.sort(tmpList, FavoriteLocation.ViaComparator);
		} else if (sort == FavLocationType.TO) {
			Collections.sort(tmpList, FavoriteLocation.ToComparator);
		}
		for (FavoriteLocation loc : tmpList) {
			list.add(loc);
		}
		return list;
	}

	@UiThread
	public void addLocation(final WrapLocation location) {
		final FavLocationType type = FROM;

		FavoriteLocation favoriteLocation = new FavoriteLocation(location.getLocation(), 0, 0, 0);
		if (favoriteLocations != null && favoriteLocations.contains(favoriteLocation)) {
			favoriteLocations.get(favoriteLocations.indexOf(favoriteLocation)).add(type);
		} else if (favoriteLocations != null) {
			favoriteLocations.add(favoriteLocation);
		}

		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				updateFavLocation(context, location.getLocation(), type);
			}
		});
	}

	private void loadLocations() {
		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				setLoadedFavoriteLocations(LocationDb.getFavLocationList(context));
			}
		});
	}

	private void setLoadedFavoriteLocations(final List<FavoriteLocation> favoriteLocations) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FavoriteLocationManager.this.favoriteLocations = favoriteLocations;
				for (FavoriteLocationsLoadedListener l : favoriteLocationsLoadedListeners) {
					if (l != null) l.onFavoriteLocationsLoaded();
				}
				favoriteLocationsLoadedListeners.clear();
			}
		});
	}

	/**
	 * Adds a listener that will be informed once favorite locations have been loaded.
	 * The listener will be removed automatically after being informed.
	 */
	@UiThread
	public void addOnFavoriteLocationsLoadedListener(FavoriteLocationsLoadedListener listener) {
		favoriteLocationsLoadedListeners.add(listener);
	}

	@Override
	public void onTransportNetworkChanged(TransportNetwork network) {
		home = null;
		work = null;
		favoriteLocations = null;
		loadHome();
		loadWork();
		loadLocations();
	}

	public interface FavoriteLocationsLoadedListener {
		void onFavoriteLocationsLoaded();
	}

}
