/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.locations;

import android.os.AsyncTask;
import androidx.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.networks.TransportNetwork;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.SuggestLocationsResult;

@ParametersAreNonnullByDefault
class SuggestLocationsTask extends AsyncTask<String, Void, SuggestLocationsResult> {

	private final TransportNetwork network;
	private final SuggestLocationsTaskCallback callback;

	SuggestLocationsTask(TransportNetwork network, SuggestLocationsTaskCallback callback) {
		this.network = network;
		this.callback = callback;
	}

	@Nullable
	@Override
	protected SuggestLocationsResult doInBackground(String... strings) {
		String search = strings[0];
		if(search.length() < LocationAdapter.TYPING_THRESHOLD) return null;

		NetworkProvider np = network.getNetworkProvider();
		try {
			return np.suggestLocations(search);
		} catch(Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(SuggestLocationsResult suggestLocationsResult) {
		callback.onSuggestLocationsResult(suggestLocationsResult);
	}

	interface SuggestLocationsTaskCallback {
		void onSuggestLocationsResult(@Nullable SuggestLocationsResult suggestLocationsResult);
	}

}
