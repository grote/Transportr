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

package de.grobox.transportr.ui

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.text.format.DateFormat.getDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.TimePicker.OnTimeChangedListener
import androidx.fragment.app.DialogFragment
import de.grobox.transportr.R
import kotlinx.android.synthetic.main.fragment_time_date.*
import java.util.*
import java.util.Calendar.*

class TimeDateFragment : DialogFragment(), OnDateSetListener, OnTimeChangedListener {

    private var listener: TimeDateListener? = null
    private var departure: Boolean? = null // null means no departure/arrival selection will be possible
    private lateinit var calendar: Calendar

    companion object {
        @JvmField
        val TAG: String = TimeDateFragment::class.java.simpleName
        private val CALENDAR = "calendar"
        private val DEPARTURE = "departure"

        @JvmStatic
        fun newInstance(calendar: Calendar, departure: Boolean? = null): TimeDateFragment {
            val f = TimeDateFragment()

            val args = Bundle()
            args.putSerializable(CALENDAR, calendar)
            args.putSerializable(DEPARTURE, departure)
            f.arguments = args

            return f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            arguments?.let {
                calendar = it.getSerializable(CALENDAR) as Calendar
                departure = it.getSerializable(DEPARTURE) as Boolean?
            } ?: throw IllegalArgumentException("Arguments missing")
        } else {
            calendar = savedInstanceState.getSerializable(CALENDAR) as Calendar
            departure = savedInstanceState.getSerializable(DEPARTURE) as Boolean?
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_time_date, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Time
        timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(context))
        timePicker.setOnTimeChangedListener(this)
        showTime(calendar)

        // Date
        dateView.setOnClickListener {
            DatePickerDialog(context!!, this@TimeDateFragment, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
                    .show()
        }
        showDate(calendar)

        // Previous and Next Date
        prevDateButton.setOnClickListener {
            calendar.add(DAY_OF_MONTH, -1)
            showDate(calendar)
        }
        nextDateButton.setOnClickListener {
            calendar.add(DAY_OF_MONTH, 1)
            showDate(calendar)
        }

        // Departure or Arrival
        departure?.let {
            departureButton.isChecked = it
            arrivalButton.isChecked = !it

            departureButton.setOnClickListener {
                departure = departureButton.isChecked
                arrivalButton.isChecked = !departure!!
            }

            arrivalButton.setOnClickListener {
                departure = !arrivalButton.isChecked
                departureButton.isChecked = departure!!
            }
        } ?: run {
            departureButton.visibility = GONE
            arrivalButton.visibility = GONE
        }

        // Buttons
        okButton.setOnClickListener {
            listener?.onTimeAndDateSet(calendar)
            departure?.let { listener?.onDepartureOrArrivalSet(it) }
            dismiss()
        }
        nowButton.setOnClickListener {
            listener?.onTimeAndDateSet(Calendar.getInstance())
            departure?.let { listener?.onDepartureOrArrivalSet(it) }
            dismiss()
        }
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CALENDAR, calendar)
        outState.putSerializable(DEPARTURE, departure)
    }

    override fun onTimeChanged(timePicker: TimePicker, hourOfDay: Int, minute: Int) {
        calendar.set(HOUR_OF_DAY, hourOfDay)
        calendar.set(MINUTE, minute)
    }

    override fun onDateSet(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        calendar.set(YEAR, year)
        calendar.set(MONTH, month)
        calendar.set(DAY_OF_MONTH, day)
        showDate(calendar)
    }

    fun setTimeDateListener(listener: TimeDateListener) {
        this.listener = listener
    }

    @Suppress("DEPRECATION")
    private fun showTime(c: Calendar) {
        timePicker.currentHour = c.get(HOUR_OF_DAY)
        timePicker.currentMinute = c.get(MINUTE)
    }

    private fun showDate(c: Calendar) {
        val now = Calendar.getInstance()
        dateView.text = when {
            c.isYesterday(now) -> getString(R.string.yesterday)
            c.isToday(now) -> getString(R.string.today)
            c.isTomorrow(now) -> getString(R.string.tomorrow)
            else -> getDateFormat(context?.applicationContext).format(calendar.time)
        }
    }

    private fun Calendar.isSameMonth(c: Calendar) = c.get(YEAR) == get(YEAR) && c.get(MONTH) == get(MONTH)
    private fun Calendar.isYesterday(now: Calendar) = isSameMonth(now) && get(DAY_OF_MONTH) == now.get(DAY_OF_MONTH) - 1
    private fun Calendar.isToday(now: Calendar) = isSameMonth(now) && get(DAY_OF_MONTH) == now.get(DAY_OF_MONTH)
    private fun Calendar.isTomorrow(now: Calendar) = isSameMonth(now) && get(DAY_OF_MONTH) == now.get(DAY_OF_MONTH) + 1

    interface TimeDateListener {
        fun onTimeAndDateSet(calendar: Calendar)
        fun onDepartureOrArrivalSet(departure: Boolean) {  }
    }

}
