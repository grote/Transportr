/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
 *
 *    This program is Free Software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.grobox.transportr.favorites.trips;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import de.grobox.transportr.R;

abstract class AbstractFavoritesViewHolder extends RecyclerView.ViewHolder {

	private final View layout;
	final ImageView icon;
	final ImageButton overflow;

	AbstractFavoritesViewHolder(View v) {
		super(v);
		layout = v;
		icon = v.findViewById(R.id.logo);
		overflow = v.findViewById(R.id.overflowButton);
	}

	@CallSuper
	void onBind(final FavoriteTripItem item, final FavoriteTripListener listener) {
		layout.setOnClickListener(v -> listener.onFavoriteClicked(item));
	}

}
