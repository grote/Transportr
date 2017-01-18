/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
 *    Quickhome-Widget
 *    Copyright (C) 2017 Patrick Kanzler
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

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.fragments.AboutMainFragment;
import de.grobox.liberario.fragments.DirectionsFragment;
import de.grobox.liberario.ui.LocationView;
import de.schildbach.pte.dto.Location;

public class QuickhomeWidget extends AppWidgetProvider {
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		final int N = appWidgetIds.length;

		// Perform this loop procedure for each App Widget that belongs to this provider
		for (int i=0; i<N; i++) {
			int appWidgetId = appWidgetIds[i];

			// Create an Intent to launch the DirectionsFragment via MainActivity
			Intent intent = new Intent(DirectionsFragment.TAG, Uri.EMPTY, context, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("from", new WrapLocation(WrapLocation.WrapType.GPS));
			intent.putExtra("to", new WrapLocation(WrapLocation.WrapType.HOME));
			intent.putExtra("search", true);
			// put that Intent into a pending Intent
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

			// Get the layout for the App Widget and attach an on-click listener
			// to the whole widget
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_quickhome);
			views.setOnClickPendingIntent(R.id.widget_quickhome_frame, pendingIntent);

			// Tell the AppWidgetManager to perform an update on the current app widget
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

}