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

import androidx.lifecycle.ViewModelProvider;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.core.content.ContextCompat;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrFragment;
import de.grobox.transportr.departures.DeparturesActivity;
import de.grobox.transportr.departures.DeparturesLoader;
import de.grobox.transportr.locations.ReverseGeocoder.ReverseGeocoderCallback;
import de.grobox.transportr.map.MapViewModel;
import de.grobox.transportr.utils.IntentUtils;
import de.grobox.transportr.utils.TransportrUtils;
import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.LineDestination;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.recyclerview.widget.RecyclerView.HORIZONTAL;
import static com.google.common.base.Strings.isNullOrEmpty;
import static de.grobox.transportr.departures.DeparturesActivity.MAX_DEPARTURES;
import static de.grobox.transportr.departures.DeparturesLoader.getBundle;
import static de.grobox.transportr.utils.Constants.LOADER_DEPARTURES;
import static de.grobox.transportr.utils.Constants.WRAP_LOCATION;
import static de.grobox.transportr.utils.IntentUtils.startGeoIntent;
import static de.schildbach.pte.dto.LocationType.COORD;
import static de.schildbach.pte.dto.QueryDeparturesResult.Status.OK;

@ParametersAreNonnullByDefault
public class LocationFragment extends TransportrFragment
		implements LoaderCallbacks<QueryDeparturesResult>, ReverseGeocoderCallback, OnGlobalLayoutListener {

	public static final String TAG = LocationFragment.class.getName();

	@Inject	ViewModelProvider.Factory viewModelFactory;

	private MapViewModel viewModel;
	private WrapLocation location;
	private final LineAdapter adapter = new LineAdapter();

	private ImageView locationIcon;
	private TextView locationName;
	private TextView locationInfo;
	private RecyclerView linesLayout;
	private Button nearbyStationsButton;
	private ProgressBar nearbyStationsProgress;

	public static LocationFragment newInstance(WrapLocation location) {
		LocationFragment f = new LocationFragment();

		Bundle args = new Bundle();
		args.putSerializable(WRAP_LOCATION, location);
		f.setArguments(args);

		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args == null) throw new IllegalStateException();
		location = (WrapLocation) args.getSerializable(WRAP_LOCATION);
		if (location == null) throw new IllegalArgumentException("No location");

		View v = inflater.inflate(R.layout.fragment_location, container, false);
		getComponent().inject(this);

		if (getActivity() == null) throw new IllegalStateException();
		viewModel = new ViewModelProvider(requireActivity(), viewModelFactory).get(MapViewModel.class);
		viewModel.nearbyStationsFound().observe(getViewLifecycleOwner(), found -> onNearbyStationsLoaded());

		// Location
		locationIcon = v.findViewById(R.id.locationIcon);
		locationName = v.findViewById(R.id.locationName);
		locationIcon.setOnClickListener(view -> onLocationClicked());
		locationName.setOnClickListener(view -> onLocationClicked());

		// Lines
		linesLayout = v.findViewById(R.id.linesLayout);
		linesLayout.setVisibility(GONE);
		linesLayout.setAdapter(adapter);
		linesLayout.setLayoutManager(new LinearLayoutManager(getContext(), HORIZONTAL, false));
		linesLayout.setOnClickListener(view -> onLocationClicked());

		// Location Info
		locationInfo = v.findViewById(R.id.locationInfo);
		showLocation();

		if (location.getLocation().type == COORD) {
			ReverseGeocoder geocoder = new ReverseGeocoder(getActivity(), this);
			geocoder.findLocation(location.getLocation());
		}

		// Departures
		Button departuresButton = v.findViewById(R.id.departuresButton);
		if (location.hasId()) {
			departuresButton.setOnClickListener(view -> {
				Intent intent = new Intent(getContext(), DeparturesActivity.class);
				intent.putExtra(WRAP_LOCATION, location);
				startActivity(intent);
			});
		} else {
			departuresButton.setVisibility(GONE);
		}

		// Nearby Stations
		nearbyStationsButton = v.findViewById(R.id.nearbyStationsButton);
		nearbyStationsProgress = v.findViewById(R.id.nearbyStationsProgress);
		nearbyStationsButton.setOnClickListener(view -> {
			nearbyStationsButton.setVisibility(INVISIBLE);
			nearbyStationsProgress.setVisibility(VISIBLE);
			IntentUtils.findNearbyStations(getContext(), location);
		});

		// Share Location
		Button shareButton = v.findViewById(R.id.shareButton);
		shareButton.setOnClickListener(view -> startGeoIntent(getActivity(), location));

		// Overflow Button
		ImageButton overflowButton = v.findViewById(R.id.overflowButton);
		overflowButton.setOnClickListener(view -> new LocationPopupMenu(getContext(), view, location).show());

		v.getViewTreeObserver().addOnGlobalLayoutListener(this);

		return v;
	}

	@Override
	public void onGlobalLayout() {
		// set peek distance to show view header
		if (getActivity() == null) return;
		if (linesLayout.getBottom() > 0) {
			viewModel.setPeekHeight(linesLayout.getBottom() + getResources().getDimensionPixelSize(R.dimen.locationPeekPadding));
		} else if (locationInfo.getBottom() > 0) {
			viewModel.setPeekHeight(locationInfo.getBottom() + getResources().getDimensionPixelSize(R.dimen.locationPeekPadding));
		}
	}

	private void showLocation() {
		locationName.setText(location.getName());
		locationIcon.setImageDrawable(ContextCompat.getDrawable(getContext(), location.getDrawable()));
		StringBuilder locationInfoStr = new StringBuilder();
		if (!isNullOrEmpty(location.getLocation().place)) {
			locationInfoStr.append(location.getLocation().place);
		}
		if (location.getLocation().hasCoord()) {
			if (locationInfoStr.length() > 0) locationInfoStr.append(", ");
			locationInfoStr.append(TransportrUtils.getCoordName(location.getLocation()));
		}
		locationInfo.setText(locationInfoStr);
	}

	private void onLocationClicked() {
		viewModel.selectedLocationClicked(location.getLatLng());
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (location.hasId()) {
			Bundle args = getBundle(location.getId(), new Date(), MAX_DEPARTURES);
			getLoaderManager().initLoader(LOADER_DEPARTURES, args, this).forceLoad();
		}
	}

	@NonNull
	@Override
	public DeparturesLoader onCreateLoader(int id, @Nullable Bundle args) {
		return new DeparturesLoader(getContext(), viewModel.getTransportNetwork().getValue(), args);
	}

	@Override
	public void onLoadFinished(Loader<QueryDeparturesResult> loader, QueryDeparturesResult data) {
		if (data != null && data.status == OK) {
			SortedSet<Line> lines = new TreeSet<>();
			for (StationDepartures s : data.stationDepartures) {
				if (s.lines != null) {
					for (LineDestination d : s.lines) lines.add(d.line);
				}
				for (Departure d : s.departures) lines.add(d.line);
			}
			adapter.swapLines(new ArrayList<>(lines));

			linesLayout.setAlpha(0f);
			linesLayout.setVisibility(VISIBLE);
			linesLayout.animate().setDuration(750).alpha(1f).start();
		}
	}

	@Override
	public void onLoaderReset(Loader<QueryDeparturesResult> loader) {
	}

	@Override
	@WorkerThread
	public void onLocationRetrieved(@NonNull final WrapLocation location) {
		if (getActivity() == null) return;
		runOnUiThread(() -> {
			LocationFragment.this.location = location;
			showLocation();
		});
	}

	private void onNearbyStationsLoaded() {
		nearbyStationsButton.setVisibility(VISIBLE);
		nearbyStationsButton.setEnabled(false);
		nearbyStationsProgress.setVisibility(INVISIBLE);
	}

	public WrapLocation getLocation() {
		return location;
	}

}
