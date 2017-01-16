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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.util.List;

import de.grobox.liberario.FavLocation;
import de.grobox.liberario.NetworkProviderFactory;
import de.grobox.liberario.Preferences;
import de.grobox.liberario.R;
import de.grobox.liberario.WrapLocation;
import de.grobox.liberario.adapters.LocationAdapter;
import de.grobox.liberario.data.RecentsDB;
import de.grobox.liberario.fragments.HomePickerDialogFragment;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.NetworkProvider;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.SuggestLocationsResult;

import static de.grobox.liberario.WrapLocation.WrapType.HOME;
import static de.grobox.liberario.WrapLocation.WrapType.MAP;
import static de.grobox.liberario.utils.TransportrUtils.getDrawableForLocation;
import static de.grobox.liberario.utils.TransportrUtils.getTintedDrawable;

public class LocationView extends LinearLayout implements LoaderManager.LoaderCallbacks, HomePickerDialogFragment.OnHomeChangedListener {

	private final String LOCATION = "location";
	private final String TEXT = "text";
	private final String TEXT_POSITION = "textPosition";
	private final String CHANGING_HOME = "changingHome";
	protected final String SUPER_STATE = "superState";
	private Location location;
	private boolean changingHome = false;
	protected FragmentActivity activity;
	protected LoaderManager loaderManager;
	protected final LocationViewHolder ui;
	protected LocationViewListener listener;
	protected String hint;

	private FavLocation.LOC_TYPE type;

