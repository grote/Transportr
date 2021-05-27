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

package de.grobox.transportr.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatDelegate.*
import de.grobox.transportr.R
import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider.Optimize
import de.schildbach.pte.NetworkProvider.WalkSpeed
import java.util.*
import javax.inject.Inject


class SettingsManager @Inject constructor(private val context: Context) {

    val settings: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val locale: Locale
        get() {
            val default = context.getString(R.string.pref_language_value_default)
            val str = settings.getString(LANGUAGE, default) ?: default
            return when {
                str == default -> Resources.getSystem().configuration.locale
                str.contains("_") -> {
                    val langArray = str.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    Locale(langArray[0], langArray[1])
                }
                else -> Locale(str)
            }
        }

    val theme: Int
        get() {
            val dark = context.getString(R.string.pref_theme_value_dark)
            val light = context.getString(R.string.pref_theme_value_light)
            val auto = context.getString(R.string.pref_theme_value_auto)
            return when (settings.getString(THEME, auto)) {
                dark -> MODE_NIGHT_YES
                light -> MODE_NIGHT_NO
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MODE_NIGHT_FOLLOW_SYSTEM else MODE_NIGHT_AUTO_BATTERY
            }
        }

    val isDarkTheme: Boolean
        get() {
            return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) or
                    (theme == MODE_NIGHT_YES)
        }

    val walkSpeed: WalkSpeed
        get() {
            return try {
                val default = context.getString(R.string.pref_walk_speed_value_default)
                WalkSpeed.valueOf(settings.getString(WALK_SPEED, default) ?: default)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                WalkSpeed.NORMAL
            }
        }

    val optimize: Optimize
        get() {
            return try {
                val default = context.getString(R.string.pref_optimize_value_default)
                Optimize.valueOf(settings.getString(OPTIMIZE, default) ?: default)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Optimize.LEAST_DURATION
            }
        }

    fun showLocationFragmentOnboarding(): Boolean = settings.getBoolean(LOCATION_ONBOARDING, true)
    fun locationFragmentOnboardingShown() {
        settings.edit().putBoolean(LOCATION_ONBOARDING, false).apply()
    }

    fun showTripDetailFragmentOnboarding(): Boolean = settings.getBoolean(TRIP_DETAIL_ONBOARDING, true)
    fun tripDetailOnboardingShown() {
        settings.edit().putBoolean(TRIP_DETAIL_ONBOARDING, false).apply()
    }

    fun getNetworkId(i: Int): NetworkId? {
        var networkSettingsStr = NETWORK_ID_1
        if (i == 2) networkSettingsStr = NETWORK_ID_2
        else if (i == 3) networkSettingsStr = NETWORK_ID_3

        val networkStr = settings.getString(networkSettingsStr, null) ?: return null

        return try {
            NetworkId.valueOf(networkStr)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun setNetworkId(newNetworkId: NetworkId) {
        val networkId1 = settings.getString(NETWORK_ID_1, "")
        if (networkId1 == newNetworkId.name) {
            return  // same network selected
        }
        val networkId2 = settings.getString(NETWORK_ID_2, "")
        val editor = settings.edit()
        if (networkId2 != newNetworkId.name) {
            editor.putString(NETWORK_ID_3, networkId2)
        }
        editor.putString(NETWORK_ID_2, networkId1)
        editor.putString(NETWORK_ID_1, newNetworkId.name)
        editor.apply()
    }

    fun showWhenLocked(): Boolean {
        return settings.getBoolean(SHOW_WHEN_LOCKED, true)
    }

    companion object {
        private const val NETWORK_ID_1 = "NetworkId"
        private const val NETWORK_ID_2 = "NetworkId2"
        private const val NETWORK_ID_3 = "NetworkId3"

        internal const val LANGUAGE = "pref_key_language"
        internal const val THEME = "pref_key_theme"
        private const val SHOW_WHEN_LOCKED = "pref_key_show_when_locked"
        private const val WALK_SPEED = "pref_key_walk_speed"
        private const val OPTIMIZE = "pref_key_optimize"
        private const val LOCATION_ONBOARDING = "locationOnboarding"
        private const val TRIP_DETAIL_ONBOARDING = "tripDetailOnboarding"
    }

}
