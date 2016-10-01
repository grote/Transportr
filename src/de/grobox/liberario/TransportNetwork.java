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

package de.grobox.liberario;

import android.content.Context;
import android.support.annotation.Nullable;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.NetworkProvider;

public class TransportNetwork {
	private NetworkProvider network;
	private String name;
	@Nullable private String description;
	@Nullable private String region;
	private Status status = Status.STABLE;
	private int logo = R.drawable.ic_placeholder;
	private int background = R.drawable.account_header_background;
	private boolean goodLineNames = false;

	public enum Status { ALPHA, BETA, STABLE }

	public TransportNetwork(Context context, NetworkProvider network) {
		this.network = network;

		findLogo(context);
	}

	public TransportNetwork(Context context, NetworkId id) {
		this(context, NetworkProviderFactory.provider(id));
	}

	public TransportNetwork setName(String name) {
		this.name = name;

		return this;
	}

	TransportNetwork setDescription(@Nullable String description) {
		this.description = description;

		return this;
	}

	TransportNetwork setDescription(String description, String networks) {
		this.description = description + "\n(" + networks + ")";

		return this;
	}

	public TransportNetwork setRegion(@Nullable String region) {
		this.region = region;

		return this;
	}

	public TransportNetwork setStatus(@Nullable Status status) {
		this.status = status;

		return this;
	}

	public TransportNetwork setLogo(int logo) {
		this.logo = logo;

		return this;
	}

	public TransportNetwork setBackground(int background) {
		this.background = background;

		return this;
	}

	TransportNetwork setGoodLineNames(boolean hasGoodLineNames) {
		this.goodLineNames = hasGoodLineNames;

		return this;
	}

	public NetworkProvider getNetworkProvider() {
		return network;
	}

	public NetworkId getId() {
		return network.id();
	}

	public String getIdString() {
		return network.id().name();
	}

	public String getName() {
		if(name == null) {
			return getIdString();
		} else {
			return name;
		}
	}

	public @Nullable String getDescription() {
		return description;
	}

	public @Nullable String getRegion() {
		return region;
	}

	public @Nullable Status getStatus() {
		return status;
	}

	public int getLogo() {
		return logo;
	}

	public int getBackground() {
		return background;
	}

	public boolean hasGoodLineNames() {
		return goodLineNames;
	}


	private TransportNetwork findLogo(Context context) {
		int res = context.getResources().getIdentifier("network_" + getIdString().toLowerCase() + "_logo", "drawable", context.getPackageName());

		if(res == 0) {
			this.logo = R.drawable.ic_placeholder;
		} else {
			this.logo = res;
		}

		return this;
	}

}
