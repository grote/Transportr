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
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.transportr.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

class FavoriteTripViewHolder extends AbstractFavoritesViewHolder {

	private final TextView from;
	private final TextView via;
	private final TextView to;
	private final ImageView viaIcon;

	FavoriteTripViewHolder(View v) {
		super(v);
		from = v.findViewById(R.id.from);
		via = v.findViewById(R.id.via);
		to = v.findViewById(R.id.to);
		viaIcon = v.findViewById(R.id.viaIcon);
	}

	@Override
	void onBind(final FavoriteTripItem item, FavoriteTripListener listener) {
		super.onBind(item, listener);

		if (item.isFavorite()) {
			icon.setImageResource(R.drawable.ic_action_star);
		} else {
			icon.setImageResource(R.drawable.ic_time);
		}

		if (item.getTo() == null) throw new IllegalStateException();
		from.setText(item.getFrom().getName());
		to.setText(item.getTo().getName());

		if (item.getVia() != null) {
			via.setText(item.getVia().getName());
			via.setVisibility(VISIBLE);
			viaIcon.setVisibility(VISIBLE);
		} else {
			via.setVisibility(GONE);
			viaIcon.setVisibility(GONE);
		}

		final FavoriteTripPopupMenu favPopup = new FavoriteTripPopupMenu(overflow.getContext(), overflow, item, listener);
		overflow.setOnClickListener(v -> favPopup.show());
	}

}
