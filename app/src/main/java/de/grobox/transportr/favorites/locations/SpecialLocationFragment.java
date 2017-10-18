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

import android.arch.lifecycle.ViewModelProvider;
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
import android.view.WindowManager.LayoutParams;

import com.mikepenz.materialdrawer.util.KeyboardUtil;

import javax.inject.Inject;

import de.grobox.transportr.AppComponent;
import de.grobox.transportr.R;
import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType;
import de.grobox.transportr.favorites.trips.FavoriteTripListener;
import de.grobox.transportr.locations.LocationView;
import de.grobox.transportr.locations.LocationsViewModel;
import de.grobox.transportr.locations.WrapLocation;
import de.grobox.transportr.settings.Preferences;

abstract class SpecialLocationFragment extends DialogFragment implements LocationView.LocationViewListener {

	@Inject protected ViewModelProvider.Factory viewModelFactory;

	protected LocationsViewModel viewModel;
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
		loc = v.findViewById(R.id.location_input);
		loc.setHint(getHint());
		loc.setLocationViewListener(this);

		// Get view model and observe data
		viewModel = getViewModel();
		viewModel.getTransportNetwork().observe(this, transportNetwork -> {
			if (transportNetwork != null) loc.setTransportNetwork(transportNetwork);
		});
		viewModel.getLocations().observe(this, favoriteLocations -> {
			if (favoriteLocations == null) return;
			loc.setFavoriteLocations(favoriteLocations);
			loc.post(() -> loc.onClick());  // don't know why this only works when posted
		});

		return v;
	}

	protected abstract LocationsViewModel getViewModel();

	protected abstract @StringRes int getHint();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getDialog().setCanceledOnTouchOutside(true);

		// set width to match parent and show keyboard
		Window window = getDialog().getWindow();
		if (window != null) {
			window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			window.setGravity(Gravity.TOP);
			window.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		KeyboardUtil.hideKeyboard(getActivity());
	}

	@Override
	public void onLocationItemClick(WrapLocation loc, FavLocationType type) {
		onSpecialLocationSet(loc);
		getDialog().cancel();
	}

	protected abstract void onSpecialLocationSet(WrapLocation location);

	@Override
	public void onLocationCleared(FavLocationType type) {
	}

	public void setListener(@Nullable FavoriteTripListener listener) {
		this.listener = listener;
	}

}
