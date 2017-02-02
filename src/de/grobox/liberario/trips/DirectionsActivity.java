package de.grobox.liberario.trips;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.AppBarLayout.OnOffsetChangedListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.grobox.liberario.favorites.FavoritesFragment;
import de.grobox.liberario.locations.LocationGpsView;
import de.grobox.liberario.locations.LocationView.LocationViewListener;
import de.grobox.liberario.locations.WrapLocation;

public class DirectionsActivity extends TransportrActivity implements OnOffsetChangedListener, LocationViewListener {

	@Nullable
	private TripsFragment tripsFragment;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directions);
		setUpCustomToolbar(true);

		AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
		appBarLayout.addOnOffsetChangedListener(this);

		LocationGpsView from = (LocationGpsView) findViewById(R.id.fromLocation);
		from.setLocationViewListener(this);

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

}
