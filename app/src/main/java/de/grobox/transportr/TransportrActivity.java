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

package de.grobox.transportr;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.Locale;

import javax.inject.Inject;

import de.grobox.transportr.settings.SettingsManager;

public abstract class TransportrActivity extends AppCompatActivity {

	@Inject SettingsManager settingsManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getComponent().inject(this);

		useLanguage();
		setTheme(settingsManager.getTheme());

		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public AppComponent getComponent() {
		return ((TransportrApplication) getApplication()).getComponent();
	}

	/**
	 * This should be called after the content view has been added in onCreate()
	 *
	 * @param ownLayout true if the custom toolbar brings its own layout
	 * @return the Toolbar object or null if content view did not contain one
	 */
	@Nullable
	protected Toolbar setUpCustomToolbar(boolean ownLayout) {
		// Custom Toolbar
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar ab = getSupportActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowCustomEnabled(ownLayout);
			ab.setDisplayShowTitleEnabled(!ownLayout);
		}
		return toolbar;
	}

	protected boolean fragmentIsVisible(String tag) {
		Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
		return fragment != null && fragment.isVisible();
	}

	private void useLanguage() {
		Locale locale = settingsManager.getLocale();
		Locale.setDefault(locale);
		Configuration config = getResources().getConfiguration();
		config.locale = locale;
		getResources().updateConfiguration(config, getResources().getDisplayMetrics());
	}

}
