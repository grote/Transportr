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

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;
import de.grobox.liberario.networks.TransportNetwork.Status;
import de.schildbach.pte.NetworkId;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@ParametersAreNonnullByDefault
class TransportNetworkBuilder {

	private NetworkId id;
	private Region region;
	private @StringRes int name;
	private @StringRes int description;
	private @StringRes int agencies;
	private Status status = Status.ALPHA;
	private @DrawableRes int logo = R.drawable.ic_placeholder;
	private boolean goodLineNames = false;

	TransportNetworkBuilder() {
	}

	TransportNetwork build() {
		checkNotNull(id);
		checkNotNull(region);
		checkArgument(description != 0 || agencies != 0);
		return new TransportNetwork(id, region, name, description, agencies, status, logo, goodLineNames);
	}

	TransportNetworkBuilder setId(NetworkId id) {
		this.id = id;
		return this;
	}

	TransportNetworkBuilder setRegion(Region region) {
		this.region = region;
		return this;
	}

	TransportNetworkBuilder setName(@StringRes int name) {
		this.name = name;
		return this;
	}

	TransportNetworkBuilder setDescription(@StringRes int description) {
		this.description = description;
		return this;
	}

	TransportNetworkBuilder setAgencies(@StringRes int agencies) {
		this.agencies = agencies;
		return this;
	}

	TransportNetworkBuilder setStatus(Status status) {
		this.status = status;
		return this;
	}

	TransportNetworkBuilder setLogo(@DrawableRes int logo) {
		this.logo = logo;
		return this;
	}

	TransportNetworkBuilder setGoodLineNames(boolean hasGoodLineNames) {
		this.goodLineNames = hasGoodLineNames;
		return this;
	}

}
