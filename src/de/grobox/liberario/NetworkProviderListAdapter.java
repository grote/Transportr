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

import java.util.HashMap;
import java.util.List;

import de.schildbach.pte.NetworkId;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class NetworkProviderListAdapter extends BaseExpandableListAdapter {

	private Context _context;
	private List<String> _listRegion;
	private HashMap<String, List<NetworkItem>> _listNetwork;

	public NetworkProviderListAdapter(Context context, List<String> listRegion,	HashMap<String, List<NetworkItem>> listNetwork) {
		this._context = context;
		this._listRegion = listRegion;
		this._listNetwork = listNetwork;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this._listNetwork.get(this._listRegion.get(groupPosition)).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public int getChildPos(String region, NetworkId network_id) {
		List<NetworkItem> networks = this._listNetwork.get(region);
		if(networks != null) {
			for(final NetworkItem network : networks) {
				if(network.id.equals(network_id)) {
					return networks.indexOf(network);
				}
			}
		}
		return -1;
	}

	@Override
	public View getChildView(int groupPos, final int childPos, boolean isLastChild, View convertView, ViewGroup parent) {

		NetworkItem network = ((NetworkItem) getChild(groupPos, childPos));

		if(convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.network_provider_item, null);
		}

		((TextView) convertView.findViewById(R.id.networkName)).setText(network.name);
		((TextView) convertView.findViewById(R.id.networkDescription)).setText(network.description);
		((TextView) convertView.findViewById(R.id.networkBeta)).setVisibility(network.beta ? View.VISIBLE : View.GONE);

		// remember region name of this child
		convertView.setTag(getGroup(groupPos));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPos) {
		return this._listNetwork.get(this._listRegion.get(groupPos)).size();
	}

	@Override
	public String getGroup(int groupPos) {
		return this._listRegion.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		return this._listRegion.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		return groupPos;
	}

	public int getGroupPos(String group) {
		return this._listRegion.indexOf(group);
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,	View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.network_provider_group, null);
		}

		((TextView) convertView.findViewById(R.id.regionName)).setText((String) getGroup(groupPosition));

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPos, int childPos) {
		return true;
	}
}
