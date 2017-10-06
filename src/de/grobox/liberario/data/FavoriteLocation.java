package de.grobox.liberario.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.support.annotation.Nullable;

import java.util.Set;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

@Entity(tableName = "locations", indices = {@Index(value = {"networkId", "id"}, unique = true)})
public class FavoriteLocation extends DbLocation {

	private int fromCount;
	private int viaCount;
	private int toCount;

	public FavoriteLocation(int uid, @Nullable NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon,
	                        @Nullable String place, @Nullable String name, @Nullable Set<Product> products, int fromCount, int viaCount,
	                        int toCount) {
		super(uid, networkId, type, id, lat, lon, place, name, products);
		this.fromCount = fromCount;
		this.viaCount = viaCount;
		this.toCount = toCount;
	}

	@Ignore
	public FavoriteLocation(int uid, NetworkId networkId, Location l) {
		super(uid, networkId, l);
		this.fromCount = 0;
		this.viaCount = 0;
		this.toCount = 0;
	}

	@Ignore
	public FavoriteLocation(NetworkId networkId, Location l) {
		super(networkId, l);
		this.fromCount = 0;
		this.viaCount = 0;
		this.toCount = 0;
	}

	public int getFromCount() {
		return fromCount;
	}

	public int getViaCount() {
		return viaCount;
	}

	public int getToCount() {
		return toCount;
	}

}
