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

package de.grobox.liberario.adapters;

import android.content.Context;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import de.grobox.liberario.R;
import de.grobox.liberario.RecentTrip;
import de.grobox.liberario.ui.RecentsPopupMenu;
import de.grobox.liberario.utils.TransportrUtils;

public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.FavouriteHolder>{

	private SortedList<RecentTrip> favourites = new SortedList<>(RecentTrip.class, new SortedList.Callback<RecentTrip>(){
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
		public int compare(RecentTrip f1, RecentTrip f2) {
			return f1.getCount() < f2.getCount() ? +1 : f1.getCount() > f2.getCount() ? -1 : 0;
		}

		@Override
		public boolean areItemsTheSame(RecentTrip f1, RecentTrip f2) {
			return f1.equals(f2);
		}

		@Override
		public boolean areContentsTheSame(RecentTrip f_old, RecentTrip f_new) {
			// return whether the favourites' visual representations are the same or not
			return f_old.equals(f_new);
		}
	});
	private Context context;

	public FavouritesAdapter(Context context) {
		this.context = context;
	}

	@Override
	public FavouriteHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
		View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.favourite_trip_list_item, viewGroup, false);

		return new FavouriteHolder(v);
	}

	@Override
	public void onBindViewHolder(final FavouriteHolder ui, final int position) {
		final RecentTrip fav = getItem(position);

		ui.favFrom.setText(TransportrUtils.getLocName(fav.getFrom()));
		ui.favTo.setText(TransportrUtils.getLocName(fav.getTo()));

		if(fav.getVia() != null) {
			ui.favVia.setText(TransportrUtils.getLocName(fav.getVia()));
			ui.favVia.setVisibility(View.VISIBLE);
		} else {
			ui.favVia.setVisibility(View.GONE);
		}

		ui.root.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			   TransportrUtils.findDirections(context, fav.getFrom(), fav.getVia(), fav.getTo());
			}
		});

		final RecentsPopupMenu favPopup = new RecentsPopupMenu(context, ui.moreButton, fav);
		favPopup.setRemovedListener(new RecentsPopupMenu.FavouriteRemovedListener() {
			@Override
			public void onFavouriteRemoved() {
				favourites.remove(fav);
			}
		});

		ui.moreButton.setOnClickListener(new View.OnClickListener() {
			  @Override
			  public void onClick(View v) {
				  favPopup.show();
			  }
		});
		ui.moreButton.setImageDrawable(TransportrUtils.getTintedDrawable(context, R.drawable.ic_more_vert));
	}

	@Override
	public int getItemCount() {
		return favourites == null ? 0 : favourites.size();
	}

	public RecentTrip getItem(int position) {
		return favourites.get(position);
	}

	public void addAll(final List<RecentTrip> favourites) {
		this.favourites.addAll(favourites);
	}

	public void clear() {
		this.favourites.beginBatchedUpdates();

		while(favourites.size() != 0) {
			favourites.removeItemAt(0);
		}

		this.favourites.endBatchedUpdates();
	}

	public static class FavouriteHolder extends RecyclerView.ViewHolder {
		public TextView favFrom;
		public TextView favVia;
		public TextView favTo;
		public ImageView moreButton;
		public View root;

		public FavouriteHolder(View v) {
			super(v);
			root = v;
			favFrom = (TextView) v.findViewById(R.id.favouriteFromView);
			favVia = (TextView) v.findViewById(R.id.favouriteViaView);
			favTo = (TextView) v.findViewById(R.id.favouriteToView);
			moreButton = (ImageView) v.findViewById(R.id.moreButton);
		}
	}
}
