package de.grobox.liberario.favorites;

import javax.annotation.ParametersAreNonnullByDefault;

import de.schildbach.pte.dto.Location;

@ParametersAreNonnullByDefault
interface FavoriteListener {

	void onFavoriteClicked(FavoritesItem item);

	void onFavoriteChanged(FavoritesItem item);

	void changeHome();

	void changeWork();

	void onHomeChanged(Location home);

	void onWorkChanged(Location work);

}
