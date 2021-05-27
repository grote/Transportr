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

import android.view.View;

import de.grobox.transportr.R;

class WorkFavoriteViewHolder extends SpecialFavoritesViewHolder {

	WorkFavoriteViewHolder(View v) {
		super(v);
	}

	@Override
	void onBind(FavoriteTripItem item, FavoriteTripListener listener) {
		super.onBind(item, listener);

		icon.setImageResource(R.drawable.ic_work);
		title.setText(R.string.work);

		if (item.getTo() != null) {
			final SpecialLocationPopupMenu popup = new WorkPopupMenu(overflow.getContext(), overflow, item, listener);
			overflow.setOnClickListener(v -> popup.show());
		}
	}

}
