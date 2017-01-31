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
import de.grobox.liberario.locations.FavLocation.FavLocationType;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.networks.TransportNetworkManager.OnFavoriteLocationsLoadedListener;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.SuggestedLocation;

import static de.grobox.liberario.locations.FavLocation.FavLocationType.FROM;
import static de.grobox.liberario.locations.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.locations.WrapLocation.WrapType.HOME;
import static de.grobox.liberario.locations.WrapLocation.WrapType.MAP;
import static de.grobox.liberario.utils.TransportrUtils.getDrawableForLocation;

@ParametersAreNonnullByDefault
class LocationAdapter extends ArrayAdapter<WrapLocation> implements Filterable, OnFavoriteLocationsLoadedListener {

	private final TransportNetworkManager manager;
	private List<WrapLocation> favoriteLocations = new ArrayList<>();
	@Nullable
	private List<SuggestedLocation> suggestedLocations;
	@Nullable
	private CharSequence search;
	private Filter filter = null;
	private final boolean includeHome, includeGps, includeFavs;
	private FavLocationType sort = FROM;

	static final int TYPING_THRESHOLD = 3;

	LocationAdapter(Context context, TransportNetworkManager manager, boolean includeHome, boolean includeGps, boolean includeFavs) {
		super(context, R.layout.location_item);
		this.manager = manager;
		this.includeHome = includeHome;
		this.includeGps = includeGps;
		this.includeFavs = includeFavs;
		getFavoriteLocations();
		addFavoriteLocationsToDropDown();
	}

	LocationAdapter(Context context, TransportNetworkManager manager, List<Location> locations) {
		this(context, manager, false, false, false);
		for (Location l : locations) {
			add(new WrapLocation(l));
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

		ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
		TextView textView = (TextView) view.findViewById(R.id.textView);

		WrapLocation wrapLocation = getItem(position);
		if(wrapLocation == null || wrapLocation.getLocation() == null) return view;
		Location l = wrapLocation.getLocation();

		if(wrapLocation.getType() == HOME) {
			Location home = manager.getHome();
			if(home != null) {
				textView.setText(getHighlightedText(home));
				textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

				// change home location on long click does not work here
				// because click events are not accepted from the list anymore
			} else {
				textView.setText(parent.getContext().getString(R.string.location_home));
				textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
			}
		}
		else if(wrapLocation.getType() == GPS) {
			textView.setText(parent.getContext().getString(R.string.location_gps));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		}
		else if(wrapLocation.getType() == MAP) {
			textView.setText(parent.getContext().getString(R.string.location_map));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		}
		// locations from favorites and auto-complete
		else if(wrapLocation instanceof FavLocation) {
			textView.setText(getHighlightedText(l));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		}
		else {
			textView.setText(getHighlightedText(l));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		}

		imageView.setImageDrawable(getDrawableForLocation(getContext(), manager.getHome(), wrapLocation, favoriteLocations.contains(wrapLocation)));

		return view;
	}

	private Spanned getHighlightedText(Location l) {
		if(search != null && search.length() >= TYPING_THRESHOLD) {
			String regex = "(?i)(" + Pattern.quote(search.toString()) + ")";
			String str = TransportrUtils.getFullLocName(l).replaceAll(regex, "<b>$1</b>");
			return Html.fromHtml(str);
		} else {
			return Html.fromHtml(TransportrUtils.getFullLocName(l));
		}
	}

	@NonNull
	@Override
	public Filter getFilter() {
		if (filter == null) {
			filter = new SuggestLocationsFilter() {
				@Override
				protected FilterResults performFiltering(CharSequence charSequence) {
					return super.performFiltering(charSequence, favoriteLocations, suggestedLocations);
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

	private void addFavoriteLocationsToDropDown() {
		addAll(favoriteLocations);
	}

	private List<WrapLocation> getFavoriteLocations() {
		WrapLocation home = null;
		if (includeHome) {
			Location home_loc = manager.getHome();
			if (home_loc == null) {
				home = new WrapLocation(HOME);
			} else {
				home = new WrapLocation(home_loc, HOME);
			}
			favoriteLocations.add(home);
		}
		if (includeGps) {
			favoriteLocations.add(new WrapLocation(GPS));
		}
		if (includeFavs) {
			List<WrapLocation> tmpList = manager.getFavoriteLocations(sort);
			if (tmpList == null) {
				manager.addOnFavoriteLocationsLoadedListener(this);
			} else {
				if (includeHome) tmpList.remove(new WrapLocation(home.getLocation()));
				favoriteLocations.addAll(tmpList);
			}
		}
		return favoriteLocations;
	}

	@Override
	public void onFavoriteLocationsLoaded() {
		resetFavoriteLocations();
		resetDropDownLocations();
	}

	private void resetFavoriteLocations() {
		favoriteLocations.clear();
		getFavoriteLocations();
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
		addFavoriteLocationsToDropDown();
	}

	void reset() {
		resetSearchTerm();
		resetFavoriteLocations();
		resetDropDownLocations();
	}

	void setSort(FavLocationType favLocationType) {
		sort = favLocationType;
		resetFavoriteLocations();
		addFavoriteLocationsToDropDown();
	}

}
