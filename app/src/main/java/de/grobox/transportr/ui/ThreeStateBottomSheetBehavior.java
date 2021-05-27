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

package de.grobox.transportr.ui;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import de.grobox.transportr.R;


public class ThreeStateBottomSheetBehavior<V extends View> extends BottomSheetBehavior<V> {

	private int screenHeight = 0;
	private final int actionBarHeight;
	private float currentY = 0;

	public ThreeStateBottomSheetBehavior(Context context, AttributeSet attrs) {
		super(context, attrs);
		actionBarHeight = context.getResources().getDimensionPixelSize(R.dimen.mapPadding);

		if (context instanceof Activity) {
			DisplayMetrics metrics = new DisplayMetrics();
			((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
			setScreenHeight(metrics.heightPixels);
		}
	}

	private void setScreenHeight(int height) {
		screenHeight = height - actionBarHeight;
	}

	@Override
	public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull V child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
		super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
		if (screenHeight == 0) {
			setScreenHeight(coordinatorLayout.getHeight());
		}

		float lastY = currentY;
		currentY = child.getY();
		boolean down = currentY > lastY;

		float middle = screenHeight / 2;

		if (down) {
			if (currentY >= screenHeight) {
				setState(STATE_COLLAPSED);
			} else if (currentY > middle) {
				setHideable(false);
				setBottom();
			} else {
				setHideable(true);
				setMiddle();
			}
		} else {
			if (currentY > middle) {
				setMiddle();
			} else {
				setHideable(false);
				setMiddle();
			}
		}
	}

	public void setMiddle() {
		setNewPeekHeight(screenHeight / 2);
	}

	public boolean isMiddle() {
		return getState() == STATE_COLLAPSED && getPeekHeight() == screenHeight / 2;
	}

	public void setBottom() {
		setNewPeekHeight(actionBarHeight);
	}

	public boolean isBottom() {
		return getState() == STATE_COLLAPSED && getPeekHeight() == actionBarHeight;
	}

	private void setNewPeekHeight(int height) {
		if (getPeekHeight() != height) {
			setPeekHeight(height);
		}
	}

	@SuppressWarnings("unchecked")
	public static <V extends View> ThreeStateBottomSheetBehavior<V> from(V view) {
		ViewGroup.LayoutParams params = view.getLayoutParams();
		if (!(params instanceof CoordinatorLayout.LayoutParams)) {
			throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
		}
		CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
		if (!(behavior instanceof ThreeStateBottomSheetBehavior)) {
			throw new IllegalArgumentException("The view is not associated with TestBottomSheetBehavior");
		}
		return (ThreeStateBottomSheetBehavior<V>) behavior;
	}

}
