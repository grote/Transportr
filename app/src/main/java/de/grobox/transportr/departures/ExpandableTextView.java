/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

package de.grobox.transportr.departures;

/**
 * Created by anjan on 06/05/18.
 * From: https://stackoverflow.com/questions/15627530/android-expandable-textview-with-animation
 * Used to make long delay messages expandable
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.view.View;

import de.grobox.transportr.R;


public class ExpandableTextView extends AppCompatTextView {
	private static final int DEFAULT_TRIM_LENGTH = 200;
	private static final String ELLIPSIS = ".....";

	private CharSequence originalText;
	private CharSequence trimmedText;
	private BufferType bufferType;
	private boolean trim = true;
	private int trimLength;

	public ExpandableTextView(Context context) {
		this(context, null);
	}

	public ExpandableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
		this.trimLength = typedArray.getInt(R.styleable.ExpandableTextView_trimLength, DEFAULT_TRIM_LENGTH);
		typedArray.recycle();

		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				trim = !trim;
				setText();
				requestFocusFromTouch();
			}
		});
	}

	private void setText() {
		super.setText(getDisplayableText(), bufferType);
	}

	private CharSequence getDisplayableText() {
		return trim ? trimmedText : originalText;
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		originalText = text;
		trimmedText = getTrimmedText(text);
		bufferType = type;
		setText();
	}

	private CharSequence getTrimmedText(CharSequence text) {
		if (originalText != null && originalText.length() > trimLength) {
			return new SpannableStringBuilder(originalText, 0, trimLength + 1).append(ELLIPSIS);
		} else {
			return originalText;
		}
	}

	public CharSequence getOriginalText() {
		return originalText;
	}

	public void setTrimLength(int trimLength) {
		this.trimLength = trimLength;
		trimmedText = getTrimmedText(originalText);
		setText();
	}

	public int getTrimLength() {
		return trimLength;
	}
}