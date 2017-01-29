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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.EnumSet;

import de.grobox.liberario.R;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.adapters.StationAdapter;
import de.grobox.liberario.tasks.AsyncQueryNearbyStationsTask;
import de.grobox.liberario.locations.LocationGpsView;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.locations.FavLocation.LOC_TYPE.FROM;
import static de.grobox.liberario.locations.WrapLocation.WrapType.MAP;
import static de.grobox.liberario.data.RecentsDB.updateFavLocation;
import static de.grobox.liberario.utils.TransportrUtils.getDragDistance;
import static de.grobox.liberario.utils.TransportrUtils.getDrawableForLocation;
import static de.grobox.liberario.utils.TransportrUtils.showLocationsOnMap;

@Deprecated
public class NearbyStationsFragment extends TransportrFragment {

	public static final String TAG = "de.grobox.liberario.nearby_locations";

	private ViewHolder ui;
	private StationAdapter stationAdapter;

	// TODO: allow user to specify location types
	EnumSet<LocationType> types = EnumSet.of(LocationType.STATION);

	private static final int MAX_DISTANCE = 0;
	private static final int MAX_STATIONS = 10;
	private int maxStations = MAX_STATIONS;

	private final String START = "start";
	private final String STATIONS = "stations";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		View v = inflater.inflate(R.layout.fragment_nearbystations, container, false);

		ui = new ViewHolder(v);

		// Location Input View
		ui.station.setCaller(MainActivity.PR_ACCESS_FINE_LOCATION_NEARBY_STATIONS);
		ui.station.setLocationViewListener(new LocationView.LocationViewListener() {
			@Override
			public void onLocationItemClick(WrapLocation loc) {
				if(loc != null && loc.getType() != MAP) {
					search();
				}
			}
			@Override
			public void onLocationCleared() { }
		});
		LocationGpsView.LocationGpsListener listener = new LocationGpsView.LocationGpsListener() {
			@Override
			public void activateGPS() {
				onRefreshStart();
			}

			@Override
			public void deactivateGPS() {
				ui.stations_area.setVisibility(GONE);
			}

			@Override
			public void onLocationChanged(Location location) {
				search();
			}
		};
		ui.station.setLocationGpsListener(listener);

		ui.recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		ui.recycler.setItemAnimator(new DefaultItemAnimator());

		if(stationAdapter == null) {
			stationAdapter = new StationAdapter(new ArrayList<Location>(), R.layout.station);

			// hide departure list initially
			ui.stations_area.setVisibility(GONE);
		}
		ui.recycler.setAdapter(stationAdapter);

		// Swipe to Refresh

