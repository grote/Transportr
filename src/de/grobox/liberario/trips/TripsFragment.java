package de.grobox.liberario.trips;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;

import de.grobox.liberario.R;
import de.grobox.liberario.favorites.FavoritesFragment;

import static com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTH;
import static com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection.BOTTOM;
import static de.grobox.liberario.utils.TransportrUtils.getDragDistance;

public class TripsFragment extends FavoritesFragment implements SwipyRefreshLayout.OnRefreshListener { // TODO extends TransportrFragment

	public static TripsFragment newInstance() {
		return new TripsFragment();
	}

	private SwipyRefreshLayout swipe;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		swipe = (SwipyRefreshLayout) v.findViewById(R.id.swipe);
		swipe.setDistanceToTriggerSync(getDragDistance(getContext()));
		swipe.setOnRefreshListener(this);

		return v;
	}

	public void setSwipeEnabled(boolean enabled) {
		swipe.setDirection(enabled ? BOTH : BOTTOM);
	}

	@Override
	public void onRefresh(SwipyRefreshLayoutDirection direction) {
		// TODO
		swipe.setRefreshing(false);
	}

}
