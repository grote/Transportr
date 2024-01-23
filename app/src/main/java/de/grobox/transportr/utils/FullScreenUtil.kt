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
import de.grobox.transportr.utils.TransportrUtils.dpToPx


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
            applyTopInset(view, 0, 8, 0 ,0)
        }

        fun applyTopInset(view: View?, margin: Int) {
            if(view == null) {
                return
            }
            applyTopInset(view, margin, margin, margin ,margin)
        }

        fun applyTopInset(view: View?, dpLeft: Int, dpTop: Int, dpRight: Int, dpBottom: Int) {
            if(view == null) {
                return
            }

            view.setOnApplyWindowInsetsListener { v, insets ->
                val originalOffset = dpToPx(view.context, dpTop)
                val top = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                } else {
                    insets.systemWindowInsetTop
                }

                val parameters = CoordinatorLayout.LayoutParams(
                    v.layoutParams.width,
                    v.layoutParams.height
                )
                parameters.setMargins(dpToPx(view.context, dpLeft), top + originalOffset, dpToPx(view.context, dpRight), dpToPx(view.context, dpBottom))
                v.layoutParams = parameters

                return@setOnApplyWindowInsetsListener insets
            }
        }
    }
}