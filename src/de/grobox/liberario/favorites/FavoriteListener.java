package de.grobox.liberario.favorites;

interface FavoriteListener {

	void onFavoriteClicked(FavoritesItem item);

	void onFavoriteRemoved(FavoritesItem item);

}
