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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.QueryTripsResult;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class AmbiguousLocationActivity extends Activity {
	private QueryTripsResult trips;
	private Location from;
	private Location to;
	private Date date;
	private Boolean departure;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ambiguous_location);

		Intent intent = getIntent();
		trips = (QueryTripsResult) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult");
		from = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult.from");
		to = (Location) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult.to");
		date = (Date) intent.getSerializableExtra("de.schildbach.pte.dto.QueryTripsResult.date");
		departure = (Boolean) intent.getBooleanExtra("de.schildbach.pte.dto.QueryTripsResult.departure", true);

		final Spinner from_spinner = ((Spinner) findViewById(R.id.fromSpinner));

		if(trips.ambiguousFrom != null) {
			from_spinner.setAdapter(new ArrayAdapter<Location>(this, R.layout.list_item, trips.ambiguousFrom));
		}
		else {
			List<Location> list = new ArrayList<Location>();
			list.add(from);
			from_spinner.setAdapter(new ArrayAdapter<Location>(this, R.layout.list_item, list));
			from_spinner.setEnabled(false);
		}

		final Spinner to_spinner = ((Spinner) findViewById(R.id.toSpinner));

		if(trips.ambiguousTo != null) {
			to_spinner.setAdapter(new ArrayAdapter<Location>(this, R.layout.list_item, trips.ambiguousTo));
		}
		else {
			List<Location> list = new ArrayList<Location>();
			list.add(to);
			to_spinner.setAdapter(new ArrayAdapter<Location>(this, R.layout.list_item, list));
			to_spinner.setEnabled(false);
		}

		Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				AsyncQueryTripsTask query_trips = new AsyncQueryTripsTask(v.getContext());

				query_trips.setDate(date);
				query_trips.setDeparture(departure);
				query_trips.setFrom((Location) from_spinner.getSelectedItem());
				query_trips.setTo((Location) to_spinner.getSelectedItem());

				query_trips.execute();
			}
		});
	}


}
