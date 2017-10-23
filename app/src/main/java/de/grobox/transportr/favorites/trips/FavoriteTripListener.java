package de.grobox.transportr.favorites.trips;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface FavoriteTripListener {

	void onFavoriteClicked(FavoriteTripItem item);

	void onFavoriteChanged(FavoriteTripItem item, boolean isFavorite);

	void onFavoriteDeleted(FavoriteTripItem item);

	void changeHome();

	void changeWork();

}
