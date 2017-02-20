package de.grobox.liberario.trips;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.grobox.liberario.favorites.trips.FavoriteTripTripsFragment;
import de.grobox.liberario.fragments.TimeDateFragment;
import de.grobox.liberario.locations.LocationGpsView;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.WrapLocation;

@ParametersAreNonnullByDefault
public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener {

	private final static String TAG = DirectionsActivity.class.getName();

	@Nullable
	private TripsFragment tripsFragment;
	private DirectionsPresenter presenter;

	private TextView date, time;
	private LocationGpsView from;
	private LocationView to;
	private FrameLayout fragmentContainer;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directions);
		Toolbar toolbar = setUpCustomToolbar(true);

		AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
		appBarLayout.addOnOffsetChangedListener(this);

		if (toolbar != null) {
			OnClickListener onTimeClickListener = new OnClickListener() {
				@Override
				public void onClick(View view) {
					TimeDateFragment fragment = TimeDateFragment.newInstance(presenter.getCalendar());
					fragment.setTimeDateListener(presenter);
					fragment.show(getSupportFragmentManager(), TimeDateFragment.TAG);
				}
			};
			OnLongClickListener onTimeLongClickListener = new OnLongClickListener() {
				@Override
				public boolean onLongClick(View view) {
					presenter.resetCalendar();
					return true;
				}
			};
			View timeIcon = toolbar.findViewById(R.id.timeIcon);
			timeIcon.setOnClickListener(onTimeClickListener);
			timeIcon.setOnLongClickListener(onTimeLongClickListener);
			date = (TextView) toolbar.findViewById(R.id.date);
			date.setOnClickListener(onTimeClickListener);
			date.setOnLongClickListener(onTimeLongClickListener);
			time = (TextView) toolbar.findViewById(R.id.time);
			time.setOnClickListener(onTimeClickListener);
			time.setOnLongClickListener(onTimeLongClickListener);
		}

		from = (LocationGpsView) findViewById(R.id.fromLocation);
		to = (LocationView) findViewById(R.id.toLocation);

		// TODO Is Departure???

		presenter = new DirectionsPresenter(this, savedInstanceState);
		from.setLocationViewListener(presenter);
		to.setLocationViewListener(presenter);
		fragmentContainer = (FrameLayout) findViewById(R.id.fragmentContainer);

		if (savedInstanceState == null) {
			showFavorites();
		} else {
			// TODO re-attach TimeDateFragment if shown
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		processIntent();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		presenter.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.directions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch(item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
		if (tripsFragment != null) {
			tripsFragment.setSwipeEnabled(verticalOffset == 0);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		processIntent();
	}

	void setTimeText(String text) {
		time.setText(text);
	}

	void setDateText(String text) {
		date.setText(text);
	}

	void setDateVisibility(int visibility) {
		date.setVisibility(visibility);
	}

	void setFromLocation(@Nullable WrapLocation location) {
		from.setWrapLocation(location);
	}

	void setToLocation(@Nullable WrapLocation location) {
		to.setWrapLocation(location);
	}

	boolean isShowingTrips() {
		return fragmentIsVisible(TripsFragment.TAG);
	}

	void showFavorites() {
		FavoriteTripTripsFragment f = FavoriteTripTripsFragment.newInstance(false);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentContainer, f, FavoriteTripTripsFragment.TAG)
				.commit();
	}

	void search() {
		Log.i(TAG, "From: " + from.getLocation());
		Log.i(TAG, "To: " + to.getLocation());
		Log.i(TAG, "Date: " + presenter.getCalendar().getTime());

		if (from.getLocation() == null || to.getLocation() == null) return;

		tripsFragment = TripsFragment.newInstance(from.getLocation(), null, to.getLocation(), presenter.getCalendar().getTime(), true);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.fragmentContainer, tripsFragment, TripsFragment.TAG)
				.commit();
		fragmentContainer.requestFocus();
	}

	private void processIntent() {
		presenter.processIntent(getIntent());
		// remove the intent (and clear its action) since it was already processed
		// and should not be processed again
		setIntent(null);
	}

}
