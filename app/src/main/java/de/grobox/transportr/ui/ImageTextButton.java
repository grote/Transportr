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

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;

import de.grobox.transportr.R;

public class ImageTextButton extends AppCompatButton {

	public ImageTextButton(Context context) {
		this(context, null);
	}

	public ImageTextButton(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.buttonStyle);
	}

	public ImageTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		Drawable drawable = getCompoundDrawables()[1];
		drawable.setColorFilter(getCurrentTextColor(), PorterDuff.Mode.SRC_IN);
		setCompoundDrawables(null, drawable, null, null);
	}

}
