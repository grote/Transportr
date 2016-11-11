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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import de.grobox.liberario.FavLocation.LOC_TYPE;
import de.grobox.liberario.R;
import de.grobox.liberario.WrapLocation;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

import static de.grobox.liberario.FavLocation.LOC_TYPE.FROM;
import static de.grobox.liberario.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.WrapLocation.WrapType.HOME;
import static de.grobox.liberario.WrapLocation.WrapType.MAP;
import static de.grobox.liberario.data.RecentsDB.getFavLocationList;
import static de.grobox.liberario.utils.TransportrUtils.getDrawableForLocation;
import static de.schildbach.pte.dto.LocationType.ANY;

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
	private LOC_TYPE sort = FROM;

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

	@NonNull
	@Override
	public Filter getFilter() {
		if(filter != null) return filter;
		else {
			// Assign every station a score, to sort them before displaying them
			final HashMap<WrapLocation, Double> resultScores = new HashMap<>();
			filter = new Filter() {
				// People make mistakes writing search terms.
				// That means we have to use approximation to score
				// stations based on their similarity to the searched term

				// This gives us an approximation between 1 and 0, using the Levenshtein distance
				private double stringSimilarity(String s1, String s2) {
					String longer = s1, shorter = s2;
					if (s1.length() < s2.length()) { // longer should always have greater length
						longer = s2; shorter = s1;
					}
					int longerLength = longer.length();
					if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
					return (longerLength - levenshteinDistance(longer, shorter)) / (double) longerLength;
				}

				// Example implementation of the Levenshtein Edit Distance
				// See http://rosettacode.org/wiki/Levenshtein_distance#Java
				private int levenshteinDistance(String s1, String s2) {
					s1 = s1.toLowerCase();
					s2 = s2.toLowerCase();

					int[] costs = new int[s2.length() + 1];
					for (int i = 0; i <= s1.length(); i++) {
						int lastValue = i;
						for (int j = 0; j <= s2.length(); j++) {
							if (i == 0)
								costs[j] = j;
							else {
								if (j > 0) {
									int newValue = costs[j - 1];
									if (s1.charAt(i - 1) != s2.charAt(j - 1))
										newValue = Math.min(Math.min(newValue, lastValue),
												costs[j]) + 1;
									costs[j - 1] = lastValue;
									lastValue = newValue;
								}
							}
						}
						if (i > 0)
							costs[s2.length()] = lastValue;
					}
					return costs[s2.length()];
				}

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
										// Favorites are most important, they have to stay at the top
										resultScores.put(l, 1d);
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
									String lName = loc.getLocation().name;
									if (lName != null && lName.toLowerCase().contains(constraint.toString().toLowerCase())) {
										// Because some providers don't preorder their results, we have to order
										// by the search term
										// if the term occurs literally, we don't need to calculate
										// the levenshtein distance.
										resultScores.put(loc, 0.9d);
									} else if (lName != null) {
										resultScores.put(loc, stringSimilarity(lName.toLowerCase(), constraint.toString().toLowerCase()));
									} else {
										resultScores.put(loc, 0d);
									}
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

						// Sort the results according to the score map
						Collections.sort(locations, new Comparator<WrapLocation>() {
							@Override
							public int compare(WrapLocation l1, WrapLocation l2)
							{
								return resultScores.get(l2).compareTo(resultScores.get(l1));
							}
						});

						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}

	@NonNull
	@Override
	public View getView(int position, View convertView, @NonNull ViewGroup parent) {
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
		else if(wrapLocation.getType() == GPS) {
			textView.setText(parent.getContext().getString(R.string.location_gps));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		}
		else if(wrapLocation.getType() == MAP) {
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

		imageView.setImageDrawable(getDrawableForLocation(getContext(), wrapLocation, defaultLocations.contains(wrapLocation)));

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
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
					defaultLocations.add(new WrapLocation(new Location(ANY, null, home_loc.getLocation().place, home_loc.getLocation().name), HOME));
				} else {
					defaultLocations.add(new WrapLocation(HOME));
				}
			}
			if(includeGpsLocation)
				defaultLocations.add(new WrapLocation(GPS));
			if(includeMapLocation)
				defaultLocations.add(new WrapLocation(MAP));

			if(includeFavLocations) {
				List<WrapLocation> tmpList = getFavLocationList(getContext(), sort, onlyIDs);
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

	public void setSort(LOC_TYPE loc_type) {
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
