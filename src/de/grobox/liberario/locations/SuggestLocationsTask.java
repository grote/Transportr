package de.grobox.liberario.locations;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.networks.TransportNetwork;
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
			e.printStackTrace();
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
