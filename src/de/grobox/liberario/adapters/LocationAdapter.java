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

package de.grobox.liberario.adapters;

import android.content.Context;
import android.graphics.Typeface;
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

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.R;
import de.grobox.liberario.WrapLocation;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

public class LocationAdapter extends ArrayAdapter<WrapLocation> implements Filterable {
	private List<WrapLocation> locations;
	private List<WrapLocation> defaultLocations;
	private List<Location> suggestedLocations;
	private CharSequence search;
	private Filter filter = null;
	private boolean onlyIDs;
	private boolean includeFavLocations = false;
	private boolean includeHomeLocation = false;
	private boolean includeGpsLocation = false;
	private boolean includeMapLocation = false;
	private FavLocation.LOC_TYPE sort = FavLocation.LOC_TYPE.FROM;

	public static final String HOME = "Transportr.HOME";
	public static final String GPS = "Transportr.GPS";
	public static final String MAP = "Transportr.MAP";

	public static final int THRESHOLD = 3;

	public LocationAdapter(Context context, boolean onlyIDs) {
		super(context, R.layout.location_item);
		this.onlyIDs = onlyIDs;
		this.locations = new ArrayList<>();
		setDefaultLocations(false);
	}

	public LocationAdapter(Context context, List<Location> locations) {
		this(context, false);
		this.locations = new ArrayList<>(locations.size());
		for(Location l : locations) {
			this.locations.add(new WrapLocation(l));
		}
	}

	@Override
	public int getCount() {
		return locations.size();
	}

	@Override
	public @Nullable WrapLocation getItem(int index) {
		if(locations.size() > 0 && locations.get(index) != null) {
			return locations.get(index);
		} else {
			return null;
		}
	}

	@Override
	public Filter getFilter() {
		if(filter != null) return filter;
		else {
			filter = new Filter() {
				@Override
				protected FilterResults performFiltering(final CharSequence constraint) {
					FilterResults filterResults = new FilterResults();

					if(constraint != null) {
						List<WrapLocation> result = new ArrayList<>();
						search = constraint;

						// add fav locations that fulfill constraint
						if(defaultLocations != null) {
							for(WrapLocation l : defaultLocations) {
								// if we only want locations with ID, make sure the location has one
								if(!onlyIDs || l.getLocation().hasId()) {
									// case-insensitive match of location name and location not already included
									if(l.getLocation().name != null && l.getLocation().name.toLowerCase().contains(constraint.toString().toLowerCase()) && !result.contains(l)) {
										result.add(l);
									}
								}
							}
						}

						// add suggested locations (from network provider) without filtering if not already included
						if(suggestedLocations != null) {
							for(Location l : suggestedLocations) {
								WrapLocation loc = new WrapLocation(l);
								// prevent duplicates and if we only want locations with ID, make sure the location has one
								if(!result.contains(loc) && (!onlyIDs || l.hasId())) {
									result.add(loc);
								}
							}
						}

						// Assign the data to the FilterResults
						filterResults.values = result;
						filterResults.count = result.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					if(results != null && results.count > 0) {
						locations = (List<WrapLocation>) results.values;
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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

		if(l.id != null && l.id.equals(HOME)) {
			Location home = RecentsDB.getHome(parent.getContext());
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
		else if(l.id != null && l.id.equals(GPS)) {
			textView.setText(parent.getContext().getString(R.string.location_gps));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		}
		else if(l.id != null && l.id.equals(MAP)) {
			textView.setText(parent.getContext().getString(R.string.location_map));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		}
		// locations from favorites and auto-complete
		else if(defaultLocations.contains(wrapLocation)) {
			textView.setText(getHighlightedText(l));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		}
		else {
			textView.setText(getHighlightedText(l));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		}

		imageView.setImageDrawable(TransportrUtils.getDrawableForLocation(getContext(), l, defaultLocations.contains(wrapLocation)));

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	public void setDefaultLocations(boolean refresh) {
		locations = getDefaultLocations(refresh);
		if(refresh && suggestedLocations != null) {
			suggestedLocations.clear();
		}
	}

	private List<WrapLocation> getDefaultLocations(boolean refresh) {
		if(refresh || defaultLocations == null || defaultLocations.size() == 0) {
			defaultLocations = new ArrayList<>();
			WrapLocation home_loc = null;

			if(includeHomeLocation) {
				home_loc = new WrapLocation(RecentsDB.getHome(getContext()));
				if(home_loc.getLocation() != null) {
					defaultLocations.add(new WrapLocation(new Location(LocationType.ANY, HOME, home_loc.getLocation().place, home_loc.getLocation().name)));
				} else {
					defaultLocations.add(new WrapLocation(new Location(LocationType.ANY, HOME)));
				}
			}
			if(includeGpsLocation)
				defaultLocations.add(new WrapLocation(new Location(LocationType.ANY, GPS)));
			if(includeMapLocation)
				defaultLocations.add(new WrapLocation(new Location(LocationType.ANY, MAP)));

			if(includeFavLocations) {
				List<WrapLocation> tmpList = RecentsDB.getFavLocationList(getContext(), sort, onlyIDs);
				// remove home location from favorites if it is set
				if(includeHomeLocation && home_loc != null) {
					tmpList.remove(home_loc);
				}

				defaultLocations.addAll(tmpList);
			}
		}
		return defaultLocations;
	}

	public void clearSearchTerm() {
		search = null;
		// when we are clearing the search term, there is no need to keep its results around

		if(suggestedLocations != null) {
			suggestedLocations.clear();
		}
	}

	public void swapSuggestedLocations(List<Location> suggestedLocations, String search) {
		this.suggestedLocations = suggestedLocations;
		if(suggestedLocations != null && search != null && search.length() >= THRESHOLD) {
			getFilter().filter(search);
		}
	}

	public void setSort(FavLocation.LOC_TYPE loc_type) {
		sort = loc_type;
	}

	public void setFavs(boolean favs) {
		this.includeFavLocations = favs;
	}

	public void setHome(boolean home) {
		this.includeHomeLocation = home;
	}

	public void setGPS(boolean gps) {
		this.includeGpsLocation = gps;
	}

	public void setMap(boolean map) {
		this.includeMapLocation = map;
	}

	private Spanned getHighlightedText(Location l) {
		if(search != null && search.length() >= THRESHOLD) {
			String regex = "(?i)(" + Pattern.quote(search.toString()) + ")";
			String str = TransportrUtils.getFullLocName(l).replaceAll(regex, "<b>$1</b>");
			return Html.fromHtml(str);
		} else {
			return Html.fromHtml(TransportrUtils.getFullLocName(l));
		}
	}
}