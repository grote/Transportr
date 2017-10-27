package de.grobox.transportr.ui;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
	public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed) {
		super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);

		if (screenHeight == 0) {
			setScreenHeight(coordinatorLayout.getHeight());
		}

		float lastY = currentY;
		currentY = child.getY();
		boolean down = currentY > lastY;

		float middle = screenHeight / 2;

//		Log.e("TEST", "CURRENT Y: " + currentY);

		if (down) {
			if (currentY >= screenHeight) {
				setState(STATE_COLLAPSED);
//				Log.e("TEST", "DOWN - AT THE END");
			} else if (currentY > middle) {
				setHideable(false);
				setBottom();
//				Log.e("TEST", "DOWN - BOTTOM HALF");
			} else {
				setHideable(true);
				setMiddle();
//				Log.e("TEST", "DOWN - BOTTOM TOP");
			}
		} else {
			if (currentY > middle) {
				setMiddle();
//				Log.e("TEST", "UP - BOTTOM HALF");
			} else {
				setHideable(false);
				setMiddle();
//				Log.e("TEST", "UP - TOP HALF");
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

	@Override
	public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
//		Log.e("TEST", event.toString());

		return super.onTouchEvent(parent, child, event);
	}

	@Override
	public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, V child, View directTargetChild, View target, int nestedScrollAxes) {
//		Log.e("TEST", "ON START SCROLL");
//		Log.e("TEST", "getY: " + child.getY());

//		if (child.getY() > screenHeight) setMiddle();

		return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
	}

	@Override
	public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
//		Log.e("TEST", "INTERCEPT TOUCH: " + event.toString());
		return super.onInterceptTouchEvent(parent, child, event);
	}

	@Override
	public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
//		Log.e("TEST", "ON STOP SCROLL");
		super.onStopNestedScroll(coordinatorLayout, child, target);
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
