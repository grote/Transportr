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

package de.grobox.transportr.utils

import android.app.Activity
import androidx.core.content.ContextCompat
import android.util.TypedValue
import de.grobox.transportr.R
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.CirclePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.backgrounds.RectanglePromptBackground
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.CirclePromptFocal
import uk.co.samuelwall.materialtaptargetprompt.extras.focals.RectanglePromptFocal


class OnboardingBuilder(activity: Activity) : MaterialTapTargetPrompt.Builder(activity) {

    init {
        backgroundColour = ContextCompat.getColor(activity, R.color.primary)
        promptBackground = RectanglePromptBackground()
        promptFocal = RectanglePromptFocal()
        setFocalPadding(R.dimen.buttonSize)

        val typedValue = TypedValue()
        activity.theme.resolveAttribute(android.R.attr.windowBackground, typedValue, true)
        focalColour = typedValue.data
    }

}

class IconOnboardingBuilder(activity: Activity) : MaterialTapTargetPrompt.Builder(activity) {

    init {
        backgroundColour = ContextCompat.getColor(activity, R.color.primary)
        promptBackground = CirclePromptBackground()
        promptFocal = CirclePromptFocal()
    }

}