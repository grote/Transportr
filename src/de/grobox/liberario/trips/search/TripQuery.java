package de.grobox.liberario.trips.search;

import android.support.annotation.Nullable;

import java.util.Date;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.favorites.trips.FavoriteTripItem;
import de.grobox.liberario.locations.WrapLocation;

@ParametersAreNonnullByDefault
public class TripQuery {

	public final long uid;
	public final WrapLocation from;
	public final @Nullable WrapLocation via;
	public final WrapLocation to;
	public final Date date;
	public final boolean departure;

	public TripQuery(long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to, Date date, boolean departure) {
		this.uid = uid;
		this.from = from;
		this.via = via;
		this.to = to;
		this.date = date;
		this.departure = departure;
	}

	public FavoriteTripItem toFavoriteTripItem() {
		return new FavoriteTripItem(uid, from, via, to);
	}

}
