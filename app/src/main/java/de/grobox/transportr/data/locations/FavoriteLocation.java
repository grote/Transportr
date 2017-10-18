package de.grobox.transportr.data.locations;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import java.util.Comparator;
import java.util.Set;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.WrapLocation;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

@Entity(
		tableName = "locations",
		indices = {
				@Index("networkId"),
				@Index("id"),
				@Index(value = {"networkId", "id"}, unique = true)
		}
)
public class FavoriteLocation extends StoredLocation {

	public enum FavLocationType {FROM, VIA, TO}

	private int fromCount;
	private int viaCount;
	private int toCount;

	public FavoriteLocation(long uid, @Nullable NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon,
	                        @Nullable String place, @Nullable String name, @Nullable Set<Product> products, int fromCount, int viaCount,
	                        int toCount) {
		super(uid, networkId, type, id, lat, lon, place, name, products);
		this.fromCount = fromCount;
		this.viaCount = viaCount;
		this.toCount = toCount;
	}

	@Ignore
	public FavoriteLocation(long uid, NetworkId networkId, WrapLocation l) {
		super(uid, networkId, l);
		this.fromCount = 0;
		this.viaCount = 0;
		this.toCount = 0;
	}

	@Ignore
	public FavoriteLocation(NetworkId networkId, WrapLocation l) {
		super(networkId, l);
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

	@Override
	@DrawableRes
	public int getDrawable() {
		return R.drawable.ic_action_star;
	}

	int getFromCount() {
		return fromCount;
	}

	int getViaCount() {
		return viaCount;
	}

	int getToCount() {
		return toCount;
	}

	public void add(FavLocationType type) {
		switch (type) {
			case FROM:
				fromCount++;
				return;
			case VIA:
				viaCount++;
				return;
			case TO:
				toCount++;
		}
	}

	@Override
	public String toString() {
		return super.toString() + "[" + fromCount + "]";
	}

	public static Comparator<FavoriteLocation> FromComparator = (loc1, loc2) -> loc2.getFromCount() - loc1.getFromCount();

	public static Comparator<FavoriteLocation> ViaComparator = (loc1, loc2) -> loc2.getViaCount() - loc1.getViaCount();

	public static Comparator<FavoriteLocation> ToComparator = (loc1, loc2) -> loc2.getToCount() - loc1.getToCount();

}
