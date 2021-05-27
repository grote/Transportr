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

package de.grobox.transportr.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.mikepenz.aboutlibraries.LibsBuilder
import de.grobox.transportr.R
import de.grobox.transportr.TransportrActivity
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : TransportrActivity() {

    companion object {
        @JvmField
        val TAG : String = AboutActivity::class.java.simpleName
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setUpCustomToolbar(false)

        pager.adapter = AboutPagerAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(pager)
    }

    private inner class AboutPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(i: Int): Fragment {
            return when (i) {
                0 -> AboutFragment()
                1 -> LibsBuilder()
                    // Pass the fields of your application to the lib so it can find all external lib information
                    .withFields(R.string::class.java.fields)
                    // get the fragment
                    .supportFragment()
                else -> throw IllegalArgumentException()
            }
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(i: Int): CharSequence? {
            return when (i) {
                0 -> getString(R.string.tab_about)
                1 -> getString(R.string.tab_libraries)
                else -> throw IllegalArgumentException()
            }
        }

    }

}
