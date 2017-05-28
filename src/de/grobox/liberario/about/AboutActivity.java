package de.grobox.liberario.about;

import android.os.Bundle;
import android.support.annotation.Nullable;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;


public class AboutActivity extends TransportrActivity {

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setUpCustomToolbar(false);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.fragment_container, new AboutMainFragment())
					.commit();
		}
	}

}
