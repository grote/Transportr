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

package de.grobox.liberario.ui;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import de.grobox.liberario.R;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

public class StationPopupMenu extends BasePopupMenu {

	private Location station;
	private Location start;

	public StationPopupMenu(Context context, View anchor, Location station, Location start) {
		super(context, anchor);

		this.getMenuInflater().inflate(R.menu.location_actions, getMenu());
		this.station = station;
		this.start = start;

		if(!station.hasLocation()) {
			getMenu().removeItem(R.id.action_show_on_map);
			getMenu().removeItem(R.id.action_show_on_external_map);
		}

		showIcons();
	}

	@Override
	public OnMenuItemClickListener getOnMenuItemClickListener() {
		return new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				String text = TransportrUtils.getLocName(start) + " " + TransportrUtils.computeDistance(start, station) + "m → " + TransportrUtils.getLocName(station);

				// handle presses on menu items
				switch(item.getItemId()) {
					// Show On Map
					case R.id.action_show_on_map:
						TransportrUtils.showLocationOnMap(context, station, start);

						return true;
					// Show On External Map
					case R.id.action_show_on_external_map:
						TransportrUtils.startGeoIntent(context, station);

						return true;
					// From Here
					case R.id.action_from_here:
						TransportrUtils.presetDirections(context, station, null);

						return true;
					// To Here
					case R.id.action_to_here:
						TransportrUtils.presetDirections(context, null, station);

						return true;
					// Show Departures
					case R.id.action_show_departures:
						TransportrUtils.findDepartures(context, station);

						return true;
					// Show Nearby Stations
					case R.id.action_show_nearby_stations:
						TransportrUtils.findNearbyStations(context, station);

						return true;
					// Share Station
					case R.id.action_share:
						//noinspection deprecation
						Intent sendIntent = new Intent()
								                    .setAction(Intent.ACTION_SEND)
								                    .putExtra(Intent.EXTRA_TEXT, text)
								                    .setType("text/plain")
								                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.action_share)));

						return true;
					// Copy Station to Clipboard
					case R.id.action_copy:
						TransportrUtils.copyToClipboard(context, text);

						return true;
					default:
						return false;
				}
			}
		};
	}

}
