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
import android.os.Build
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
import de.grobox.transportr.databinding.FragmentTimeDateBinding
import java.util.*
import java.util.Calendar.*

class TimeDateFragment : DialogFragment(), OnDateSetListener, OnTimeChangedListener {

    private var listener: TimeDateListener? = null
    private var departure: Boolean? = null // null means no departure/arrival selection will be possible
    private lateinit var calendar: Calendar
    private lateinit var viewBinding: FragmentTimeDateBinding

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewBinding = FragmentTimeDateBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Time
        viewBinding.timePicker.setIs24HourView(android.text.format.DateFormat.is24HourFormat(context))
        viewBinding.timePicker.setOnTimeChangedListener(this)
        showTime(calendar)

        // Date
        viewBinding.dateView.setOnClickListener {
            DatePickerDialog(requireContext(), this@TimeDateFragment, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
                    .show()
        }
        showDate(calendar)

        // Previous and Next Date
        viewBinding.prevDateButton.setOnClickListener {
            calendar.add(DAY_OF_MONTH, -1)
            showDate(calendar)
        }
        viewBinding.nextDateButton.setOnClickListener {
            calendar.add(DAY_OF_MONTH, 1)
            showDate(calendar)
        }

        // Departure or Arrival
        departure?.let {
            viewBinding.departureButton.isChecked = it
            viewBinding.arrivalButton.isChecked = !it

            viewBinding.departureButton.setOnClickListener {
                departure = viewBinding.departureButton.isChecked
                viewBinding.arrivalButton.isChecked = !departure!!
            }

            viewBinding.arrivalButton.setOnClickListener {
                departure = !viewBinding.arrivalButton.isChecked
                viewBinding.departureButton.isChecked = departure!!
            }
        } ?: run {
            viewBinding.departureButton.visibility = GONE
            viewBinding.arrivalButton.visibility = GONE
        }

        // Buttons
        viewBinding.okButton.setOnClickListener {
            listener?.onTimeAndDateSet(calendar)
            departure?.let { listener?.onDepartureOrArrivalSet(it) }
            dismiss()
        }
        viewBinding.nowButton.setOnClickListener {
            listener?.onTimeAndDateSet(Calendar.getInstance())
            departure?.let { listener?.onDepartureOrArrivalSet(it) }
            dismiss()
        }
        viewBinding.cancelButton.setOnClickListener {
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
        val hour = c.get(HOUR_OF_DAY)
        val min = c.get(MINUTE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            viewBinding.timePicker.hour = hour
            viewBinding.timePicker.minute = min
        } else {
            viewBinding.timePicker.currentHour = hour
            viewBinding.timePicker.currentMinute = min
        }
    }

    private fun showDate(c: Calendar) {
        val now = Calendar.getInstance()
        viewBinding.dateView.text = when {
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
