package de.grobox.liberario.trips.search;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.grobox.liberario.favorites.trips.FavoriteTripsFragment;
import de.grobox.liberario.fragments.TimeDateFragment;
import de.grobox.liberario.locations.LocationGpsView;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetwork;

import static de.grobox.liberario.utils.TransportrUtils.getDrawableForLocation;

@ParametersAreNonnullByDefault
public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener {

	private final static String TAG = DirectionsActivity.class.getName();

	@Inject ViewModelProvider.Factory viewModelFactory;

	@Nullable
	private TripsFragment tripsFragment;
	private DirectionsViewModel viewModel;
	private DirectionsPresenter presenter;

	private TextView date, time;
	private LocationGpsView from;
	private LocationView to;
	private CardView fromCard, toCard;
	private FrameLayout fragmentContainer;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getComponent().inject(this);
		setContentView(R.layout.activity_directions);
		Toolbar toolbar = setUpCustomToolbar(true);

		AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
		appBarLayout.addOnOffsetChangedListener(this);

		if (toolbar != null) {
			OnClickListener onTimeClickListener = view -> {
				TimeDateFragment fragment = TimeDateFragment.newInstance(presenter.getCalendar());
				fragment.setTimeDateListener(presenter);
				fragment.show(getSupportFragmentManager(), TimeDateFragment.TAG);
			};
			OnLongClickListener onTimeLongClickListener = view -> {
				presenter.resetCalendar();
				return true;
			};
			View timeIcon = toolbar.findViewById(R.id.timeIcon);
			timeIcon.setOnClickListener(onTimeClickListener);
			timeIcon.setOnLongClickListener(onTimeLongClickListener);
			date = toolbar.findViewById(R.id.date);
			date.setOnClickListener(onTimeClickListener);
			date.setOnLongClickListener(onTimeLongClickListener);
			time = toolbar.findViewById(R.id.time);
			time.setOnClickListener(onTimeClickListener);
			time.setOnLongClickListener(onTimeLongClickListener);
		}

		from = findViewById(R.id.fromLocation);
		to = findViewById(R.id.toLocation);

		fromCard = findViewById(R.id.fromCard);
		toCard = findViewById(R.id.toCard);

		// TODO Is Departure???

		// get view model and observe data
		viewModel = ViewModelProviders.of(this, viewModelFactory).get(DirectionsViewModel.class);
		TransportNetwork network = viewModel.getTransportNetwork().getValue();
		if (network == null) throw new IllegalStateException();
		from.setTransportNetwork(network);
		to.setTransportNetwork(network);
		viewModel.getHome().observe(this, homeLocation -> {
			from.setHomeLocation(homeLocation);
			to.setHomeLocation(homeLocation);
		});
		viewModel.getWork().observe(this, workLocation -> {
			from.setWorkLocation(workLocation);
			to.setWorkLocation(workLocation);
		});
		viewModel.getLocations().observe(this, favoriteLocations -> {
			if (favoriteLocations == null) return;
			from.setFavoriteLocations(favoriteLocations);
			to.setFavoriteLocations(favoriteLocations);
		});

		presenter = new DirectionsPresenter(this, viewModel, savedInstanceState);
		from.setLocationViewListener(presenter);
		to.setLocationViewListener(presenter);
		fragmentContainer = findViewById(R.id.fragmentContainer);

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
			case R.id.action_swap_locations:
				swapLocations();
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
		from.setLocation(location);
	}

	void setToLocation(@Nullable WrapLocation location) {
		to.setLocation(location);
	}

	boolean isShowingTrips() {
		return fragmentIsVisible(TripsFragment.TAG);
	}

	void showFavorites() {
		FavoriteTripsFragment f = FavoriteTripsFragment.newInstance(false);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.fragmentContainer, f, FavoriteTripsFragment.TAG)
				.commit();
	}

	void search() {
		Log.i(TAG, "From: " + from.getLocation());
		Log.i(TAG, "To: " + to.getLocation());
		Log.i(TAG, "Date: " + presenter.getCalendar().getTime());

		if (from.getLocation() == null || to.getLocation() == null) return;

		// TODO favTripUid
		tripsFragment = TripsFragment.newInstance(0, from.getLocation(), null, to.getLocation(), presenter.getCalendar().getTime(), true);
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

	private void swapLocations() {
		Animation slideUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.ABSOLUTE, fromCard.getY()*-1);
		slideUp.setDuration(400);
		slideUp.setFillAfter(true);
		slideUp.setFillEnabled(true);

		Animation slideDown = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.ABSOLUTE, fromCard.getY());
		slideDown.setDuration(400);
		slideDown.setFillAfter(true);
		slideDown.setFillEnabled(true);

		fromCard.startAnimation(slideDown);
		toCard.startAnimation(slideUp);

		slideUp.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				// swap location objects
				WrapLocation tmp = to.getLocation();
				if(!from.isSearching()) {
					to.setLocation(from.getLocation(), getDrawableForLocation(DirectionsActivity.this, from.getLocation()));
				} else {
					// TODO: GPS currently only supports from location, so don't swap it for now
					to.clearLocation();
				}
				from.setLocation(tmp, getDrawableForLocation(DirectionsActivity.this, tmp));

				fromCard.clearAnimation();
				toCard.clearAnimation();

				search();
			}
		});
	}

}
