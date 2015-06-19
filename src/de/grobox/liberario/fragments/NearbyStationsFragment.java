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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
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
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.adapters.StationAdapter;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.tasks.AsyncQueryNearbyStationsTask;
import de.grobox.liberario.ui.LocationInputGPSView;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;

public class NearbyStationsFragment extends LiberarioFragment {
	private View mView;
	private ViewHolder ui;
	private NearbyStationsInputView loc;
	private StationAdapter stationAdapter;

	// TODO: allow user to specify location types
	EnumSet<LocationType> types = EnumSet.of(LocationType.STATION);

	private static int MAX_STATIONS = 5;
	private int maxStations = MAX_STATIONS;

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

		// Find Nearby Stations GPS Search Button

		ui.gps.setOnClickListener(new OnClickListener() {
			                          @Override
			                          public void onClick(View v) {
				                          loc.activateGPS();
			                          }
		                          });
		ui.gps.setColorFilter(LiberarioUtils.getButtonIconColor(getActivity()));

		// Find Nearby Stations Search Button

		ui.search.setOnClickListener(new OnClickListener() {
			                             @Override
			                             public void onClick(View v) {
				                             search();
			                             }
		                             });

		// hide departure list initially
		ui.stations_card.setVisibility(View.GONE);

		ui.recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		ui.recycler.setItemAnimator(new DefaultItemAnimator());

		stationAdapter = new StationAdapter(new ArrayList<Location>(), R.layout.station);
		ui.recycler.setAdapter(stationAdapter);

		// Swipe to Refresh

		ui.swipe_refresh.setColorSchemeResources(R.color.accent);
		ui.swipe_refresh.setDirection(SwipyRefreshLayoutDirection.BOTTOM);
		ui.swipe_refresh.setDistanceToTriggerSync(150);
		ui.swipe_refresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			                                      @Override
			                                      public void onRefresh(final SwipyRefreshLayoutDirection direction) {
				                                      maxStations += MAX_STATIONS;

				                                      AsyncQueryNearbyStationsTask task = new AsyncQueryNearbyStationsTask(NearbyStationsFragment.this, types, stationAdapter.getLocation(), 0, maxStations);
				                                      task.execute();
			                                      }
		                                      });

		return mView;
	}

	@Override
	public void onNetworkProviderChanged(TransportNetwork network) {
		if(mView == null) return;

		stationAdapter.clear();
		ui.stations_card.setVisibility(View.GONE);
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
				FavDB.updateFavLocation(getActivity(), loc.getLocation(), FavLocation.LOC_TYPE.FROM);
			}

			// play animations before clearing departures list
			onRefreshStart();

			// clear old list
			stationAdapter.clear();

			// set location origin
			stationAdapter.setLocation(loc.getLocation());

			// reset number of stations to retrieve
			maxStations = MAX_STATIONS;

			AsyncQueryNearbyStationsTask task = new AsyncQueryNearbyStationsTask(this, types, loc.getLocation(), 0, maxStations);
			task.execute();
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
		}
	}

	public void addStations(NearbyLocationsResult result) {
		stationAdapter.addAll(result.locations);

		onRefreshComplete();
	}

	public void onRefreshStart() {
		if(ui.stations_card.getVisibility() == View.GONE) {
			// only fade in departure list on first search
			ui.stations_card.setAlpha(0f);
			ui.stations_card.setVisibility(View.VISIBLE);
			ui.stations_card.animate().alpha(1f).setDuration(750);
		}

		// fade out recycler view
		ui.recycler.animate().alpha(0f).setDuration(750);
		ui.recycler.setVisibility(View.GONE);

		// fade in progress bar
		ui.progress.setAlpha(0f);
		ui.progress.setVisibility(View.VISIBLE);
		ui.progress.animate().alpha(1f).setDuration(750);
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
	}

	private static class ViewHolder {

		public LocationInputView.LocationInputViewHolder station;
		public ImageButton gps;
		public Button search;
		public CardView stations_card;
		public ProgressBar progress;
		public SwipyRefreshLayout swipe_refresh;
		public RecyclerView recycler;

		public ViewHolder(View view) {
			station = new LocationInputView.LocationInputViewHolder(view);
			search = (Button) view.findViewById(R.id.searchButton);
			gps = (ImageButton) view.findViewById(R.id.gpsButton);
			stations_card = (CardView) view.findViewById(R.id.nearbystations_list);
			progress = (ProgressBar) view.findViewById(R.id.progressBar);
			swipe_refresh = (SwipyRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
			recycler = (RecyclerView) view.findViewById(R.id.departures_recycler_view);
		}
	}

	private static class NearbyStationsInputView extends LocationInputGPSView {

		NearbyStationsFragment fragment;

		public NearbyStationsInputView(NearbyStationsFragment fragment, LocationInputViewHolder holder) {
			super(fragment.getActivity(), holder);

			this.fragment = fragment;
		}

		@Override
		public void activateGPS() {
			super.activateGPS();

			fragment.onRefreshStart();
		}

		@Override
		public void deactivateGPS() {
			super.deactivateGPS();
			fragment.ui.stations_card.setVisibility(View.GONE);
		}

		@Override
		public void onLocationChanged(Location location) {
			super.onLocationChanged(location);

			fragment.search();
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

