package de.grobox.liberario.favorites.trips;

import javax.annotation.ParametersAreNonnullByDefault;

import de.schildbach.pte.dto.Location;

@ParametersAreNonnullByDefault
interface FavoriteTripListener {

	void onFavoriteClicked(FavoriteTripItem item);

	void onFavoriteChanged(FavoriteTripItem item);

	void changeHome();

	void changeWork();

	void onHomeChanged(Location home);

	void onWorkChanged(Location work);

}
