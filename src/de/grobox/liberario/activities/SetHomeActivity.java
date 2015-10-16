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

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.ui.LocationInputView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SetHomeActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.DialogTheme);
		} else {
			setTheme(R.style.DialogTheme_Light);
		}

		getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
		getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_action_home);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_set_home);

		setTitle(getString(R.string.home_dialog_title));

		Intent intent = getIntent();

		// show new home text
		if(!intent.getBooleanExtra("new", true)) {
			findViewById(R.id.homeMsgView).setVisibility(View.GONE);
		}

		final LocationInputView.LocationInputViewHolder holder = new LocationInputView.LocationInputViewHolder(findViewById(R.id.setHomeView));

		final LocationInputView loc = new LocationInputView(this, holder, true);
		loc.setFavs(true);

		holder.location.setHint(R.string.home);

		// OK Button
		Button okButton = (Button) findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(loc.getLocation() != null) {
					// save home location in file
					FavDB.setHome(v.getContext(), loc.getLocation());

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
