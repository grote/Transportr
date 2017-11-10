/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2017 Torsten Grote
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
import android.os.Build;
import android.view.ContextThemeWrapper;

import de.cketti.library.changelog.ChangeLog;
import de.grobox.transportr.R;

public class TransportrChangeLog extends ChangeLog {

	public final static String TAG = TransportrChangeLog.class.getName();

	public TransportrChangeLog(Context context, boolean dark) {
		super(new ContextThemeWrapper(context, getDialogTheme(dark)), theme(dark));
	}

	private static int getDialogTheme(boolean dark) {
		if (dark) {
			return R.style.DialogTheme;
		} else {
			return R.style.DialogTheme_Light;
		}
	}

	private static String theme(boolean dark) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			if (dark) {
				// holo dark
				return "body { color: #e7e3e7; font-size: 0.9em; background-color: #292829; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			} else {
				// holo light
				return "body { color: #212421; font-size: 0.9em; background-color: #f7f7f7; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			}
		} else {
			if (dark) {
				// material dark
				return "body { color: #f3f3f3; font-size: 0.9em; background-color: #424242; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			} else {
				// material light
				return "body { color: #202020; font-size: 0.9em; background-color: transparent; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }";
			}
		}
	}

}
