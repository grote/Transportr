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

package de.grobox.transportr.locations;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

import de.grobox.transportr.R;

public class LocationGpsView extends LocationView {

	private boolean searching = false;

	public LocationGpsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setLocation(WrapLocation loc, int icon, boolean setText) {
		if (setText) clearSearching();
		super.setLocation(loc, icon, setText);
	}

	@Override
	protected void clearLocationAndShowDropDown() {
		clearSearching();
		super.clearLocationAndShowDropDown();
	}

	public void setSearching() {
		if (searching) return;
		searching = true;

		// clear input
		setLocation(null, R.drawable.ic_gps, false);

		// show GPS button blinking
		final Animation animation = new AlphaAnimation(1, 0);
		animation.setDuration(500);
		animation.setInterpolator(new LinearInterpolator());
		animation.setRepeatCount(Animation.INFINITE);
		animation.setRepeatMode(Animation.REVERSE);
		ui.status.startAnimation(animation);
		ui.status.setEnabled(false);

		ui.location.setHint(R.string.stations_searching_position);
		ui.location.setEnabled(false);
		ui.clear.setVisibility(VISIBLE);
	}

	private void clearSearching() {
		ui.status.setEnabled(true);
		ui.status.clearAnimation();

		ui.location.setEnabled(true);
		ui.location.setHint(hint);

		searching = false;
	}

	public boolean isSearching() {
		return searching;
	}

}
