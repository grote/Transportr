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

package de.grobox.transportr.ui

import android.content.Context
import android.graphics.Color
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.cketti.library.changelog.ChangeLog
import de.grobox.transportr.R
import de.grobox.transportr.settings.SettingsManager


class TransportrChangeLog(context: Context, settingsManager: SettingsManager) : ChangeLog(
    context,
    settingsManager.settings,
    theme(settingsManager.isDarkTheme)
) {
    fun getMaterialDialog(full: Boolean): AlertDialog {
        val wv = WebView(mContext)
        wv.setBackgroundColor(Color.TRANSPARENT)
        wv.loadDataWithBaseURL(null, getLog(full), "text/html", "UTF-8", null)

        val builder = MaterialAlertDialogBuilder(mContext)
        builder.setTitle(
            mContext.resources.getString(
                if (full) R.string.changelog_full_title else R.string.changelog_title
            )
        )
            .setView(wv)
            .setCancelable(false)
            .setPositiveButton(
                mContext.resources.getString(R.string.changelog_ok_button)
            ) { _, _ ->
                updateVersionInPreferences()
            }

        if (!full) {
            builder.setNegativeButton(
                R.string.changelog_show_full
            ) { _, _ -> getMaterialDialog(true) }
        }

        return builder.create()
    }

    companion object {

        private fun theme(dark: Boolean): String {
            return if (dark) {
                // material dark
                "body { color: #f3f3f3; font-size: 0.9em; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }"
            } else {
                // material light
                "body { color: #202020; font-size: 0.9em; } h1 { font-size: 1.3em; } ul { padding-left: 2em; }"
            }
        }

    }

}
