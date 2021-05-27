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

import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import de.grobox.transportr.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

abstract class SpecialFavoritesViewHolder extends AbstractFavoritesViewHolder {

	protected final TextView title;
	private final TextView description;

	SpecialFavoritesViewHolder(View v) {
		super(v);
		title = v.findViewById(R.id.title);
		description = v.findViewById(R.id.description);
	}

	@Override
	void onBind(final FavoriteTripItem item, final FavoriteTripListener listener) {
		super.onBind(item, listener);

		if (item.getTo() == null) {
			description.setText(R.string.tap_to_set);
			description.setTypeface(null, Typeface.ITALIC);
			overflow.setVisibility(GONE);
		} else {
			description.setText(item.getTo().getName());
			description.setTypeface(null, Typeface.NORMAL);
			overflow.setVisibility(VISIBLE);
		}
	}

}
