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

package de.grobox.liberario.locations;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
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

import javax.inject.Inject;

import de.grobox.liberario.R;
import de.grobox.liberario.TransportrApplication;
import de.grobox.liberario.favorites.locations.FavoriteLocation.FavLocationType;
import de.grobox.liberario.favorites.locations.FavoriteLocationManager;
import de.grobox.liberario.locations.SuggestLocationsTask.SuggestLocationsTaskCallback;
import de.grobox.liberario.networks.TransportNetworkManager;
import de.grobox.liberario.utils.TransportrUtils;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.SuggestLocationsResult;

import static de.grobox.liberario.locations.WrapLocation.WrapType.HOME;
import static de.grobox.liberario.locations.WrapLocation.WrapType.MAP;
import static de.grobox.liberario.utils.TransportrUtils.getDrawableForLocation;

public class LocationView extends LinearLayout implements SuggestLocationsTaskCallback {

	private final static String LOCATION = "location";
	private final static String TEXT = "text";
	private final static String TEXT_POSITION = "textPosition";
	private final static String CHANGING_HOME = "changingHome";
	private final static int AUTO_COMPLETION_DELAY = 300;
	protected final static String SUPER_STATE = "superState";

	@Inject TransportNetworkManager transportNetworkManager;
	@Inject FavoriteLocationManager favoriteLocationManager;
	private SuggestLocationsTask task;
	private Location location;
	private boolean changingHome = false, suggestLocationsTaskPending = false;
	protected final LocationViewHolder ui;
	protected LocationViewListener listener;
	protected String hint;

	private FavLocationType type = FavLocationType.FROM;

	public LocationView(Context context, AttributeSet attrs) {
		super(context, attrs);

		if (!isInEditMode()) {
			((TransportrApplication) getContext().getApplicationContext()).getComponent().inject(this);
		}

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LocationView, 0, 0);
		boolean includeHome = a.getBoolean(R.styleable.LocationView_homeLocation, false);
		boolean includeFavs = a.getBoolean(R.styleable.LocationView_favLocation, false);
		boolean showIcon = a.getBoolean(R.styleable.LocationView_showIcon, true);
		hint = a.getString(R.styleable.LocationView_hint);
		a.recycle();

		setOrientation(LinearLayout.HORIZONTAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.location_view, this, true);
		ui = new LocationViewHolder(this);

		ui.location.setHint(hint);
		if (!isInEditMode()) ui.location.setAdapter(createLocationAdapter(includeHome, includeFavs));
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

		if (!showIcon) ui.status.setVisibility(View.GONE);

		ui.status.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getAdapter().resetDropDownLocations();
				LocationView.this.onClick();
			}
		});

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

	protected static class LocationViewHolder {
		public ImageView status;
		public AutoCompleteTextView location;
		ProgressBar progress;
		public ImageButton clear;

		private LocationViewHolder(View view) {
			status = view.findViewById(R.id.statusButton);
			location = view.findViewById(R.id.location);
			clear = view.findViewById(R.id.clearButton);
			progress = view.findViewById(R.id.progress);
		}
	}

	protected LocationAdapter createLocationAdapter(boolean includeHome, boolean includeFavs) {
		return new LocationAdapter(getContext(), favoriteLocationManager, includeHome, false, includeFavs);
	}

	/* State Saving and Restoring */

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
			}
			int position = bundle.getInt(TEXT_POSITION);
			ui.location.setSelection(position);

			changingHome = bundle.getBoolean(CHANGING_HOME);
