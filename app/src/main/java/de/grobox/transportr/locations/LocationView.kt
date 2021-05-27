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

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.AsyncTask.Status.FINISHED
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.common.base.Strings.isNullOrEmpty
import de.grobox.transportr.R
import de.grobox.transportr.data.locations.FavoriteLocation
import de.grobox.transportr.data.locations.FavoriteLocation.FavLocationType
import de.grobox.transportr.data.locations.HomeLocation
import de.grobox.transportr.data.locations.WorkLocation
import de.grobox.transportr.locations.LocationAdapter.TYPING_THRESHOLD
import de.grobox.transportr.locations.SuggestLocationsTask.SuggestLocationsTaskCallback
import de.grobox.transportr.networks.TransportNetwork
import de.schildbach.pte.dto.SuggestLocationsResult

open class LocationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs),
    SuggestLocationsTaskCallback {

    companion object {
        private const val LOCATION = "location"
        private const val TEXT = "text"
        private const val TEXT_POSITION = "textPosition"
        private const val AUTO_COMPLETION_DELAY = 300
        private const val SUPER_STATE = "superState"
    }

    private lateinit var adapter: LocationAdapter
    private var task: SuggestLocationsTask? = null
    private var transportNetwork: TransportNetwork? = null
    private var location: WrapLocation? = null
    private var suggestLocationsTaskPending = false
    protected val ui: LocationViewHolder
    protected var listener: LocationViewListener? = null
    protected val hint: String?

    private var ignoreTextChanged = false

    var type = FavLocationType.FROM
        set(type) {
            field = type
            adapter.setSort(type)
        }

    private val text: String?
        get() = if (ui.location.text != null) {
            ui.location.text.toString()
        } else {
            null
        }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LocationView, 0, 0)
        val showIcon = a.getBoolean(R.styleable.LocationView_showIcon, true)
        hint = a.getString(R.styleable.LocationView_hint)
        a.recycle()

        orientation = HORIZONTAL

        val inflater = context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.location_view, this, true)
        ui = LocationViewHolder(this)

        ui.location.hint = hint
        if (!isInEditMode) {
            adapter = LocationAdapter(getContext())
            ui.location.setAdapter(adapter)
        }
        ui.location.setOnItemClickListener { _, _, position, _ ->
            try {
                val loc = adapter.getItem(position)
                if (loc != null) onLocationItemClick(loc)
            } catch (e: ArrayIndexOutOfBoundsException) {
                Log.e(LocationView::class.java.simpleName, e.message, e)
            }
        }
        ui.location.onFocusChangeListener = OnFocusChangeListener { v, hasFocus -> this@LocationView.onFocusChange(v, hasFocus) }
        ui.location.setOnClickListener { this@LocationView.onClick() }

        if (showIcon) {
            ui.status.setOnClickListener { _ ->
                adapter.resetDropDownLocations()
                this@LocationView.post { this.onClick() }
            }
        } else {
            ui.status.visibility = GONE
        }

        // clear text button
        ui.clear.setOnClickListener { clearLocationAndShowDropDown(true) }

        // From text input changed
        ui.location.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (ignoreTextChanged) return
                if (count - before == 1 || before == 1 && count == 0) handleTextChanged(s)
            }

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        })
    }

    protected class LocationViewHolder internal constructor(view: View) {
        val status: ImageView = view.findViewById(R.id.statusButton)
        val location: AutoCompleteTextView = view.findViewById(R.id.location)
        internal val progress: ProgressBar = view.findViewById(R.id.progress)
        val clear: ImageButton = view.findViewById(R.id.clearButton)
    }

    /* State Saving and Restoring */

    public override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(SUPER_STATE, super.onSaveInstanceState())
        bundle.putInt(TEXT_POSITION, ui.location.selectionStart)
        bundle.putSerializable(LOCATION, location)
        if (location == null && ui.location.text.isNotEmpty()) {
            bundle.putString(TEXT, ui.location.text.toString())
        }
        return bundle
    }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) { // implicit null check
            val loc = state.getSerializable(LOCATION) as WrapLocation?
            val text = state.getString(TEXT)
            if (loc != null) {
                setLocation(loc)
            } else if (!isNullOrEmpty(text)) {
                ui.location.setText(text)
                ui.clear.visibility = VISIBLE
            }
            val position = state.getInt(TEXT_POSITION)
            ui.location.setSelection(position)

            // replace state by super state
            super.onRestoreInstanceState(state.getParcelable(SUPER_STATE))
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        // Makes sure that the state of the child views are not saved
        super.dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        // Makes sure that the state of the child views are not restored
        super.dispatchThawSelfOnly(container)
    }

    fun setTransportNetwork(transportNetwork: TransportNetwork) {
        this.transportNetwork = transportNetwork
    }

    fun setHomeLocation(homeLocation: HomeLocation?) {
        adapter.setHomeLocation(homeLocation)
    }

    fun setWorkLocation(workLocation: WorkLocation?) {
        adapter.setWorkLocation(workLocation)
    }

    fun setFavoriteLocations(favoriteLocations: List<FavoriteLocation>) {
        adapter.setFavoriteLocations(favoriteLocations)
    }

    /* Auto-Completion */

    private fun handleTextChanged(s: CharSequence) {
        // show clear button
        if (s.isNotEmpty()) {
            ui.clear.visibility = VISIBLE
            // clear location tag
            setLocation(null, R.drawable.ic_location, false)

            if (s.length >= TYPING_THRESHOLD) {
                onContentChanged()
            }
        } else {
            clearLocationAndShowDropDown(false)
        }
    }

    private fun onContentChanged() {
        ui.progress.visibility = VISIBLE
        startSuggestLocationsTaskDelayed()
    }

    private fun startSuggestLocationsTaskDelayed() {
        if (transportNetwork == null) {
            stopSuggestLocationsTask()
            return
        }
        if (suggestLocationsTaskPending) return
        suggestLocationsTaskPending = true
        postDelayed({
            if (task != null && task!!.status != FINISHED) task!!.cancel(true)
            task = SuggestLocationsTask(transportNetwork!!, this@LocationView)
            task!!.execute(text)
            suggestLocationsTaskPending = false
        }, AUTO_COMPLETION_DELAY.toLong())
    }

    override fun onSuggestLocationsResult(suggestLocationsResult: SuggestLocationsResult?) {
        ui.progress.visibility = GONE
        if (suggestLocationsResult == null) return

        adapter.swapSuggestedLocations(suggestLocationsResult.suggestedLocations, ui.location.text.toString())
    }

    private fun stopSuggestLocationsTask() {
        if (task != null) task!!.cancel(true)
        ui.progress.visibility = GONE
    }

    /* Setter and Getter */

    open fun setLocation(loc: WrapLocation?, @DrawableRes icon: Int, setText: Boolean) {
        location = loc

        if (setText) {
            if (loc != null) {
                ignoreTextChanged = true
                ui.location.setText(loc.getName())
                ignoreTextChanged = false
                ui.location.setSelection(loc.getName().length)
                ui.location.dismissDropDown()
                ui.clear.visibility = VISIBLE
                stopSuggestLocationsTask()
            } else {
                if (ui.location.text.isNotEmpty()) ui.location.text = null
                ui.clear.visibility = GONE
            }
        }
        ui.status.setImageResource(icon)
    }

    fun setLocation(loc: WrapLocation?) {
        setLocation(loc, loc?.drawable ?: R.drawable.ic_location, true)
    }

    fun getLocation(): WrapLocation? {
        return this.location
    }

    fun setHint(@StringRes hint: Int) {
        ui.location.setHint(hint)
    }

    /* Behavior */

    private fun onFocusChange(v: View, hasFocus: Boolean) {
        if (v is AutoCompleteTextView && ViewCompat.isAttachedToWindow(v)) {
            if (hasFocus) {
                v.showDropDown()
            } else {
                hideSoftKeyboard()
            }
        }
    }

    private fun onLocationItemClick(loc: WrapLocation) {
        setLocation(loc)  // TODO set via ViewModel
        ui.location.requestFocus()

        // hide soft-keyboard
        hideSoftKeyboard()

        if (listener != null) listener!!.onLocationItemClick(loc, type)
    }

    fun onClick() {
        if (adapter.count > 0) {
            ui.location.showDropDown()
        }
    }

    fun clearLocation() {
        clearLocation(true)
    }

    private fun clearLocation(setText: Boolean) {
        setLocation(null, R.drawable.ic_location, setText)
        adapter.resetSearchTerm()
    }

    protected open fun clearLocationAndShowDropDown(setText: Boolean) {
        clearLocation(setText)
        stopSuggestLocationsTask()
        reset()
        if (listener != null) listener!!.onLocationCleared(type)
        ui.clear.visibility = GONE
        if (isShown) {
            showSoftKeyboard()
            ui.location.requestFocus()
            ui.location.post { ui.location.showDropDown() }
        }
    }

    fun reset() {
        adapter.reset()
    }

    private fun showSoftKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(ui.location, 0)
    }

    private fun hideSoftKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(ui.location.windowToken, 0)
    }

    /* Listener */

    fun setLocationViewListener(listener: LocationViewListener) {
        this.listener = listener
    }

    interface LocationViewListener {
        fun onLocationItemClick(loc: WrapLocation, type: FavLocationType)

        fun onLocationCleared(type: FavLocationType)
    }

}
