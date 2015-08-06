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
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.ArrayList;
import java.util.Date;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.adapters.DepartureAdapter;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.tasks.AsyncQueryDeparturesTask;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;

public class DeparturesFragment extends LiberarioFragment {
	private View mView;
	private ViewHolder ui;
	private DepartureAdapter departureAdapter;
	private LocationInputView loc;
	private String stationId;
	private Date date;

	private static final int MAX_DEPARTURES = 12;
	private static final int SAFETY_MARGIN = 6;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_departures, container, false);

		ui = new ViewHolder(mView);

		// Location Input View

		loc = new DepartureInputView(this, ui.station, true);
		loc.setFavs(true);
		loc.setHome(true);

		ui.station.location.setHint(R.string.departure_station);

		DateUtils.setUpTimeDateUi(mView);

		// Find Departures Search Button

		ui.search.setColorFilter(LiberarioUtils.getButtonIconColor(getActivity()));
		ui.search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				search();
			}
		});

		// hide departure list initially
		ui.departure_list.setVisibility(View.GONE);

		ui.recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
		ui.recycler.setItemAnimator(new DefaultItemAnimator());

		departureAdapter = new DepartureAdapter(getActivity(), new ArrayList<Departure>(), R.layout.departure);
		ui.recycler.setAdapter(departureAdapter);

		// Swipe to Refresh

		ui.swipe_refresh.setColorSchemeResources(R.color.accent);
		ui.swipe_refresh.setDirection(SwipyRefreshLayoutDirection.BOTH);
		ui.swipe_refresh.setDistanceToTriggerSync(150);
		ui.swipe_refresh.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh(final SwipyRefreshLayoutDirection direction) {
				searchMore(direction != SwipyRefreshLayoutDirection.TOP);
			}
		});

		return mView;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after new home location was selected, put it right into the input field
		if(resultCode == AppCompatActivity.RESULT_OK && requestCode == MainActivity.CHANGED_HOME) {
			//noinspection deprecation
			loc.setLocation(FavDB.getHome(getActivity()), getResources().getDrawable(R.drawable.ic_action_home));
		}
	}

	@Override
	public void onNetworkProviderChanged(TransportNetwork network) {
		if(mView == null) return;

		// TODO we probably want to switch the section when network does not support departures
//		NetworkProvider np = network.getNetworkProvider();
//      if(!np.hasCapabilities(Capability.DEPARTURES))

		ui.departure_list.setVisibility(View.GONE);

		//clear favorites for auto-complete
		if(ui.station.location.getAdapter() != null) {
			((LocationAdapter) ui.station.location.getAdapter()).resetList();
		}

		// clear text view
		loc.clearLocation();
	}

	public void search() {
		if(loc.getLocation() != null) {
			// use location to query departures

			if(!loc.getLocation().hasId()) {
				Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
				return;
			}

			// Location is valid, so make it a favorite or increase counter
			FavDB.updateFavLocation(getActivity(), loc.getLocation(), FavLocation.LOC_TYPE.FROM);

			date = DateUtils.getDateFromUi(mView);
			stationId = loc.getLocation().id;

			// play animations before clearing departures list
			onRefreshStart();

			// clear old list
			departureAdapter.clear();

			AsyncQueryDeparturesTask query_stations = new AsyncQueryDeparturesTask(DeparturesFragment.this, stationId, date, true, MAX_DEPARTURES);
			query_stations.execute();
		} else {
			Toast.makeText(getActivity(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
		}
	}

	public void addDepartures(QueryDeparturesResult result, boolean later, boolean more) {
		int count = 0;

		for(final StationDepartures stadep : result.stationDepartures) {
			departureAdapter.addAll(stadep.departures);

			if(more) count += stadep.departures.size();
		}

		if(more && count < MAX_DEPARTURES) {
			Toast.makeText(getActivity(), R.string.warning_departure_gap, Toast.LENGTH_LONG).show();
		}

		onRefreshComplete(later);
	}

	public void onRefreshStart() {
		if(ui.departure_list.getVisibility() == View.GONE) {
			// only fade in departure list on first search
			ui.departure_list.setAlpha(0f);
			ui.departure_list.setVisibility(View.VISIBLE);
			ui.departure_list.animate().alpha(1f).setDuration(750);
		}

		// fade out recycler view
		ui.recycler.animate().alpha(0f).setDuration(750);
		ui.recycler.setVisibility(View.GONE);

		// fade in progress bar
		ui.progress.setAlpha(0f);
		ui.progress.setVisibility(View.VISIBLE);
		ui.progress.animate().alpha(1f).setDuration(750);
	}

	public void onRefreshComplete(boolean later) {
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
			ui.recycler.smoothScrollBy(0, later ? 150 : -150);
		}

		ui.progress.setVisibility(View.GONE);
	}

	public void onNoResults(boolean later, boolean more) {
		onRefreshComplete(later);

		if(!more) {
			// fade out departure list
			ui.departure_list.animate().alpha(0f).setDuration(750);
			// can't use withEndAction() in this API level
			final Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				                    @Override
				                    public void run() {
					                    ui.departure_list.setVisibility(View.GONE);
				                    }
			                    }, 750
			);
		}
	}

	public void searchMore(boolean later) {
		int item_pos;
		int max_departures = MAX_DEPARTURES;

		// search from end + safety margin
		if(later) {
			if(departureAdapter.getItemCount() - SAFETY_MARGIN > 0) {
				item_pos = departureAdapter.getItemCount() - SAFETY_MARGIN;
				max_departures = MAX_DEPARTURES + SAFETY_MARGIN;
			} else {
				item_pos = departureAdapter.getItemCount() - 1;
			}
			date = departureAdapter.getItem(item_pos).getTime();
		}
		// search from beginning + safety margin
		else {
			Date earliest = departureAdapter.getItem(0).getTime();
			Date latest;
			long span;

			if(departureAdapter.getItemCount() >= MAX_DEPARTURES) {
				latest = departureAdapter.getItem(MAX_DEPARTURES - 1).getTime();
			} else {
				latest = departureAdapter.getItem(departureAdapter.getItemCount() - 1).getTime();
			}
			span = latest.getTime() - earliest.getTime();
			date.setTime(earliest.getTime() - span);

			max_departures = MAX_DEPARTURES + SAFETY_MARGIN;
		}

		new AsyncQueryDeparturesTask(this, stationId, date, later, max_departures, true).execute();
	}

	public void searchByLocation(Location loc) {
		this.loc.setLocation(loc, null);
		search();
	}

	private static class ViewHolder {

		public LocationInputView.LocationInputViewHolder station;
		public ImageButton search;
		public ViewGroup departure_list;
		public SwipyRefreshLayout swipe_refresh;
		public ProgressBar progress;
		public RecyclerView recycler;

		public ViewHolder(View view) {
			station = new LocationInputView.LocationInputViewHolder(view);
			search = (ImageButton) view.findViewById(R.id.stationButton);
			departure_list = (ViewGroup) view.findViewById(R.id.departure_list);
			swipe_refresh = (SwipyRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
			progress = (ProgressBar) view.findViewById(R.id.progressBar);
			recycler = (RecyclerView) view.findViewById(R.id.departures_recycler_view);
		}
	}

	private static class DepartureInputView extends LocationInputView {

		DeparturesFragment fragment;

		public DepartureInputView(DeparturesFragment fragment, LocationInputViewHolder holder, boolean onlyIDs) {
			super(fragment.getActivity(), holder, onlyIDs);

			this.fragment = fragment;
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
