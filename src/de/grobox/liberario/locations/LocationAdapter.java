/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

package de.grobox.liberario.locations;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;
import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.data.locations.HomeLocation;
import de.grobox.liberario.data.locations.WorkLocation;
import de.schildbach.pte.dto.SuggestedLocation;

import static de.grobox.liberario.data.locations.FavoriteLocation.FavLocationType.FROM;
import static de.grobox.liberario.locations.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.locations.WrapLocation.WrapType.MAP;
import static de.grobox.liberario.locations.WrapLocation.WrapType.NORMAL;

@ParametersAreNonnullByDefault
class LocationAdapter extends ArrayAdapter<WrapLocation> implements Filterable {

	private @Nullable HomeLocation homeLocation;
	private @Nullable WorkLocation workLocation;
	private List<FavoriteLocation> favoriteLocations = new ArrayList<>();
	private List<WrapLocation> locations = new ArrayList<>();
	private @Nullable List<SuggestedLocation> suggestedLocations;
	private @Nullable CharSequence search;
	private Filter filter = null;
	private final boolean includeHome, includeGps, includeFavs;
	private FavLocationType sort = FROM;

	static final int TYPING_THRESHOLD = 3;

	LocationAdapter(Context context, boolean includeHome, boolean includeGps, boolean includeFavs) {
		super(context, R.layout.location_item);
		this.includeHome = includeHome;
		this.includeGps = includeGps;
		this.includeFavs = includeFavs;
	}

	/* TODO new stuff */

	public void setHomeLocation(@Nullable HomeLocation homeLocation) {
		this.homeLocation = homeLocation;
		updateLocations();
		resetDropDownLocations();
	}

	void setWorkLocation(@Nullable WorkLocation workLocation) {
		this.workLocation = workLocation;
		updateLocations();
		resetDropDownLocations();
	}

	void setFavoriteLocations(List<FavoriteLocation> favoriteLocations) {
		this.favoriteLocations = favoriteLocations;
		// TODO sort
		updateLocations();
		resetDropDownLocations();
	}

	private void updateLocations() {
		locations = new ArrayList<>();
		if (includeHome && homeLocation != null) {
			favoriteLocations.remove(homeLocation);
			locations.add(homeLocation);
		}
		if (workLocation != null) {
			favoriteLocations.remove(workLocation);
			locations.add(workLocation);
		}
		if (includeGps) locations.add(new WrapLocation(GPS));
//		locations.add(new WrapLocation(MAP));

		if (includeFavs) {
			locations.addAll(favoriteLocations);
		}
	}

	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, ViewGroup parent) {
		return getDropDownView(position, convertView, parent);
	}

	@NonNull
	@Override
	public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		View view;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			view = inflater.inflate(R.layout.location_item, parent, false);
		} else {
			view = convertView;
		}

		ImageView imageView = view.findViewById(R.id.imageView);
		TextView textView = view.findViewById(R.id.textView);

		WrapLocation wrapLocation = getItem(position);
		if(wrapLocation == null || wrapLocation.getLocation() == null) return view;

		if (wrapLocation.getWrapType() == NORMAL) {
			textView.setText(getHighlightedText(wrapLocation));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		} else {
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
			if(wrapLocation.getWrapType() == GPS) {
				textView.setText(parent.getContext().getString(R.string.location_gps));
			} else if(wrapLocation.getWrapType() == MAP) {
				textView.setText(parent.getContext().getString(R.string.location_map));
			}
		}
		//imageView.setImageDrawable(getDrawableForLocation(getContext(), homeLocation, wrapLocation, locations.contains(wrapLocation)));
		imageView.setImageResource(wrapLocation.getDrawable());

		return view;
	}

	private Spanned getHighlightedText(WrapLocation l) {
		if(search != null && search.length() >= TYPING_THRESHOLD) {
			String regex = "(?i)(" + Pattern.quote(search.toString()) + ")";
			String str = l.getFullName().replaceAll(regex, "<b>$1</b>");
			return Html.fromHtml(str);
		} else {
			return Html.fromHtml(l.getFullName());
		}
	}

	@NonNull
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new SuggestLocationsFilter() {
				@Override
				protected FilterResults performFiltering(CharSequence charSequence) {
					return super.performFiltering(charSequence, locations, suggestedLocations);
				}

				@Override
				protected void publishResults(CharSequence charSequence, FilterResults results) {
					if (results.count > 0) {
						clear();
						//noinspection unchecked
						addAll((List<WrapLocation>) results.values);
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
		}
		return filter;
	}

	void swapSuggestedLocations(List<SuggestedLocation> suggestedLocations, String search) {
		this.suggestedLocations = suggestedLocations;
		if (search.length() >= TYPING_THRESHOLD) {
			this.search = search;
			getFilter().filter(search);
		}
	}

	void resetSearchTerm() {
		search = null;
		// when we are clearing the search term, there is no need to keep its results around
		if (suggestedLocations != null) {
			suggestedLocations.clear();
		}
	}

	void resetDropDownLocations() {
		clear();
		addAll(locations);
	}

	void reset() {
		resetSearchTerm();
		updateLocations();
		resetDropDownLocations();
	}

	void setSort(FavLocationType favLocationType) {
		// TODO
		sort = favLocationType;
		updateLocations();
		resetDropDownLocations();
	}

}
