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
import java.util.concurrent.ExecutionException;

import de.grobox.liberario.data.FavDB;
import de.schildbach.pte.dto.Location;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class LocationAdapter extends ArrayAdapter<Location> implements Filterable {
	private List<Location> filteredList;
	private Boolean addedFavs = false;
	private boolean onlyIDs;
	int resource;

	public LocationAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		resource = textViewResourceId;
		this.onlyIDs = false;
	}

	public LocationAdapter(Context context, int textViewResourceId, boolean onlyIDs) {
		super(context, textViewResourceId);
		resource = textViewResourceId;
		this.onlyIDs = onlyIDs;
	}

	// constructor that enables reuse of this class by AmbiguousLocationActivity
	public LocationAdapter(Context context, int textViewResourceId, List<Location> filteredList) {
		super(context, textViewResourceId);
		resource = textViewResourceId;
		this.onlyIDs = false;
		this.filteredList = filteredList;
	}

	@Override
	public int getCount() {
		return filteredList.size();
	}

	@Override
	public Location getItem(int index) {
		return filteredList.get(index);
	}

	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults filterResults = new FilterResults();

				if (constraint != null) {
					AsyncLocationAutoCompleteTask autocomplete = new AsyncLocationAutoCompleteTask(getContext(), constraint.toString());

					List<Location> resultList = null;

					// Retrieve the auto-complete results.
					try {
						resultList = autocomplete.execute().get().locations;
						addedFavs = false;
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if(resultList != null) {
						if(onlyIDs) {
							filteredList = new ArrayList<Location>();

							// only add those locations from result that have a station id
							for(int i = 0; i < resultList.size(); i++) {
								Location l = resultList.get(i);
								if(l.hasId()) filteredList.add(l);
							}
						} else {
							filteredList = resultList;
						}

						// Assign the data to the FilterResults
						filterResults.values = filteredList;
						filterResults.count = filteredList.size();
					}
				}
				return filterResults;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.count > 0) {
					notifyDataSetChanged();
				}
				else {
					notifyDataSetInvalidated();
				}
			}};
			return filter;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			view = inflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}

		// set name of location in list item
		Location l = getItem(position);
		if(l.place != null) {
			((TextView) view).setText(l.name + ", " + l.place);
		} else {
			((TextView) view).setText(l.uniqueShortName());
		}

		return view;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return getView(position, convertView, parent);
	}

	public int addFavs(FavLocation.LOC_TYPE sort) {
		if(!addedFavs) {
			filteredList = FavDB.getFavLocationList(getContext(), sort, onlyIDs);

			addedFavs = true;
		}
		return filteredList.size();
	}

	public void clearFavs() {
		if(filteredList != null) {
			filteredList.clear();
		}
		addedFavs = false;
	}

}