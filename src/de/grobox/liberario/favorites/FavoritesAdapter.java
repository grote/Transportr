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

package de.grobox.liberario.favorites;

import android.support.annotation.Nullable;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;

import static android.support.v7.util.SortedList.INVALID_POSITION;
import static de.grobox.liberario.favorites.FavoritesType.HOME;
import static de.grobox.liberario.favorites.FavoritesType.WORK;

@ParametersAreNonnullByDefault
public class FavoritesAdapter extends RecyclerView.Adapter<AbstractFavoritesViewHolder> {

	private SortedList<FavoritesItem> items = new SortedList<>(FavoritesItem.class, new SortedList.Callback<FavoritesItem>(){
		@Override
		public void onInserted(int position, int count) {
			notifyItemRangeInserted(position, count);
		}

		@Override
		public void onChanged(int position, int count) {
			notifyItemRangeChanged(position, count);
		}

		@Override
		public void onMoved(int fromPosition, int toPosition) {
			notifyItemMoved(fromPosition, toPosition);
		}

		@Override
		public void onRemoved(int position, int count) {
			notifyItemRangeRemoved(position, count);
		}

		@Override
		public int compare(FavoritesItem f1, FavoritesItem f2) {
			return f1.compareTo(f2);
		}

		@Override
		public boolean areItemsTheSame(FavoritesItem f1, FavoritesItem f2) {
			return f1.equals(f2);
		}

		@Override
		public boolean areContentsTheSame(FavoritesItem f1, FavoritesItem f2) {
			return f1.equalsAllFields(f2);
		}
	});
	private final FavoriteListener listener;

	public FavoritesAdapter(FavoriteListener listener) {
		this.listener = listener;
	}

	@Override
	public int getItemViewType(int position) {
		FavoritesItem item = items.get(position);
		return item.getType().getValue();
	}

	@Override
	public AbstractFavoritesViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
		if (type == HOME.getValue()) {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_special_favorite, viewGroup, false);
			return new HomeFavoriteViewHolder(v);
		} else if (type == WORK.getValue()) {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_special_favorite, viewGroup, false);
			return new WorkFavoriteViewHolder(v);
		} else {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_favorite, viewGroup, false);
			return new FavoritesViewHolder(v);
		}
	}

	@Override
	public void onBindViewHolder(final AbstractFavoritesViewHolder ui, final int position) {
		ui.onBind(items.get(position), listener);
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	@Nullable
	public FavoritesItem getHome() {
		return getSpecialItem(HOME);
	}

	@Nullable
	public FavoritesItem getWork() {
		return getSpecialItem(WORK);
	}

	@Nullable
	private FavoritesItem getSpecialItem(FavoritesType type) {
		for (int i = 0; i < items.size(); i++) {
			FavoritesItem item = items.get(i);
			if (item.getType() == type) return item;
		}
		return null;
	}

	int findItemPosition(FavoritesItem item) {
		// items.indexOf() doesn't work on multiple (position) changes
		for (int i = 0; i < items.size(); i++) {
			if (item.equals(items.get(i))) return i;
		}
		return INVALID_POSITION;
	}

	void addAll(Collection<FavoritesItem> favorites) {
		items.addAll(favorites);
	}

	void updateItem(int position, FavoritesItem item) {
		items.updateItemAt(position, item);
	}

	void clear() {
		items.clear();
	}

}
