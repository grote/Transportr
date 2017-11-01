package de.grobox.transportr.trips.detail;


import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import de.grobox.transportr.R;
import de.schildbach.pte.dto.Stop;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;

class StopViewHolder extends BaseViewHolder {

	private final ImageView circle;
	private final TextView stopLocation;
	private final ImageButton stopButton;

	StopViewHolder(View v) {
		super(v);
		circle = v.findViewById(R.id.circle);
		stopLocation = v.findViewById(R.id.stopLocation);
		stopButton = v.findViewById(R.id.stopButton);
	}

	void bind(Stop stop, LegClickListener listener, int color) {
		if (stop.getArrivalTime() != null) {
			setArrivalTimes(fromTime, fromDelay, stop);
		} else {
			fromDelay.setVisibility(GONE);
		}

		if (stop.getDepartureTime() != null) {
			if (stop.getDepartureTime().equals(stop.getArrivalTime())) {
				toTime.setVisibility(GONE);
				toDelay.setVisibility(GONE);
			} else {
				setDepartureTimes(toTime, toDelay, stop);
				toTime.setVisibility(VISIBLE);
			}
		} else {
			toTime.setVisibility(GONE);
			toDelay.setVisibility(GONE);
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
