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
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.view.View;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;

@ParametersAreNonnullByDefault
class CountryItem extends ParentRegionItem<ContinentItem, CountryViewHolder, TransportNetworkItem> {

	protected final Country country;

	CountryItem(Country country) {
		super();
		this.country = country;
	}

	@Override
	protected String getName(Context context) {
		return this.country.getName(context);
	}

	@IdRes
	@Override
	public int getType() {
		return R.id.list_item_transport_country;
	}

	@Override
	@LayoutRes
	public int getLayoutRes() {
		return R.layout.list_item_transport_country;
	}

	@Override
	public void bindView(CountryViewHolder ui, List<Object> payloads) {
		super.bindView(ui, payloads);
		ui.bind(country, isExpanded());
	}

	@Override
	public CountryViewHolder getViewHolder(View view) {
		return new CountryViewHolder(view);
	}

	@Override
	public long getIdentifier() {
		return country.getName();
	}

}
