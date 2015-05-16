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

package de.grobox.liberario.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.grobox.liberario.LiberarioApplication;
import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.adapters.NetworkProviderListAdapter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

public class PickNetworkProviderActivity extends AppCompatActivity {
	private NetworkProviderListAdapter listAdapter;
	private ExpandableListView expListView;
	private boolean back = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pick_network_provider);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			setSupportActionBar(toolbar);
		}

		Intent intent = getIntent();
		if(intent.getBooleanExtra("FirstRun", false)) {
			// hide cancel button on first run
			findViewById(R.id.cancelNetworkProviderButton).setVisibility(View.GONE);
			// prevent going back
			back = false;
			// show first time notice
			findViewById(R.id.firstRunTextView).setVisibility(View.VISIBLE);
		}
		else {
			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		expListView = (ExpandableListView) findViewById(R.id.expandableNetworkProviderListView);

		HashMap<String, List<TransportNetwork>> listNetwork = ((LiberarioApplication) getApplicationContext()).getTransportNetworks().getHashMapByRegion();
		List<String> listRegion = new ArrayList<>(listNetwork.keySet());
		Collections.sort(listRegion);

		listAdapter = new NetworkProviderListAdapter(this, listRegion, listNetwork);
		expListView.setAdapter(listAdapter);

		selectItem();

		expListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				parent.setItemChecked(index, true);
				return false;
			}
		});

		// on OK click
		Button button = (Button) findViewById(R.id.pickNetworkProviderButton);
		button.setOnClickListener(new OnClickListener() {
			@SuppressLint("CommitPrefEdits")
			@Override
			public void onClick(View v) {
				if(expListView.getCheckedItemPosition() >= 0) {
					TransportNetwork network = ((TransportNetwork) expListView.getItemAtPosition(expListView.getCheckedItemPosition()));

					Preferences.setNetworkId(v.getContext(), network.getId());

					Intent returnIntent = new Intent();
					setResult(RESULT_OK, returnIntent);
					finish();
				}
				else {
					Toast.makeText(getBaseContext(), getResources().getText(R.string.error_pick_network), Toast.LENGTH_SHORT).show();
				}
			}
		});

		// on Cancel click
		Button button_cancel = (Button) findViewById(R.id.cancelNetworkProviderButton);
		button_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED, returnIntent);
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(back) {
			Intent returnIntent = new Intent();
			setResult(RESULT_CANCELED, returnIntent);
			finish();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		if(menuItem.getItemId() == android.R.id.home) {
			onBackPressed();
		}
		return true;
	}

	private void selectItem() {
		// get current network from settings
		TransportNetwork tn = Preferences.getTransportNetwork(this);

		// return if no network is set
		if(tn == null || tn.getIdString() == null || tn.getRegion() == null) {
			Log.d(getClass().getSimpleName(), "No NetworkId in Settings.");
			return;
		}

		// we have a network, so pre-select it in the list
		int region = listAdapter.getGroupPos(tn.getRegion());
		int network = listAdapter.getChildPos(tn.getRegion(), tn.getId());
		if(network >= 0) {
			expListView.expandGroup(region);
			int index = expListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(region, network));
			expListView.setItemChecked(index, true);
		}
	}

}
