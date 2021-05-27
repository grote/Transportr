/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.favorites.trips;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collection;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.transportr.R;

import static androidx.recyclerview.widget.SortedList.INVALID_POSITION;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.HOME;
import static de.grobox.transportr.favorites.trips.FavoriteTripType.WORK;

@ParametersAreNonnullByDefault
class FavoriteTripAdapter extends RecyclerView.Adapter<AbstractFavoritesViewHolder> {

	private final SortedList<FavoriteTripItem> items = new SortedList<>(FavoriteTripItem.class, new SortedList.Callback<FavoriteTripItem>() {
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
		public int compare(FavoriteTripItem f1, FavoriteTripItem f2) {
			return f1.compareTo(f2);
		}

		@Override
		public boolean areItemsTheSame(FavoriteTripItem f1, FavoriteTripItem f2) {
			return f1.equals(f2);
		}

		@Override
		public boolean areContentsTheSame(FavoriteTripItem f1, FavoriteTripItem f2) {
			return f1.equalsAllFields(f2);
		}
	});
	private final FavoriteTripListener listener;

	FavoriteTripAdapter(FavoriteTripListener listener) {
		this.listener = listener;
	}

	@Override
	public int getItemViewType(int position) {
		FavoriteTripItem item = items.get(position);
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
			return new FavoriteTripViewHolder(v);
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
	public FavoriteTripItem getHome() {
		return getSpecialItem(HOME);
	}

	@Nullable
	public FavoriteTripItem getWork() {
		return getSpecialItem(WORK);
	}

	@Nullable
	private FavoriteTripItem getSpecialItem(FavoriteTripType type) {
		int end = items.size() <= 1 ? items.size() : 2;
		for (int i = 0; i < end; i++) {
			FavoriteTripItem item = items.get(i);
			if (item.getType() == type) return item;
		}
		return null;
	}

	int findItemPosition(FavoriteTripItem item) {
		// items.indexOf() doesn't work on multiple (position) changes
		for (int i = 0; i < items.size(); i++) {
			if (item.equals(items.get(i))) return i;
		}
		return INVALID_POSITION;
	}

	void add(FavoriteTripItem favorite) {
		items.add(favorite);
	}

	void swap(Collection<FavoriteTripItem> favorites) {
		FavoriteTripItem home = getHome();
		FavoriteTripItem work = getWork();

		items.beginBatchedUpdates();
		items.clear();
		if (home != null) items.add(home);
		if (work != null) items.add(work);
		items.addAll(favorites);
		items.endBatchedUpdates();
	}

	void updateItem(int position, FavoriteTripItem item) {
		items.updateItemAt(position, item);
	}

	void remove(FavoriteTripItem item) {
		items.remove(item);
	}

}
