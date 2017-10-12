/*    Transportr
 *    Copyright (C) 2013 - 2016 Torsten Grote
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

package de.grobox.liberario.about;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.aboutlibraries.LibsBuilder;

import de.grobox.liberario.R;

public class AboutMainFragment extends Fragment {

	public static final String TAG = "de.grobox.liberario.about";
	AboutPagerAdapter mPagerAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about_main, container, false);

		final ViewPager viewPager = view.findViewById(R.id.pager);

		// don't recreate the fragments when changing tabs
		viewPager.setOffscreenPageLimit(3);

		mPagerAdapter = new AboutPagerAdapter(getChildFragmentManager());
		viewPager.setAdapter(mPagerAdapter);

		final TabLayout tabLayout = view.findViewById(R.id.tab_layout);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.setTabsFromPagerAdapter(mPagerAdapter);
		tabLayout.setTabMode(TabLayout.MODE_FIXED);

		return view;
	}

	private class AboutPagerAdapter extends FragmentPagerAdapter {
		AboutPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			if(i == 1) {
				return new AboutDevelopersFragment();
			} else if(i == 2) {
				return new LibsBuilder()
						       // Pass the fields of your application to the lib so it can find all external lib information
						       .withFields(R.string.class.getFields())
						       // get the fragment
						       .supportFragment();
			}
			return new AboutFragment();
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
