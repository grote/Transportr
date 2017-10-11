package de.grobox.liberario.trips;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.ParametersAreNonnullByDefault;

import de.grobox.liberario.R;
import de.grobox.liberario.data.locations.FavoriteLocation;
import de.grobox.liberario.fragments.TimeDateFragment.TimeDateListener;
import de.grobox.liberario.locations.LocationView.LocationViewListener;
import de.grobox.liberario.locations.WrapLocation;
import de.grobox.liberario.utils.DateUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.fragments.DirectionsFragment.TASK_BRING_ME_HOME;
import static de.grobox.liberario.locations.WrapLocation.WrapType.GPS;
import static de.grobox.liberario.utils.Constants.DATE;
import static de.grobox.liberario.utils.Constants.FROM;
import static de.grobox.liberario.utils.Constants.NOW;
import static de.grobox.liberario.utils.Constants.SEARCH;
import static de.grobox.liberario.utils.Constants.TO;
import static de.grobox.liberario.utils.Constants.VIA;
import static de.grobox.liberario.utils.DateUtils.getDate;
import static de.grobox.liberario.utils.DateUtils.getTime;

@ParametersAreNonnullByDefault
class DirectionsPresenter implements LocationViewListener, TimeDateListener {

	private final DirectionsActivity activity;
	private DirectionsViewModel viewModel;

	private boolean now = true;
	private Calendar calendar = Calendar.getInstance();

	DirectionsPresenter(DirectionsActivity activity, DirectionsViewModel viewModel, @Nullable Bundle savedInstanceState) {
		this.activity = activity;
		this.viewModel = viewModel;
		if (savedInstanceState != null) {
			calendar = (Calendar) savedInstanceState.getSerializable(DATE);
			now = savedInstanceState.getBoolean(NOW);
			onTimeAndDateSet(calendar, now, DateUtils.isToday(calendar), false);
		}
	}

	void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(DATE, calendar);
		outState.putBoolean(NOW, now);
	}

	@Override
	public void onLocationItemClick(WrapLocation loc) {
		viewModel.clickLocation(loc, FavoriteLocation.FavLocationType.FROM);
		activity.search();
	}

	@Override
	public void onLocationCleared() {
		if (activity.isShowingTrips()) {
			activity.showFavorites();
		}
	}

	@Override
	public void onTimeAndDateSet(Calendar calendar, boolean isNow, boolean isToday) {
		onTimeAndDateSet(calendar, isNow, isToday, true);
	}

	private void onTimeAndDateSet(Calendar calendar, boolean isNow, boolean isToday, boolean search) {
		this.now = isNow;
		this.calendar = calendar;
		if (isNow) {
			activity.setTimeText(getContext().getString(R.string.now));
			activity.setDateVisibility(GONE);
		} else if (isToday) {
			activity.setTimeText(getTime(getContext(), calendar.getTime()));
			activity.setDateVisibility(GONE);
		} else {
			activity.setTimeText(getTime(getContext(), calendar.getTime()));
			activity.setDateText(getDate(getContext(), calendar.getTime()));
			activity.setDateVisibility(VISIBLE);
		}
		if (search) {
			activity.search();
			// TODO show onboarding on first use for how to reset time
		}
	}

	void resetCalendar() {
		onTimeAndDateSet(Calendar.getInstance(), true, true);
	}

	Calendar getCalendar() {
		if (now) return Calendar.getInstance();
		return calendar;
	}

	void processIntent(@Nullable Intent intent) {
		if (intent == null) return;

		WrapLocation from, via, to;
		boolean search;
		Date date;
		String special = (String) intent.getSerializableExtra("special");
		if (special != null && special.equals(TASK_BRING_ME_HOME)) {
			from = new WrapLocation(GPS);
			to = viewModel.getHome().getValue();
			search = true;
		} else {
			from = (WrapLocation) intent.getSerializableExtra(FROM);
			to = (WrapLocation) intent.getSerializableExtra(TO);
			search = intent.getBooleanExtra(SEARCH, false);
		}
		via = (WrapLocation) intent.getSerializableExtra(VIA);
		date = (Date) intent.getSerializableExtra(DATE);

		if (search) searchFromTo(from, via, to, date);
		else presetFromTo(from, via, to, date);
	}

	private void presetFromTo(@Nullable WrapLocation from, @Nullable WrapLocation via, @Nullable WrapLocation to, @Nullable Date date) {
		if (from != null && from.getWrapType() == GPS) {
			// TODO
//			activateGPS();
			activity.setFromLocation(null);
		} else {
			activity.setFromLocation(from);
		}

		// TODO via

		activity.setToLocation(to);

		// handle date
		if (date != null) {
			// TODO is now?
			now = false;
			calendar.setTime(date);
		}
	}

	private void searchFromTo(WrapLocation from, @Nullable WrapLocation via, WrapLocation to, Date date) {
		presetFromTo(from, via, to, date);
		activity.search();
	}

	private Context getContext() {
		return activity;
	}

}
