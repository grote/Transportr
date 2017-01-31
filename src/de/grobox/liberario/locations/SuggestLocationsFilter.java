package de.grobox.liberario.locations;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

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
			if (l.getLocation().name != null && l.getLocation().name.toLowerCase().contains(constraint.toString().toLowerCase()) && !result.contains(l)) {
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
