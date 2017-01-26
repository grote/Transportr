package de.grobox.liberario.favorites;


import android.view.View;

import de.grobox.liberario.R;

import static de.grobox.liberario.utils.TransportrUtils.getLocationName;

class HomeFavoriteViewHolder extends SpecialFavoritesViewHolder {

	HomeFavoriteViewHolder(View v) {
		super(v);
	}

	@Override
	void onBind(FavoritesItem item, FavoriteListener listener) {
		super.onBind(item, listener);

		icon.setImageResource(R.drawable.ic_action_home);
		title.setText(R.string.home);
		if (item.getTo() == null) {
			description.setText(R.string.tap_to_set);
		} else {
			description.setText(getLocationName(item.getTo()));
		}
	}

}
