/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.liberario.favorites;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.liberario.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;

class FavoritesViewHolder extends AbstractFavoritesViewHolder {

	private final TextView from;
	private final TextView via;
	private final TextView to;
	private final ImageView viaIcon;

	FavoritesViewHolder(View v) {
		super(v);
		from = (TextView) v.findViewById(R.id.from);
		via = (TextView) v.findViewById(R.id.via);
		to = (TextView) v.findViewById(R.id.to);
		viaIcon = (ImageView) v.findViewById(R.id.viaIcon);
	}

	@Override
	void onBind(final FavoritesItem item, FavoriteListener listener) {
		super.onBind(item, listener);

		if (item.isFavorite()) {
			icon.setImageResource(R.drawable.ic_action_star);
		} else {
			icon.setImageResource(R.drawable.ic_time);
		}

		from.setText(getLocationName(item.getFrom()));
		to.setText(getLocationName(item.getTo()));

		if(item.getVia() != null) {
			via.setText(getLocationName(item.getVia()));
			via.setVisibility(VISIBLE);
			viaIcon.setVisibility(VISIBLE);
		} else {
			via.setVisibility(GONE);
			viaIcon.setVisibility(GONE);
		}

		final FavoritesPopupMenu favPopup = new FavoritesPopupMenu(overflow.getContext(), overflow, item, listener);
		overflow.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				favPopup.show();
			}
		});
	}

}
