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
import android.support.v4.view.ViewCompat;

import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem;
import com.mikepenz.fastadapter.listeners.OnClickListener;

import java.util.Comparator;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;

@ParametersAreNonnullByDefault
abstract class RegionItem<Parent extends ParentRegionItem, VH extends RegionViewHolder, Child extends RegionItem> extends AbstractExpandableItem<Parent, VH, Child> {

	protected abstract String getName(Context context);

	static class RegionComparator implements Comparator<RegionItem> {

		private final Context context;

		RegionComparator(Context context) {
			super();
			this.context = context;
		}

		@Override
		public int compare(RegionItem r1, RegionItem r2) {
			// sort countries before networks
			if (r1 instanceof TransportNetworkItem && r2 instanceof ParentRegionItem) return 1;
			if (r1 instanceof ParentRegionItem && r2 instanceof TransportNetworkItem) return -1;
			// sort regions alphabetically
			return r1.getName(context).compareTo(r2.getName(context));
		}
	}
}

abstract class ParentRegionItem<Parent extends ParentRegionItem, VH extends RegionViewHolder, Child extends RegionItem> extends RegionItem<Parent, VH, Child> {
	
	@Override
	public OnClickListener<Parent> getOnItemClickListener() {
		return (v, adapter, item, position) -> {
			if (item.getSubItems() != null) {
				if (!item.isExpanded()) {
					List<Child> subItems = item.getSubItems();
					for (Child subItem : subItems) {
						if (subItem.getSubItems() != null && subItem.isExpanded()) {
							subItem.withIsExpanded(false);
							adapter.getFastAdapter().notifyAdapterDataSetChanged();
						}
					}
					ViewCompat.animate(v.findViewById(R.id.chevron)).rotation(180).start();
				} else {
					ViewCompat.animate(v.findViewById(R.id.chevron)).rotation(0).start();
				}
				return true;
			}
			return false;
		};
	}
}
