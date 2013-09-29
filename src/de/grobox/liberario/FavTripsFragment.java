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

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FavTripsFragment extends ListFragment {
	private FavTripArrayAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		adapter = new FavTripArrayAdapter(getActivity(), R.layout.list_item, FavFile.getFavTripList(getActivity()));
		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

		// remove trip on long click
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final FavTrip trip = (FavTrip) parent.getItemAtPosition(position);

				new AlertDialog.Builder(getActivity())
				.setMessage(getActivity().getResources().getString(R.string.clear_fav_trips))
				.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						FavFile.unfavTrip(getActivity(), trip);
						adapter.remove(trip);
					}
				})
				.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.show();
				return true;
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		// reload data because it might have changed
		adapter.clear();
		adapter.addAll(FavFile.getFavTripList(getActivity()));
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		FavTrip trip = (FavTrip) l.getItemAtPosition(position);

		AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());
		query_trips.setFrom(trip.getFrom());
		query_trips.setTo(trip.getTo());

		// remember trip
		FavFile.useFavTrip(getActivity(), trip);

		query_trips.execute();
	}


	private class FavTripArrayAdapter extends ArrayAdapter<FavTrip> {
		public FavTripArrayAdapter(Context context, int textViewResourceId,	List<FavTrip> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

}

