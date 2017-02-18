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

package de.grobox.liberario.favorites;

import de.grobox.liberario.AppComponent;
import de.grobox.liberario.R;
import de.schildbach.pte.dto.Location;

public class WorkPickerDialogFragment extends SpecialLocationFragment {

	public static final String TAG = WorkPickerDialogFragment.class.getName();

	public WorkPickerDialogFragment() {
	}

	public static WorkPickerDialogFragment newInstance() {
		return new WorkPickerDialogFragment();
	}

	@Override
	protected void inject(AppComponent component) {
		component.inject(this);
	}

	@Override
	protected int getHint() {
		return R.string.work_hint;
	}

	@Override
	protected void onSpecialLocationSet(Location location) {
		manager.setWork(location);
		if (listener != null) listener.onWorkChanged(location);
	}

}
