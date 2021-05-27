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

package de.grobox.transportr.locations

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import androidx.annotation.RequiresPermission
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import de.grobox.transportr.R

class LocationGpsView(context: Context, attrs: AttributeSet) : LocationView(context, attrs) {

    var isSearching = false
        private set

    init {
        ui.location.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (isSearching) {
                    clearSearching()
                    ui.location.setText(s)
                    ui.location.setSelection(s.length)
                }
            }

            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })
    }

    override fun setLocation(loc: WrapLocation?, icon: Int, setText: Boolean) {
        if (setText) clearSearching()
        super.setLocation(loc, icon, setText)
    }

    override fun clearLocationAndShowDropDown(setText: Boolean) {
        clearSearching()
        super.clearLocationAndShowDropDown(setText)
    }

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun setSearching() {
        if (isSearching) return
        isSearching = true

        // clear input
        setLocation(null, R.drawable.ic_gps, false)

        // show GPS button blinking
        val animation = AlphaAnimation(1f, 0f)
        animation.duration = 500
        animation.interpolator = LinearInterpolator()
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        ui.status.startAnimation(animation)

        ui.location.setHint(R.string.stations_searching_position)
        ui.clear.visibility = VISIBLE
    }

    fun clearSearching() {
        if (!isSearching) return

        ui.status.clearAnimation()
        ui.location.hint = hint

        isSearching = false
    }

}
