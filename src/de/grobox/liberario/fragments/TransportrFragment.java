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

package de.grobox.liberario.fragments;

import android.support.v4.app.Fragment;

import de.grobox.liberario.AppComponent;
import de.grobox.liberario.TransportrApplication;


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

}
