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

import java.io.IOException;
import java.util.List;

import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NetworkProviderFactory;
import de.schildbach.pte.dto.Location;
import android.content.Context;
import android.os.AsyncTask;

public class AsyncLocationAutoCompleteTask extends AsyncTask<Void, Void, List<Location>> {
	private Context context;
	private String query = "";

	public AsyncLocationAutoCompleteTask(Context context, String query) {
		this.context = context;
		this.query = query;
	}

	@Override
	protected List<Location> doInBackground(Void... params) {
		NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(context));

		List<Location> loc_list = null;

		try {
			if(AsyncQueryTripsTask.isNetworkAvailable(context)) {
				loc_list = np.autocompleteStations(query);
			}
			else {
				return null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return loc_list;
	}


}
