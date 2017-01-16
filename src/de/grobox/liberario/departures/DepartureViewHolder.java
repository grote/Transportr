package de.grobox.liberario.departures;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;

import java.util.Date;

import de.grobox.liberario.R;
import de.grobox.liberario.ui.LineView;
import de.schildbach.pte.dto.Departure;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.liberario.utils.DateUtils.getDelay;
import static de.grobox.liberario.utils.DateUtils.getDelayString;
import static de.grobox.liberario.utils.DateUtils.getDifferenceInMinutes;
import static de.grobox.liberario.utils.DateUtils.getTime;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;
import static de.grobox.liberario.utils.TransportrUtils.getTintedDrawable;

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
		line = (LineView) v.findViewById(R.id.line);
		lineName = (TextView) v.findViewById(R.id.lineNameView);
		timeRel = (TextView) v.findViewById(R.id.departureTimeRel);
		timeAbs = (TextView) v.findViewById(R.id.departureTimeAbs);
		delay = (TextView) v.findViewById(R.id.delay);
		arrow = (ImageView) v.findViewById(R.id.arrowView);
		destination = (TextView) v.findViewById(R.id.destinationView);
		position = (TextView) v.findViewById(R.id.positionView);
		message = (TextView) v.findViewById(R.id.messageView);
	}

	void bind(Departure dep) {
		// times and delay
		if (dep.plannedTime != null) {
			bindTimeAbs(dep.plannedTime);
			if (dep.predictedTime != null) {
				bindTimeRel(dep.predictedTime);
				bindDelay(getDelay(dep.plannedTime, dep.predictedTime));
			} else {
				bindTimeRel(dep.plannedTime);
				bindDelay(0);
			}
		} else {
			bindTimeAbs(dep.predictedTime);
			bindTimeRel(dep.predictedTime);
			bindDelay(0);
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

	private void bindTimeRel(Date date) {
		long difference = getDifferenceInMinutes(date);
		if (difference > 99 || difference < -99) {
			timeRel.setVisibility(GONE);
		} else if (difference == 0) {
			timeRel.setText(timeRel.getContext().getString(R.string.now_small));
			timeRel.setVisibility(VISIBLE);
		} else if (difference > 0) {
			timeRel.setText(timeRel.getContext().getString(R.string.in_x_minutes, difference));
			timeRel.setVisibility(VISIBLE);
		} else {
			timeRel.setText(timeRel.getContext().getString(R.string.x_minutes_ago, difference * -1));
			timeRel.setVisibility(VISIBLE);
		}
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
