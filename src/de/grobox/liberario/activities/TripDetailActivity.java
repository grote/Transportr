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

package de.grobox.liberario.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.adapters.TripAdapter;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.dto.Trip;

public class TripDetailActivity extends AppCompatActivity {

	private Trip trip;
	private TripAdapter.BaseTripHolder ui;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_details);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			toolbar.setSubtitle(Preferences.getTransportNetwork(this).getName());
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// If we're being restored from a previous state, then we don't need to do anything and
		// should return or else we could end up with overlapping fragments.
		if (savedInstanceState != null) {
			return;
		}

		trip = (Trip) getIntent().getSerializableExtra("de.schildbach.pte.dto.Trip");
		ui = new TripAdapter.BaseTripHolder(findViewById(R.id.cardView), trip.legs.size());

		int i = 0;
		for(final Trip.Leg leg : trip.legs) {
			TripAdapter.bindLeg(this, ui.legs.get(i), leg, true);
			i += 1;
		}

		setHeader();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_details, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			case R.id.action_share:
				LiberarioUtils.share(this, trip);

				return true;
			case R.id.action_calendar:
				LiberarioUtils.intoCalendar(this, trip);

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void setHeader() {
		((TextView) findViewById(R.id.departureView)).setText(trip.from.uniqueShortName());
		((TextView) findViewById(R.id.arrivalView)).setText(trip.to.uniqueShortName());
		((TextView) findViewById(R.id.durationView)).setText(DateUtils.getDuration(trip.getDuration()));
		((TextView) findViewById(R.id.dateView)).setText(DateUtils.getDate(this, trip.getFirstDepartureTime()));
	}

}