/*			if(changingHome) {
				Fragment homePicker = activity.getSupportFragmentManager().findFragmentByTag(HomePickerDialogFragment.TAG);
				if(homePicker != null && homePicker.isAdded()) {
					((HomePickerDialogFragment) homePicker).setListener(this);
				}
			}
*/
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

	/* Auto-Completion */

	public void handleTextChanged(CharSequence s) {
		// show clear button
		if(s.length() > 0) {
			ui.clear.setVisibility(View.VISIBLE);
			// clear location tag
			setLocation(null, null, false);

			if(s.length() >= LocationAdapter.TYPING_THRESHOLD) {
				onContentChanged();
			}
		} else {
			clearLocationAndShowDropDown();
		}
	}

	private void onContentChanged() {
		ui.progress.setVisibility(View.VISIBLE);
		startSuggestLocationsTaskDelayed();
	}

	private void startSuggestLocationsTaskDelayed() {
		if (suggestLocationsTaskPending || transportNetworkManager.getTransportNetwork() == null) return;
		suggestLocationsTaskPending = true;
		postDelayed(new Runnable() {
			@Override
			public void run() {
				if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) task.cancel(true);
				task = new SuggestLocationsTask(transportNetworkManager.getTransportNetwork(), LocationView.this);
				task.execute(getText());
				suggestLocationsTaskPending = false;
			}
		}, AUTO_COMPLETION_DELAY);
	}

	@Override
	public void onSuggestLocationsResult(@Nullable SuggestLocationsResult suggestLocationsResult) {
		ui.progress.setVisibility(View.GONE);
		if(suggestLocationsResult == null) return;

		if(getAdapter() != null) {
			getAdapter().swapSuggestedLocations(suggestLocationsResult.suggestedLocations, ui.location.getText().toString());
		}
	}

	private void stopSuggestLocationsTask() {
		if(task != null) task.cancel(true);
		ui.progress.setVisibility(View.GONE);
	}

	/* Setter and Getter */

	protected LocationAdapter getAdapter() {
		return (LocationAdapter) ui.location.getAdapter();
	}

	public void setLocation(Location loc, Drawable icon, boolean setText) {
		location = loc;

		if(setText) {
			if(loc != null) {
				ui.location.setText(TransportrUtils.getLocationName(loc));
				ui.location.dismissDropDown();
				ui.clear.setVisibility(View.VISIBLE);
				stopSuggestLocationsTask();
			} else {
				ui.location.setText(null);
				ui.clear.setVisibility(View.GONE);
			}
		}

		if(icon != null) {
			ui.status.setImageDrawable(icon);
		} else {
			ui.status.setImageResource(R.drawable.ic_location);
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
		if (loc == null) {
			setLocation(null);
		} else if (loc.getType() == HOME) {
			// special case: home location
			Location home = favoriteLocationManager.getHome();
			if (home != null) {
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

	public void setType(FavLocationType type) {
		this.type = type;
		getAdapter().setSort(type);
	}

	public FavLocationType getType() {
		return type;
	}

	public void setHint(@StringRes int hint) {
		ui.location.setHint(hint);
	}

	/* Behavior */

	protected void onFocusChange(View v, boolean hasFocus) {
		if(hasFocus && ViewCompat.isAttachedToWindow(v) && v instanceof AutoCompleteTextView) {
			((AutoCompleteTextView) v).showDropDown();
		}
	}

	public void onLocationItemClick(WrapLocation loc, View view) {
		Drawable icon = ((ImageView) view.findViewById(R.id.imageView)).getDrawable();

		// special case: home location
		if(loc.getType() == HOME) {
			Location home = favoriteLocationManager.getHome();

			if(home != null) {
				setLocation(home, icon);
				favoriteLocationManager.addLocation(loc);
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
			favoriteLocationManager.addLocation(loc);
		}

		// hide soft-keyboard
		hideSoftKeyboard();

		if(!changingHome && listener != null) listener.onLocationItemClick(loc);
	}

	public void onClick() {
		if(getAdapter().getCount() > 0) {
			ui.location.showDropDown();
		}
	}

	public void clearLocation() {
		setLocation(null, null);
		if(getAdapter() != null) {
			getAdapter().resetSearchTerm();
		}
	}

	protected void clearLocationAndShowDropDown() {
		clearLocation();
		stopSuggestLocationsTask();
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
			getAdapter().reset();
		}
	}

	public void resetIfEmpty() {
		if(ui.location.getText().length() == 0) {
			reset();
		}
	}

	public void hideSoftKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(ui.location.getWindowToken(), 0);
		}
	}

	/* Changing Home Location */

	public void selectHomeLocation() {
		changingHome = true;
		// show home picker dialog
/*		HomePickerDialogFragment setHomeFragment = HomePickerDialogFragment.newInstance();
		setHomeFragment.setListener(this);
		FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
		setHomeFragment.show(ft, HomePickerDialogFragment.TAG);
	}

	@Override
	public void onHomeChanged(Location home) {
		changingHome = false;
		setLocation(home, ContextCompat.getDrawable(getContext(), R.drawable.ic_action_home));
		if(listener != null) listener.onLocationItemClick(new WrapLocation(home, HOME));
*/	}

	public boolean isChangingHome() {
		return changingHome;
	}

	/* Listener */

	public void setLocationViewListener(LocationViewListener listener) {
		this.listener = listener;
	}

	public interface LocationViewListener {
		void onLocationItemClick(WrapLocation loc);
		void onLocationCleared();
	}

}
