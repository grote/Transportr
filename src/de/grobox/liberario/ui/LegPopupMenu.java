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

package de.grobox.liberario.ui;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;

public class LegPopupMenu extends BasePopupMenu {
	private Location loc1 = null;
	private Location loc2 = null;
	private String text;

	public LegPopupMenu(Context context, View anchor, Trip.Leg leg, boolean is_last) {
		super(context, anchor);

		if(is_last) {
			this.loc1 = leg.arrival;
			this.loc2 = leg.departure;
		} else {
			this.loc1 = leg.departure;
			this.loc2 = leg.arrival;
		}
		this.text = LiberarioUtils.legToString(context, leg);
		this.getMenuInflater().inflate(R.menu.location_actions, getMenu());

		showIcons();
	}

	public LegPopupMenu(Context context, View anchor, Trip.Leg leg) {
		this(context, anchor, leg, false);
	}

	public LegPopupMenu(Context context, View anchor, Stop stop) {
		super(context, anchor);

		this.loc1 = stop.location;
		this.text = DateUtils.getTime(context, stop.getArrivalTime()) + " " + stop.location.uniqueShortName();
		this.getMenuInflater().inflate(R.menu.location_actions, getMenu());

		showIcons();
	}

	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				// handle presses on menu items
				switch(item.getItemId()) {
					// Show On Map
					case R.id.action_show_on_map:
						// remember station to arrive at
						ArrayList<Location> loc_list = new ArrayList<>();
						if(loc2 != null) loc_list.add(loc2);
						loc_list.add(loc1);

						LiberarioUtils.showLocationsOnMap(context, loc_list, loc1);

						return true;
					// Show On External Map
					case R.id.action_show_on_external_map:
						LiberarioUtils.startGeoIntent(context, loc1);

						return true;
					// Show Departures
					case R.id.action_show_departures:
						LiberarioUtils.findDepartures(context, loc1);

						return true;
					// Show Nearby Stations
					case R.id.action_show_nearby_stations:
						LiberarioUtils.findNearbyStations(context, loc1);

						return true;
					// Share Leg
					case R.id.action_share:
						Intent sendIntent = new Intent()
								                    .setAction(Intent.ACTION_SEND)
								                    .putExtra(Intent.EXTRA_SUBJECT, loc1.uniqueShortName())
								                    .putExtra(Intent.EXTRA_TEXT, text)
								                    .setType("text/plain")
								                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.action_share)));

						return true;
					// Copy Leg to Clipboard
					case R.id.action_copy:
						LiberarioUtils.copyToClipboard(context, loc1.uniqueShortName());

						return true;
					default:
						return false;
				}
			}
		};
	}

}
