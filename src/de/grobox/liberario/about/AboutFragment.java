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

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import de.grobox.liberario.R;

public class AboutFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);
		Activity activity = getActivity();

		String versionName;
		try {
			versionName = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = "?.?";
		}

		// add app name and version
		TextView aboutApp = (TextView) view.findViewById(R.id.aboutApp);
		aboutApp.setText(getResources().getString(R.string.app_name) + "  " + versionName);

		// create real paragraphs
		TextView t = (TextView) view.findViewById(R.id.aboutTextView);
		t.setText(Html.fromHtml(
				getString(R.string.about) +
				String.format(getString(R.string.about_bottom), getString(R.string.website), getString(R.string.bugtracker), getString(R.string.website)+"#donate")
		));

		// make links in about text clickable
		t.setMovementMethod(LinkMovementMethod.getInstance());
		t.setLinkTextColor(ContextCompat.getColor(activity, R.color.accent));

		Button website = (Button) view.findViewById(R.id.websiteButton);
		website.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.website)));
				startActivity(launchBrowser);
			}
		});

		return view;
	}
}