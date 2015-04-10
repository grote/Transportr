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

import java.util.Date;

import de.schildbach.pte.dto.Trip;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class TripDetailActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trip_details);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		// If we're being restored from a previous state, then we don't need to do anything and
		// should return or else we could end up with overlapping fragments.
		if (savedInstanceState != null) {
			return;
		}

		// Create a new Fragment to be placed in the activity layout
		TripDetailFragment tripDetailFragment = new TripDetailFragment();
		tripDetailFragment.setEmbedded(false);

		// In case this activity was started with special instructions from an
		// Intent, pass the Intent's extras to the fragment as arguments
		tripDetailFragment.setArguments(getIntent().getExtras());

		// Add the fragment to the 'tripDetailsScrollView' FrameLayout
		getSupportFragmentManager().beginTransaction().add(R.id.tripDetailsScrollView, tripDetailFragment).commit();

		addHeader((Trip) getIntent().getSerializableExtra("de.schildbach.pte.dto.Trip"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// TODO remove deprecated code
	@SuppressWarnings("deprecation")
	private void addHeader(Trip trip) {
		Date d = trip.getFirstDepartureTime();

		((TextView) findViewById(R.id.tripDetailsDurationView)).setText(DateUtils.getDuration(trip.getDuration()));
		((TextView) findViewById(R.id.tripDetailsDateView)).setText(DateUtils.formatDate(this, d.getYear()+1900, d.getMonth(), d.getDate()));
	}

}
