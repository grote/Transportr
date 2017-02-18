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

import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import de.grobox.liberario.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;

abstract class SpecialFavoritesViewHolder extends AbstractFavoritesViewHolder {

	protected final TextView title;
	private final TextView description;

	SpecialFavoritesViewHolder(View v) {
		super(v);
		title = (TextView) v.findViewById(R.id.title);
		description = (TextView) v.findViewById(R.id.description);
	}

	@Override
	void onBind(final FavoritesItem item, final FavoriteListener listener) {
		super.onBind(item, listener);

		if (item.getTo() == null) {
			description.setText(R.string.tap_to_set);
			description.setTypeface(null, Typeface.ITALIC);
			overflow.setVisibility(GONE);
		} else {
			description.setText(getLocationName(item.getTo()));
			description.setTypeface(null, Typeface.NORMAL);
			overflow.setVisibility(VISIBLE);
			final SpecialLocationPopupMenu popup = new SpecialLocationPopupMenu(overflow.getContext(), overflow, item);
			overflow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					popup.show();
				}
			});
		}
	}

}
