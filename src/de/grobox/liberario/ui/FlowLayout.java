/*
 * Copyright 2010 Romain Guy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.grobox.liberario.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class FlowLayout extends ViewGroup {
	private int mHorizontalSpacing;
	private int mVerticalSpacing;
	private Paint mPaint;

	public FlowLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		mHorizontalSpacing = 15;
		mVerticalSpacing = 15;

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xffff0000);
		mPaint.setStrokeWidth(2.0f);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingRight() - 150; // last is dirty hack
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);

		boolean growHeight = widthMode != MeasureSpec.UNSPECIFIED;

		int width = 0;
		int height = getPaddingTop();

		int currentWidth = getPaddingLeft();
		int currentHeight = 0;

		boolean breakLine = false;
		boolean newLine = false;
		int spacing = 0;

		final int count = getChildCount();
		for(int i = 0; i < count; i++) {
			View child = getChildAt(i);
			measureChild(child, widthMeasureSpec, heightMeasureSpec);

			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			spacing = mHorizontalSpacing;

			if(growHeight && (breakLine || currentWidth + child.getMeasuredWidth() > widthSize)) {
				height += currentHeight + mVerticalSpacing;
				currentHeight = 0;
				width = Math.max(width, currentWidth - spacing);
				currentWidth = getPaddingLeft();
				newLine = true;
			} else {
				newLine = false;
			}

			lp.x = currentWidth;
			lp.y = height;

			currentWidth += child.getMeasuredWidth() + spacing;
			currentHeight = Math.max(currentHeight, child.getMeasuredHeight());

			breakLine = lp.breakLine;
		}

		if(!newLine) {
			height += currentHeight;
			width = Math.max(width, currentWidth - spacing);
		}

		width += getPaddingRight();
		height += getPaddingBottom();

		setMeasuredDimension(resolveSize(width, widthMeasureSpec),
				resolveSize(height, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			LayoutParams lp = (LayoutParams) child.getLayoutParams();
			child.layout(lp.x, lp.y, lp.x + child.getMeasuredWidth(), lp.y
					+ child.getMeasuredHeight());
		}
	}

	@Override
	protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
		boolean more = super.drawChild(canvas, child, drawingTime);
		LayoutParams lp = (LayoutParams) child.getLayoutParams();
		if (lp.horizontalSpacing > 0) {
			float x = child.getRight();
			float y = child.getTop() + child.getHeight() / 2.0f;
			canvas.drawLine(x, y - 4.0f, x, y + 4.0f, mPaint);
			canvas.drawLine(x, y, x + lp.horizontalSpacing, y, mPaint);
			canvas.drawLine(x + lp.horizontalSpacing, y - 4.0f, x
					+ lp.horizontalSpacing, y + 4.0f, mPaint);
		}
		if (lp.breakLine) {
			float x = child.getRight();
			float y = child.getTop() + child.getHeight() / 2.0f;
			canvas.drawLine(x, y, x, y + 6.0f, mPaint);
			canvas.drawLine(x, y + 6.0f, x + 6.0f, y + 6.0f, mPaint);
		}
		return more;
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p.width, p.height);
	}

	public static class LayoutParams extends ViewGroup.LayoutParams {
		int x;
		int y;

		public int horizontalSpacing;
		public boolean breakLine;

		public LayoutParams(Context context, AttributeSet attrs) {
			super(context, attrs);

			horizontalSpacing = 15;
			breakLine = true;
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}
	}

}
