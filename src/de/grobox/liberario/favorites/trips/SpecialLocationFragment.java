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

package de.grobox.liberario.favorites.trips;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.mikepenz.materialdrawer.util.KeyboardUtil;

import javax.inject.Inject;

import de.grobox.liberario.AppComponent;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportrApplication;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.settings.Preferences;
import de.schildbach.pte.dto.Location;

abstract class SpecialLocationFragment extends DialogFragment implements LocationView.LocationViewListener {

	@Inject TransportNetworkManager manager;
	protected @Nullable FavoriteTripListener listener;

	private LocationView loc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		inject(((TransportrApplication) getActivity().getApplication()).getComponent());

		if (Preferences.darkThemeEnabled(getActivity())) {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme);
		} else {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme_Light);
		}
	}

	protected abstract void inject(AppComponent component);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = inflater.inflate(R.layout.fragment_special_location, container);

		// Initialize LocationView
		loc = (LocationView) v.findViewById(R.id.location_input);
		loc.setHint(getHint());
		loc.setLocationViewListener(this);

		return v;
	}

	protected abstract @StringRes int getHint();

	@Override
	public void onResume() {
		super.onResume();

		getDialog().setCanceledOnTouchOutside(true);

		// set width to match parent
		Window window = getDialog().getWindow();
		if (window != null) {
			window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
			window.setGravity(Gravity.TOP);
		}

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(loc, InputMethodManager.SHOW_FORCED);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		KeyboardUtil.hideKeyboard(getActivity());
	}

	@Override
	public void onLocationItemClick(WrapLocation loc) {
		onSpecialLocationSet(loc.getLocation());
		getDialog().cancel();
	}

	protected abstract void onSpecialLocationSet(Location location);

	@Override
	public void onLocationCleared() {
	}

	public void setListener(@Nullable FavoriteTripListener listener) {
		this.listener = listener;
	}

}
