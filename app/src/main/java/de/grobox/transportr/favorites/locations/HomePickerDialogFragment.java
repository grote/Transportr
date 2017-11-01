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

package de.grobox.transportr.favorites.locations;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.AppComponent;
import de.grobox.transportr.R;
import de.grobox.transportr.locations.WrapLocation;

@ParametersAreNonnullByDefault
public abstract class HomePickerDialogFragment extends SpecialLocationFragment {

	public static final String TAG = HomePickerDialogFragment.class.getName();

	@Override
	protected void inject(AppComponent component) {
		component.inject(this);
	}

	@Override
	protected int getHint() {
		return R.string.home_dialog_title;
	}

	@Override
	protected void onSpecialLocationSet(WrapLocation location) {
		viewModel.setHome(location);
	}

}
