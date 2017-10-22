package de.grobox.transportr.locations;

import android.support.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.annotation.ParametersAreNonnullByDefault;

import de.schildbach.pte.dto.Location;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static de.schildbach.pte.dto.LocationType.ADDRESS;

@ParametersAreNonnullByDefault
public class OsmReverseGeocoder {

	private final OsmReverseGeocoderCallback callback;

	public OsmReverseGeocoder(OsmReverseGeocoderCallback callback) {
		this.callback = callback;
	}

	void findLocation(final Location location) {
		if (!location.hasLocation()) return;
		findLocation(location.getLatAsDouble(), location.getLonAsDouble());
	}

	public void findLocation(final android.location.Location location) {
		findLocation(location.getLatitude(), location.getLongitude());
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	private void findLocation(final double lat, final double lon) {
		OkHttpClient client = new OkHttpClient();

		// https://nominatim.openstreetmap.org/reverse?lat=52.5217&lon=13.4324&format=json
		StringBuilder url = new StringBuilder("https://nominatim.openstreetmap.org/reverse?");
		url.append("lat=").append(lat).append("&");
		url.append("lon=").append(lon).append("&");
		url.append("format=json");

		Request request = new Request.Builder()
				.header("User-Agent", "Transportr (https://transportr.grobox.de)")
				.url(url.toString())
				.build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String result = response.body().string();
				response.body().close();

				if (!response.isSuccessful()) return;

				try {
					JSONObject data = new JSONObject(result);
					JSONObject address = data.getJSONObject("address");
					String name = address.optString("road", null);
					if (name != null) {
						String number = address.optString("house_number", null);
						if (number != null) name += " " + number;
					} else {
						name = data.getString("display_name").split(",")[0];
					}
					String place = address.optString("city", null);
					if (place == null) place = address.optString("state", null);

					final Location l = new Location(ADDRESS, null, (int) (lat * 1E6), (int) (lon * 1E6), place, name);
					callback.onLocationRetrieved(new WrapLocation(l));
				} catch (JSONException e) {
					throw new IOException(e);
				}
			}
		});
	}

	public interface OsmReverseGeocoderCallback {
		@WorkerThread
		void onLocationRetrieved(WrapLocation location);
	}

}
