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

package de.grobox.transportr.ui;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip.Leg;

import static de.grobox.transportr.utils.DateUtils.getTime;
import static de.grobox.transportr.utils.TransportrUtils.copyToClipboard;
import static de.grobox.transportr.utils.TransportrUtils.findDepartures;
import static de.grobox.transportr.utils.TransportrUtils.findNearbyStations;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;
import static de.grobox.transportr.utils.TransportrUtils.legToString;
import static de.grobox.transportr.utils.TransportrUtils.presetDirections;
import static de.grobox.transportr.utils.TransportrUtils.showLocationsOnMap;

public class LegPopupMenu extends BasePopupMenu {
	private Location loc1 = null;
	private Location loc2 = null;
	private String text;

	public LegPopupMenu(Context context, View anchor, Leg leg, boolean is_last) {
		super(context, anchor);

		if (is_last) {
			this.loc1 = leg.arrival;
			this.loc2 = leg.departure;
		} else {
			this.loc1 = leg.departure;
			this.loc2 = leg.arrival;
		}
		this.text = legToString(context, leg);
		this.getMenuInflater().inflate(R.menu.leg_location_actions, getMenu());

		if (!loc1.hasId()) {
			getMenu().removeItem(R.id.action_show_departures);
		}

		showIcons();
	}

	public LegPopupMenu(Context context, View anchor, Leg leg) {
		this(context, anchor, leg, false);
	}

	public LegPopupMenu(Context context, View anchor, Stop stop) {
		super(context, anchor);

		this.loc1 = stop.location;
		this.text = getTime(context, stop.getArrivalTime()) + " " + getLocationName(stop.location);
		this.getMenuInflater().inflate(R.menu.location_actions, getMenu());

		showIcons();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			// Show On Map
			case R.id.action_show_on_map:
				// remember station to arrive at
				ArrayList<Location> loc_list = new ArrayList<>();
				if (loc2 != null) loc_list.add(loc2);
				loc_list.add(loc1);

				showLocationsOnMap(context, loc_list, loc1);

				return true;
			// Show On External Map
			case R.id.action_show_on_external_map:
				TransportrUtils.startGeoIntent(context, loc1);

				return true;
			// From Here
			case R.id.action_from_here:
				presetDirections(context, 0, new WrapLocation(loc1), null, null);

				return true;
			// To Here
			case R.id.action_to_here:
				presetDirections(context, 0, null, null, new WrapLocation(loc1));

				return true;
			// Show Departures
			case R.id.action_show_departures:
				findDepartures(context, loc1);

				return true;
			// Show Nearby Stations
			case R.id.action_show_nearby_stations:
				findNearbyStations(context, loc1);

				return true;
			// Share Leg
			case R.id.action_share:
				Intent sendIntent = new Intent()
						.setAction(Intent.ACTION_SEND)
						.putExtra(Intent.EXTRA_SUBJECT, getLocationName(loc1))
						.putExtra(Intent.EXTRA_TEXT, text)
						.setType("text/plain")
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.action_share)));

				return true;
			// Copy Leg to Clipboard
			case R.id.action_copy:
				copyToClipboard(context, getLocationName(loc1));

				return true;
			default:
				return false;
		}
	}

}
