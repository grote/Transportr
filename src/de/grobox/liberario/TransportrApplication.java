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

package de.grobox.liberario;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.fragments.DirectionsFragment;

public class TransportrApplication extends Application {
	private TransportNetworks networks;

	@Override
	public void onCreate() {
		super.onCreate();

		initializeNetworks(getBaseContext());
		// ShortcutIcon(); //TODO
	}

	public void initializeNetworks(Context context) {
		if(networks == null) networks = new TransportNetworks(context);
	}

	public TransportNetworks getTransportNetworks(Context context) {
		// sometimes we need to reinitialize for some reason
		if(networks == null) initializeNetworks(context);

		return networks;
	}

	private void ShortcutIcon(){

		Intent shortcutIntent = new Intent(DirectionsFragment.TAG, Uri.EMPTY, getApplicationContext(), MainActivity.class);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		shortcutIntent.putExtra("from", new WrapLocation(WrapLocation.WrapType.GPS)); //TODO this crashes the launcher at the moment
		shortcutIntent.putExtra("to", new WrapLocation(WrapLocation.WrapType.HOME)); //TODO this also
		shortcutIntent.putExtra("search", true);

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.widget_name_quickhome));
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(getApplicationContext(), R.drawable.ic_quickhome_widget));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		getApplicationContext().sendBroadcast(addIntent);
	}
}
