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

package de.grobox.liberario.networks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.data.LocationDb;
import de.grobox.liberario.data.SpecialLocationDb;
import de.grobox.liberario.locations.FavLocation;
import de.grobox.liberario.locations.FavLocation.FavLocationType;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.settings.SettingsManager;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;

@ParametersAreNonnullByDefault
public class TransportNetworkManager {

	private final Context context;
	private final SettingsManager settingsManager;

	@Nullable
	private TransportNetwork transportNetwork, transportNetwork2, transportNetwork3;
	@Nullable
	private Location home, work;
	@Nullable
	private List<FavLocation> favoriteLocations;
	private List<FavoriteLocationsLoadedListener> favoriteLocationsLoadedListeners = new ArrayList<>();
	private List<TransportNetworkChangedListener> transportNetworkChangedListeners = new ArrayList<>();

	@Inject
	public TransportNetworkManager(Context context, SettingsManager settingsManager) {
		this.context = context;
		this.settingsManager = settingsManager;
		transportNetwork = loadTransportNetwork(1);
		transportNetwork2 = loadTransportNetwork(2);
		transportNetwork3 = loadTransportNetwork(3);
		loadHome();
		loadWork();
		loadFavoriteLocations();
	}

	@Nullable
	private TransportNetwork loadTransportNetwork(int i) {
		NetworkId networkId = settingsManager.getNetworkId(i);

		if (networkId != null) {
			for (TransportNetwork network : TransportNetworks.networks) {
				if (network.getId().equals(networkId)) return network;
			}
		}
		return null;
	}

	@Nullable
	public TransportNetwork getTransportNetwork() {
		return getTransportNetwork(0);
	}

	@Nullable
	public TransportNetwork getTransportNetwork(int i) {
		if (i == 0 || i == 1) {
			return transportNetwork;
		} else if (i == 2) {
			return transportNetwork2;
		} else if (i == 3) {
			return transportNetwork3;
		} else {
			throw new IllegalArgumentException();
		}
	}

	public void setTransportNetwork(TransportNetwork transportNetwork) {
		Log.e("TEST", "SET TRANSPORT NETWORK: " + transportNetwork.getId().name());

		// check if same network was selected again
		if (this.transportNetwork != null && this.transportNetwork.equals(transportNetwork)) return;
		settingsManager.setNetworkId(transportNetwork.getId());

		// move 2nd network to 3rd if existing and not re-selected
		if (this.transportNetwork2 != null && !this.transportNetwork2.equals(transportNetwork)) {
			this.transportNetwork3 = this.transportNetwork2;
		}
		// swap remaining networks
		this.transportNetwork2 = this.transportNetwork;
		this.transportNetwork = transportNetwork;

		// TODO improve
		home = null;
		loadHome();
		work = null;
		loadWork();
		favoriteLocations = null;
		loadFavoriteLocations();

		// inform listeners
		for (TransportNetworkChangedListener l : transportNetworkChangedListeners) {
			if (l != null) l.onTransportNetworkChanged(transportNetwork);
		}
	}

	@Nullable
	public Location getHome() {
		return home;
	}

	private void loadHome() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				setLoadedHome(SpecialLocationDb.getHome(context));
			}
		}).start();
	}

	private void setLoadedHome(@Nullable final Location home) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				TransportNetworkManager.this.home = home;
			}
		});
	}

	public void setHome(final Location home) {
		this.home = home;
		new Thread(new Runnable() {
			@Override
			public void run() {
				SpecialLocationDb.setHome(context, home);
			}
		}).start();
	}

	@Nullable
	public Location getWork() {
		return work;
	}

	private void loadWork() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				setLoadedWork(SpecialLocationDb.getWork(context));
			}
		}).start();
	}

	private void setLoadedWork(@Nullable final Location work) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				TransportNetworkManager.this.work = work;
			}
		});
	}

	public void setWork(final Location work) {
		this.work = work;
		new Thread(new Runnable() {
			@Override
			public void run() {
				SpecialLocationDb.setWork(context, work);
			}
		}).start();
	}

	/**
	 * Returns a copy of cached favorite locations.
	 */
	@Nullable
	public List<FavLocation> getFavoriteLocations() {
		return favoriteLocations == null ? null : new ArrayList<>(favoriteLocations);
	}

	@Nullable
	public List<WrapLocation> getFavoriteLocations(FavLocationType sort) {
		if (favoriteLocations == null) return null;
		List<WrapLocation> list = new ArrayList<>();
		List<FavLocation> tmpList = new ArrayList<>(favoriteLocations);

		if (sort == FavLocationType.FROM) {
			Collections.sort(tmpList, FavLocation.FromComparator);
		} else if (sort == FavLocationType.VIA) {
			Collections.sort(tmpList, FavLocation.ViaComparator);
		} else if (sort == FavLocationType.TO) {
			Collections.sort(tmpList, FavLocation.ToComparator);
		}
		for (FavLocation loc : tmpList) {
			list.add(loc);
		}
		return list;
	}

	private void loadFavoriteLocations() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				setLoadedFavoriteLocations(LocationDb.getFavLocationList(context));
			}
		}).start();
	}

	private void setLoadedFavoriteLocations(final List<FavLocation> favoriteLocations) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				TransportNetworkManager.this.favoriteLocations = favoriteLocations;
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

	@UiThread
	public void addOnTransportNetworkChangedListener(TransportNetworkChangedListener listener) {
		transportNetworkChangedListeners.add(listener);
	}

	@UiThread
	public void removeOnTransportNetworkChangedListener(TransportNetworkChangedListener listener) {
		transportNetworkChangedListeners.remove(listener);
	}

	public interface FavoriteLocationsLoadedListener {
		void onFavoriteLocationsLoaded();
	}

	public interface TransportNetworkChangedListener {
		void onTransportNetworkChanged(TransportNetwork network);
	}

}
