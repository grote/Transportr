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

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.List;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.activities.MainActivity;
import de.grobox.liberario.activities.SetHomeActivity;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.SuggestLocationsResult;

public class LocationInputView implements LoaderManager.LoaderCallbacks {

	// define loader IDs
	public final static int DIRECTIONS_FROM = 0;
	public final static int DIRECTIONS_TO = 2;
	public final static int DEPARTURES = 3;
	public final static int NEARBY_STATIONS = 4;
	public final static int SET_HOME = 5;

	public LocationInputViewHolder ui;

	protected FragmentActivity context;
	protected Location loc;
	protected String hint;

	private boolean is_changing = false;
	private int loaderId;
	private FavLocation.LOC_TYPE type;

	public LocationInputView(final FragmentActivity context, LocationInputViewHolder ui, int loaderId, boolean onlyIDs) {
		this.context = context;
		this.ui = ui;
		this.loaderId = loaderId;

		context.getSupportLoaderManager().initLoader(getLoaderId(), null, this);

		ui.location.setAdapter(new LocationAdapter(context, onlyIDs));
		ui.location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				onLocationItemClick(getAdapter().getItem(position), view);
			}
		});
		ui.location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LocationInputView.this.onClick();
			}
		});

		ui.status.setOnClickListener(new View.OnClickListener() {
			                             @Override
			                             public void onClick(View v) {
				                             getAdapter().setDefaultLocations(false);
				                             LocationInputView.this.onClick();
			                             }
		                             });
		ui.status.setImageDrawable(TransportrUtils.getTintedDrawable(context, R.drawable.ic_location));

		// clear text button
		ui.clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearLocationAndShowDropDown();
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

	@Override
	public AsyncTaskLoader onCreateLoader(int id, Bundle args) {
		AsyncTaskLoader loader = new AsyncTaskLoader<List<Location>>(context) {
			@Override
			public List<Location> loadInBackground() {
				String search = getText();
				if(search.length() < LocationAdapter.THRESHOLD) return null;

				NetworkProvider np = NetworkProviderFactory.provider(Preferences.getNetworkId(getContext()));
				try {
					// get locations from network provider
					SuggestLocationsResult result = np.suggestLocations(search);
					return result.getLocations();
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
		loader.setUpdateThrottle(750);
		return loader;
	}

	@Override
	public void onLoadFinished(Loader loader, final Object data) {
		ui.progress.setVisibility(View.GONE);
		if(data == null) return;

		getAdapter().swapSuggestedLocations((List<Location>) data, ui.location.getText().toString());
	}

	@Override
	public void onLoaderReset(Loader loader) {
		getAdapter().swapSuggestedLocations(null, null);
		ui.progress.setVisibility(View.GONE);
	}

	public void restartLoader() {
		context.getSupportLoaderManager().restartLoader(getLoaderId(), null, this);
	}

	private void onContentChanged() {
		ui.progress.setVisibility(View.VISIBLE);
		context.getSupportLoaderManager().getLoader(getLoaderId()).onContentChanged();
	}

	private int getLoaderId() {
		return loaderId;
	}

	protected LocationAdapter getAdapter() {
		return (LocationAdapter) ui.location.getAdapter();
	}

	public void setLocation(Location loc, Drawable icon, boolean setText) {
		if(!is_changing) {
			is_changing = true;

			this.loc = loc;

			if(setText) {
				if(loc != null) {
					ui.location.setText(TransportrUtils.getLocName(loc));
					ui.clear.setVisibility(View.VISIBLE);
					ui.location.dismissDropDown();
				} else {
					ui.location.setText(null);
				}
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

	public String getText() {
		if(ui.location != null) {
			return ui.location.getText().toString();
		} else {
			return null;
		}
	}

	public void clearLocation() {
		setLocation(null, null);
		if(getAdapter() != null) {
			getAdapter().clearSearchTerm();
		}
	}

	private void clearLocationAndShowDropDown() {
		clearLocation();
		ui.clear.setVisibility(View.GONE);
		ui.location.requestFocus();
		ui.location.showDropDown();
	}

	public void reset() {
		if(getAdapter() != null) {
			getAdapter().setDefaultLocations(true);
		}
	}

	public void setType(FavLocation.LOC_TYPE type) {
		this.type = type;
	}

	public FavLocation.LOC_TYPE getType() {
		getAdapter().setSort(type);
		return this.type;
	}

	public void setFavs(boolean activate) {
		getAdapter().setFavs(activate);
	}

	public void setHome(boolean activate) {
		getAdapter().setHome(activate);
	}

	public void setMap(boolean activate) {
		getAdapter().setMap(activate);
	}

	public void setHint(int hint) {
		this.hint = context.getString(hint);
		ui.location.setHint(this.hint);
	}

	public void onLocationItemClick(Location loc, View view) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();

		// special case: home location
		if(loc.id != null && loc.id.equals(LocationAdapter.HOME)) {
			Location home = RecentsDB.getHome(context);

			if(home != null) {
				setLocation(home, icon);
			} else {
				// prevent home.toString() from being shown in the TextView
				ui.location.setText("");

				selectHomeLocation();
			}
		}
		else if(loc.id != null && loc.id.equals(LocationAdapter.MAP)) {
			TransportrUtils.showMap(context);
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
		if(ui.location.getText().length() == 0) {
			getAdapter().setDefaultLocations(false);
		}
		if(getAdapter().getCount() > 0) {
			ui.location.showDropDown();
		}
	}

	public void handleTextChanged(CharSequence s) {
		if(is_changing) return;

		// show clear button
		if(s.length() > 0) {
			ui.clear.setVisibility(View.VISIBLE);
			// clear location tag
			setLocation(null, null, false);

			if(s.length() >= LocationAdapter.THRESHOLD) {
				onContentChanged();
			}
		} else {
			clearLocationAndShowDropDown();
		}
	}

	public void blockOnTextChanged() {
		is_changing = true;
	}

	public void unblockOnTextChanged() {
		is_changing = false;
	}

	public void selectHomeLocation() {
		// show dialog to set home screen
		Intent intent = new Intent(context, SetHomeActivity.class);
		intent.putExtra("new", true);
		context.startActivityForResult(intent, MainActivity.CHANGED_HOME);
	}

	public static class LocationInputViewHolder {
		public ImageView status;
		public NoTextChangeAutoCompleteTextView location;
		public ProgressBar progress;
		public ImageButton clear;

		public LocationInputViewHolder(View view) {
			status = (ImageView) view.findViewById(R.id.statusButton);
			location = (NoTextChangeAutoCompleteTextView) view.findViewById(R.id.location);
			clear = (ImageButton) view.findViewById(R.id.clearButton);
			progress = (ProgressBar) view.findViewById(R.id.progress);
		}

	}
}