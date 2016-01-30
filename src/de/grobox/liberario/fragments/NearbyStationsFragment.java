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

package de.grobox.liberario.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.EnumSet;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.MapStationsActivity;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.adapters.StationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.tasks.AsyncQueryNearbyStationsTask;
import de.grobox.liberario.ui.LocationInputGPSView;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

public class NearbyStationsFragment extends TransportrFragment {
	private View mView;
	private ViewHolder ui;
	private NearbyStationsInputView loc;
	private StationAdapter stationAdapter;

	// TODO: allow user to specify location types
	EnumSet<LocationType> types = EnumSet.of(LocationType.STATION);

	private static final int MAX_DISTANCE = 0;
	private static final int MAX_STATIONS = 5;
	private int maxStations = MAX_STATIONS;

	private static final String TAG = NearbyStationsFragment.class.toString();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_nearbystations, container, false);

		ui = new ViewHolder(mView);

		// Location Input View

		loc = new NearbyStationsInputView(this, ui.station);
		loc.setFavs(true);
		loc.setHome(true);
		loc.setHint(R.string.location);

		// Find Nearby Stations Search Button

		ui.search.setOnClickListener(new OnClickListener() {
			                             @Override
			                             public void onClick(View v) {
				                             search();
			                             }
		                             });

		// hide departure list initially
		ui.stations_area.setVisibility(View.GONE);

		ui.recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		ui.recycler.setItemAnimator(new DefaultItemAnimator());

		stationAdapter = new StationAdapter(new ArrayList<Location>(), R.layout.station);
		ui.recycler.setAdapter(stationAdapter);

		// Swipe to Refresh

		ui.swipe_refresh.setColorSchemeResources(R.color.accent);
		ui.swipe_refresh.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
		ui.swipe_refresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			                                      @Override
			                                      public void onRefresh(final SwipyRefreshLayoutDirection direction) {
				                                      maxStations += MAX_STATIONS;

				                                      AsyncQueryNearbyStationsTask task = new AsyncQueryNearbyStationsTask(NearbyStationsFragment.this, types, stationAdapter.getStart(), MAX_DISTANCE, maxStations);
				                                      task.execute();
			                                      }
		                                      });

		return mView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(ui.menu_map != null) {
			outState.putBoolean("menu_map", ui.menu_map.isVisible());
		}

		if(loc != null && loc.getLocation() != null) {
			outState.putSerializable("loc", loc.getLocation());
		}

		if(stationAdapter != null && stationAdapter.getItemCount() > 0) {
			outState.putSerializable("stations", stationAdapter.getStations());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null) {
			if(ui.menu_map != null) {
				// TODO do this later, because onCreateOptionsMenu will be called later, so menu_map does not yet exist
				ui.menu_map.setVisible(savedInstanceState.getBoolean("menu_map"));
			}

			Location location = (Location) savedInstanceState.getSerializable("loc");
			if(location != null) {
				loc.setLocation(location, TransportrUtils.getDrawableForLocation(getContext(), location));
				stationAdapter.setStart(location);
			}

			ArrayList<Location> stations = (ArrayList<Location>) savedInstanceState.getSerializable("stations");
			if(stations != null && stations.size() > 0) {
				stationAdapter.addAll(stations);
				ui.stations_area.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.nearbystations, menu);

		ui.menu_map = menu.findItem(R.id.action_location_map);
		ui.menu_map.setVisible(false);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case R.id.action_location_map:
				ArrayList<Location> stations = stationAdapter.getStations();

				if(stations == null) {
					Toast.makeText(getActivity(), getString(R.string.error_no_station_location), Toast.LENGTH_SHORT).show();
					return false;
				}

				// show stations on map
				Intent intent = new Intent(getActivity(), MapStationsActivity.class);
				intent.putExtra("List<de.schildbach.pte.dto.Location>", stations);
				intent.putExtra("de.schildbach.pte.dto.Location", stationAdapter.getStart());
				startActivity(intent);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onNetworkProviderChanged(TransportNetwork network) {
		if(mView == null) return;

		stationAdapter.clear();
		ui.stations_area.setVisibility(View.GONE);
		loc.setLocation(null, null);
	}

	private void search() {
		if(loc.getLocation() != null) {
			// use location to query nearby stations

			if(!loc.getLocation().hasId() && !loc.getLocation().hasLocation()) {
				Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
				return;
			}

			if(loc.getLocation().hasId()) {
				// Location is valid, so make it a favorite or increase counter
				RecentsDB.updateFavLocation(getActivity(), loc.getLocation(), FavLocation.LOC_TYPE.FROM);
			}

			// play animations before clearing departures list
			onRefreshStart();

			// clear old list
			stationAdapter.clear();

			// set location origin
			stationAdapter.setStart(loc.getLocation());

			// reset number of stations to retrieve
			maxStations = MAX_STATIONS;

			AsyncQueryNearbyStationsTask task = new AsyncQueryNearbyStationsTask(this, types, loc.getLocation(), MAX_DISTANCE, maxStations);
			task.execute();
		} else if(!loc.isSearching()) {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
		}
	}

	public void searchByLocation(Location loc) {
		if(this.loc != null) {
			this.loc.setLocation(loc, TransportrUtils.getDrawableForLocation(getContext(), loc));
			search();
		}
	}

	public void activateGPS() {
		if(this.loc != null) {
			this.loc.activateGPS();
			search();
		}
	}

	public void addStations(NearbyLocationsResult result) {
		Log.d(TAG, "Nearby Stations: " + result.locations.toString());
		stationAdapter.addAll(result.locations);

		onRefreshComplete();
	}

	public void onRefreshStart() {
		if(ui.stations_area.getVisibility() == View.GONE) {
			// only fade in departure list on first search
			ui.stations_area.setAlpha(0f);
			ui.stations_area.setVisibility(View.VISIBLE);
			ui.stations_area.animate().alpha(1f).setDuration(750);
		}

		// fade out recycler view
		ui.recycler.animate().alpha(0f).setDuration(750);
		ui.recycler.setVisibility(View.GONE);

		// fade in progress bar
		ui.progress.setAlpha(0f);
		ui.progress.setVisibility(View.VISIBLE);
		ui.progress.animate().alpha(1f).setDuration(750);

		if(ui.menu_map != null) ui.menu_map.setVisible(false);
	}

	public void onRefreshComplete() {
		if(ui.progress.getVisibility() == View.VISIBLE) {
			// fade departures in and progress out
			ui.recycler.setAlpha(0f);
			ui.recycler.setVisibility(View.VISIBLE);
			ui.recycler.animate().alpha(1f).setDuration(750);
			ui.progress.animate().alpha(0f).setDuration(750);
		} else {
			// hide progress indicator
			ui.swipe_refresh.setRefreshing(false);

			// scroll smoothly up or down when we have new trips
			ui.recycler.smoothScrollBy(0, 150);
		}

		ui.progress.setVisibility(View.GONE);

		ArrayList<Location> stations = stationAdapter.getStations();
		if(ui.menu_map != null && stations != null) {
			boolean hasLocation = false;

			// check if at least one station has a location attached to it
			for(Location station : stations) {
				if(station.hasLocation()) {
					hasLocation = true;
					break;
				}
			}

			if(hasLocation) {
				ui.menu_map.setVisible(true);
			}
		}
	}

	public void onRefreshError() {
		//ui.recycler.setVisibility(View.GONE);
		ui.stations_area.setVisibility(View.GONE);

		// hide progress indicator
		ui.swipe_refresh.setRefreshing(false);
		ui.progress.setVisibility(View.GONE);

		if(ui.menu_map != null) {
			ui.menu_map.setVisible(false);
		}
	}

	private static class ViewHolder {

		public LocationInputView.LocationInputViewHolder station;
		public Button search;
		public ViewGroup stations_area;
		public ProgressBar progress;
		public SwipyRefreshLayout swipe_refresh;
		public RecyclerView recycler;
		public MenuItem menu_map;

		public ViewHolder(View view) {
			station = new LocationInputView.LocationInputViewHolder(view);
			search = (Button) view.findViewById(R.id.searchButton);
			stations_area = (ViewGroup) view.findViewById(R.id.nearbystations_list);
			progress = (ProgressBar) view.findViewById(R.id.progressBar);
			swipe_refresh = (SwipyRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
			recycler = (RecyclerView) view.findViewById(R.id.nearbystations_recycler_view);
		}
	}

	private static class NearbyStationsInputView extends LocationInputGPSView {

		NearbyStationsFragment fragment;

		public NearbyStationsInputView(NearbyStationsFragment fragment, LocationInputViewHolder holder) {
			super(fragment.getActivity(), holder, MainActivity.PR_ACCESS_FINE_LOCATION_NEARBY_STATIONS);

			this.fragment = fragment;
		}

		@Override
		public void activateGPS() {
			super.activateGPS();

			if(!isRequestingPermission()) fragment.onRefreshStart();
		}

		@Override
		public void deactivateGPS() {
			super.deactivateGPS();
			fragment.ui.stations_area.setVisibility(View.GONE);
		}

		@Override
		public void onLocationChanged(Location location) {
			super.onLocationChanged(location);

			if(!isRequestingPermission()) fragment.search();
		}

		@Override
		public void onLocationItemClick(Location loc, View view) {
			super.onLocationItemClick(loc, view);

			if(!isRequestingPermission()) fragment.search();
		}

		@Override
		public void selectHomeLocation() {
			// show dialog to set home screen
			Intent intent = new Intent(fragment.getActivity(), SetHomeActivity.class);
			intent.putExtra("new", true);

			fragment.startActivityForResult(intent, MainActivity.CHANGED_HOME);
		}

	}
}

