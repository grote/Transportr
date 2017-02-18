package de.grobox.liberario.favorites;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.Collection;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.data.SpecialLocationDb;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.ui.LceAnimator;
import de.schildbach.pte.dto.Location;

import static android.support.v7.util.SortedList.INVALID_POSITION;
import static de.grobox.liberario.data.FavoritesDb.getFavoriteTripList;
import static de.grobox.liberario.favorites.FavoritesType.HOME;
import static de.grobox.liberario.favorites.FavoritesType.TRIP;
import static de.grobox.liberario.favorites.FavoritesType.WORK;
import static de.grobox.liberario.utils.Constants.LOADER_FAVORITES;
import static de.grobox.liberario.utils.TransportrUtils.findDirections;

@ParametersAreNonnullByDefault
public class FavoritesFragment extends TransportrFragment implements FavoriteListener, LoaderCallbacks<Collection<FavoritesItem>> {

	public static final String TAG = FavoritesFragment.class.getName();

	@Inject
	TransportNetworkManager manager;
	private ProgressBar progressBar;
	private RecyclerView list;
	private FavoritesAdapter adapter;

	public static FavoritesFragment newInstance() {
		return new FavoritesFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_favorites, container, false);
		getComponent().inject(this);

		progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

		list = (RecyclerView) v.findViewById(R.id.favorites);
		adapter = new FavoritesAdapter(this);
		list.setAdapter(adapter);
		list.setLayoutManager(new LinearLayoutManager(getContext()));

		LceAnimator.showLoading(progressBar, list, null);

		boolean hasLoader = getLoaderManager().getLoader(LOADER_FAVORITES) != null;
		Loader loader = getLoaderManager().initLoader(LOADER_FAVORITES, null, this);
		if (savedInstanceState == null || !hasLoader) {
			loader.forceLoad();
		}

		return v;
	}

	@Override
	public Loader<Collection<FavoritesItem>> onCreateLoader(int id, Bundle args) {
		return new AsyncTaskLoader<Collection<FavoritesItem>>(getContext()) {
			@Override
			public Collection<FavoritesItem> loadInBackground() {
				List<FavoritesItem> favorites = getFavoriteTripList(getContext());
				Location home = SpecialLocationDb.getHome(getContext());
				Location work = SpecialLocationDb.getWork(getContext());
				favorites.add(new FavoritesItem(HOME, home));
				favorites.add(new FavoritesItem(WORK, work));
				return favorites;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Collection<FavoritesItem>> loader, Collection<FavoritesItem> favorites) {
		LceAnimator.showContent(progressBar, list, null);
		adapter.clear();
		adapter.addAll(favorites);
	}

	@Override
	public void onLoaderReset(Loader<Collection<FavoritesItem>> loader) {
		adapter.clear();
	}

	@Override
	public void onFavoriteClicked(FavoritesItem item) {
		if (item.getType() == HOME) {
			if (manager.getHome() == null) {
				changeHome();
			} else {
				findDirections(getContext(), item.getFrom(), item.getVia(), manager.getHome());
			}
		} else if (item.getType() == WORK) {
			if (manager.getWork() == null) {
				changeWork();
			} else {
				findDirections(getContext(), item.getFrom(), item.getVia(), manager.getWork());
			}
		} else if (item.getType() == TRIP) {
			if (item.getFrom() == null || item.getTo() == null) throw new IllegalArgumentException();
			findDirections(getContext(), item.getFrom(), item.getVia(), item.getTo());
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public void onFavoriteChanged(FavoritesItem item) {
		int position = adapter.findItemPosition(item);
		if (position != INVALID_POSITION) {
			adapter.updateItem(position, item);
		}
	}

	@Override
	public void changeHome() {
		HomePickerDialogFragment f = HomePickerDialogFragment.newInstance();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), HomePickerDialogFragment.TAG);
	}

	@Override
	public void changeWork() {
		WorkPickerDialogFragment f = WorkPickerDialogFragment.newInstance();
		f.setListener(this);
		f.show(getActivity().getSupportFragmentManager(), WorkPickerDialogFragment.TAG);
	}

	@Override
	public void onHomeChanged(Location home) {
		onSpecialLocationChanged(adapter.getHome(), new FavoritesItem(HOME, home));
	}

	@Override
	public void onWorkChanged(Location work) {
		onSpecialLocationChanged(adapter.getWork(), new FavoritesItem(WORK, work));
	}

	private void onSpecialLocationChanged(@Nullable FavoritesItem oldItem, FavoritesItem newItem) {
		if (oldItem == null) return;
		int position = adapter.findItemPosition(oldItem);
		if (position == INVALID_POSITION) return;

		View view = list.findViewHolderForAdapterPosition(position).itemView;
		ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.getWidth(), 0).start();

		adapter.updateItem(position, newItem);
	}

}
