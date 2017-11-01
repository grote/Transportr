package de.grobox.transportr.trips.detail;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.Date;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Stop;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.transportr.utils.DateUtils.getDelayText;
import static de.grobox.transportr.utils.DateUtils.getTime;

abstract class BaseViewHolder extends RecyclerView.ViewHolder {

	protected final TextView fromTime;
	protected final TextView toTime;
	final TextView fromDelay;
	final TextView toDelay;

	BaseViewHolder(View v) {
		super(v);
		fromTime = v.findViewById(R.id.fromTime);
		toTime = v.findViewById(R.id.toTime);
		fromDelay = v.findViewById(R.id.fromDelay);
		toDelay = v.findViewById(R.id.toDelay);
	}

	void setArrivalTimes(TextView timeView, TextView delayView, Stop stop) {
		if(stop.getArrivalTime() == null) return;

		Date time = new Date(stop.getArrivalTime().getTime());

		if(stop.isArrivalTimePredicted() && stop.getArrivalDelay() != null) {
			long delay = stop.getArrivalDelay();
			time.setTime(time.getTime() - delay);
			delayView.setText(getDelayText(delay));
			if (delay <= 0) delayView.setTextColor(ContextCompat.getColor(delayView.getContext(), R.color.md_green_500));
			else delayView.setTextColor(ContextCompat.getColor(delayView.getContext(), R.color.md_red_500));
			delayView.setVisibility(VISIBLE);
		} else {
			delayView.setVisibility(GONE);
		}
		timeView.setText(getTime(timeView.getContext(), time));
	}

	void setDepartureTimes(TextView timeView, TextView delayView, Stop stop) {
		if(stop.getDepartureTime() == null) return;

		Date time = new Date(stop.getDepartureTime().getTime());

		if(stop.isDepartureTimePredicted() && stop.getDepartureDelay() != null) {
			long delay = stop.getDepartureDelay();
			time.setTime(time.getTime() - delay);
			delayView.setText(getDelayText(delay));
			if (delay <= 0) delayView.setTextColor(ContextCompat.getColor(delayView.getContext(), R.color.md_green_500));
			else delayView.setTextColor(ContextCompat.getColor(delayView.getContext(), R.color.md_red_500));
			delayView.setVisibility(VISIBLE);
		} else {
			delayView.setVisibility(GONE);
		}
		timeView.setText(getTime(timeView.getContext(), time));
	}

}
