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

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.liberario.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.networks.TransportNetwork.Status.ALPHA;
import static de.grobox.liberario.networks.TransportNetwork.Status.STABLE;

class TransportNetworkViewHolder extends RecyclerView.ViewHolder {

	private final ImageView logo;
	private final TextView name;
	private final TextView desc;
	private final TextView status;

	TransportNetworkViewHolder(View v) {
		super(v);
		logo = (ImageView) v.findViewById(R.id.logo);
		name = (TextView) v.findViewById(R.id.name);
		desc = (TextView) v.findViewById(R.id.desc);
		status = (TextView) v.findViewById(R.id.status);
	}

	void bind(TransportNetwork network) {
		logo.setImageResource(network.getLogo());
		name.setText(network.getName(name.getContext()));
		desc.setText(network.getDescription(desc.getContext()));
		if (network.getStatus() == STABLE) {
			status.setVisibility(GONE);
		} else {
			if (network.getStatus() == ALPHA) {
				status.setText(status.getContext().getString(R.string.alpha));
			} else {
				status.setText(status.getContext().getString(R.string.beta));
			}
			status.setVisibility(VISIBLE);
		}
	}

}