		ui.swipe_refresh.setColorSchemeResources(R.color.accent);
		ui.swipe_refresh.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
		ui.swipe_refresh.setDistanceToTriggerSync(getDragDistance(getContext()));
		ui.swipe_refresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			                                      @Override
			                                      public void onRefresh(final SwipyRefreshLayoutDirection direction) {
				                                      maxStations += MAX_STATIONS;

				                                      AsyncQueryNearbyStationsTask task = new AsyncQueryNearbyStationsTask(NearbyStationsFragment.this, types, stationAdapter.getStart(), MAX_DISTANCE, maxStations);
				                                      task.execute();
			                                      }
		                                      });

		return v;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if(ui != null && ui.station.getLocation() != null) {
			outState.putSerializable(START, ui.station.getLocation());
		}

		if(stationAdapter != null && stationAdapter.getItemCount() > 0) {
			outState.putSerializable(STATIONS, stationAdapter.getStations());
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(savedInstanceState != null) {
			Location start = (Location) savedInstanceState.getSerializable(START);
			if(start != null) stationAdapter.setStart(start);

			@SuppressWarnings("unchecked")
			ArrayList<Location> stations = (ArrayList<Location>) savedInstanceState.getSerializable(STATIONS);
			if(stations != null && stations.size() > 0) {
				stationAdapter.addAll(stations);
				ui.stations_area.setVisibility(VISIBLE);
			}

		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// clear favorites for auto-complete
		ui.station.resetIfEmpty();

		// check if there's an intent for us and if so, act on it
		processIntent();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.nearbystations, menu);

		ui.menu_map = menu.findItem(R.id.action_location_map);
		if(stationAdapter != null && stationAdapter.getItemCount() > 0) {
			ui.menu_map.setVisible(true);
		} else ui.menu_map.setVisible(false);
		TransportrUtils.fixToolbarIcon(getContext(), ui.menu_map);

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
				showLocationsOnMap(getContext(), stations, stationAdapter.getStart());

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void search() {
		if(ui.station.getLocation() != null) {
			// use location to query nearby stations

			if(!ui.station.getLocation().hasId() && !ui.station.getLocation().hasLocation()) {
				Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
				return;
			}

			if(ui.station.getLocation().hasId()) {
				// Location is valid, so make it a favorite or increase counter
				updateFavLocation(getActivity(), ui.station.getLocation(), FROM);
			}

			// play animations before clearing departures list
			onRefreshStart();

			// clear old list
			stationAdapter.clear();

			// set location origin
			stationAdapter.setStart(ui.station.getLocation());

			// reset number of stations to retrieve
			maxStations = MAX_STATIONS;

			AsyncQueryNearbyStationsTask task = new AsyncQueryNearbyStationsTask(this, types, ui.station.getLocation(), MAX_DISTANCE, maxStations);
			task.execute();
		} else if(!ui.station.isSearching()) {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
		}
	}

	private void processIntent() {
		final Intent intent = getActivity().getIntent();
		if(intent != null) {
			final String action = intent.getAction();
			if(action != null && action.equals(TAG)) {
				// get location and search departures for it
				Location loc = (Location) intent.getSerializableExtra("location");
				if(loc != null) {
					ui.station.setLocation(loc, getDrawableForLocation(getContext(), loc));
					search();
				}
			}

			// remove the intent (and clear its action) since it was already processed
			// and should not be processed again
			intent.setAction(null);
			getActivity().setIntent(null);
		}
	}

	public void activateGPS() {
		if(ui.station != null) {
			ui.station.activateGPS();
			search();
		}
	}

	public void addStations(NearbyLocationsResult result) {
		if(result.locations != null) {
			Log.d(NearbyStationsFragment.class.getName(), "Nearby Stations: " + result.locations.toString());
			stationAdapter.addAll(result.locations);
		}

		onRefreshComplete();
	}

	public void onRefreshStart() {
		if(ui.stations_area.getVisibility() == GONE) {
			// only fade in departure list on first search
			ui.stations_area.setAlpha(0f);
			ui.stations_area.setVisibility(VISIBLE);
			ui.stations_area.animate().alpha(1f).setDuration(750);
		}

		// fade out recycler view
		ui.recycler.animate().alpha(0f).setDuration(750);
		ui.recycler.setVisibility(GONE);

		// fade in progress bar
		ui.progress.setAlpha(0f);
		ui.progress.setVisibility(VISIBLE);
		ui.progress.animate().alpha(1f).setDuration(750);

		if(ui.menu_map != null) ui.menu_map.setVisible(false);
	}

	public void onRefreshComplete() {
		if(ui.progress.getVisibility() == VISIBLE) {
			// fade departures in and progress out
			ui.recycler.setAlpha(0f);
			ui.recycler.setVisibility(VISIBLE);
			ui.recycler.animate().alpha(1f).setDuration(750);
			ui.progress.animate().alpha(0f).setDuration(750);
		} else {
			// hide progress indicator
			ui.swipe_refresh.setRefreshing(false);

			// scroll smoothly up or down when we have new trips
			ui.recycler.smoothScrollBy(0, 150);
		}

		ui.progress.setVisibility(GONE);

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
		ui.stations_area.setVisibility(GONE);

		// hide progress indicator
		ui.swipe_refresh.setRefreshing(false);
		ui.progress.setVisibility(GONE);

		if(ui.menu_map != null) {
			ui.menu_map.setVisible(false);
		}
	}

	private static class ViewHolder {

		public LocationGpsView station;
		Button search;
		ViewGroup stations_area;
		ProgressBar progress;
		SwipyRefreshLayout swipe_refresh;
		RecyclerView recycler;
		MenuItem menu_map;

		public ViewHolder(View view) {
			station = (LocationGpsView) view.findViewById(R.id.location_input);
			search = (Button) view.findViewById(R.id.searchButton);
			stations_area = (ViewGroup) view.findViewById(R.id.nearbystations_list);
			progress = (ProgressBar) view.findViewById(R.id.progressBar);
			swipe_refresh = (SwipyRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
			recycler = (RecyclerView) view.findViewById(R.id.nearbystations_recycler_view);
		}
	}

}
