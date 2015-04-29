/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

package de.grobox.liberario;

import java.util.ArrayList;
import java.util.List;

import de.grobox.liberario.data.FavDB;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class LocationAdapter extends ArrayAdapter<Location> implements Filterable {
	private List<Location> filteredList = new ArrayList<>();
	private List<Location> favList = new ArrayList<>();
	private boolean onlyIDs = false;
	private boolean favs = false;
	private boolean home = false;
	private boolean gps = false;
	private FavLocation.LOC_TYPE sort;

	public LocationAdapter(Context context, FavLocation.LOC_TYPE sort) {
		super(context, R.layout.location_item);
		this.sort = sort;
	}

	public LocationAdapter(Context context, FavLocation.LOC_TYPE sort, boolean onlyIDs) {
		super(context, R.layout.location_item);
		this.onlyIDs = onlyIDs;
		this.sort = sort;
	}

	// constructor that enables reuse of this class by AmbiguousLocationActivity
	public LocationAdapter(Context context, FavLocation.LOC_TYPE sort, List<Location> filteredList) {
		super(context, R.layout.location_item);
		this.sort = sort;
		this.filteredList = filteredList;
	}

	@Override
	public int getCount() {
		return filteredList.size();
	}

	@Override
	public Location getItem(int index) {
		if(filteredList.get(index) != null) {
			return filteredList.get(index);
		} else {
			return null;
		}
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			// This method could be optimized a lot, but hey processors are fast nowadays
			protected FilterResults performFiltering(final CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				if(constraint != null) {
					List<Location> resultList = null;
					NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));

					// get the auto-complete results
					if(constraint.length() > 2) {
						try {
							// get locations from network provider
							resultList = np.suggestLocations(constraint.toString()).getLocations();
						} catch(Exception e) {
							e.printStackTrace();
						}
					}

					// reset filtered list
					resetList();

					// add favorite locations that fulfill constraint
					for(Location l : favList) {
						// if we only want locations with ID, make sure the location has one
						if(!onlyIDs || l.hasId()) {
							// case-insensitive match of location name and location not already included
							if(l.name != null && l.name.toLowerCase().contains(constraint.toString().toLowerCase()) && !filteredList.contains(l)) {
								filteredList.add(l);
							}
						}
					}

					if(resultList != null) {
						// add locations from result that are not already inside
						for(Location l : resultList) {
							if((!onlyIDs || l.hasId()) && !filteredList.contains(l)) {
								filteredList.add(l);
							}
						}
					}

					// Assign the data to the FilterResults
					filterResults.values = filteredList;
					filterResults.count = filteredList.size();
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if(results != null && results.count > 0) {
					filteredList = (List<Location>) results.values;
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
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

		Location l = getItem(position);

		if(l.id != null && l.id.equals("Liberario.HOME")) {
			imageView.setImageResource(R.drawable.ic_action_home);
			Location home = FavDB.getHome(parent.getContext());
			if(home != null) {
				textView.setText(home.uniqueShortName());
				textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));

				// change home location on long click does not work here
				// because click events are not accepted from the list anymore
			} else {
				textView.setText(parent.getContext().getString(R.string.location_home));
				textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
			}
		}
		else if(l.id != null && l.id.equals("Liberario.GPS")) {
			imageView.setImageResource(R.drawable.ic_gps);
			textView.setText(parent.getContext().getString(R.string.location_gps));
			textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
		}
		// locations from favorites and auto-complete
		else if(favList.contains(l)) {
			imageView.setImageResource(R.drawable.ic_action_star);
			textView.setText(LiberarioUtils.getLocName(l));
		}
		else {
			if(l.type.equals(LocationType.ADDRESS)) {
				imageView.setImageResource(R.drawable.ic_location_address);
			} else if(l.type.equals(LocationType.POI)) {
				imageView.setImageResource(android.R.drawable.ic_menu_info_details);
			} else if(l.type.equals(LocationType.STATION)) {
				imageView.setImageResource(R.drawable.ic_tab_stations);
			} else {
				imageView.setImageDrawable(null);
			}
			textView.setText(LiberarioUtils.getLocName(l));
		}

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	public void setFavs(boolean favs) {
		this.favs = favs;
	}

	public void setHome(boolean home) {
		this.home = home;
	}

	public void setGPS(boolean gps) {
		this.gps = gps;
	}

	public void resetList() {
		favList.clear();
		filteredList.clear();

		Location home_loc = null;

		if(home) {
			home_loc = FavDB.getHome(getContext());
			if(home_loc != null) {
				favList.add(new Location(LocationType.ANY, "Liberario.HOME", home_loc.place, home_loc.name));
			} else {
				favList.add(new Location(LocationType.ANY, "Liberario.HOME"));
			}
		}
		if(gps) favList.add(new Location(LocationType.ANY, "Liberario.GPS"));

		if(favs) {
			List<Location> tmpList = FavDB.getFavLocationList(getContext(), sort, onlyIDs);
			// remove home location from favorites if it is set
			if(home && home_loc != null) {
				tmpList.remove(home_loc);
			}

			favList.addAll(tmpList);
		}
	}

	public int addFavs() {
		resetList();
		filteredList.addAll(favList);

		return filteredList.size();
	}

}