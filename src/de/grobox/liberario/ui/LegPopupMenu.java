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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import de.grobox.liberario.DateUtils;
import de.grobox.liberario.LiberarioUtils;
import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;

public class LegPopupMenu extends PopupMenu {
	private Context context;
	private Location loc1 = null;
	private Location loc2 = null;
	private String text;

	public LegPopupMenu(Context context, View anchor, Trip.Leg leg) {
		super(context, anchor);

		this.context = context;
		this.loc1 = leg.departure;
		this.loc2 = leg.arrival;
		this.text = LiberarioUtils.legToString(context, leg);
		this.getMenuInflater().inflate(R.menu.leg_actions, getMenu());
		showPopupIcons();
		setOnMenuItemClickListener(getOnMenuItemClickListener());
	}

	public LegPopupMenu(Context context, View anchor, Trip.Leg leg, boolean is_last) {
		super(context, anchor);

		this.context = context;
		this.loc1 = leg.arrival;
		this.text = DateUtils.getTime(context, leg.getArrivalTime()) + " " + leg.arrival.uniqueShortName();
		this.getMenuInflater().inflate(R.menu.leg_actions, getMenu());
		showPopupIcons();
		setOnMenuItemClickListener(getOnMenuItemClickListener());
	}

	public LegPopupMenu(Context context, View anchor, Stop stop) {
		super(context, anchor);

		this.context = context;
		this.loc1 = stop.location;
		this.text = DateUtils.getTime(context, stop.getArrivalTime()) + " " + stop.location.uniqueShortName();
		this.getMenuInflater().inflate(R.menu.leg_actions, getMenu());
		showPopupIcons();
		setOnMenuItemClickListener(getOnMenuItemClickListener());
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
					case R.id.action_leg_share:
						Intent sendIntent = new Intent()
								                    .setAction(Intent.ACTION_SEND)
								                    .putExtra(Intent.EXTRA_SUBJECT, loc1.uniqueShortName())
								                    .putExtra(Intent.EXTRA_TEXT, text)
								                    .setType("text/plain")
								                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
						context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.action_trip_share)));

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

	private void showPopupIcons() {
		// very ugly hack to show icons in PopupMenu
		// see: http://stackoverflow.com/a/18431605
		try {
			Field[] fields = getClass().getSuperclass().getDeclaredFields();
			for(Field field : fields) {
				if("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(this);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
					setForceIcons.invoke(menuPopupHelper, true);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
