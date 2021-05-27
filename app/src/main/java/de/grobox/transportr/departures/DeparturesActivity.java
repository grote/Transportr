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

package de.grobox.transportr.departures;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.omadahealth.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrActivity;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.ui.LceAnimator;
import de.grobox.transportr.ui.TimeDateFragment;
import de.grobox.transportr.ui.TimeDateFragment.TimeDateListener;
import de.grobox.transportr.utils.TransportrUtils;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.StationDepartures;

import static android.view.View.INVISIBLE;
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
	private View errorLayout;
	private TextView errorText;
	private SwipyRefreshLayout swipe;
	private RecyclerView list;
	private DepartureAdapter adapter;

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

		// Toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		toolbar.setSubtitle(location.getName());
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setHomeButtonEnabled(true);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		// Swipe to Refresh
		swipe = findViewById(R.id.swipe);
		swipe.setColorSchemeResources(R.color.accent);
		swipe.setDirection(SwipyRefreshLayoutDirection.BOTH);
		swipe.setDistanceToTriggerSync(getDragDistance(this));
		swipe.setOnRefreshListener(direction -> loadMoreDepartures(direction != TOP));
		swipe.setEnabled(false);

		// Departures List
		adapter = new DepartureAdapter();
		list = findViewById(R.id.list);
		list.setVisibility(INVISIBLE);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(this));

		// Loader
		Bundle args = getBundle(location.getId(), new Date(), MAX_DEPARTURES);
		Loader<QueryDeparturesResult> loader = getSupportLoaderManager().initLoader(LOADER_DEPARTURES, args, this);

		// Progress Bar and Error View
		progressBar = findViewById(R.id.progressBar);
		errorLayout = findViewById(R.id.errorLayout);
		errorText = errorLayout.findViewById(R.id.errorText);
		errorLayout.findViewById(R.id.errorButton).setOnClickListener(view -> {
			LceAnimator.showLoading(progressBar, list, errorLayout);
			getSupportLoaderManager().restartLoader(LOADER_DEPARTURES, args, this).forceLoad();
		});

		if (loader.isReset()) {
			LceAnimator.showLoading(progressBar, list, errorLayout);
			loader.forceLoad();
		}

		if (savedInstanceState != null) {
			calendar = (Calendar) savedInstanceState.getSerializable(DATE);
			// re-attach fragment listener
			List<Fragment> fragments = getSupportFragmentManager().getFragments();
			if (fragments != null && fragments.size() > 0 && fragments.get(0) instanceof TimeDateFragment) {
				((TimeDateFragment) fragments.get(0)).setTimeDateListener(this);
			}
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
				TimeDateFragment fragment = TimeDateFragment.newInstance(calendar, null);
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
		LceAnimator.showLoading(progressBar, list, errorLayout);

		Bundle args = getBundle(location.getId(), calendar.getTime(), MAX_DEPARTURES);
		getSupportLoaderManager().restartLoader(LOADER_DEPARTURES, args, this).forceLoad();
	}

	@Override
	public void onDepartureOrArrivalSet(boolean departure) {
	}

	private synchronized void loadMoreDepartures(boolean later) {
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
			// FIXME for some reason this can crash
			Log.i(DeparturesActivity.class.getSimpleName(), "Count: " + count + " Get Item: " + itemPos);
			date = adapter.getItem(itemPos).getTime();
		}
		// search from beginning + safety margin
		else {
			Date earliest = adapter.getEarliestDate();
			Date latest;
			int itemPos;
			if (count >= MAX_DEPARTURES) {
				itemPos = MAX_DEPARTURES - 1;
			} else {
				itemPos = count - 1;
			}
			latest = adapter.getItem(itemPos).getTime();
			long span = latest.getTime() - earliest.getTime();
			date.setTime(earliest.getTime() - span);

			maxDepartures = MAX_DEPARTURES + SAFETY_MARGIN;
		}

		searchState = later ? SearchState.BOTTOM : SearchState.TOP;
		Bundle args = getBundle(location.getId(), date, maxDepartures);
		getSupportLoaderManager().restartLoader(LOADER_DEPARTURES, args, this).forceLoad();
	}

	@NonNull
	@Override
	public DeparturesLoader onCreateLoader(int i, @Nullable Bundle args) {
		return new DeparturesLoader(this, manager.getTransportNetwork().getValue(), args);
	}

	@Override
	public void onLoadFinished(Loader<QueryDeparturesResult> loader, @Nullable QueryDeparturesResult departures) {
		if (departures != null && departures.status == OK && departures.stationDepartures.size() > 0) {
			for (StationDepartures s : departures.stationDepartures) {
				adapter.addAll(s.departures);
			}
			if (searchState == SearchState.INITIAL) {
				LceAnimator.showContent(progressBar, list, errorLayout);
			} else {
				// scroll smoothly up or down when we have new trips
				list.smoothScrollBy(0, searchState == SearchState.BOTTOM ? 150 : -150);
			}
			swipe.setEnabled(true);
		} else {
			int errorMsg = R.string.error_departures;
			if (!TransportrUtils.hasInternet(this)) {
				errorMsg = R.string.error_no_internet;
			}
			if (searchState == SearchState.INITIAL) {
				errorText.setText(errorMsg);
				LceAnimator.showErrorView(progressBar, list, errorLayout);
				swipe.setEnabled(false);
			} else {
				Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
				swipe.setEnabled(true);
			}
		}
		swipe.setRefreshing(false);
	}

	@Override
	public void onLoaderReset(Loader<QueryDeparturesResult> loader) {
	}

}
