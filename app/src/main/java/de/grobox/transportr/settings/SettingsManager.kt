package de.grobox.transportr.settings

import android.content.Context
import android.preference.PreferenceManager
import android.support.annotation.StyleRes
import de.grobox.transportr.R
import de.schildbach.pte.NetworkId
import de.schildbach.pte.NetworkProvider.Optimize
import de.schildbach.pte.NetworkProvider.WalkSpeed
import java.util.*
import javax.inject.Inject


class SettingsManager @Inject constructor(private val context: Context) {

    private val settings = PreferenceManager.getDefaultSharedPreferences(context)

    val locale: Locale
        get() {
            val default = context.getString(R.string.pref_language_value_default)
            val str = settings.getString(LANGUAGE, default)
            return when {
                str == default -> Locale.getDefault()
                str.contains("_") -> {
                    val langArray = str.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    Locale(langArray[0], langArray[1])
                }
                else -> Locale(str)
            }
        }

    val theme: Int
        @StyleRes get() {
            val dark = context.getString(R.string.pref_theme_value_dark)
            val light = context.getString(R.string.pref_theme_value_light)
            val theme = settings.getString(THEME, light)
            return if (theme == dark) {
                R.style.AppTheme
            } else R.style.AppTheme_Light
        }

    val walkSpeed: WalkSpeed
        get() {
            return try {
                WalkSpeed.valueOf(settings.getString(WALK_SPEED, context.getString(R.string.pref_walk_speed_value_default)))
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                WalkSpeed.NORMAL
            }
        }

    val optimize: Optimize
        get() {
            return try {
                Optimize.valueOf(settings.getString(OPTIMIZE, context.getString(R.string.pref_optimize_value_default)))
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                Optimize.LEAST_DURATION
            }
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
        private val NETWORK_ID_1 = "NetworkId"
        private val NETWORK_ID_2 = "NetworkId2"
        private val NETWORK_ID_3 = "NetworkId3"

        @JvmField internal val LANGUAGE = "pref_key_language"
        @JvmField internal val THEME = "pref_key_theme"
        private val SHOW_WHEN_LOCKED = "pref_key_show_when_locked"
        private val WALK_SPEED = "pref_key_walk_speed"
        private val OPTIMIZE = "pref_key_optimize"
    }

}
