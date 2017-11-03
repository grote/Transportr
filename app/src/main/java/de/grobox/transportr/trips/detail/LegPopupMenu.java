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

package de.grobox.transportr.trips.detail;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import de.grobox.transportr.R;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.ui.BasePopupMenu;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip.Leg;

import static de.grobox.transportr.trips.detail.TripUtils.legToString;
import static de.grobox.transportr.utils.DateUtils.getTime;
import static de.grobox.transportr.utils.IntentUtils.findDepartures;
import static de.grobox.transportr.utils.IntentUtils.findNearbyStations;
import static de.grobox.transportr.utils.IntentUtils.presetDirections;
import static de.grobox.transportr.utils.IntentUtils.startGeoIntent;
import static de.grobox.transportr.utils.TransportrUtils.copyToClipboard;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;

public class LegPopupMenu extends BasePopupMenu {
	private final WrapLocation loc1;
	private final String text;

	LegPopupMenu(Context context, View anchor, Leg leg, boolean isLast) {
		super(context, anchor);

		if (isLast) {
			this.loc1 = new WrapLocation(leg.arrival);
		} else {
			this.loc1 = new WrapLocation(leg.departure);
		}
		this.text = legToString(context, leg);
		this.getMenuInflater().inflate(R.menu.leg_location_actions, getMenu());

		if (!loc1.hasId()) {
			getMenu().removeItem(R.id.action_show_departures);
		}
		showIcons();
	}

	LegPopupMenu(Context context, View anchor, Leg leg) {
		this(context, anchor, leg, false);
	}

	LegPopupMenu(Context context, View anchor, Stop stop) {
		super(context, anchor);

		this.loc1 = new WrapLocation(stop.location);
		this.text = getTime(context, stop.getArrivalTime()) + " " + getLocationName(stop.location);
		this.getMenuInflater().inflate(R.menu.location_actions, getMenu());

		showIcons();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			// Show On External Map
			case R.id.action_show_on_external_map:
				startGeoIntent(context, loc1);

				return true;
			// From Here
			case R.id.action_from_here:
				presetDirections(context, 0, loc1, null, null);

				return true;
			// To Here
			case R.id.action_to_here:
				presetDirections(context, 0, null, null, loc1);

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
						.putExtra(Intent.EXTRA_SUBJECT, loc1.getName())
						.putExtra(Intent.EXTRA_TEXT, text)
						.setType("text/plain")
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.action_share)));

				return true;
			// Copy Leg to Clipboard
			case R.id.action_copy:
				copyToClipboard(context, loc1.getName());

				return true;
			default:
				return false;
		}
	}

}
