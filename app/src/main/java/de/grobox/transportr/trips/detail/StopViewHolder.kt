package de.grobox.transportr.trips.detail


import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import de.grobox.transportr.utils.TransportrUtils.getLocationName
import de.schildbach.pte.dto.Stop
import kotlinx.android.synthetic.main.list_item_stop.view.*


internal class StopViewHolder(v: View, listener: LegClickListener) : BaseViewHolder(v, listener) {

    private val circle: ImageView = v.circle
    private val stopLocation: TextView = v.stopLocation
    private val stopButton: ImageButton = v.stopButton

    fun bind(stop: Stop, color: Int) {
        if (stop.arrivalTime != null) {
            setArrivalTimes(fromTime, fromDelay, stop)
        } else {
            fromDelay.visibility = GONE
        }

        if (stop.departureTime != null) {
            if (stop.departureTime == stop.arrivalTime) {
                toTime.visibility = GONE
                toDelay.visibility = GONE
            } else {
                setDepartureTimes(toTime, toDelay, stop)
                toTime.visibility = VISIBLE
            }
        } else {
            toTime.visibility = GONE
            toDelay.visibility = GONE
        }

        circle.setColorFilter(color)

        stopLocation.text = getLocationName(stop.location)
        stopLocation.setOnClickListener { listener.onLocationClick(stop.location) }
        stopLocation.addPlatform(stop.arrivalPosition)

        // show popup on button click
        stopButton.setOnClickListener { LegPopupMenu(stopButton.context, stopButton, stop).show() }
    }

}
