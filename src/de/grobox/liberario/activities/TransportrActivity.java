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

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.util.Locale;

import de.grobox.liberario.AppComponent;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportrApplication;
import de.grobox.liberario.settings.Preferences;

public abstract class TransportrActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		useLanguage(this);

		// Use current theme
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}

		super.onCreate(savedInstanceState);
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
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

	protected static void useLanguage(Context context) {
		String lang = Preferences.getLanguage(context);
		if(!lang.equals(context.getString(R.string.pref_language_value_default))) {
			Locale locale;
			if(lang.contains("_")) {
				String[] lang_array = lang.split("_");
				locale = new Locale(lang_array[0], lang_array[1]);
			} else {
				locale = new Locale(lang);
			}
			Locale.setDefault(locale);
			Configuration config = context.getResources().getConfiguration();
			config.locale = locale;
			context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
		} else {
			// use default language
			context.getResources().updateConfiguration(Resources.getSystem().getConfiguration(), context.getResources().getDisplayMetrics());
		}
	}

	protected void runOnThread(final Runnable task) {
		new Thread(task).start();
	}

}
