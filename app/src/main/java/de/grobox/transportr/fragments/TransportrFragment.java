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

package de.grobox.transportr.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.AppComponent;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.activities.TransportrActivity;


@ParametersAreNonnullByDefault
public abstract class TransportrFragment extends Fragment {

	protected AppComponent getComponent() {
		return ((TransportrApplication) getActivity().getApplication()).getComponent();
	}

	protected void runOnUiThread(final Runnable task) {
		getActivity().runOnUiThread(task);
	}

	protected void runOnThread(final Runnable task) {
		new Thread(task).start();
	}

	protected void setUpToolbar(Toolbar toolbar) {
		((TransportrActivity) getActivity()).setSupportActionBar(toolbar);
		ActionBar ab = ((TransportrActivity) getActivity()).getSupportActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(true);
			ab.setDisplayHomeAsUpEnabled(true);
			ab.setDisplayShowCustomEnabled(true);
			ab.setDisplayShowTitleEnabled(false);
		}
	}

}
