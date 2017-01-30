package de.grobox.liberario.networks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.data.RecentsDB;
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
	private Location home;
	@Nullable
	private List<FavLocation> favoriteLocations;
	private List<OnFavoriteLocationsLoadedListener> onFavoriteLocationsLoadedListeners = new ArrayList<>();

	@Inject
	public TransportNetworkManager(Context context, SettingsManager settingsManager) {
		this.context = context;
		this.settingsManager = settingsManager;
		transportNetwork = loadTransportNetwork(1);
		transportNetwork2 = loadTransportNetwork(2);
		transportNetwork3 = loadTransportNetwork(3);
		loadHome();
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

	@Nullable
	public Location getHome() {
		return home;
	}

	public void setHome(@Nullable final Location home) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				TransportNetworkManager.this.home = home;
			}
		});
	}

	private void loadHome() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				setHome(RecentsDB.getHome(context));
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
				setFavoriteLocations(RecentsDB.getFavLocationList(context));
			}
		}).start();
	}

	private void setFavoriteLocations(final List<FavLocation> favoriteLocations) {
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				TransportNetworkManager.this.favoriteLocations = favoriteLocations;
				for (OnFavoriteLocationsLoadedListener l : onFavoriteLocationsLoadedListeners) {
					if (l != null) l.onFavoriteLocationsLoaded();
				}
				onFavoriteLocationsLoadedListeners.clear();
			}
		});
	}

	/**
	 * Adds a listener that will be informed once favorite locations have been loaded.
	 * The listener will be removed automatically after being informed.
	 */
	@UiThread
	public void addOnFavoriteLocationsLoadedListener(OnFavoriteLocationsLoadedListener listener) {
		onFavoriteLocationsLoadedListeners.add(listener);
	}

	public interface OnFavoriteLocationsLoadedListener {
		void onFavoriteLocationsLoaded();
	}

}
