package de.grobox.liberario.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;

import de.grobox.liberario.R;
import de.grobox.liberario.favorites.FavoritesFragment;
import de.grobox.liberario.trips.TripsFragment;

public class DirectionsActivity extends TransportrActivity implements AppBarLayout.OnOffsetChangedListener {

	@Nullable
	private TripsFragment tripsFragment;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directions);

		AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
		appBarLayout.addOnOffsetChangedListener(this);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragmentContainer, FavoritesFragment.newInstance())
					.commit();
		}
	}

	@Override
	public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
		if (tripsFragment != null) {
			tripsFragment.setSwipeEnabled(verticalOffset == 0);
		}
	}

}
