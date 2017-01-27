package de.grobox.liberario.favorites;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
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

import de.grobox.liberario.R;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.fragments.TransportrFragment;
import de.grobox.liberario.ui.LceAnimator;
import de.schildbach.pte.dto.Location;

import static de.grobox.liberario.favorites.FavoritesDatabase.getFavoriteTripList;
import static de.grobox.liberario.favorites.FavoritesType.HOME;
import static de.grobox.liberario.favorites.FavoritesType.WORK;
import static de.grobox.liberario.utils.Constants.LOADER_FAVORITES;

public class FavoritesFragment extends TransportrFragment implements FavoriteListener, LoaderManager.LoaderCallbacks<Collection<FavoritesItem>> {

	public static final String TAG = FavoritesFragment.class.getName();

	public static FavoritesFragment newInstance() {
		return new FavoritesFragment();
	}

	private ProgressBar progressBar;
	private RecyclerView list;
	private FavoritesAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_favorites, container, false);

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
				Location home = RecentsDB.getHome(getContext());
				Location work = null; // TODO
				favorites.add(new FavoritesItem(HOME, home));
				favorites.add(new FavoritesItem(WORK, work));
				return favorites;
			}
		};
	}

	@Override
	public void onLoadFinished(Loader<Collection<FavoritesItem>> loader, Collection<FavoritesItem> favorites) {
		LceAnimator.showContent(progressBar, list, null);
		adapter.addAll(favorites);
	}

	@Override
	public void onLoaderReset(Loader<Collection<FavoritesItem>> loader) {
		adapter.clear();
	}

	@Override
	public void onFavoriteClicked(FavoritesItem item) {
		// TODO
	}

	@Override
	public void onFavoriteRemoved(FavoritesItem item) {
		// TODO
	}

}
