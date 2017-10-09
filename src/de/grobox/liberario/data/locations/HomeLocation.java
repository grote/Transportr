package de.grobox.liberario.data.locations;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Set;

import de.grobox.liberario.R;
import de.grobox.liberario.locations.WrapLocation;
import de.schildbach.pte.NetworkId;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.Product;

@Entity(
		tableName = "home_locations",
		indices = {
				@Index(value = {"networkId"}, unique = true)
		}
)
public class HomeLocation extends StoredLocation {

	public HomeLocation(long uid, @Nullable NetworkId networkId, LocationType type, @Nullable String id, int lat, int lon, @Nullable String place, @Nullable String name, @Nullable Set<Product> products) {
		super(uid, networkId, type, id, lat, lon, place, name, products);
	}

	@Ignore
	public HomeLocation(@NonNull NetworkId networkId, WrapLocation l) {
		super(networkId, l);
	}

	@Ignore
	public HomeLocation(@NonNull NetworkId networkId, Location l) {
		super(networkId, l);
	}

	@Override
	@DrawableRes
	public int getDrawable() {
		return R.drawable.ic_action_home;
	}

}
