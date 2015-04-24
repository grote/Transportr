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

package de.grobox.liberario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.schildbach.pte.NetworkId;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
	private List<String> listRegion;
	private HashMap<String, List<NetworkItem>> listNetwork;
	private int selectedRegion = -1;
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

		prepareListData();

		listAdapter = new NetworkProviderListAdapter(this, listRegion, listNetwork);
		expListView.setAdapter(listAdapter);

		selectItem();

		expListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
				parent.setItemChecked(index, true);
				selectedRegion = groupPosition;
				return false;
			}
		});

		// on OK click
		Button button = (Button) findViewById(R.id.pickNetworkProviderButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(expListView.getCheckedItemPosition() >= 0) {
					String region = listAdapter.getGroup(selectedRegion);
					String network = ((NetworkItem) expListView.getItemAtPosition(expListView.getCheckedItemPosition())).id.name();

					SharedPreferences settings = getSharedPreferences(Preferences.PREFS, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();

					editor.putString("NetworkRegion", region);
					editor.putString("NetworkId", network);

					editor.commit();

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
		SharedPreferences settings = getSharedPreferences(Preferences.PREFS, Context.MODE_PRIVATE);
		String network_string = settings.getString("NetworkId", null);
		String region_string = settings.getString("NetworkRegion", null);

		// return if no network is set
		if(network_string == null || region_string == null) {
			Log.d(getClass().getSimpleName(), "No NetworkId in Settings.");
			return;
		}

		// construct NetworkId object from network string
		NetworkId network_id;
		try {
			network_id = NetworkId.valueOf(network_string);
		}
		catch (IllegalArgumentException e) {
			Log.d(getClass().getSimpleName(), "Invalid NetworkId in Settings.");
			return;
		}

		// we have a network, so pre-select it in the list
		int region = listAdapter.getGroupPos(region_string);
		int network = listAdapter.getChildPos(region_string, network_id);
		if(network >= 0) {
			expListView.expandGroup(region);
			selectedRegion = region;
			int index = expListView.getFlatListPosition(ExpandableListView.getPackedPositionForChild(region, network));
			expListView.setItemChecked(index, true);
		}
	}

	private void prepareListData() {
		listRegion = new ArrayList<>();
		listNetwork = new HashMap<>();

		listRegion.add(getString(R.string.np_region_europe));
		List<NetworkItem> eu = new ArrayList<>();
		eu.add(new NetworkItem(NetworkId.RT, "EU", getString(R.string.np_desc_rt)));
		listNetwork.put(getString(R.string.np_region_europe), eu);

		listRegion.add(getString(R.string.np_region_germany));
		List<NetworkItem> de = new ArrayList<>();
		de.add(new NetworkItem(NetworkId.DB, getString(R.string.np_name_db), getString(R.string.np_desc_db)));
		de.add(new NetworkItem(NetworkId.BVG, "BVG", getString(R.string.np_desc_bvg)));
		de.add(new NetworkItem(NetworkId.VBB, "VBB", getString(R.string.np_desc_vbb)));
		de.add(new NetworkItem(NetworkId.BAYERN, getString(R.string.np_name_bayern), getString(R.string.np_desc_bayern)));
		de.add(new NetworkItem(NetworkId.AVV, "AVV", getString(R.string.np_desc_avv), true));
		de.add(new NetworkItem(NetworkId.MVV, "MVV", getString(R.string.np_desc_mvv)));
		de.add(new NetworkItem(NetworkId.RSAG, "RSAG", getString(R.string.np_desc_rsag)));
		de.add(new NetworkItem(NetworkId.INVG, "INVG", getString(R.string.np_desc_invg), true));
//		de.add(new NetworkItem(NetworkId.VGN, "VGN", getString(R.string.np_desc_vgn), true));
		de.add(new NetworkItem(NetworkId.VVM, "VVM", getString(R.string.np_desc_vvm)));
		de.add(new NetworkItem(NetworkId.VMV, "VMV", getString(R.string.np_desc_vmv)));
//		de.add(new NetworkItem(NetworkId.HVV, "HVV", getString(R.string.np_desc_hvv)));
		de.add(new NetworkItem(NetworkId.SH, "SH", getString(R.string.np_desc_sh)));
		de.add(new NetworkItem(NetworkId.GVH, "GVH", getString(R.string.np_desc_gvh)));
		de.add(new NetworkItem(NetworkId.BSVAG, "BSVAG", getString(R.string.np_desc_bsvag)));
		de.add(new NetworkItem(NetworkId.VBN, "VBN", getString(R.string.np_desc_vbn)));
		de.add(new NetworkItem(NetworkId.VVO, "VVO", getString(R.string.np_desc_vvo)));
		de.add(new NetworkItem(NetworkId.VMS, "VMS", getString(R.string.np_desc_vms), true));
		de.add(new NetworkItem(NetworkId.NASA, "NASA", getString(R.string.np_desc_nasa), true));
		de.add(new NetworkItem(NetworkId.VRR, "VRR", getString(R.string.np_desc_vrr)));
//		de.add(new NetworkItem(NetworkId.VRT, "VRT", getString(R.string.np_desc_vrt), true));
		de.add(new NetworkItem(NetworkId.MVG, "MVG", getString(R.string.np_desc_mvg)));
		de.add(new NetworkItem(NetworkId.NVV, "NVV/RMV", getString(R.string.np_desc_nvv)));
		de.add(new NetworkItem(NetworkId.VRN, "VRN", getString(R.string.np_desc_vrn)));
		de.add(new NetworkItem(NetworkId.VVS, "VVS", getString(R.string.np_desc_vvs)));
		de.add(new NetworkItem(NetworkId.DING, "DING", getString(R.string.np_desc_ding)));
		de.add(new NetworkItem(NetworkId.KVV, "KVV", getString(R.string.np_desc_kvv)));
		de.add(new NetworkItem(NetworkId.VAGFR, "VAGFR", getString(R.string.np_desc_vagfr)));
		de.add(new NetworkItem(NetworkId.NVBW, "NVBW", getString(R.string.np_desc_nvbw)));
		de.add(new NetworkItem(NetworkId.VVV, "VVV", getString(R.string.np_desc_vvv)));
		de.add(new NetworkItem(NetworkId.VGS, "VGS", getString(R.string.np_desc_vgs)));
		de.add(new NetworkItem(NetworkId.VRS, "VRS", getString(R.string.np_desc_vrs)));
		listNetwork.put(getString(R.string.np_region_germany), de);

		listRegion.add(getString(R.string.np_region_austria));
		List<NetworkItem> at = new ArrayList<>();
		at.add(new NetworkItem(NetworkId.OEBB, "OEBB", getString(R.string.np_desc_oebb)));
		at.add(new NetworkItem(NetworkId.VOR, "VOR", getString(R.string.np_desc_vor)));
		at.add(new NetworkItem(NetworkId.LINZ, "LINZ", getString(R.string.np_desc_linz)));
		at.add(new NetworkItem(NetworkId.SVV, "SVV", getString(R.string.np_desc_svv)));
		at.add(new NetworkItem(NetworkId.VVT, "VVT", getString(R.string.np_desc_vvt)));
		at.add(new NetworkItem(NetworkId.IVB, "IVB", getString(R.string.np_desc_ivb)));
		at.add(new NetworkItem(NetworkId.STV, "STV", getString(R.string.np_desc_stv)));
		at.add(new NetworkItem(NetworkId.WIEN, getString(R.string.np_name_wien), getString(R.string.np_desc_wien)));
		listNetwork.put(getString(R.string.np_region_austria), at);

		listRegion.add(getString(R.string.np_region_liechtenstein));
		List<NetworkItem> li = new ArrayList<>();
		li.add(new NetworkItem(NetworkId.VMOBIL, "VMOBIL", getString(R.string.np_desc_vmobil)));
		listNetwork.put(getString(R.string.np_region_liechtenstein), li);

		listRegion.add(getString(R.string.np_region_switzerland));
		List<NetworkItem> ch = new ArrayList<>();
		ch.add(new NetworkItem(NetworkId.SBB, "SBB", ""));
//		ch.add(new NetworkItem(NetworkId.BVB, "BVB", ""));
		ch.add(new NetworkItem(NetworkId.VBL, "VBL", getString(R.string.np_desc_vbl)));
		ch.add(new NetworkItem(NetworkId.ZVV, "ZVV", getString(R.string.np_desc_zvv)));
		listNetwork.put(getString(R.string.np_region_switzerland), ch);

		listRegion.add(getString(R.string.np_region_belgium));
		List<NetworkItem> be = new ArrayList<>();
		be.add(new NetworkItem(NetworkId.SNCB, "SNCB", ""));
		listNetwork.put(getString(R.string.np_region_belgium), be);

		listRegion.add(getString(R.string.np_region_luxembourg));
		List<NetworkItem> lu = new ArrayList<>();
		lu.add(new NetworkItem(NetworkId.LU, "LU", ""));
		listNetwork.put(getString(R.string.np_region_luxembourg), lu);

		listRegion.add(getString(R.string.np_region_netherlands));
		List<NetworkItem> nl = new ArrayList<>();
		nl.add(new NetworkItem(NetworkId.NS, "NS", getString(R.string.np_desc_ns), true));
		listNetwork.put(getString(R.string.np_region_netherlands), nl);

		listRegion.add(getString(R.string.np_region_denmark));
		List<NetworkItem> dk = new ArrayList<>();
		dk.add(new NetworkItem(NetworkId.DSB, "DSB", getString(R.string.np_desc_dsb)));
		listNetwork.put(getString(R.string.np_region_denmark), dk);

		listRegion.add(getString(R.string.np_region_sweden));
		List<NetworkItem> sv = new ArrayList<>();
		sv.add(new NetworkItem(NetworkId.SE, "SE", getString(R.string.np_desc_se)));
		sv.add(new NetworkItem(NetworkId.STOCKHOLM, getString(R.string.np_name_stockholm), getString(R.string.np_desc_stockholm), true));
		listNetwork.put(getString(R.string.np_region_sweden), sv);

		listRegion.add(getString(R.string.np_region_norway));
		List<NetworkItem> no = new ArrayList<>();
		no.add(new NetworkItem(NetworkId.NRI, "NRI", getString(R.string.np_desc_nri)));
		listNetwork.put(getString(R.string.np_region_norway), no);

		listRegion.add(getString(R.string.np_region_finland));
		List<NetworkItem> fi = new ArrayList<>();
		fi.add(new NetworkItem(NetworkId.HSL, "HSL", getString(R.string.np_desc_hsl)));
		listNetwork.put(getString(R.string.np_region_finland), fi);

		listRegion.add(getString(R.string.np_region_gb));
		List<NetworkItem> gb = new ArrayList<>();
		gb.add(new NetworkItem(NetworkId.TLEM, "TLEM", getString(R.string.np_desc_tlem)));
		gb.add(new NetworkItem(NetworkId.MERSEY, "MERSEY", getString(R.string.np_desc_mersey)));
		listNetwork.put(getString(R.string.np_region_gb), gb);

		listRegion.add(getString(R.string.np_region_ireland));
		List<NetworkItem> ie = new ArrayList<>();
		ie.add(new NetworkItem(NetworkId.TFI, "TFI", getString(R.string.np_desc_tfi)));
//		ie.add(new NetworkItem(NetworkId.EIREANN, "EIREANN", "Bus Éireann"));
		listNetwork.put(getString(R.string.np_region_ireland), ie);

		listRegion.add(getString(R.string.np_region_italy));
		List<NetworkItem> it = new ArrayList<>();
		it.add(new NetworkItem(NetworkId.ATC, "ATC", getString(R.string.np_desc_atc), true));
		listNetwork.put(getString(R.string.np_region_italy), it);

		listRegion.add(getString(R.string.np_region_poland));
		List<NetworkItem> pl = new ArrayList<>();
		pl.add(new NetworkItem(NetworkId.PL, "PL", getString(R.string.np_desc_pl)));
		listNetwork.put(getString(R.string.np_region_poland), pl);

		listRegion.add(getString(R.string.np_region_uae));
		List<NetworkItem> ae = new ArrayList<>();
		ae.add(new NetworkItem(NetworkId.DUB, "DUB", getString(R.string.np_desc_dub), true));
		listNetwork.put(getString(R.string.np_region_uae), ae);

		listRegion.add(getString(R.string.np_region_usa));
		List<NetworkItem> us = new ArrayList<>();
		us.add(new NetworkItem(NetworkId.SF, "SF", getString(R.string.np_desc_sf)));
		us.add(new NetworkItem(NetworkId.SEPTA, "SEPTA", getString(R.string.np_desc_septa), true));
//		us.add(new NetworkItem(NetworkId.USNY, getString(R.string.np_name_usny), getString(R.string.np_desc_usny), true));
		listNetwork.put(getString(R.string.np_region_usa), us);

		listRegion.add(getString(R.string.np_region_australia));
		List<NetworkItem> au = new ArrayList<>();
		au.add(new NetworkItem(NetworkId.SYDNEY, getString(R.string.np_name_sydney), getString(R.string.np_desc_sydney)));
		au.add(new NetworkItem(NetworkId.MET, "MET", getString(R.string.np_desc_met)));
		listNetwork.put(getString(R.string.np_region_australia), au);

		listRegion.add(getString(R.string.np_region_israel));
		List<NetworkItem> il = new ArrayList<>();
		il.add(new NetworkItem(NetworkId.JET, "JET", getString(R.string.np_desc_jet), true));
		listNetwork.put(getString(R.string.np_region_israel), il);

		listRegion.add(getString(R.string.np_region_france));
		List<NetworkItem> fr = new ArrayList<>();
		fr.add(new NetworkItem(NetworkId.PARIS, getString(R.string.np_name_paris), getString(R.string.np_desc_paris), true));
		fr.add(new NetworkItem(NetworkId.PACA, "PACA", getString(R.string.np_desc_paca), true));
		listNetwork.put(getString(R.string.np_region_france), fr);

		listRegion.add(getString(R.string.np_region_nz));
		List<NetworkItem> nz = new ArrayList<>();
		nz.add(new NetworkItem(NetworkId.NZ, "NZ", getString(R.string.np_desc_nz), true));
		listNetwork.put(getString(R.string.np_region_nz), nz);

		listRegion.add(getString(R.string.np_region_spain));
		List<NetworkItem> es = new ArrayList<>();
		es.add(new NetworkItem(NetworkId.SPAIN, getString(R.string.np_name_spain), getString(R.string.np_desc_spain), true));
		listNetwork.put(getString(R.string.np_region_spain), es);
	}


}
