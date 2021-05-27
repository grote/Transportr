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

package de.grobox.transportr.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 *  Linkify is a simplified version of android.text.Linkify
 *  which doesn't care about schemes or lower-casing the url,
 *  but just adds a clickable link to every match of the given pattern.
 */
public class Linkify {

	/**
	 *  Applies a regex to the text of a TextView turning the matches into
	 *  links.  If links are found then UrlSpans are applied to the link
	 *  text match areas, and the movement method for the text is changed
	 *  to LinkMovementMethod.
	 *
	 *  @param textView TextView whose text is to be marked-up with links.
	 *  @param pattern Regex pattern to be used for finding links.
	 *  @param url The url to be used linked to.
	 */
	public static boolean addLinks(@NonNull TextView textView, @NonNull Pattern pattern, @NonNull String url) {
		SpannableString spannable = SpannableString.valueOf(textView.getText());

		boolean found = false;
		Matcher m = pattern.matcher(spannable);
		while (m.find()) {
			int start = m.start();
			int end = m.end();
			URLSpan span = new URLSpan(url);
			spannable.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			found = true;
		}

		textView.setText(spannable);
		textView.setMovementMethod(LinkMovementMethod.getInstance());

		return found;
	}
}
