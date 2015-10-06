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

package de.grobox.liberario.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import de.grobox.liberario.FavTrip;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.utils.LiberarioUtils;

public class FavTripsFragment extends LiberarioListFragment {
	private FavTripArrayAdapter adapter;
	private ActionMode mActionMode;
	private Toolbar toolbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		adapter = new FavTripArrayAdapter(getActivity(), R.layout.fav_trip_list_item, FavDB.getFavTripList(getActivity(), Preferences.getPref(getActivity(), Preferences.SORT_FAV_TRIPS_COUNT)));
		setListAdapter(adapter);

		toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);

		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}

	@Override
	public void onResume() {
		super.onResume();

		setEmptyText(getString(R.string.fav_trips_empty));

		// although the network provider has not changed, other things might have, so reload data
		onNetworkProviderChanged(Preferences.getTransportNetwork(getActivity()));
	}

	@Override
	public void onPause() {
		super.onPause();

		if(mActionMode != null) mActionMode.finish();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.fav_trip_actions, menu);

		if(Preferences.getPref(getActivity(), Preferences.SORT_FAV_TRIPS_COUNT)) {
			menu.findItem(R.id.action_fav_trips_sort_count).setChecked(true);
		} else {
			menu.findItem(R.id.action_fav_trips_sort_recent).setChecked(true);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_fav_trips_sort_count:
				adapter = new FavTripArrayAdapter(getActivity(), R.layout.fav_trip_list_item, FavDB.getFavTripList(getActivity(), true));
				Preferences.setPref(getActivity(), Preferences.SORT_FAV_TRIPS_COUNT, true);
				setListAdapter(adapter);
				item.setChecked(true);
				return true;
			case R.id.action_fav_trips_sort_recent:
				adapter = new FavTripArrayAdapter(getActivity(), R.layout.fav_trip_list_item, FavDB.getFavTripList(getActivity(), false));
				Preferences.setPref(getActivity(), Preferences.SORT_FAV_TRIPS_COUNT, false);
				setListAdapter(adapter);
				item.setChecked(true);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	// change things for a different network provider
	public void onNetworkProviderChanged(TransportNetwork network) {
		// reload data because it has changed
		if(getActivity() != null && adapter != null) {
			adapter.clear();
			adapter.addAll(FavDB.getFavTripList(getActivity(), Preferences.getPref(getActivity(), Preferences.SORT_FAV_TRIPS_COUNT)));
		}
	}

	private void checkTrip(View v, int position) {
		getListView().setItemChecked(position, true);

		CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
		checkBox.setChecked(true);

		if(mActionMode == null) {
			mActionMode = toolbar.startActionMode(mFavTripActionMode);
		}
	}

	private void uncheckTrip(View v, int position) {
		getListView().setItemChecked(position, false);

		CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
		checkBox.setChecked(false);

		if(mActionMode == null) {
			mActionMode = toolbar.startActionMode(mFavTripActionMode);
		}
		else if(getListView().getCheckedItemCount() == 0) {
			mActionMode.finish();
		}
	}

	private class FavTripArrayAdapter extends ArrayAdapter<FavTrip> {
		public FavTripArrayAdapter(Context context, int textViewResourceId,	List<FavTrip> objects) {
			super(context, textViewResourceId, objects);
		}

		private void queryFavTrip(final int position, final boolean swap) {
			FavTrip trip = (FavTrip) getListView().getItemAtPosition(position);

			if(swap) {
				LiberarioUtils.findDirections(getActivity(), trip.getTo(), trip.getFrom());
			}
			else {
				LiberarioUtils.findDirections(getActivity(), trip.getFrom(), trip.getTo());
			}
		}

		@Override
		public View getView(final int position, View v, ViewGroup parent) {
			if(v == null) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.fav_trip_list_item, parent, false);
			}

			// handle click on row
			v.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					queryFavTrip(position, false);
				}
			});

			// handle click on direction swap button
			ImageButton swapButton = (ImageButton) v.findViewById(R.id.swapButton);
			swapButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					queryFavTrip(position, true);
				}
			});
			swapButton.setColorFilter(LiberarioUtils.getButtonIconColor(getActivity()), PorterDuff.Mode.SRC_IN);

			// select trip on long click
			v.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					if(getListView().getCheckedItemPositions().get(position)) {
						uncheckTrip(v, position);
					} else {
						checkTrip(v, position);
					}
					return true;
				}
			});

			FavTrip trip = this.getItem(position);

			TextView favFromView = (TextView) v.findViewById(R.id.favFromView);
			favFromView.setText(trip.getFrom().uniqueShortName());
			TextView favToView = (TextView) v.findViewById(R.id.favToView);
			favToView.setText(trip.getTo().uniqueShortName());
			TextView favCountView = (TextView) v.findViewById(R.id.favCountView);
			favCountView.setText(String.valueOf(trip.getCount()));

			// handle click on check box
			CheckBox checkBox = (CheckBox) v.findViewById(R.id.checkBox);
			checkBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					View parent = (View) v.getParent();
					if(getListView().getCheckedItemPositions().get(position)) {
						uncheckTrip(parent, position);
					} else {
						checkTrip(parent, position);
					}
				}
			});

			return v;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}
	}

	private ActionMode.Callback mFavTripActionMode = new ActionMode.Callback() {
		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.fav_trip_select_actions, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after onCreateActionMode,
		// but may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
			switch(item.getItemId()) {
				case R.id.action_trip_delete:
					new AlertDialog.Builder(getActivity())
					.setMessage(getActivity().getResources().getString(R.string.clear_fav_trips, getListView().getCheckedItemCount()))
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							SparseBooleanArray tmp = getListView().getCheckedItemPositions();

							// loop over selected items and delete associated trips
							for(int i = tmp.size()-1; i >= 0; i--) {
								if(tmp.valueAt(i)) {
									int pos = tmp.keyAt(i);
									FavTrip trip = adapter.getItem(pos);
									FavDB.unfavTrip(getActivity(), trip);
									adapter.remove(trip);
								}
							}
							mode.finish();
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					})
					.show();
					return true;
				default:
					return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			SparseBooleanArray tmp = getListView().getCheckedItemPositions();

			// loop over checked items and deselect them
			for(int i = tmp.size()-1; i >= 0; i--) {
				if(tmp.valueAt(i)) {
					int pos = tmp.keyAt(i);

					if(getListView().getChildAt(pos) != null) {
						uncheckTrip(getListView().getChildAt(pos), pos);
					}
				}
			}
			mActionMode = null;
		}
	};
}

