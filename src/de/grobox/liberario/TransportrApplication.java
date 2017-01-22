/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

public class TransportrApplication extends Application {
	private TransportNetworks networks;

	@Override
	public void onCreate() {
		super.onCreate();

		initializeNetworks(getBaseContext());
	}

	public void initializeNetworks(Context context) {
		if(networks == null) networks = new TransportNetworks(context);
	}

	public TransportNetworks getTransportNetworks(Context context) {
		// sometimes we need to reinitialize for some reason
		if(networks == null) initializeNetworks(context);

		return networks;
	}
}
