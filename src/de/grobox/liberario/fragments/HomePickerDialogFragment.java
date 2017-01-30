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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mikepenz.materialdrawer.util.KeyboardUtil;

import de.grobox.liberario.settings.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.locations.LocationView;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

public class HomePickerDialogFragment extends DialogFragment {

	public static final String TAG = "de.grobox.liberario.home_picker";

	private OnHomeChangedListener listener;

	public HomePickerDialogFragment() {
	}

	public static HomePickerDialogFragment newInstance() {
		HomePickerDialogFragment f = new HomePickerDialogFragment();

		Bundle args = new Bundle();
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(Preferences.darkThemeEnabled(getActivity())) {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme);
		} else {
			setStyle(DialogFragment.STYLE_NO_TITLE, R.style.SetHomeDialogTheme_Light);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View v = inflater.inflate(R.layout.fragment_set_home, container);

		// Adapt Title Icon
		final TextView title = (TextView) v.findViewById(R.id.homeTitleView);
		title.setCompoundDrawables(TransportrUtils.getTintedDrawable(getContext(), title.getCompoundDrawables()[0]), null, null, null);

		// Initialize LocationView
		final LocationView loc = (LocationView) v.findViewById(R.id.location_input);

		// OK Button
		Button okButton = (Button) v.findViewById(R.id.okButton);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(loc.getLocation() != null) {
					// save home location in file
					RecentsDB.setHome(v.getContext(), loc.getLocation());

					// call listener if set
					if(listener != null) listener.onHomeChanged(loc.getLocation());

					getDialog().cancel();
				} else {
					Toast.makeText(v.getContext(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
				}
			}
		});

		// Cancel Button
		Button cancelButton = (Button) v.findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getDialog().cancel();
			}
		});
		return v;
	}

	@Override
	public void onResume() {
		// set width to match parent
		Window window = getDialog().getWindow();
		window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
		window.setGravity(Gravity.CENTER);

		super.onResume();

	}

	@Override
	public void onCancel(DialogInterface dialog) {
		KeyboardUtil.hideKeyboard(getActivity());
	}

	public void setOnHomeChangedListener(OnHomeChangedListener listener) {
		this.listener = listener;
	}

	public interface OnHomeChangedListener {
		void onHomeChanged(Location home);
	}

}
