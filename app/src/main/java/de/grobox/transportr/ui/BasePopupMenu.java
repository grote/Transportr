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
import androidx.annotation.ColorInt;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import de.grobox.transportr.R;

public abstract class BasePopupMenu extends PopupMenu implements OnMenuItemClickListener {

	protected final Context context;
	@ColorInt
	protected final int iconColor;

	protected BasePopupMenu(Context context, View anchor) {
		super(context, anchor);

		this.context = context;
		setOnMenuItemClickListener(this);

		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(R.attr.colorControlNormal, typedValue, true);
		iconColor = typedValue.data;
	}

	protected void showIcons() {
		// very ugly hack to show icons in PopupMenu
		// see: http://stackoverflow.com/a/18431605
		try {
			Field[] fields = BasePopupMenu.class.getSuperclass().getDeclaredFields();
			for (Field field : fields) {
				if ("mPopup".equals(field.getName())) {
					field.setAccessible(true);
					Object menuPopupHelper = field.get(this);
					Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
					Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
					setForceIcons.invoke(menuPopupHelper, true);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// colorize icons according to theme
		for (int i = 0; i < getMenu().size(); i++) {
			DrawableCompat.setTint(getMenu().getItem(i).getIcon().mutate(), iconColor);
		}
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		return false;
	}

}
