package de.grobox.liberario.data;

import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

public abstract class DbLocation {

	@PrimaryKey(autoGenerate = true)
	private final int uid;
	public @Nullable NetworkId networkId;

	public LocationType type;
	public @Nullable String id;
	public int lat, lon;
	public @Nullable String place;
	public @Nullable String name;
	public @Nullable Set<Product> products;

	public DbLocation(int uid, @Nullable NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name, @Nullable Set<Product> products) {
		this.uid = uid;
		this.networkId = networkId;

		this.type = type;
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.place = place;
		this.name = name;
		this.products = products;
	}

	@Ignore
	public DbLocation(int uid, @NonNull NetworkId networkId, Location l) {
		this(uid, networkId, l.type, l.id, l.lat, l.lon, l.place, l.name, l.products);
	}

	@Ignore
	public DbLocation(@NonNull NetworkId networkId, Location l) {
		this(0, networkId, l);
	}

	public int getUid() {
		return uid;
	}

	public Location getLocation() {
		if (type == LocationType.ANY && id != null) {
			return new Location(type, null, lat, lon, place, name, products);
		}
		return new Location(type, id, lat, lon, place, name, products);
	}

}
