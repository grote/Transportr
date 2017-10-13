package de.grobox.liberario.trips.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout.OnRefreshListener;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.Date;

import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.trips.TripDetailActivity;
import de.grobox.liberario.favorites.trips.FavoriteTripManager;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.ui.LceAnimator;
import de.schildbach.pte.dto.QueryTripsContext;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.Trip;

import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout.LARGE;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTH;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTTOM;
import static de.grobox.liberario.trips.TripDetailActivity.TRIP;
import static de.grobox.liberario.utils.Constants.DATE;
import static de.grobox.liberario.utils.Constants.FAV_TRIP_UID;
import static de.grobox.liberario.utils.Constants.FROM;
import static de.grobox.liberario.utils.Constants.IS_DEPARTURE;
import static de.grobox.liberario.utils.Constants.LOADER_MORE_TRIPS;
import static de.grobox.liberario.utils.Constants.LOADER_TRIPS;
import static de.grobox.liberario.utils.Constants.TO;
import static de.grobox.liberario.utils.Constants.VIA;
import static de.grobox.liberario.utils.TransportrUtils.getDragDistance;
import static de.schildbach.pte.dto.QueryTripsResult.Status.OK;

public class TripsFragment extends TransportrFragment implements LoaderCallbacks<QueryTripsResult>, OnRefreshListener, TripAdapter.OnTripClickListener {

	final static String TAG = TripsFragment.class.getName();

	@Inject TransportNetworkManager manager;
	@Inject FavoriteTripManager favoriteTripManager;

	private ProgressBar progressBar;
	private SwipyRefreshLayout swipe;
	private RecyclerView list;
	private TripAdapter adapter;

	private long favTripUid;
	private WrapLocation from, to;
	private @Nullable WrapLocation via;
	private Date date;
	private boolean departure;
	private @Nullable QueryTripsContext queryTripsContext;
	private boolean queryMoreLater;

	public static TripsFragment newInstance(long favTripUid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to, Date date, boolean departure) {
		TripsFragment f = new TripsFragment();
		Bundle args = new Bundle();
		args.putLong(FAV_TRIP_UID, favTripUid);
		args.putSerializable(FROM, from);
		args.putSerializable(VIA, via);
		args.putSerializable(TO, to);
		args.putSerializable(DATE, date);
		args.putBoolean(IS_DEPARTURE, departure);
		f.setArguments(args);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_trips, container, false);
		getComponent().inject(this);

		// initialize arguments
		Bundle bundle = getArguments();
		favTripUid = bundle.getLong(FAV_TRIP_UID, 0);
		from = (WrapLocation) bundle.getSerializable(FROM);
		via = (WrapLocation) bundle.getSerializable(VIA);
		to = (WrapLocation) bundle.getSerializable(TO);
		date = (Date) bundle.getSerializable(DATE);
		departure = bundle.getBoolean(IS_DEPARTURE);
		if (from == null || to == null || date == null) throw new IllegalArgumentException();

		// Progress Bar
		progressBar = v.findViewById(R.id.progressBar);

		// Swipe to Refresh
		swipe = v.findViewById(R.id.swipe);
		swipe.setColorSchemeResources(R.color.accent);
		swipe.setProgressBackgroundColor(R.color.cardview_dark_background);
		swipe.setSize(LARGE);
		swipe.setDistanceToTriggerSync(getDragDistance(getContext()));
		swipe.setOnRefreshListener(this);

		list = v.findViewById(R.id.list);
		LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
		list.setLayoutManager(layoutManager);
		adapter = new TripAdapter(this);
		adapter.setHasStableIds(false);
		list.setHasFixedSize(false);
		list.setAdapter(adapter);

		LceAnimator.showLoading(progressBar, list, null);

		Loader loader = getLoaderManager().initLoader(LOADER_TRIPS, null, this);
		if (savedInstanceState == null) {
			loader.forceLoad();
			favoriteTripManager.addTrip(favTripUid, from, via, to);
		}

		return v;
	}

	public void setSwipeEnabled(boolean enabled) {
		if (swipe != null) swipe.setDirection(enabled ? BOTH : BOTTOM);
	}

	@Override
	public void onRefresh(SwipyRefreshLayoutDirection direction) {
		queryMoreLater = direction == BOTTOM;
		getLoaderManager().restartLoader(LOADER_MORE_TRIPS, null, this).forceLoad();
	}

	@Override
	public Loader<QueryTripsResult> onCreateLoader(int id, Bundle args) {
		if (id == LOADER_MORE_TRIPS)
			return new MoreTripsLoader(getContext(), manager, queryTripsContext, queryMoreLater);
		return new TripsLoader(getContext(), manager, from.getLocation(), via != null ? via.getLocation() : null, to.getLocation(), date, departure);
	}

	@Override
	public void onLoadFinished(Loader<QueryTripsResult> loader, @Nullable QueryTripsResult result) {
		if (result != null && result.status == OK) {
			queryTripsContext = result.context;
			adapter.addAll(result.trips);
		} else {
			// TODO show error message
			LceAnimator.showContent(progressBar, list, null);
		}

		if (loader.getId() == LOADER_MORE_TRIPS) {
			swipe.setRefreshing(false);
			list.smoothScrollBy(0, queryMoreLater ? 200 : -200);
		} else {
			LceAnimator.showContent(progressBar, list, null);
		}
	}

	@Override
	public void onLoaderReset(Loader<QueryTripsResult> loader) {
		adapter.clear();
		queryTripsContext = null;
	}

	@Override
	public void onClick(Trip trip) {
		Log.e("TEST", trip.toString());

		Intent i = new Intent(getContext(), TripDetailActivity.class);
		i.putExtra(TRIP, trip);
//		i.putExtra("de.schildbach.pte.dto.Trip.from", from);
//		i.putExtra("de.schildbach.pte.dto.Trip.to", to);
//		i.putExtra("de.schildbach.pte.dto.Trip.products", trip.products().toArray());
		startActivity(i);
	}

}
