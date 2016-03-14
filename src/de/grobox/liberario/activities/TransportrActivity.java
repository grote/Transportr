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

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;

@SuppressLint("Registered")
public class TransportrActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Use current theme
		if(Preferences.darkThemeEnabled(this)) {
			setTheme(R.style.AppTheme);
		} else {
			setTheme(R.style.AppTheme_Light);
		}

		super.onCreate(savedInstanceState);
	}

}
