package de.grobox.transportr.locations;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.WorkerThread;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.annotation.ParametersAreNonnullByDefault;

import de.schildbach.pte.dto.Location;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static de.schildbach.pte.dto.LocationType.ADDRESS;

@ParametersAreNonnullByDefault
public class ReverseGeocoder {

	private final Context context;
	private final ReverseGeocoderCallback callback;

	public ReverseGeocoder(Context context, ReverseGeocoderCallback callback) {
		this.context = context;
		this.callback = callback;
	}

	void findLocation(final Location location) {
		if (!location.hasLocation()) return;
		findLocation(location.getLatAsDouble(), location.getLonAsDouble());
	}

	public void findLocation(final android.location.Location location) {
		findLocation(location.getLatitude(), location.getLongitude());
	}

	private void findLocation(final double lat, final double lon) {
		if (Geocoder.isPresent()) {
			try {
				findLocationWithGeocoder(lat, lon);
			} catch (IOException e) {
				if (!e.getMessage().equals("Service not Available")) {
					e.printStackTrace();
				}
				findLocationWithOsm(lat, lon);
			}
		} else {
			findLocationWithOsm(lat, lon);
		}
	}

	private void findLocationWithGeocoder(final double lat, final double lon) throws IOException {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
		if (addresses == null || addresses.size() == 0) return;

		Address address = addresses.get(0);
		String name = address.getThoroughfare();
		if (name != null && address.getFeatureName() != null) name += " " + address.getFeatureName();
		String place = address.getLocality();

		int latInt = (int) (lat * 1E6);
		int lonInt = (int) (lon * 1E6);
		Location l = new Location(ADDRESS, null, latInt, lonInt, place, name);

		callback.onLocationRetrieved(new WrapLocation(l));
	}

	@SuppressWarnings("StringBufferReplaceableByString")
	private void findLocationWithOsm(final double lat, final double lon) {
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
				callback.onLocationRetrieved(getWrapLocation(lat, lon));
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				ResponseBody body = response.body();
				if (!response.isSuccessful() || body == null) {
					callback.onLocationRetrieved(getWrapLocation(lat, lon));
					return;
				}

				String result = body.string();
				body.close();

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

					int latInt = (int) (lat * 1E6);
					int lonInt = (int) (lon * 1E6);
					Location l = new Location(ADDRESS, null, latInt, lonInt, place, name);
					callback.onLocationRetrieved(new WrapLocation(l));
				} catch (JSONException e) {
					callback.onLocationRetrieved(getWrapLocation(lat, lon));
					throw new IOException(e);
				}
			}
		});
	}

	private WrapLocation getWrapLocation(double lat, double lon) {
		return new WrapLocation(new LatLng(lat, lon));
	}

	public interface ReverseGeocoderCallback {
		@WorkerThread
		void onLocationRetrieved(WrapLocation location);
	}

}
