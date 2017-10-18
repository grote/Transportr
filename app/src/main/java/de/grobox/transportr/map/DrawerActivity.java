/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.util.KeyboardUtil;

import javax.inject.Inject;

import de.grobox.transportr.R;
import de.grobox.transportr.about.AboutActivity;
import de.grobox.transportr.about.AboutMainFragment;
import de.grobox.transportr.activities.TransportrActivity;
import de.grobox.transportr.networks.PickTransportNetworkActivity;
import de.grobox.transportr.networks.TransportNetwork;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.settings.Preferences;
import de.grobox.transportr.settings.SettingsActivity;
import de.grobox.transportr.settings.SettingsFragment;
import de.grobox.transportr.ui.TransportrChangeLog;
import de.grobox.transportr.utils.TransportrUtils;

import static android.support.v4.app.ActivityOptionsCompat.makeScaleUpAnimation;

abstract class DrawerActivity extends TransportrActivity {

	@Inject
	TransportNetworkManager manager;

	private Drawer drawer;
	private AccountHeader accountHeader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Accounts aka TransportNetworks
		accountHeader = new AccountHeaderBuilder()
				.withActivity(this)
				.withHeaderBackground(R.drawable.account_header_background)
				.withDividerBelowHeader(true)
				.withSelectionListEnabled(false)
				.withThreeSmallProfileImages(true)
				.withOnAccountHeaderListener((view, profile, currentProfile) -> {
					if (currentProfile) {
						openPickNetworkProviderActivity();
						return true;
					} else if (profile != null && profile instanceof ProfileDrawerItem) {
						TransportNetwork network = (TransportNetwork) ((ProfileDrawerItem) profile).getTag();
						if (network != null) manager.setTransportNetwork(network);
					}
					return false;
				})
				.withOnAccountHeaderSelectionViewClickListener((view, profile) -> {
					openPickNetworkProviderActivity();
					return true;
				})
				.build();

		// Drawer
		drawer = new DrawerBuilder()
				.withActivity(this)
				.withAccountHeader(accountHeader)
				.addDrawerItems(
						new DividerDrawerItem(),
						getDrawerItem(SettingsFragment.TAG, R.drawable.ic_action_settings),
						getDrawerItem(TransportrChangeLog.TAG, R.drawable.ic_action_changelog),
						getDrawerItem(AboutMainFragment.TAG, R.drawable.ic_action_about)
				)
				.withOnDrawerListener(new Drawer.OnDrawerListener() {
					@Override
					public void onDrawerOpened(View drawerView) {
						KeyboardUtil.hideKeyboard(DrawerActivity.this);
					}

					@Override
					public void onDrawerClosed(View drawerView) {
					}

					@Override
					public void onDrawerSlide(View drawerView, float slideOffset) {
					}
				})
				.withFireOnInitialOnClick(false)
				.withSavedInstance(savedInstanceState)
				.build();

		// add transport networks to header
		addAccounts();
	}

	private void addAccounts() {
		TransportNetwork network = manager.getTransportNetwork().getValue();
		if (network != null) {
			ProfileDrawerItem item1 = new ProfileDrawerItem()
					.withName(network.getName(this))
					.withEmail(network.getDescription(this))
					.withIcon(ContextCompat.getDrawable(this, network.getLogo()));
			item1.withTag(network);
			accountHeader.addProfile(item1, accountHeader.getProfiles().size());
		}

		TransportNetwork network2 = manager.getTransportNetwork(2);
		if (network2 != null) {
			ProfileDrawerItem item2 = new ProfileDrawerItem()
					.withName(network2.getName(this))
					.withEmail(network2.getDescription(this))
					.withIcon(ContextCompat.getDrawable(this, network2.getLogo()));
			item2.withTag(network2);
			accountHeader.addProfile(item2, accountHeader.getProfiles().size());
		}

		TransportNetwork network3 = manager.getTransportNetwork(3);
		if (network3 != null) {
			ProfileDrawerItem item3 = new ProfileDrawerItem()
					.withName(network3.getName(this))
					.withEmail(network3.getDescription(this))
					.withIcon(ContextCompat.getDrawable(this, network3.getLogo()));
			item3.withTag(network3);
			accountHeader.addProfile(item3, accountHeader.getProfiles().size());
		}
	}

	private PrimaryDrawerItem getDrawerItem(final String tag, final int icon) {
		Drawer.OnDrawerItemClickListener onClick;
		String name;

		if (tag.equals(TransportrChangeLog.TAG)) {
			onClick = (view, position, drawerItem) -> {
				new TransportrChangeLog(DrawerActivity.this, Preferences.darkThemeEnabled(DrawerActivity.this)).getFullLogDialog().show();
				return true;
			};
			name = getString(R.string.drawer_changelog);
		} else {
			onClick = (view, position, drawerItem) -> {
				drawer.closeDrawer();
				if (tag.equals(SettingsFragment.TAG)) {
					Intent i = new Intent(DrawerActivity.this, SettingsActivity.class);
					startActivity(i);
				} else if (tag.equals(AboutMainFragment.TAG)) {
					Intent i = new Intent(DrawerActivity.this, AboutActivity.class);
					startActivity(i);
				}
				return true;
			};
			name = getFragmentName(tag);
		}
		return new PrimaryDrawerItem()
				.withName(name)
				.withTag(tag)
				.withIcon(TransportrUtils.getTintedDrawable(this, icon))
				.withSelectable(false)
				.withOnDrawerItemClickListener(onClick);
	}

	private String getFragmentName(String tag) {
		if (tag.equals(SettingsFragment.TAG)) return getString(R.string.drawer_settings);
		if (tag.equals(AboutMainFragment.TAG)) return getString(R.string.drawer_about);
		throw new IllegalArgumentException("Could not find fragment name");
	}

	protected void openPickNetworkProviderActivity() {
		Intent intent = new Intent(this, PickTransportNetworkActivity.class);
		ActivityOptionsCompat options = makeScaleUpAnimation(getCurrentFocus(), 0, 0, 0, 0);
		ActivityCompat.startActivity(this, intent, options.toBundle());
	}

	protected void openDrawer() {
		drawer.openDrawer();
	}

}
