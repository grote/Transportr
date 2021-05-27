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

import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import de.grobox.transportr.R
import de.grobox.transportr.TransportrFragment


class AboutFragment : TransportrFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_about, container, false)

        // add app name and version
        val versionName = try {
            val activity = activity ?: throw NameNotFoundException()
            activity.packageManager.getPackageInfo(activity.packageName, 0).versionName
        } catch (e: NameNotFoundException) {
            "?.?"
        }
        val appNameVersion = v.findViewById<TextView>(R.id.appNameVersion)
        appNameVersion.text = "${getString(R.string.app_name)} $versionName"

        // website button
        val websiteButton = v.findViewById<Button>(R.id.websiteButton)
        websiteButton.setOnClickListener {
            val launchBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website) + getString(R.string.website_source_app)))
            startActivity(launchBrowser)
        }

        return v
    }

}
