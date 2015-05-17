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

package de.grobox.liberario.adapters;

import java.util.HashMap;
import java.util.List;

import de.grobox.liberario.TransportNetwork;
import de.grobox.liberario.R;
import de.schildbach.pte.NetworkId;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NetworkProviderListAdapter extends BaseExpandableListAdapter {

	private Context _context;
	private List<String> _listRegion;
	private HashMap<String, List<TransportNetwork>> _listNetwork;

	public NetworkProviderListAdapter(Context context, List<String> listRegion,	HashMap<String, List<TransportNetwork>> listNetwork) {
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
		List<TransportNetwork> networks = this._listNetwork.get(region);
		if(networks != null) {
			for(final TransportNetwork network : networks) {
				if(network.getId().equals(network_id)) {
					return networks.indexOf(network);
				}
			}
		}
		return -1;
	}

	@Override
	public View getChildView(int groupPos, final int childPos, boolean isLastChild, View convertView, ViewGroup parent) {
		TransportNetwork network = ((TransportNetwork) getChild(groupPos, childPos));

		LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		convertView = infalInflater.inflate(R.layout.network_provider_item, null);

		((TextView) convertView.findViewById(R.id.networkName)).setText(network.getName());
		((TextView) convertView.findViewById(R.id.networkDescription)).setText(network.getDescription());

		// set alpha/beta state of transport network
		TextView beta = (TextView) convertView.findViewById(R.id.networkBeta);
		if(network.getStatus() == TransportNetwork.Status.STABLE) {
			beta.setVisibility(View.GONE);
		} else {
			beta.setVisibility(View.VISIBLE);
			if(network.getStatus() == TransportNetwork.Status.APLHA) {
				beta.setText(_context.getString(R.string.alpha));
			}
		}

		// set logo of provider if we have one
		if(network.getLogo() != 0) {
			((ImageView) convertView.findViewById(R.id.icon)).setImageDrawable(_context.getResources().getDrawable(network.getLogo()));
		}

		// remember region name of this child
		convertView.setTag(getGroup(groupPos));

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPos) {
		List<TransportNetwork> region = this._listNetwork.get(this._listRegion.get(groupPos));

		if(region != null) {
			return region.size();
		} else {
			// should not happen, but a region might accidentally be empty
			return 0;
		}
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

		((TextView) convertView.findViewById(R.id.regionName)).setText(getGroup(groupPosition));

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
