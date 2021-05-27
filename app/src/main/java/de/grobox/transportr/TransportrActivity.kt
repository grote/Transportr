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

package de.grobox.transportr

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.networks.PickTransportNetworkActivity.Companion.FORCE_NETWORK_SELECTION
import de.grobox.transportr.networks.TransportNetworkManager
import de.grobox.transportr.settings.SettingsManager
import java.util.*
import javax.inject.Inject


abstract class TransportrActivity : AppCompatActivity() {

    @Inject
    lateinit var manager: TransportNetworkManager
    @Inject
    lateinit var settingsManager: SettingsManager

    protected val component: AppComponent
        get() = (application as TransportrApplication).component

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)

        useLanguage()
        AppCompatDelegate.setDefaultNightMode(settingsManager.theme)
        ensureTransportNetworkSelected()

        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        useLanguageTitle()
        super.onStart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * This should be called after the content view has been added in onCreate()
     *
     * @param ownLayout true if the custom toolbar brings its own layout
     * @return the Toolbar object or null if content view did not contain one
     */
    protected fun setUpCustomToolbar(ownLayout: Boolean): Toolbar? {
        // Custom Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val ab = supportActionBar
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true)
            ab.setDisplayHomeAsUpEnabled(true)
            ab.setDisplayShowCustomEnabled(ownLayout)
            ab.setDisplayShowTitleEnabled(!ownLayout)
        }
        return toolbar
    }

    protected fun fragmentIsVisible(tag: String): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(tag)
        return fragment != null && fragment.isVisible
    }

    @Suppress("DEPRECATION")
    private fun useLanguage() {
        val locale = settingsManager.locale
        Locale.setDefault(locale)
        val config = resources.configuration
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    private fun useLanguageTitle() {
        val activityInfo = packageManager.getActivityInfo(
            componentName,
            PackageManager.GET_META_DATA
        )
        if (activityInfo.labelRes != 0)
            setTitle(activityInfo.labelRes)
    }

    private fun ensureTransportNetworkSelected() {
        if (this.javaClass == PickTransportNetworkActivity::class.java) return
        val network = manager.transportNetwork.value
        if (network == null) {
            val intent = Intent(this, PickTransportNetworkActivity::class.java)
            intent.putExtra(FORCE_NETWORK_SELECTION, true)
            startActivity(intent)
            finish()
        }
    }

}
