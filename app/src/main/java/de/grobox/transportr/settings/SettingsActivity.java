package de.grobox.transportr.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrActivity;


public class SettingsActivity extends TransportrActivity {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpCustomToolbar(false);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, new SettingsFragment())
					.commit();
		}
	}

}
