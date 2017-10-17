package de.grobox.liberario.data.locations;

import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.Nullable;

import java.util.Set;

import de.grobox.liberario.locations.WrapLocation;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

public abstract class StoredLocation extends WrapLocation {

	@PrimaryKey(autoGenerate = true)
	private final long uid;
	private NetworkId networkId;

	StoredLocation(long uid, NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name, @Nullable Set<Product> products) {
		super(type, id, lat, lon, place, name, products);
		this.uid = uid;
		this.networkId = networkId;
	}

	@Ignore
	StoredLocation(long uid, NetworkId networkId, WrapLocation l) {
		this(uid, networkId, l.type, l.id, l.lat, l.lon, l.place, l.name, l.products);
	}

	@Ignore
	StoredLocation(NetworkId networkId, WrapLocation l) {
		this(0, networkId, l);
	}

	@Ignore
	StoredLocation(NetworkId networkId, Location l) {
		this(0, networkId, l.type, l.id, l.lat, l.lon, l.place, l.name, l.products);
	}

	public long getUid() {
		return uid;
	}

	public NetworkId getNetworkId() {
		return networkId;
	}

}
