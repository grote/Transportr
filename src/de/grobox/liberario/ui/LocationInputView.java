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

package de.grobox.liberario.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;

public class LocationInputView {
	public View.OnClickListener onClickListener;
	public LocationInputViewHolder ui;

	protected Activity context;
	protected LocationAdapter locAdapter;
	protected Location loc;
	protected String hint;

	private boolean is_changing = false;
	private FavLocation.LOC_TYPE type;

	public LocationInputView(Activity context, LocationInputViewHolder ui) {
		this.context = context;
		this.locAdapter = new LocationAdapter(context);
		this.ui = ui;

		setLocationInputUI();
	}

	public LocationInputView(Activity context, LocationInputViewHolder ui, boolean onlyIDs) {
		this.context = context;
		this.locAdapter = new LocationAdapter(context, onlyIDs);
		this.ui = ui;

		setLocationInputUI();
	}

	private void setLocationInputUI() {
		ui.location.setAdapter(locAdapter);
		ui.location.setLoadingIndicator(ui.progress);
		ui.location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				onLocationItemClick(locAdapter.getItem(position), view);
			}
		});

		onClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LocationInputView.this.onClick();
			}
		};
		ui.location.setOnClickListener(onClickListener);

		ui.status.setOnClickListener(onClickListener);
		ui.status.setImageDrawable(TransportrUtils.getTintedDrawable(context, R.drawable.ic_location));

		// clear from text button
		ui.clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ui.location.requestFocus();
				clearLocation();
				ui.clear.setVisibility(View.GONE);
			}
		});
		ui.clear.setImageDrawable(TransportrUtils.getTintedDrawable(context, ui.clear.getDrawable()));

		// From text input changed
		ui.location.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				handleTextChanged(s);
			}

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
	}

	public void setLocation(Location loc, Drawable icon, boolean setText) {
		if(!is_changing) {
			is_changing = true;

			this.loc = loc;

			if(setText) {
				if(loc != null) {
					// set text
					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
						ui.location.setText(loc.uniqueShortName(), false);
					} else {
						ui.location.setText(loc.uniqueShortName());
						ui.location.cancelFiltering();
					}
					ui.clear.setVisibility(View.VISIBLE);
				} else {
					ui.location.setText(null);
				}
				ui.location.dismissDropDown();
			}

			if(icon != null) {
				ui.status.setImageDrawable(icon);
			} else {
				ui.status.setImageDrawable(TransportrUtils.getTintedDrawable(context, R.drawable.ic_location));
			}

			is_changing = false;
		}
	}

	public void setLocation(Location loc, Drawable icon) {
		setLocation(loc, icon, true);
	}

	@Nullable
	public Location getLocation() {
		return loc;
	}

	public void clearLocation() {
		setLocation(null, null);
	}

	public void setType(FavLocation.LOC_TYPE type) {
		this.type = type;
	}

	public FavLocation.LOC_TYPE getType() {
		locAdapter.setSort(type);
		return this.type;
	}

	public void setFavs(boolean activate) {
		locAdapter.setFavs(activate);
	}

	public void setHome(boolean activate) {
		locAdapter.setHome(activate);
	}

	public void setHint(int hint) {
		this.hint = context.getString(hint);
		ui.location.setHint(this.hint);
	}

	public void onLocationItemClick(Location loc, View view) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();

		// special case: home location
		if(loc.id != null && loc.id.equals("Transportr.HOME")) {
			Location home = RecentsDB.getHome(context);

			if(home != null) {
				setLocation(home, icon);
			} else {
				// prevent home.toString() from being shown in the TextView
				ui.location.setText("");

				selectHomeLocation();
			}
		}
		// all other cases
		else {
			setLocation(loc, icon);
			ui.location.requestFocus();
		}

		// hide soft-keyboard
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ui.location.getWindowToken(), 0);
	}

	public void onClick() {
		int size = locAdapter.addFavs();

		if(size > 0) {
			ui.location.showDropDown();
		}
		else {
			Toast.makeText(context, context.getResources().getString(R.string.error_no_favs), Toast.LENGTH_SHORT).show();
		}
	}

	public void handleTextChanged(CharSequence s) {
		if(is_changing) return;

		// show clear button
		if(s.length() > 0) {
			ui.clear.setVisibility(View.VISIBLE);
			// clear location
			setLocation(null, null, false);
		} else {
			ui.clear.setVisibility(View.GONE);
			clearLocation();
			// clear drop-down list
			locAdapter.resetList();
		}
	}

	public void selectHomeLocation() {
		// show dialog to set home screen
		Intent intent = new Intent(context, SetHomeActivity.class);
		intent.putExtra("new", true);
		context.startActivity(intent);
	}

	public static class LocationInputViewHolder {
		public ImageView status;
		public DelayAutoCompleteTextView location;
		public ProgressBar progress;
		public ImageButton clear;

		public LocationInputViewHolder(View view) {
			status = (ImageView) view.findViewById(R.id.statusButton);
			location = (DelayAutoCompleteTextView) view.findViewById(R.id.location);
			clear = (ImageButton) view.findViewById(R.id.clearButton);
			progress = (ProgressBar) view.findViewById(R.id.progress);
		}

	}
}