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

package de.grobox.liberario.networks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;
import de.grobox.liberario.networks.Region.RegionComparator;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.ExpandableListView.getPackedPositionForChild;

public class PickTransportNetworkActivity extends TransportrActivity {

	public final static String FORCE_NETWORK_SELECTION = "ForceNetworkSelection";

	@Inject
	TransportNetworkManager manager;
	private TransportNetworkAdapter adapter;
	private ExpandableListView list;
	private boolean backAllowed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_network_provider);
		getComponent().inject(this);
		setUpCustomToolbar(false);

		Intent intent = getIntent();
		ActionBar actionBar = getSupportActionBar();
		if (intent.getBooleanExtra(FORCE_NETWORK_SELECTION, false)) {
			backAllowed = false;
			if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(false);
			findViewById(R.id.firstRunTextView).setVisibility(VISIBLE);
		} else {
			backAllowed = true;
			if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
			findViewById(R.id.firstRunTextView).setVisibility(GONE);
		}
		setResult(RESULT_CANCELED);

		HashMap<Region, List<TransportNetwork>> listNetwork = getTransportNetworksByRegion();
		List<Region> listRegion = new ArrayList<>(listNetwork.keySet());
		RegionComparator regionComparator = new RegionComparator(this);
		Collections.sort(listRegion, regionComparator);

		adapter = new TransportNetworkAdapter(this, listRegion, listNetwork);
		list = (ExpandableListView) findViewById(R.id.expandableNetworkProviderListView);
		list.setAdapter(adapter);

		selectItem();

		list.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				int index = parent.getFlatListPosition(getPackedPositionForChild(groupPosition, childPosition));
				parent.setItemChecked(index, true);
				if (index >= 0) {
					TransportNetwork network = (TransportNetwork) parent.getItemAtPosition(index);
					manager.setTransportNetwork(network);
					setResult(RESULT_OK);
					supportFinishAfterTransition();
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (backAllowed) super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if (menuItem.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else {
			return false;
		}
	}

	private void selectItem() {
		TransportNetwork network = manager.getTransportNetwork();
		if (network == null) return;

		int region = adapter.getGroupPos(network.getRegion());
		int position = adapter.getChildPos(network.getRegion(), network.getId());
		if (position >= 0) {
			list.expandGroup(region);
			int index = list.getFlatListPosition(getPackedPositionForChild(region, position));
			list.setItemChecked(index, true);
		}
	}

	private HashMap<Region, List<TransportNetwork>> getTransportNetworksByRegion() {
		HashMap<Region, List<TransportNetwork>> networks = new HashMap<>();
		for (TransportNetwork n : TransportNetworks.networks) {
			if (networks.containsKey(n.getRegion())) {
				networks.get(n.getRegion()).add(n);
			} else {
				List<TransportNetwork> list = new ArrayList<>();
				list.add(n);
				networks.put(n.getRegion(), list);
			}
		}
		return networks;
	}

}
