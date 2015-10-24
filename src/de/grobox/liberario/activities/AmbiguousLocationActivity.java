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

package de.grobox.liberario.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.grobox.liberario.tasks.AsyncQueryTripsTask;
import de.grobox.liberario.FavLocation;
import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.QueryTripsResult;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

public class AmbiguousLocationActivity extends AppCompatActivity {
	private Date date;
	private Boolean departure;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ambiguous_location);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(Preferences.getTransportNetwork(this).getName());
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Intent intent = getIntent();
		QueryTripsResult trips = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");
		Location from = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult.from");
		Location to = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult.to");
		date = (Date) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult.date");
		departure = intent.getBooleanExtra("de.schildbach.pte.dto.QueryTripsResult.departure", true);

		final Spinner from_spinner = ((Spinner) findViewById(R.id.fromSpinner));

		if(trips.ambiguousFrom != null) {
			LocationAdapter loca = new LocationAdapter(this, trips.ambiguousFrom);
			loca.setSort(FavLocation.LOC_TYPE.FROM);
			from_spinner.setAdapter(loca);
		}
		else {
			List<Location> list = new ArrayList<>();
			list.add(from);
			LocationAdapter loca = new LocationAdapter(this, list);
			loca.setSort(FavLocation.LOC_TYPE.FROM);
			from_spinner.setAdapter(loca);
			from_spinner.setEnabled(false);
		}

		final Spinner to_spinner = ((Spinner) findViewById(R.id.toSpinner));

		if(trips.ambiguousTo != null) {
			LocationAdapter loca = new LocationAdapter(this, trips.ambiguousTo);
			loca.setSort(FavLocation.LOC_TYPE.TO);
			to_spinner.setAdapter(loca);
		}
		else {
			List<Location> list = new ArrayList<>();
			list.add(to);
			LocationAdapter loca = new LocationAdapter(this, list);
			loca.setSort(FavLocation.LOC_TYPE.TO);
			to_spinner.setAdapter(loca);
			to_spinner.setEnabled(false);
		}

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Location from = (Location) from_spinner.getSelectedItem();
				Location to = (Location) to_spinner.getSelectedItem();

				// remember location and trip
				RecentsDB.updateFavLocation(getApplicationContext(), from, FavLocation.LOC_TYPE.FROM);
				RecentsDB.updateFavLocation(getApplicationContext(), to, FavLocation.LOC_TYPE.TO);
				RecentsDB.updateRecentTrip(getApplicationContext(), new RecentTrip(from, to));

				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				query_trips.setDate(date);
				query_trips.setDeparture(departure);
				query_trips.setFrom(from);
				query_trips.setTo(to);

				query_trips.execute();
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}


}
