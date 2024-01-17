/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2024 Torsten Grote
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

package de.grobox.transportr.utils

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat


class FullScreenUtil {
    companion object {
        fun drawBehindStatusbar(activity: Activity) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, false)
            // required for the mail view, which cant properly use the theme.
            activity.window.statusBarColor = Color.TRANSPARENT
        }

        fun showStatusbar(activity: Activity) {
            WindowCompat.setDecorFitsSystemWindows(activity.window, true)
            // required for the mail view, which cant properly use the theme.
            //activity.window.statusBarColor = Color.TRANSPARENT
        }

        fun applyTopInset(view: View?) {
            if(view == null) {
                return
            }

            view.setOnApplyWindowInsetsListener { v, insets ->
                val originalOffset = (8 * (view.resources.displayMetrics.densityDpi / 160f)).toInt()
                val top = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                } else {
                    insets.systemWindowInsetTop
                }

                val parameters = CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                    CoordinatorLayout.LayoutParams.WRAP_CONTENT
                )
                parameters.setMargins(0, top + originalOffset, 0, 0)
                v.layoutParams = parameters

                return@setOnApplyWindowInsetsListener insets
            }
        }
    }
}