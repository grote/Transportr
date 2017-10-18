package de.grobox.transportr.trips.search;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.EnumSet;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.favorites.trips.FavoriteTripItem;
import de.grobox.transportr.locations.WrapLocation;
import de.schildbach.pte.dto.Product;

@ParametersAreNonnullByDefault
public class TripQuery {

	public final long uid;
	public final WrapLocation from;
	public final @Nullable WrapLocation via;
	public final WrapLocation to;
	public final Date date;
	public final boolean departure;
	public final EnumSet<Product> products;

	TripQuery(long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to, Date date, @Nullable Boolean departure, @Nullable EnumSet<Product> products) {
		this.uid = uid;
		this.from = from;
		this.via = via;
		this.to = to;
		this.date = date;
		this.departure = departure == null ? true : departure;
		this.products = products == null ? EnumSet.allOf(Product.class) : products;
	}

	FavoriteTripItem toFavoriteTripItem() {
		return new FavoriteTripItem(uid, from, via, to);
	}

}
