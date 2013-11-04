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

import de.grobox.liberario.R;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.LineDestination;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.NearbyStationsResult;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StationsListActivity extends Activity {
	private LinearLayout main;
	private LocationManager locationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stations_list);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		main = (LinearLayout) findViewById(R.id.activity_stations_list);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Intent intent = getIntent();
		NearbyStationsResult stations = (NearbyStationsResult) intent.getSerializableExtra("de.schildbach.pte.dto.NearbyStationsResult");

		addStations(stations.stations);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			// TODO add position button
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void addStations(List<Location> stations) {
		android.location.Location cur_loc = new android.location.Location("");
		android.location.Location sta_loc = new android.location.Location("");

		// get last known position
		for(String provider : locationManager.getProviders(true)) {
			// Register the listener with the Location Manager to receive location updates
			android.location.Location tmp_loc = locationManager.getLastKnownLocation(provider);
			if(tmp_loc.getTime() > cur_loc.getTime()) {
				cur_loc = tmp_loc;
			}
			Log.d(getClass().getSimpleName(), "Received last known location: " + cur_loc.toString());
		}

		for(final Location station : stations) {
			LinearLayout stationView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.station, null);

			TextView stationNameView = (TextView) stationView.findViewById(R.id.stationNameView);
			stationNameView.setText(station.uniqueShortName());

			// transform station location into android format
			sta_loc.setLatitude(station.lat / 1E6);
			sta_loc.setLongitude(station.lon / 1E6);

			TextView distanceView = (TextView) stationView.findViewById(R.id.distanceView);
			distanceView.setText(String.valueOf(Math.round(cur_loc.distanceTo(sta_loc))) + " m");

			stationView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					ViewGroup parent = ((ViewGroup) view.getParent());
					View v = parent.getChildAt(parent.indexOfChild(view) + 1);
					if(v != null) {
						if(v.getVisibility() == View.GONE) {
							v.setVisibility(View.VISIBLE);
						}
						else if(v.getVisibility() == View.VISIBLE) {
							v.setVisibility(View.GONE);
						}
					}
				}
			});

			main.addView(stationView);

			AsyncQueryDeparturesTask query_stations = new AsyncQueryDeparturesTask(this, stationView, station.id);
			query_stations.execute();

			LinearLayout depList = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.departure_list, null);
			main.addView(depList);

			main.addView(TripsActivity.getDivider(this));
		}
	}

	public void addDepartures(View v, QueryDeparturesResult result) {
		LinearLayout layout = (LinearLayout) main.getChildAt(main.indexOfChild(v) + 1);

		for(final StationDepartures stat_dep : result.stationDepartures) {
			// add line boxes if available
			if(stat_dep.lines != null) {
				LinearLayout stationView = (LinearLayout) v;
				LinearLayout lineLayout = (LinearLayout) stationView.findViewById(R.id.lineLayout);
				for(LineDestination line : stat_dep.lines) {
					TripsActivity.addLineBox(this, lineLayout, line.line);
				}
			}

			// get maximum number of departures
			int max = AsyncQueryDeparturesTask.max_departures;
			if(stat_dep.departures.size() <= max) {
				max = stat_dep.departures.size() - 1;
			}

			for(final Departure dep : stat_dep.departures.subList(0, max)) {
				LinearLayout view = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.departure, null);

				TextView timeView = (TextView) view.findViewById(R.id.timeView);
				timeView.setText(DateUtils.getTime(dep.plannedTime));

				if(dep.predictedTime != null) {
					long delay = dep.predictedTime.getTime() - dep.plannedTime.getTime();

					if(delay > 0) {
						TextView delayView = (TextView) view.findViewById(R.id.delayView);
						delayView.setText("+" + Long.toString(delay / 1000 / 60));
					}
				}

				LinearLayout lineLayout = (LinearLayout) view.findViewById(R.id.lineLayout);
				TripsActivity.addLineBox(this, lineLayout, dep.line, 0);

				TextView destinationView = (TextView) view.findViewById(R.id.destinationView);
				destinationView.setText(dep.destination.uniqueShortName());

				// TODO use position preference
				if(dep.position != null) {
					TextView positionView = (TextView) view.findViewById(R.id.positionView);
					positionView.setText(dep.position);
				}

				layout.addView(view);
			}
		}

	}

}
