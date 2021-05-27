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

package de.grobox.transportr.map

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat.makeScaleUpAnimation
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import android.view.View
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialize.util.KeyboardUtil
import de.grobox.transportr.R
import de.grobox.transportr.TransportrActivity
import de.grobox.transportr.about.AboutActivity
import de.grobox.transportr.about.ContributorsActivity
import de.grobox.transportr.networks.PickTransportNetworkActivity
import de.grobox.transportr.networks.TransportNetwork
import de.grobox.transportr.settings.SettingsActivity
import de.grobox.transportr.ui.TransportrChangeLog

internal abstract class DrawerActivity : TransportrActivity() {

    private lateinit var drawer: Drawer
    private lateinit var accountHeader: AccountHeader

    protected fun setupDrawer(savedInstanceState: Bundle?) {
        // Accounts aka TransportNetworks
        accountHeader = AccountHeaderBuilder()
            .withActivity(this)
            .withHeaderBackground(R.drawable.account_header_background)
            .withDividerBelowHeader(true)
            .withSelectionListEnabled(false)
            .withThreeSmallProfileImages(true)
            .withOnAccountHeaderListener { view, profile, currentProfile ->
                if (currentProfile) {
                    openPickNetworkProviderActivity(view)
                    true
                } else if (profile != null && profile is ProfileDrawerItem) {
                    val network = profile.tag as TransportNetwork
                    manager.setTransportNetwork(network)
                    false
                } else {
                    false
                }
            }
            .withOnAccountHeaderSelectionViewClickListener { view, _ ->
                openPickNetworkProviderActivity(view)
                true
            }
            .withSavedInstance(savedInstanceState)
            .build()

        // Drawer
        drawer = DrawerBuilder()
            .withActivity(this)
            .withAccountHeader(accountHeader)
            .addDrawerItems(
                getDrawerItem(R.string.drawer_settings, R.drawable.ic_action_settings) {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                },
                DividerDrawerItem(),
                getDrawerItem(R.string.drawer_changelog, R.drawable.ic_action_changelog) {
                    TransportrChangeLog(this, settingsManager).fullLogDialog.show()
                },
                getDrawerItem(R.string.drawer_contributors, R.drawable.ic_people) {
                    val intent = Intent(this, ContributorsActivity::class.java)
                    startActivity(intent)
                },
                getDrawerItem(R.string.drawer_report_issue, R.drawable.ic_bug_report) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.bug_tracker)))
                    startActivity(intent)
                },
                getDrawerItem(R.string.drawer_about, R.drawable.ic_action_about) {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }
            )
            .withOnDrawerListener(object : Drawer.OnDrawerListener {
                override fun onDrawerOpened(drawerView: View) {
                    KeyboardUtil.hideKeyboard(this@DrawerActivity)
                }

                override fun onDrawerClosed(drawerView: View) {}

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            })
            .withFireOnInitialOnClick(false)
            .withShowDrawerOnFirstLaunch(false)
            .withSavedInstance(savedInstanceState)
            .withSelectedItem(NO_POSITION.toLong())
            .build()

        // add transport networks to header
        addAccounts()
    }

    private fun getDrawerItem(@StringRes name: Int, @DrawableRes icon: Int, onClick: () -> Unit): PrimaryDrawerItem {
        return PrimaryDrawerItem()
            .withName(name)
            .withIcon(icon)
            .withIconTintingEnabled(true)
            .withSelectable(false)
            .withOnDrawerItemClickListener { _, _, _ ->
                closeDrawer()
                onClick.invoke()
                return@withOnDrawerItemClickListener true
            }
    }

    private fun addAccounts() {
        manager.transportNetwork.value?.let { addAccountItem(it) }
        manager.getTransportNetwork(2)?.let { addAccountItem(it) }
        manager.getTransportNetwork(3)?.let { addAccountItem(it) }
    }

    private fun addAccountItem(network: TransportNetwork) {
        val item = ProfileDrawerItem()
            .withName(network.getName(this))
            .withEmail(network.getDescription(this))
            .withIcon(network.logo)
            .withTag(network)
        accountHeader.addProfile(item, accountHeader.profiles.size)
    }

    private fun openPickNetworkProviderActivity(view: View) {
        val intent = Intent(this, PickTransportNetworkActivity::class.java)
        val options = makeScaleUpAnimation(view, 0, 0, 0, 0)
        ActivityCompat.startActivity(this, intent, options.toBundle())
    }

    protected fun openDrawer() {
        drawer.openDrawer()
    }

    protected fun closeDrawer() {
        drawer.closeDrawer()
    }

}
