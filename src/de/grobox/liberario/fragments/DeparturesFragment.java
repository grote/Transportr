/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.FavDB;
import de.grobox.liberario.ui.DelayAutoCompleteTextView;
import de.grobox.liberario.ui.LocationInputView;
import de.grobox.liberario.utils.DateUtils;
import de.grobox.liberario.utils.LiberarioUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.NetworkProvider.Capability;
import it.neokree.materialnavigationdrawer.MaterialNavigationDrawer;

public class DeparturesFragment extends LiberarioFragment {
	private View mView;
	public ProgressDialog pd;
	LocationInputView.LocationInputViewHolder loc_ui;
	LocationInputView loc;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// remember view for UI changes when fragment is not active
		mView = inflater.inflate(R.layout.fragment_departures, container, false);

		TransportNetwork network = Preferences.getTransportNetwork(getActivity());
		if(network != null) {
			((MaterialNavigationDrawer) getActivity()).getToolbar().setSubtitle(network.getName());
		}

		// Location Input View

		loc_ui = new LocationInputView.LocationInputViewHolder();
		loc_ui.location = (DelayAutoCompleteTextView) mView.findViewById(R.id.location);
		loc_ui.clear = (ImageButton) mView.findViewById(R.id.clearButton);
		loc_ui.progress = (ProgressBar) mView.findViewById(R.id.progress);
		loc_ui.status = (ImageView) mView.findViewById(R.id.statusButton);

		loc = new LocationInputView(getActivity(), loc_ui, true);
		loc.setFavs(true);
		loc.setHome(true);
		loc.setGPS(false);

		loc_ui.location.setHint(R.string.departure_station);

		DateUtils.setUpTimeDateUi(mView);

		// Find Departures Search Button

		Button stationButton = (Button) mView.findViewById(R.id.stationButton);
		stationButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(loc.getLocation() != null) {
					// use location to query departures

					if(!loc.getLocation().hasId()) {
						Toast.makeText(getActivity(), getResources().getString(R.string.error_no_proper_station), Toast.LENGTH_SHORT).show();
						return;
					}

					// Location is valid, so make it a favorite or increase counter
					FavDB.updateFavLocation(getActivity(), loc.getLocation(), FavLocation.LOC_TYPE.FROM);

					LiberarioUtils.findDepartures(getActivity(), loc.getLocation());
				} else {
					Toast.makeText(getActivity(), getResources().getString(R.string.error_only_autocomplete_station), Toast.LENGTH_SHORT).show();
				}
			}
		});

		return mView;
	}

	@Override
	public void onNetworkProviderChanged(TransportNetwork network) {
		if(mView == null) return;

		// TODO still needed?
		// we probably don't want to hide things, but switch the section when network does not
		// support departures

		LinearLayout departuresLayout = (LinearLayout) mView.findViewById(R.id.cardView);

		NetworkProvider np = network.getNetworkProvider();

		if(np.hasCapabilities(Capability.DEPARTURES)) {
			departuresLayout.setVisibility(View.VISIBLE);

			// clear favorites for auto-complete
			if(loc_ui.location.getAdapter() != null) {
				((LocationAdapter) loc_ui.location.getAdapter()).resetList();
			}

			// clear text view
			loc.clearLocation();
		} else {
			departuresLayout.setVisibility(View.GONE);
		}
	}

}

