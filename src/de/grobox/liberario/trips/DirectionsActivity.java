package de.grobox.liberario.trips;

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
import android.widget.TextView;

import java.util.Calendar;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.grobox.liberario.favorites.FavoritesFragment;
import de.grobox.liberario.fragments.TimeDateFragment;
import de.grobox.liberario.fragments.TimeDateFragment.TimeDateListener;
import de.grobox.liberario.locations.LocationGpsView;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.LocationView.LocationViewListener;
import de.grobox.liberario.locations.WrapLocation;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.utils.DateUtils.getDate;
import static de.grobox.liberario.utils.DateUtils.getTime;

@ParametersAreNonnullByDefault
public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener, LocationViewListener, TimeDateListener {

	private final static String TAG = DirectionsActivity.class.getName();

	@Nullable
	private TripsFragment tripsFragment;

	private TextView date, time;
	private LocationGpsView from;
	private LocationView to;

	private boolean now = true;
	private Calendar calendar = Calendar.getInstance();

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
					TimeDateFragment fragment = TimeDateFragment.newInstance(calendar);
					fragment.setTimeDateListener(DirectionsActivity.this);
					fragment.show(getSupportFragmentManager(), TimeDateFragment.TAG);
				}
			};
			toolbar.findViewById(R.id.timeIcon).setOnClickListener(onTimeClickListener);
			date = (TextView) toolbar.findViewById(R.id.date);
			date.setOnClickListener(onTimeClickListener);
			time = (TextView) toolbar.findViewById(R.id.time);
			time.setOnClickListener(onTimeClickListener);
		}

		from = (LocationGpsView) findViewById(R.id.fromLocation);
		from.setLocationViewListener(this);

		to = (LocationView) findViewById(R.id.toLocation);
		to.setLocationViewListener(this);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragmentContainer, FavoritesFragment.newInstance())
					.commit();
		}
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
	public void onLocationItemClick(WrapLocation loc) {

	}

	@Override
	public void onLocationCleared() {

	}

	@Override
	public void onTimeAndDateSet(Calendar calendar, boolean isNow, boolean isToday) {
		this.now = isNow;
		this.calendar = calendar;
		if (isNow) {
			time.setText(R.string.now);
			date.setVisibility(GONE);
		} else if (isToday) {
			time.setText(getTime(this, calendar.getTime()));
			date.setVisibility(GONE);
		} else {
			time.setText(getTime(this, calendar.getTime()));
			date.setText(getDate(this, calendar.getTime()));
			date.setVisibility(VISIBLE);
		}
		search();
	}

	private void search() {
		Log.i(TAG, "From: " + from.getLocation());
		Log.i(TAG, "To: " + to.getLocation());
		Log.i(TAG, "Date: " + calendar.getTime());
	}

}
