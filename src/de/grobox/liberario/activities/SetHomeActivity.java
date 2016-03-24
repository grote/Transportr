/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

package de.grobox.liberario.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.ui.LocationView;

// TODO turn this into a DialogFragment
public class SetHomeActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		TransportrActivity.useLanguage(this);

		getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_action_home);

		// Use current theme
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.SetHomeDialogTheme);
		} else {
			setTheme(R.style.SetHomeDialogTheme_Light);
		}

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_set_home);

		setTitle(getString(R.string.home_dialog_title));

		Intent intent = getIntent();

		// show new home text
		if(!intent.getBooleanExtra("new", true)) {
			View msg = findViewById(R.id.homeMsgView);
			if(msg != null) msg.setVisibility(View.GONE);
		}

		final LocationView loc = (LocationView) findViewById(R.id.location_input);
		if(loc == null) return;

		// OK Button
		Button okButton = (Button) findViewById(R.id.okButton);
		if(okButton == null) return;
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(loc.getLocation() != null) {
					// save home location in file
					RecentsDB.setHome(v.getContext(), loc.getLocation());

					Intent returnIntent = new Intent();
					setResult(RESULT_OK, returnIntent);

					close(v);
				} else {
					Toast.makeText(v.getContext(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Cancel Button
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		if(cancelButton == null) return;
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);

				close(v);
			}
		});
	}

	public void close(View v) {
		ActivityCompat.finishAfterTransition(this);
	}

}
