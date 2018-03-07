/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.networks;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import de.schildbach.pte.NetworkId;
import de.schildbach.pte.NetworkProvider;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
@ParametersAreNonnullByDefault
public class TransportNetwork implements Region {

	private final NetworkId id;
	private final ParentRegion region;
	private final @StringRes int name;
	private final @StringRes int description;
	private final @StringRes int agencies;
	private final Status status;
	private final @DrawableRes int logo;
	private final boolean goodLineNames;

	public enum Status {ALPHA, BETA, STABLE}

	TransportNetwork(NetworkId id, ParentRegion region, int name, int description, int agencies, Status status, int logo, boolean goodLineNames) {
		checkArgument(description != 0 || agencies != 0);
		this.id = id;
		this.region = region;
		this.name = name;
		this.description = description;
		this.agencies = agencies;
		this.status = status;
		this.logo = logo;
		this.goodLineNames = goodLineNames;
		this.region.addSubRegion(this);
	}

	public NetworkId getId() {
		return id;
	}

	public NetworkProvider getNetworkProvider() {
		return NetworkProviderFactory.provider(id);
	}

	public Region getRegion() {
		return region;
	}

	@NonNull
	@Override
	public String getName(Context context) {
		if (name == 0) {
			return id.name();
		} else {
			return context.getString(name);
		}
	}

	@Nullable
	public String getDescription(Context context) {
		if (description != 0 && agencies != 0) {
			return context.getString(description) + " (" + context.getString(agencies) + ")";
		} else if (description != 0) {
			return context.getString(description);
		} else if (agencies == 0) {
			return context.getString(agencies);
		} else {
			throw new IllegalArgumentException();
		}
	}

	public Status getStatus() {
		return status;
	}

	@DrawableRes
	public int getLogo() {
		return logo;
	}

	public boolean hasGoodLineNames() {
		return goodLineNames;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof TransportNetwork) {
			TransportNetwork network = (TransportNetwork) o;
			return this.id.equals(network.id);
		}
		return super.equals(o);
	}

}
