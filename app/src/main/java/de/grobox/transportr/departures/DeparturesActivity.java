package de.grobox.transportr.departures;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrActivity;
import de.grobox.transportr.ui.TimeDateFragment;
import de.grobox.transportr.ui.TimeDateFragment.TimeDateListener;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.ui.LceAnimator;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.TOP;
import static de.grobox.transportr.departures.DeparturesLoader.getBundle;
import static de.grobox.transportr.utils.Constants.DATE;
import static de.grobox.transportr.utils.Constants.LOADER_DEPARTURES;
import static de.grobox.transportr.utils.Constants.WRAP_LOCATION;
import static de.grobox.transportr.utils.TransportrUtils.getDragDistance;
import static de.schildbach.pte.dto.QueryDeparturesResult.Status.OK;

@ParametersAreNonnullByDefault
public class DeparturesActivity extends TransportrActivity
		implements LoaderCallbacks<QueryDeparturesResult>, TimeDateListener {

	public final static int MAX_DEPARTURES = 12;
	private final static int SAFETY_MARGIN = 6;

	private enum SearchState {INITIAL, TOP, BOTTOM}

	private ProgressBar progressBar;
	private SwipyRefreshLayout swipe;
	private RecyclerView list;
	private DepartureAdapter adapter;

	@Inject TransportNetworkManager manager;

	private WrapLocation location;
	private SearchState searchState = SearchState.INITIAL;
	private Calendar calendar;
	private final CountDownTimer listUpdateTimer = new CountDownTimer(Long.MAX_VALUE, 1000 * 30) {
		@Override
		public void onTick(long millisUntilFinished) {
			adapter.notifyDataSetChanged();
		}

		@Override
		public void onFinish() {
		}
	};

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		location = (WrapLocation) intent.getSerializableExtra(WRAP_LOCATION);
		if (location == null || location.getLocation() == null)
			throw new IllegalArgumentException("No Location");

		setContentView(R.layout.activity_departures);

		getComponent().inject(this);

		// Toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setSubtitle(location.getName());
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Progress Bar
		progressBar = findViewById(R.id.progressBar);

		// Swipe to Refresh
		swipe = findViewById(R.id.swipe);
		swipe.setColorSchemeResources(R.color.accent);
		swipe.setDirection(SwipyRefreshLayoutDirection.BOTH);
		swipe.setDistanceToTriggerSync(getDragDistance(this));
		swipe.setOnRefreshListener(direction -> loadMoreDepartures(direction != TOP));

		// Departures List
		adapter = new DepartureAdapter();
		list = findViewById(R.id.list);
		list.setVisibility(INVISIBLE);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(this));

		LceAnimator.showLoading(progressBar, list, null);

		// Loader
		Bundle args = getBundle(location.getId(), new Date(), MAX_DEPARTURES);
		Loader<QueryDeparturesResult> loader = getSupportLoaderManager().initLoader(LOADER_DEPARTURES, args, this);

		if (savedInstanceState != null) {
			calendar = (Calendar) savedInstanceState.getSerializable(DATE);
			// re-attach fragment listener
			List<Fragment> fragments = getSupportFragmentManager().getFragments();
			if (fragments != null && fragments.size() > 0 && fragments.get(0) instanceof TimeDateFragment) {
				((TimeDateFragment) fragments.get(0)).setTimeDateListener(this);
			}
		} else {
			loader.forceLoad();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		listUpdateTimer.start();
	}

	@Override
	public void onStop() {
		super.onStop();
		listUpdateTimer.cancel();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DATE, calendar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.departures, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.action_time:
				if (calendar == null) calendar = Calendar.getInstance();
				TimeDateFragment fragment = TimeDateFragment.newInstance(calendar);
				fragment.setTimeDateListener(this);
				fragment.show(getSupportFragmentManager(), TimeDateFragment.TAG);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onTimeAndDateSet(Calendar calendar) {
		this.calendar = calendar;
		adapter.clear();
		searchState = SearchState.INITIAL;
		progressBar.setVisibility(VISIBLE);
		list.setVisibility(INVISIBLE);

		Bundle args = getBundle(location.getId(), calendar.getTime(), MAX_DEPARTURES);
		getSupportLoaderManager().restartLoader(LOADER_DEPARTURES, args, this).forceLoad();
	}

	private void loadMoreDepartures(boolean later) {
		Date date = new Date();
		int maxDepartures = MAX_DEPARTURES;
		int count = adapter.getItemCount();

		// search from end + safety margin
		if (later) {
			int itemPos;
			if (count - SAFETY_MARGIN > 0) {
				itemPos = count - SAFETY_MARGIN;
				maxDepartures = MAX_DEPARTURES + SAFETY_MARGIN;
			} else {
				itemPos = count - 1;
			}
			date = adapter.getItem(itemPos).getTime();
		}
		// search from beginning + safety margin
		else {
			Date earliest = adapter.getEarliestDate();
			Date latest;

			if (count >= MAX_DEPARTURES) {
				latest = adapter.getItem(MAX_DEPARTURES - 1).getTime();
			} else {
				latest = adapter.getItem(count - 1).getTime();
			}
			long span = latest.getTime() - earliest.getTime();
			date.setTime(earliest.getTime() - span);

			maxDepartures = MAX_DEPARTURES + SAFETY_MARGIN;
		}

		searchState = later ? SearchState.BOTTOM : SearchState.TOP;
		Bundle args = getBundle(location.getId(), date, maxDepartures);
		getSupportLoaderManager().restartLoader(LOADER_DEPARTURES, args, this).forceLoad();
	}

	@Override
	public DeparturesLoader onCreateLoader(int i, Bundle args) {
		return new DeparturesLoader(this, manager.getTransportNetwork().getValue(), args);
	}

	@Override
	public void onLoadFinished(Loader<QueryDeparturesResult> loader, QueryDeparturesResult departures) {
		if (departures.status == OK) {
			for (StationDepartures s : departures.stationDepartures) {
				adapter.addAll(s.departures);
			}
		} else {
			// TODO
			Log.e("TEST", "LOAD FAILED!!!");
		}
		swipe.setRefreshing(false);

		if (searchState == SearchState.INITIAL) {
			LceAnimator.showContent(progressBar, list, null);
		} else {
			// scroll smoothly up or down when we have new trips
			list.smoothScrollBy(0, searchState == SearchState.BOTTOM ? 150 : -150);
		}
	}

	@Override
	public void onLoaderReset(Loader<QueryDeparturesResult> loader) {
		adapter.clear();
	}

}
