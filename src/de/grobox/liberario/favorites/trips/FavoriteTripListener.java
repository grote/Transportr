package de.grobox.liberario.favorites.trips;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
interface FavoriteTripListener {

	void onFavoriteClicked(FavoriteTripItem item);

	void onFavoriteChanged(FavoriteTripItem item, boolean isFavorite);

	void changeHome();

	void changeWork();

}