	public LocationView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LocationView, 0, 0);
		boolean onlyIDs = a.getBoolean(R.styleable.LocationView_onlyIds, true);
		boolean includeHome = a.getBoolean(R.styleable.LocationView_homeLocation, false);
		boolean includeFavs = a.getBoolean(R.styleable.LocationView_favLocation, false);
		boolean includeMap = a.getBoolean(R.styleable.LocationView_mapLocation, false);
		boolean showIcon = a.getBoolean(R.styleable.LocationView_showIcon, true);
		hint = a.getString(R.styleable.LocationView_hint);
		a.recycle();

		setOrientation(LinearLayout.HORIZONTAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.location_view, this, true);
		ui = new LocationViewHolder(this);

		if(!isInEditMode() && context instanceof FragmentActivity) {
			initialize((FragmentActivity) context);
		}

		ui.location.setHint(hint);
		ui.location.setAdapter(new LocationAdapter(context, onlyIDs));
		ui.location.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
				WrapLocation loc = getAdapter().getItem(position);
				if(loc != null) onLocationItemClick(loc, view);
			}
		});
		ui.location.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				LocationView.this.onFocusChange(v, hasFocus);
			}
		});
		ui.location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LocationView.this.onClick();
			}
		});

		setHome(includeHome);
		setFavs(includeFavs);
		setMap(includeMap);
		if (!showIcon) ui.status.setVisibility(View.GONE);

		ui.status.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getAdapter().setDefaultLocations(false);
				LocationView.this.onClick();
			}
		});
		if(!isInEditMode()) {
			ui.status.setImageDrawable(getTintedDrawable(context, R.drawable.ic_location));
			ui.clear.setImageDrawable(getTintedDrawable(context, ui.clear.getDrawable()));
		} else {
			ui.status.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_location));
		}

		// clear text button
		ui.clear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clearLocationAndShowDropDown();
			}
		});

		// From text input changed
		ui.location.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if((count == 1 && before == 0) || (count == 0 && before == 1)) handleTextChanged(s);
			}

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
		});
	}

	public LocationView(Context context) {
		this(context, null);
	}

	public void initialize(FragmentActivity a) {
		loaderManager = a.getSupportLoaderManager();
		loaderManager.initLoader(getId(), null, this);
		activity = a;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState());
		bundle.putInt(TEXT_POSITION, ui.location.getSelectionStart());
		bundle.putSerializable(LOCATION, location);
		if(location == null && ui.location.getText().length() > 0) {
			bundle.putString(TEXT, ui.location.getText().toString());
		}
		bundle.putBoolean(CHANGING_HOME, changingHome);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if(state instanceof Bundle) { // implicit null check
			Bundle bundle = (Bundle) state;
			Location loc = (Location) bundle.getSerializable(LOCATION);
			String text = bundle.getString(TEXT);
			if(loc != null) {
				setLocation(loc);
			}
			else if(text != null && text.length() > 0) {
				ui.location.setText(text);
				ui.clear.setVisibility(View.VISIBLE);

				// load the auto-completion results again as they seem to get lost during restart
				Loader loader = loaderManager.getLoader(getId());
				if(loader != null) loader.onContentChanged();
			}
			int position = bundle.getInt(TEXT_POSITION);
			ui.location.setSelection(position);

			changingHome = bundle.getBoolean(CHANGING_HOME);
			if(changingHome) {
				// re-set OnHomeChangedListener if home picker is shown
				Fragment homePicker = activity.getSupportFragmentManager().findFragmentByTag(HomePickerDialogFragment.TAG);
				if(homePicker != null && homePicker.isAdded()) {
					((HomePickerDialogFragment) homePicker).setOnHomeChangedListener(this);
				}
			}

			// replace state by super state
			state = bundle.getParcelable(SUPER_STATE);
		}
		super.onRestoreInstanceState(state);
	}

	@Override
	 protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
		// Makes sure that the state of the child views are not saved
		super.dispatchFreezeSelfOnly(container);
	}

	@Override
	protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
		// Makes sure that the state of the child views are not restored
		super.dispatchThawSelfOnly(container);
	}

	@Override
	protected void onDetachedFromWindow() {
		// Important: Destroy Loader, because it holds a reference to the old LocationView
		loaderManager.destroyLoader(getId());

		super.onDetachedFromWindow();
	}

	@Override
	public AsyncTaskLoader onCreateLoader(int id, Bundle args) {
		AsyncTaskLoader loader = new AsyncTaskLoader<List<Location>>(getContext()) {
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

	@SuppressWarnings("unchecked")
	@Override
	public void onLoadFinished(Loader loader, final Object data) {
		ui.progress.setVisibility(View.GONE);
		if(data == null) return;

		if(getAdapter() != null) {
			getAdapter().swapSuggestedLocations((List<Location>) data, ui.location.getText().toString());
		}
	}

	@Override
	public void onLoaderReset(Loader loader) {
		getAdapter().swapSuggestedLocations(null, null);
		ui.progress.setVisibility(View.GONE);
	}

	private void stopLoader() {
		Loader loader = loaderManager.getLoader(getId());
		if(loader != null) loader.cancelLoad();
		ui.progress.setVisibility(View.GONE);
	}

	private void onContentChanged() {
		ui.progress.setVisibility(View.VISIBLE);
		Loader loader = loaderManager.getLoader(getId());
		if(loader != null) loader.onContentChanged();
	}

	protected void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus && v.isShown() && v instanceof AutoCompleteTextView) {
			((AutoCompleteTextView) v).showDropDown();
		}
	}

	protected LocationAdapter getAdapter() {
		return (LocationAdapter) ui.location.getAdapter();
	}

	public boolean isChangingHome() {
		return changingHome;
	}

	public void setLocation(Location loc, Drawable icon, boolean setText) {
		location = loc;

		if(setText) {
			if(loc != null) {
				ui.location.setText(TransportrUtils.getLocationName(loc));
				ui.location.setSelection(ui.location.getText().length());
				ui.location.dismissDropDown();
				ui.clear.setVisibility(View.VISIBLE);
				stopLoader();
			} else {
				ui.location.setText(null);
				ui.clear.setVisibility(View.GONE);
			}
		}

		if(icon != null) {
			ui.status.setImageDrawable(icon);
		} else {
			ui.status.setImageDrawable(getTintedDrawable(getContext(), R.drawable.ic_location));
		}
	}

	public void setLocation(Location loc, Drawable icon) {
		setLocation(loc, icon, true);
	}

	public void setLocation(@Nullable Location loc) {
		Drawable drawable = getDrawableForLocation(getContext(), loc);
		setLocation(loc, drawable, true);
	}

	public void setWrapLocation(@Nullable WrapLocation loc) {
		if(loc == null) {
			setLocation(null);
		} else if(loc.getType() == HOME) {
			// special case: home location
			Location home = RecentsDB.getHome(getContext());

			if(home != null) {
				setLocation(home);
			} else {
				// prevent home.toString() from being shown in the TextView
				ui.location.setText("");

				selectHomeLocation();
			}
		} else {
			// all other cases
			setLocation(loc.getLocation());
		}
	}

	@Nullable
	public Location getLocation() {
		return this.location;
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

	protected void clearLocationAndShowDropDown() {
		clearLocation();
		stopLoader();
		reset();
		if (listener != null) listener.onLocationCleared();
		ui.clear.setVisibility(View.GONE);
		if (isShown()) {
			ui.location.requestFocus();
			ui.location.showDropDown();
		}
	}

	public void reset() {
		if(getAdapter() != null) {
			getAdapter().setDefaultLocations(true);
		}
	}

	public void resetIfEmpty() {
		if(ui.location.getText().length() == 0) {
			reset();
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

	public void onLocationItemClick(WrapLocation loc, View view) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();

		// special case: home location
		if(loc.getType() == HOME) {
			Location home = RecentsDB.getHome(getContext());

			if(home != null) {
				setLocation(home, icon);
			} else {
				// prevent home.toString() from being shown in the TextView
				ui.location.setText("");

				selectHomeLocation();
			}
		}
		else if(loc.getType() == MAP) {
			// prevent MAP from being shown in the TextView
			ui.location.setText("");

			TransportrUtils.showMap(getContext());
		}
		// all other cases
		else {
			setLocation(loc.getLocation(), icon);
			ui.location.requestFocus();
		}

		// hide soft-keyboard
		hideSoftKeyboard();

		if(!changingHome && listener != null) listener.onLocationItemClick(loc);
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

	public void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(ui.location.getWindowToken(), 0);
	}

	public void selectHomeLocation() {
		changingHome = true;

		// show home picker dialog
		HomePickerDialogFragment setHomeFragment = HomePickerDialogFragment.newInstance();
		setHomeFragment.setOnHomeChangedListener(this);
		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
		setHomeFragment.show(ft, HomePickerDialogFragment.TAG);
	}

	@Override
	public void onHomeChanged(Location home) {
		changingHome = false;
		setLocation(home, getTintedDrawable(getContext(), R.drawable.ic_action_home));
		if(listener != null) listener.onLocationItemClick(new WrapLocation(home, HOME));
	}

	protected static class LocationViewHolder {
		public ImageView status;
		public AutoCompleteTextView location;
		ProgressBar progress;
		public ImageButton clear;

		private LocationViewHolder(View view) {
			status = (ImageView) view.findViewById(R.id.statusButton);
			location = (AutoCompleteTextView) view.findViewById(R.id.location);
			clear = (ImageButton) view.findViewById(R.id.clearButton);
			progress = (ProgressBar) view.findViewById(R.id.progress);
		}
	}

	public void setLocationViewListener(LocationViewListener listener) {
		this.listener = listener;
	}

	public interface LocationViewListener {
		void onLocationItemClick(WrapLocation loc);
		void onLocationCleared();
	}

}