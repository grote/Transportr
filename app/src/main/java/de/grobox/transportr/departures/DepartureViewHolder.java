/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2018 Torsten Grote
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

package de.grobox.transportr.departures;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
import static de.grobox.transportr.utils.DateUtils.setRelativeDepartureTime;

class DepartureViewHolder extends RecyclerView.ViewHolder {

	private final CardView card;
	private final LineView line;
	private final TextView lineName;
	private final TextView timeRel;
	private final TextView timeAbs;
	private final TextView delay;
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
			if (delayTime <= 0) delay.setTextColor(ContextCompat.getColor(delay.getContext(), R.color.md_green_500));
			else delay.setTextColor(ContextCompat.getColor(delay.getContext(), R.color.md_red_500));
		}
	}

}
