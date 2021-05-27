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

package de.grobox.transportr.locations;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.schildbach.pte.dto.SuggestedLocation;

abstract class SuggestLocationsFilter extends Filter {

	/**
	 * Starts an asynchronous filtering operation on a worker thread.
	 */
	FilterResults performFiltering(CharSequence constraint, List<WrapLocation> favoriteLocations, List<SuggestedLocation> suggestedLocations) {
		FilterResults filterResults = new FilterResults();
		if (constraint == null) return filterResults;
		List<WrapLocation> result = new ArrayList<>();

		// add fav locations that fulfill constraint
		for (WrapLocation l : favoriteLocations) {
			// case-insensitive match of location name and location not already included
			// TODO don't only match name, but also place
			if (l.getLocation().name != null && l.getLocation().name.toLowerCase(Locale.getDefault()).contains(constraint.toString().toLowerCase(Locale.getDefault())) && !result.contains(l)) {
				result.add(l);
			}
		}

		// add suggested locations (from network provider) without filtering if not already included
		if (suggestedLocations != null) {
			// the locations are pre-sorted by priority
			for (SuggestedLocation l : suggestedLocations) {
				WrapLocation loc = new WrapLocation(l.location);
				// prevent duplicates
				if (!result.contains(loc)) {
					result.add(loc);
				}
			}
		}

		// Assign the data to the FilterResults
		filterResults.values = result;
		filterResults.count = result.size();

		return filterResults;
	}

}
