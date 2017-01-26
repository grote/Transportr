package de.grobox.liberario.favorites;


import android.view.View;

import de.grobox.liberario.R;

import static de.grobox.liberario.utils.TransportrUtils.getLocationName;

class WorkFavoriteViewHolder extends SpecialFavoritesViewHolder {

	WorkFavoriteViewHolder(View v) {
		super(v);
	}

	@Override
	void onBind(FavoritesItem item, FavoriteListener listener) {
		super.onBind(item, listener);

		icon.setImageResource(R.drawable.ic_work);
		title.setText(R.string.work);
		if (item.getTo() == null) {
			description.setText(R.string.tap_to_set);
		} else {
			description.setText(getLocationName(item.getTo()));
		}
	}

}
