package de.grobox.transportr.departures;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.Date;

import de.grobox.transportr.R;
import de.grobox.transportr.ui.LineView;
import de.schildbach.pte.dto.Departure;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.transportr.utils.DateUtils.getDelay;
import static de.grobox.transportr.utils.DateUtils.getDelayString;
import static de.grobox.transportr.utils.DateUtils.getTime;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;
import static de.grobox.transportr.utils.TransportrUtils.getTintedDrawable;
import static de.grobox.transportr.utils.TransportrUtils.setRelativeDepartureTime;

class DepartureViewHolder extends RecyclerView.ViewHolder {

	private final CardView card;
	private final LineView line;
	private final TextView lineName;
	private final TextView timeRel;
	private final TextView timeAbs;
	private final TextView delay;
	private final ImageView arrow;
	private final TextView destination;
	private final TextView position;
	private final TextView message;

	DepartureViewHolder(View v) {
		super(v);

		card = (CardView) v;
		line = v.findViewById(R.id.line);
		lineName = v.findViewById(R.id.lineNameView);
		timeRel = v.findViewById(R.id.departureTimeRel);
		timeAbs = v.findViewById(R.id.departureTimeAbs);
		delay = v.findViewById(R.id.delay);
		arrow = v.findViewById(R.id.arrowView);
		destination = v.findViewById(R.id.destinationView);
		position = v.findViewById(R.id.positionView);
		message = v.findViewById(R.id.messageView);
	}

	void bind(Departure dep) {
		// times and delay
		if (dep.plannedTime != null) {
			bindTimeAbs(dep.plannedTime);
			if (dep.predictedTime != null) {
				setRelativeDepartureTime(timeRel, dep.predictedTime);
				bindDelay(getDelay(dep.plannedTime, dep.predictedTime));
			} else {
				setRelativeDepartureTime(timeRel, dep.plannedTime);
				bindDelay(0);
			}
		} else if (dep.predictedTime != null) {
			bindTimeAbs(dep.predictedTime);
			setRelativeDepartureTime(timeRel, dep.predictedTime);
			bindDelay(0);
		} else {
			throw new RuntimeException();
		}

		// line icon and name
		line.setLine(dep.line);
		lineName.setText(dep.line.name);

		// line destination
		arrow.setImageDrawable(getTintedDrawable(arrow.getContext(), arrow.getDrawable()));
		if (dep.destination != null) {
			destination.setText(getLocationName(dep.destination));
		} else {
			destination.setText(null);
		}

		// platform/position
		if (dep.position != null) {
			position.setText(dep.position.name);
			position.setVisibility(VISIBLE);
		} else {
			position.setVisibility(GONE);
		}

		// show message if available
		if (Strings.isNullOrEmpty(dep.message)) {
			message.setVisibility(GONE);
		} else {
			message.setText(dep.message);
			message.setVisibility(VISIBLE);
		}

		// TODO show line from here on
		card.setClickable(false);
	}

	private void bindTimeAbs(Date date) {
		timeAbs.setText(getTime(timeAbs.getContext(), date));
	}

	private void bindDelay(long delayTime) {
		if (delayTime == 0) {
			delay.setVisibility(GONE);
		} else {
			delay.setText(getDelayString(delayTime));
			delay.setVisibility(VISIBLE);
			// TODO color delay green if negative (never seen in the wild so far)
		}
	}

}
