/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2020 Torsten Grote
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

package de.grobox.transportr.ui

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import de.grobox.transportr.R

/**
 * An editable text preference that gives immediate user feedback by
 * disabling the "OK" button of the edit dialog if the entered text
 * does not match the validation criteria.
 *
 * Validation can be configured by setting the <code>validate</code>
 * property.
 */
class ValidatedEditTextPreference(ctx: Context, attrs: AttributeSet) :
    Preference(ctx, attrs, R.attr.preferenceStyle) {

    var validate: ((String) -> Boolean) = { true }
    private var persistedValue: String = ""
    private var currentValue: String = persistedValue

    init {
        setSummaryProvider { persistedValue }

        setOnPreferenceClickListener {
            currentValue = persistedValue

            val dialog = AlertDialog.Builder(ctx)
                .setTitle(title)
                .setView(R.layout.dialog_validated_edit_text_preference)
                .setCancelable(true)
                .setPositiveButton(R.string.ok) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                    if (callChangeListener(currentValue)) {
                        persistString(currentValue)
                        persistedValue = currentValue
                        notifyChanged()
                    }
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, _ ->
                    dialogInterface.cancel()
                    currentValue = persistedValue
                }
                .create()
            dialog.show()

            val editText = dialog.findViewById<EditText>(R.id.text_input)!!
            val okButton = dialog.findViewById<Button>(android.R.id.button1)!!

            fun updateUIForValidity() {
                okButton.isEnabled = validate(currentValue)
            }
            updateUIForValidity()

            editText.setText(currentValue, TextView.BufferType.NORMAL)
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(text: Editable?) {
                    currentValue = text.toString()
                    updateUIForValidity()
                }
            })
            editText.postDelayed({
                editText.requestFocus()
                editText.setSelection(currentValue.length)
                val inputMethodManager = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)

            true
        }
    }

    override fun onGetDefaultValue(a: TypedArray?, index: Int): String? {
        return a!!.getString(index)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        persistedValue = getPersistedString(defaultValue?.toString()) ?: persistedValue
        currentValue = persistedValue
    }
}
