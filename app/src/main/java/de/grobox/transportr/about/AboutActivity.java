package de.grobox.transportr.about;

import android.os.Bundle;
import android.support.annotation.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;
import de.grobox.transportr.TransportrActivity;


@ParametersAreNonnullByDefault
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
