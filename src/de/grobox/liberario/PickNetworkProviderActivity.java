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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

public class PickNetworkProviderActivity extends FragmentActivity {
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

		Intent intent = getIntent();
		if(intent.getBooleanExtra("FirstRun", false)) {
			// hide cancel button on first run
			((Button) findViewById(R.id.cancelNetworkProviderButton)).setVisibility(View.GONE);
			// prevent going back
			back = false;
			// show first time notice
			((TextView) findViewById(R.id.firstRunTextView)).setVisibility(View.VISIBLE);
		}
		else {
			getActionBar().setDisplayHomeAsUpEnabled(true);
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
		NetworkId network_id = null;
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
		listRegion = new ArrayList<String>();
		listNetwork = new HashMap<String, List<NetworkItem>>();

		listRegion.add("Europe");
		List<NetworkItem> eu = new ArrayList<NetworkItem>();
		eu.add(new NetworkItem(NetworkId.RT, "Europe", "long-distance only"));
		listNetwork.put("Europe", eu);

		listRegion.add("Germany");
		List<NetworkItem> de = new ArrayList<NetworkItem>();
		de.add(new NetworkItem(NetworkId.DB, "Deutsche Bahn"));
		de.add(new NetworkItem(NetworkId.BVG, "BVG", "Berlin"));
		de.add(new NetworkItem(NetworkId.VBB, "VBB", "Brandenburg, Berlin"));
		de.add(new NetworkItem(NetworkId.BAYERN, "Bayern", true));
		de.add(new NetworkItem(NetworkId.AVV, "AVV", "Augsburg", true));
		de.add(new NetworkItem(NetworkId.MVV, "MVV", "München"));
		de.add(new NetworkItem(NetworkId.INVG, "INVG", "Ingolstadt", true));
//		de.add(new NetworkItem(NetworkId.VGN, "VGB", "Nürnberg, Fürth, Erlangen", true));
		de.add(new NetworkItem(NetworkId.VVM, "VVM", "Bayern, Würzburg, Regensburg", true));
		de.add(new NetworkItem(NetworkId.VMV, "VMV", "Mecklenburg-Vorpommern, Schwerin"));
//		de.add(new NetworkItem(NetworkId.HVV, "HVV", "Hamburg"));
		de.add(new NetworkItem(NetworkId.SH, "SH", "Schleswig-Holstein, Kiel, Lübeck, Hamburg"));
		de.add(new NetworkItem(NetworkId.GVH, "GVH", "Niedersachsen, Hannover, Hamburg"));
		de.add(new NetworkItem(NetworkId.BSVAG, "BSVAG", "Braunschweig, Wolfsburg"));
		de.add(new NetworkItem(NetworkId.VBN, "VBN", "Niedersachsen, Bremen, Bremerhaven, Oldenburg (Oldenburg)", true));
		de.add(new NetworkItem(NetworkId.VVO, "VVO", "Sachsen, Dresden", true));
		de.add(new NetworkItem(NetworkId.VMS, "VMS", "Mittelsachsen, Chemnitz", true));
		de.add(new NetworkItem(NetworkId.NASA, "NASA", "Sachsen, Leipzig, Sachsen-Anhalt, Magdeburg, Halle", true));
		de.add(new NetworkItem(NetworkId.VRR, "VRR", "Nordrhein-Westfalen, Köln, Bonn, Essen, Dortmund, Düsseldorf, Münster, Paderborn, Höxter"));
//		de.add(new NetworkItem(NetworkId.VRT, "VRT", "Rhein-Neckar-Dreieck", true));
		de.add(new NetworkItem(NetworkId.MVG, "MVG", "Märkischer Kreis, Lüdenscheid", true));
		de.add(new NetworkItem(NetworkId.NVV, "NVV", "Hessen, Kassel"));
		de.add(new NetworkItem(NetworkId.VRN, "VRN", "Baden-Württemberg, Rheinland-Pfalz, Mannheim, Mainz, Trier"));
		de.add(new NetworkItem(NetworkId.VVS, "VVS", "Baden-Württemberg, Stuttgart"));
		de.add(new NetworkItem(NetworkId.NALDO, "NALDO", "Reutlingen, Rottweil, Tübingen, Sigmaringen"));
		de.add(new NetworkItem(NetworkId.DING, "DING", "Baden-Württemberg, Ulm, Neu-Ulm", true));
		de.add(new NetworkItem(NetworkId.KVV, "KVV", "Baden-Württemberg, Karlsruhe"));
		de.add(new NetworkItem(NetworkId.VAGFR, "VAGFR", "Elsass, Bas-Rhin, Straßburg, Freiburg im Breisgau"));
		de.add(new NetworkItem(NetworkId.NVBW, "NVBW", "Baden-Württemberg, Konstanz, Basel, Basel-Stadt"));
		de.add(new NetworkItem(NetworkId.VVV, "VVV", "Vogtland, Plauen"));
		de.add(new NetworkItem(NetworkId.VGS, "VGS", "Saarland, Saarbrücken"));
		listNetwork.put("Germany", de);

		listRegion.add("Österreich");
		List<NetworkItem> at = new ArrayList<NetworkItem>();
		at.add(new NetworkItem(NetworkId.OEBB, "OEBB", "Ganz Österreich"));
		at.add(new NetworkItem(NetworkId.VOR, "VOR", "Niederösterreich, Burgenland, Wien"));
		at.add(new NetworkItem(NetworkId.LINZ, "LINZ", "Oberösterreich, Linz"));
		at.add(new NetworkItem(NetworkId.SVV, "SVV", "Salzburg"));
		at.add(new NetworkItem(NetworkId.VVT, "VVT", "Tirol"));
		at.add(new NetworkItem(NetworkId.IVB, "IVB", "Innsbruck"));
		at.add(new NetworkItem(NetworkId.STV, "STV", "Steiermark, Graz"));
		at.add(new NetworkItem(NetworkId.WIEN, "WIEN", "WIENER LINIEN", true));
		listNetwork.put("Österreich", at);

		listRegion.add("Liechtenstein");
		List<NetworkItem> li = new ArrayList<NetworkItem>();
		li.add(new NetworkItem(NetworkId.VMOBIL, "VMOBIL", "Liechtenstein, Vorarlberg, Bregenz"));
		listNetwork.put("Liechtenstein", li);

		listRegion.add("Schweiz");
		List<NetworkItem> ch = new ArrayList<NetworkItem>();
//		ch.add(new NetworkItem(NetworkId.SBB, "SBB", ""));
		ch.add(new NetworkItem(NetworkId.BVB, "BVB", ""));
		ch.add(new NetworkItem(NetworkId.VBL, "VBL", "Luzern"));
		ch.add(new NetworkItem(NetworkId.ZVV, "ZVL", "Zürich"));
		listNetwork.put("Schweiz", ch);

		listRegion.add("Belgique");
		List<NetworkItem> be = new ArrayList<NetworkItem>();
		be.add(new NetworkItem(NetworkId.SNCB, "SNCB", ""));
		listNetwork.put("Belgique", be);

		listRegion.add("Lëtzebuerg");
		List<NetworkItem> lu = new ArrayList<NetworkItem>();
		lu.add(new NetworkItem(NetworkId.LU, "LU", ""));
		listNetwork.put("Lëtzebuerg", lu);

		listRegion.add("Nederland");
		List<NetworkItem> nl = new ArrayList<NetworkItem>();
		nl.add(new NetworkItem(NetworkId.NS, "NS", "Nederland, Amsterdam", true));
		listNetwork.put("Nederland", nl);

		listRegion.add("Danmark");
		List<NetworkItem> dk = new ArrayList<NetworkItem>();
		dk.add(new NetworkItem(NetworkId.DSB, "DSB", "Danmark, København"));
		listNetwork.put("Danmark", dk);

		listRegion.add("Sverige");
		List<NetworkItem> sv = new ArrayList<NetworkItem>();
		sv.add(new NetworkItem(NetworkId.SE, "SE", "Sverige, Stockholm"));
		sv.add(new NetworkItem(NetworkId.STOCKHOLM, "STOCKHOLM", "Stockholm", true));
		listNetwork.put("Sverige", sv);

		listRegion.add("Norge");
		List<NetworkItem> no = new ArrayList<NetworkItem>();
		no.add(new NetworkItem(NetworkId.NRI, "NRI", "Norge, Oslo, Bergen"));
		listNetwork.put("Norge", no);

		listRegion.add("Great Britan");
		List<NetworkItem> gb = new ArrayList<NetworkItem>();
		gb.add(new NetworkItem(NetworkId.TFL, "TFL", "London & Greater Area"));
		gb.add(new NetworkItem(NetworkId.TLEM, "TLEM", "Great Britan", true));
		gb.add(new NetworkItem(NetworkId.TLWM, "TLWM", "Birmingham", true));
		gb.add(new NetworkItem(NetworkId.TLSW, "TLSW", "Somerset, Gloucestershire, Wiltshire, Dorset, Devon, Cornwall, West Devon, Stowford, Eastleigh, Swindon, Gloucester, Plymouth, Torbay, Bournemouth, Poole", true));
		listNetwork.put("Great Britan", gb);

		listRegion.add("Ireland");
		List<NetworkItem> ie = new ArrayList<NetworkItem>();
		ie.add(new NetworkItem(NetworkId.TFI, "TFI", "Ireland, Dublin"));
//		ie.add(new NetworkItem(NetworkId.EIREANN, "EIREANN", "Bus Éireann"));
		listNetwork.put("Ireland", ie);

		listRegion.add("Italia");
		List<NetworkItem> it = new ArrayList<NetworkItem>();
		it.add(new NetworkItem(NetworkId.ATC, "ATC", "Bologna", true));
		listNetwork.put("Italia", it);

		listRegion.add("Polska");
		List<NetworkItem> pl = new ArrayList<NetworkItem>();
		pl.add(new NetworkItem(NetworkId.PL, "PL", "Polska, Warszawa"));
		listNetwork.put("Polska", pl);

		listRegion.add("United Arab Emirates");
		List<NetworkItem> ae = new ArrayList<NetworkItem>();
		ae.add(new NetworkItem(NetworkId.DUB, "DUB", "United Arab Emirates, Dubai", true));
		listNetwork.put("United Arab Emirates", ae);

		listRegion.add("United States of America");
		List<NetworkItem> us = new ArrayList<NetworkItem>();
		us.add(new NetworkItem(NetworkId.SF, "SF", "California, San Francisco"));
		us.add(new NetworkItem(NetworkId.SEPTA, "SEPTA", "Pennsylvania, Philadelphia", true));
		listNetwork.put("United States of America", us);

		listRegion.add("Australia");
		List<NetworkItem> au = new ArrayList<NetworkItem>();
		au.add(new NetworkItem(NetworkId.SYDNEY, "SYDNEY", "New South Wales, Sydney", true));
		au.add(new NetworkItem(NetworkId.MET, "MET", "Victoria, Melbourne", true));
		listNetwork.put("Australia", au);

		listRegion.add("Israel");
		List<NetworkItem> il = new ArrayList<NetworkItem>();
		il.add(new NetworkItem(NetworkId.JET, "JET", "Jerusalem", true));
		listNetwork.put("Israel", il);

		listRegion.add("France");
		List<NetworkItem> fr = new ArrayList<NetworkItem>();
		fr.add(new NetworkItem(NetworkId.PACA, "PACA", "Provence-Alpes-Côte d'Azur", true));
		listNetwork.put("France", fr);
	}


}
