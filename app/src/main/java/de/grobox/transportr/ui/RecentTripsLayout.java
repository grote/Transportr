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

package de.grobox.transportr.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;

import de.grobox.transportr.R;

public class RecentTripsLayout extends LinearLayout implements Checkable {
	private CheckBox checkbox;

	public RecentTripsLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		checkbox = findViewById(R.id.checkBox);
	}

	@Override
	public boolean isChecked() {
		return checkbox != null && checkbox.isChecked();
	}

	@Override
	public void setChecked(boolean checked) {
		if(checkbox != null) {
			checkbox.setChecked(checked);
		}
	}

	@Override
	public void toggle() {
		if(checkbox != null) {
			checkbox.toggle();
		}
	}
}