/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.mikepenz.fastadapter.IItem;
import com.mikepenz.fastadapter.ISelectionListener;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.activities.TransportrActivity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PickTransportNetworkActivity extends TransportrActivity {

	public final static String FORCE_NETWORK_SELECTION = "ForceNetworkSelection";

	@Inject
	TransportNetworkManager manager;
	private FastItemAdapter<IItem> adapter;
	private RecyclerView list;
	private boolean backAllowed, selectAllowed = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_transport_network);
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

		adapter = new FastItemAdapter<>();
		adapter.withSelectable(true);
		adapter.getItemAdapter().withComparator(new RegionItem.RegionComparator(this));
		adapter.withSelectionListener(new ISelectionListener<IItem>() {
			@Override
			public void onSelectionChanged(IItem item, boolean selected) {
				if (!selectAllowed || !selected) return;
				selectAllowed = false;
				TransportNetworkItem networkItem = (TransportNetworkItem) item;
				manager.setTransportNetwork(networkItem.getTransportNetwork());
				setResult(RESULT_OK);
				supportFinishAfterTransition();
			}
		});
		list = (RecyclerView) findViewById(R.id.list);
		list.setLayoutManager(new LinearLayoutManager(this));
		list.setAdapter(adapter);

		Map<Region, List<TransportNetwork>> networksByRegion = getTransportNetworksByRegion();
		for (Region region : networksByRegion.keySet()) {
			RegionItem regionItem = new RegionItem(region);
			List<TransportNetwork> networks = networksByRegion.get(region);
			List<TransportNetworkItem> networkItems = new ArrayList<>(networks.size());
			for (TransportNetwork n : networks) networkItems.add(new TransportNetworkItem(n));
			regionItem.withSubItems(networkItems);
			adapter.add(regionItem);
		}
		adapter.withSavedInstanceState(savedInstanceState);

		selectItem();
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
		if (network == null) {
			selectAllowed = true;
			return;
		}
		int pos = adapter.getPosition(new RegionItem(network.getRegion()));
		if (pos != -1) {
			adapter.expand(pos);
			pos = adapter.getPosition(new TransportNetworkItem(network));
			if (pos != -1) {
				adapter.select(pos, false);
				list.scrollToPosition(pos);
				list.smoothScrollBy(0, -75);
				selectAllowed = true;
			}
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
