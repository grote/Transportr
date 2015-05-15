/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

package de.grobox.liberario.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.grobox.liberario.R;
import de.grobox.liberario.fragments.FragmentAbout;
import de.grobox.liberario.ui.SlidingTabLayout;

public class AboutActivity extends AppCompatActivity {
	AboutPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if(toolbar != null) {
			setSupportActionBar(toolbar);

			ActionBar actionBar = getSupportActionBar();
			if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);
		}

		setTitle(getResources().getString(R.string.action_about) + " " + getResources().getString(R.string.app_name));

		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

		// don't recreate the fragments when changing tabs
		viewPager.setOffscreenPageLimit(3);

		mPagerAdapter = new AboutPagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(mPagerAdapter);

		SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
		slidingTabLayout.setDistributeEvenly(true);
		slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
		slidingTabLayout.setViewPager(viewPager);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == android.R.id.home) {
			onBackPressed();
			return true;
		} else {
			return false;
		}
	}


	class AboutPagerAdapter extends FragmentStatePagerAdapter {
		// Since this is an object collection, use a FragmentStatePagerAdapter,
		// and NOT a FragmentPagerAdapter.
		public AboutPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			if(i == 1) {
				// TODO: Developers
				return new FragmentAbout();
			} else if(i == 2) {
				// TODO: Libraries
				return new FragmentAbout();
			}
			return new FragmentAbout();
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int i) {
			if(i == 1) {
				return getString(R.string.tab_devs);
			} else if(i == 2) {
				return getString(R.string.tab_libraries);
			}
			return getString(R.string.tab_about);
		}
	}
}
