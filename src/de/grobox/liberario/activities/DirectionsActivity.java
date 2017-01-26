package de.grobox.liberario.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import de.grobox.liberario.R;
import de.grobox.liberario.favorites.FavoritesFragment;

public class DirectionsActivity extends TransportrActivity {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directions);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragmentContainer, FavoritesFragment.newInstance())
					.commit();
		}
	}

}
