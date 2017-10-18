/*    Transportr
 *    Copyright (C) 2013 - 2017 Torsten Grote
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

package de.grobox.transportr.trips.search;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import de.grobox.transportr.R;
import de.grobox.transportr.trips.search.TripAdapter.OnTripClickListener;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Individual;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.grobox.transportr.utils.DateUtils.getDelayText;
import static de.grobox.transportr.utils.DateUtils.getDuration;
import static de.grobox.transportr.utils.DateUtils.getTime;
import static de.grobox.transportr.utils.TransportrUtils.addLineBox;
import static de.grobox.transportr.utils.TransportrUtils.addWalkingBox;
import static de.grobox.transportr.utils.TransportrUtils.getLocationName;
import static de.grobox.transportr.utils.TransportrUtils.setRelativeDepartureTime;

class TripViewHolder extends RecyclerView.ViewHolder {

	private final View root;
	private final TextView fromTimeRel;
	private final TextView fromTime, fromLocation, fromDelay;
	private final TextView toTime, toLocation, toDelay;
	private final ViewGroup lines;
	private final TextView duration;

	TripViewHolder(View v) {
		super(v);
		root = v;
		fromTimeRel = v.findViewById(R.id.fromTimeRel);
		fromTime = v.findViewById(R.id.fromTime);
		fromLocation = v.findViewById(R.id.fromLocation);
		fromDelay = v.findViewById(R.id.fromDelay);
		toTime = v.findViewById(R.id.toTime);
		toLocation = v.findViewById(R.id.toLocation);
		toDelay = v.findViewById(R.id.toDelay);
		lines = v.findViewById(R.id.lines);
		duration = v.findViewById(R.id.duration);
	}

	void bind(final Trip trip, OnTripClickListener listener) {
		// Relative Departure Time
		setRelativeDepartureTime(fromTimeRel, trip.getFirstDepartureTime());

		// Departure Time
		Leg firstLeg = trip.legs.get(0);
		if (firstLeg instanceof Public) {
			fromTime.setText(getTime(fromTime.getContext(), ((Public) firstLeg).getDepartureTime(true)));
		} else {
			fromTime.setText(getTime(fromTime.getContext(), firstLeg.getDepartureTime()));
		}

		// Departure Delay
		Public publicLeg = trip.getFirstPublicLeg();
		if (publicLeg != null && publicLeg.getDepartureDelay() != null && publicLeg.getDepartureDelay() != 0) {
			fromDelay.setText(getDelayText(publicLeg.getDepartureDelay()));
			fromDelay.setVisibility(VISIBLE);
		} else {
			fromDelay.setVisibility(GONE);
		}
		fromLocation.setText(getLocationName(trip.from));

		// Lines and Trip Duration
		lines.removeAllViews();
		for (Leg leg : trip.legs) {
			if (leg instanceof Public) {
				addLineBox(lines.getContext(), lines, ((Public) leg).line);
			} else if (leg instanceof Individual) {
				addWalkingBox(lines.getContext(), lines);
			} else {
				throw new RuntimeException();
			}
		}
		duration.setText(getDuration(trip.getDuration()));

		// Arrival Time
		Leg lastLeg = trip.legs.get(trip.legs.size() - 1);
		if (lastLeg instanceof Public) {
			toTime.setText(getTime(toTime.getContext(), ((Public) lastLeg).getDepartureTime(true)));
		} else {
			toTime.setText(getTime(toTime.getContext(), lastLeg.getDepartureTime()));
		}

		// Arrival Delay
		publicLeg = trip.getLastPublicLeg();
		if (publicLeg != null && publicLeg.getArrivalDelay() != null && publicLeg.getArrivalDelay() != 0) {
			toDelay.setText(getDelayText(publicLeg.getArrivalDelay()));
			toDelay.setVisibility(VISIBLE);
		} else {
			toDelay.setVisibility(GONE);
		}
		toLocation.setText(getLocationName(trip.to));

		// Click Listener
		root.setOnClickListener(view -> listener.onClick(trip));
	}

}
