package de.grobox.liberario.trips.detail;


import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.liberario.R;
import de.grobox.liberario.ui.LegPopupMenu;
import de.schildbach.pte.dto.Stop;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;
import static de.grobox.liberario.utils.TransportrUtils.setArrivalTimes;
import static de.grobox.liberario.utils.TransportrUtils.setDepartureTimes;

class StopViewHolder extends ViewHolder {

	private final TextView arrivalTime;
	private final TextView arrivalDelay;
	private final TextView departureTime;
	private final TextView departureDelay;
	private final ImageView circle;
	private final TextView stopLocation;
	private final ImageButton stopButton;

	StopViewHolder(View v) {
		super(v);
		arrivalTime = v.findViewById(R.id.arrivalTime);
		arrivalDelay = v.findViewById(R.id.arrivalDelay);
		departureTime = v.findViewById(R.id.departureTime);
		departureDelay = v.findViewById(R.id.departureDelay);
		circle = v.findViewById(R.id.circle);
		stopLocation = v.findViewById(R.id.stopLocation);
		stopButton = v.findViewById(R.id.stopButton);
	}

	void bind(Stop stop, LegClickListener listener, int color) {
		if (stop.getArrivalTime() != null) {
			setArrivalTimes(arrivalTime.getContext(), arrivalTime, arrivalDelay, stop);
		} else {
			arrivalDelay.setVisibility(GONE);
		}

		if (stop.getDepartureTime() != null) {
			if (stop.getDepartureTime().equals(stop.getArrivalTime())) {
				departureTime.setVisibility(GONE);
				departureDelay.setVisibility(GONE);
			} else {
				setDepartureTimes(departureTime.getContext(), departureTime, departureDelay, stop);
				departureTime.setVisibility(VISIBLE);
			}
		} else {
			departureTime.setVisibility(GONE);
			departureDelay.setVisibility(GONE);
		}

		circle.setColorFilter(color);

		stopLocation.setText(getLocationName(stop.location));
		stopLocation.setOnClickListener(view -> listener.onLocationClick(stop.location));

		// platforms
//		if (stop.plannedArrivalPosition != null) {
//			arrivalPlatform.setText(stop.plannedArrivalPosition.name);
//		}
//		if (stop.plannedDeparturePosition != null) {
//			departurePlatform.setText(stop.plannedDeparturePosition.name);
//		}

		// show popup on button click
		stopButton.setOnClickListener(view -> new LegPopupMenu(stopButton.getContext(), stopButton, stop).show());
	}

}
