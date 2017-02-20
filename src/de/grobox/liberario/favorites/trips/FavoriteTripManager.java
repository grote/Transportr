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

package de.grobox.liberario.favorites.trips;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.AbstractManager;
import de.grobox.liberario.data.SpecialLocationDb;
import de.grobox.liberario.networks.TransportNetwork;
import de.grobox.liberario.networks.TransportNetworkManager.TransportNetworkChangedListener;
import de.schildbach.pte.dto.Location;

import static de.grobox.liberario.data.FavoritesDb.getFavoriteTripList;
import static de.grobox.liberario.data.FavoritesDb.updateFavoriteTrip;

@ParametersAreNonnullByDefault
public class FavoriteTripManager extends AbstractManager implements TransportNetworkChangedListener {

	private final Context context;

	private @Nullable List<FavoriteTripItem> favoriteTrips;
	private List<FavoriteTripsLoadedListener> favoriteTripsLoadedListeners = new ArrayList<>();

	@Inject
	public FavoriteTripManager(Context context) {
		this.context = context;
		loadTrips();
	}

	/**
	 * Returns a copy of cached favorite trips.
	 */
	@Nullable
	public List<FavoriteTripItem> getTrips() {
		return favoriteTrips == null ? null : new ArrayList<>(favoriteTrips);
	}

	@UiThread
	public void addTrip(Location from, @Nullable Location via, Location to) {
		final FavoriteTripItem trip = new FavoriteTripItem(from, via, to);

		if (favoriteTrips != null && favoriteTrips.contains(trip)) {
			favoriteTrips.get(favoriteTrips.indexOf(trip)).use();
		} else if (favoriteTrips != null) {
			favoriteTrips.add(trip);
		}

		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				updateFavoriteTrip(context, trip);
			}
		});
	}

	public void loadTrips() {
		runOnBackgroundThread(new Runnable() {
			@Override
			public void run() {
				List<FavoriteTripItem> favorites = getFavoriteTripList(context);
				Location home = SpecialLocationDb.getHome(context);
				Location work = SpecialLocationDb.getWork(context);
				favorites.add(new FavoriteTripItem(FavoriteTripType.HOME, home));
				favorites.add(new FavoriteTripItem(FavoriteTripType.WORK, work));
				setLoadedFavoriteTrips(favorites);
			}
		});
	}

	private void setLoadedFavoriteTrips(final List<FavoriteTripItem> favoriteTrips) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				FavoriteTripManager.this.favoriteTrips = favoriteTrips;
				for (FavoriteTripsLoadedListener l : favoriteTripsLoadedListeners) {
					if (l != null) l.onFavoriteTripsLoaded(favoriteTrips);
				}
				favoriteTripsLoadedListeners.clear();
			}
		});
	}

	/**
	 * Adds a listener that will be informed once favorite trips have been loaded.
	 * The listener will be removed automatically after being informed.
	 */
	@UiThread
	void addOnFavoriteTripsLoadedListener(FavoriteTripsLoadedListener listener) {
		favoriteTripsLoadedListeners.add(listener);
	}

	@Override
	public void onTransportNetworkChanged(TransportNetwork network) {
		favoriteTrips = null;
		loadTrips();
	}

	interface FavoriteTripsLoadedListener {
		void onFavoriteTripsLoaded(List<FavoriteTripItem> trips);
	}

}
