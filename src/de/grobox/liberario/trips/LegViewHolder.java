package de.grobox.liberario.trips;

import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;

import de.grobox.liberario.R;
import de.grobox.liberario.ui.LegPopupMenu;
import de.grobox.liberario.ui.LineView;
import de.grobox.liberario.utils.DateUtils;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Individual;
import de.schildbach.pte.dto.Trip.Leg;

import static android.support.v4.content.ContextCompat.getColor;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.common.base.Strings.isNullOrEmpty;
import static de.grobox.liberario.trips.LegViewHolder.LegType.FIRST;
import static de.grobox.liberario.trips.LegViewHolder.LegType.FIRST_LAST;
import static de.grobox.liberario.trips.LegViewHolder.LegType.MIDDLE;
import static de.grobox.liberario.utils.DateUtils.getDuration;
import static de.grobox.liberario.utils.TransportrUtils.getLocationName;
import static de.grobox.liberario.utils.TransportrUtils.setArrivalTimes;
import static de.grobox.liberario.utils.TransportrUtils.setDepartureTimes;
import static de.schildbach.pte.dto.Style.RED;


class LegViewHolder extends ViewHolder {

	private final static int DEFAULT_LINE_COLOR = RED;
	enum LegType {FIRST, MIDDLE, LAST, FIRST_LAST}

	private final TextView fromTime;
	private final TextView fromDelay;
	private final ImageView fromCircle;
	private final TextView fromLocation;
	private final ImageButton fromButton;

	private final ImageView lineBar;
	private final LineView lineView;
	private final TextView lineDestination;
	private final TextView message;
	private final TextView duration;
	private final TextView stopsText;
	private final ImageButton stopsButton;

	private final TextView toTime;
	private final TextView toDelay;
	private final ImageView toCircle;
	private final TextView toLocation;
	private final ImageButton toButton;

	LegViewHolder(View v) {
		super(v);

		fromTime = v.findViewById(R.id.fromTime);
		fromDelay = v.findViewById(R.id.fromDelay);
		fromCircle = v.findViewById(R.id.fromCircle);
		fromLocation = v.findViewById(R.id.fromLocation);
		fromButton = v.findViewById(R.id.fromButton);

		lineBar = v.findViewById(R.id.lineBar);
		lineView = v.findViewById(R.id.lineView);
		lineDestination = v.findViewById(R.id.lineDestination);
		message = v.findViewById(R.id.message);
		duration = v.findViewById(R.id.duration);
		stopsText = v.findViewById(R.id.stopsText);
		stopsButton = v.findViewById(R.id.stopsButton);

		toTime = v.findViewById(R.id.toTime);
		toDelay = v.findViewById(R.id.toDelay);
		toCircle = v.findViewById(R.id.toCircle);
		toLocation = v.findViewById(R.id.toLocation);
		toButton = v.findViewById(R.id.toButton);
	}

	void bind(Leg leg, LegType legType, LegClickListener listener, boolean showLineName) {
		// Locations
		fromLocation.setText(getLocationName(leg.departure));
		toLocation.setText(getLocationName(leg.arrival));

		fromLocation.setOnClickListener(view -> listener.onLocationClick(leg.departure));
		toLocation.setOnClickListener(view -> listener.onLocationClick(leg.arrival));

		fromButton.setOnClickListener(view -> new LegPopupMenu(fromButton.getContext(), fromLocation, leg).show());
		toButton.setOnClickListener(view -> new LegPopupMenu(toButton.getContext(), toLocation, leg, true).show());

		// Line bar
		if (legType == FIRST || legType == FIRST_LAST) {
			fromCircle.setImageResource(R.drawable.leg_circle_end);
		} else {
			fromCircle.setImageResource(R.drawable.leg_circle_middle);
		}
		if (legType == MIDDLE || legType == FIRST) {
			toCircle.setImageResource(R.drawable.leg_circle_middle);
		} else {
			toCircle.setImageResource(R.drawable.leg_circle_end);
		}
		fromCircle.setOnClickListener(view -> listener.onLegClick(leg));
		lineBar.setOnClickListener(view -> listener.onLegClick(leg));
		toCircle.setOnClickListener(view -> listener.onLegClick(leg));

		lineView.setOnClickListener(view -> listener.onLegClick(leg));
		lineDestination.setOnClickListener(view -> listener.onLegClick(leg));

		// Leg duration
		duration.setText(getDuration(leg.getDepartureTime(), leg.getArrivalTime()));

		if(leg instanceof Trip.Public) {
			Trip.Public publicLeg = ((Trip.Public) leg);

			setDepartureTimes(fromTime.getContext(), fromTime, fromDelay, publicLeg.departureStop);
			setArrivalTimes(toTime.getContext(), toTime, toDelay, publicLeg.arrivalStop);

			// Departure Platform
			// TODO
//			if(detail && publicLeg.getDeparturePosition() != null) {
//				departurePlatform.setText(publicLeg.getDeparturePosition().toString());
//			} else {
//				departurePlatform.setVisibility(View.GONE);
//			}

			// Arrival Platform
//			if(detail && publicLeg.getArrivalPosition() != null) {
//				arrivalPlatform.setText(publicLeg.getArrivalPosition().toString());
//			} else {
//				arrivalPlatform.setVisibility(View.GONE);
//			}

			// Line
			lineView.setLine(publicLeg.line);
			if(showLineName && !isNullOrEmpty(publicLeg.line.name)) {
				lineDestination.setText(publicLeg.line.name);
			} else if(publicLeg.destination != null) {
				lineDestination.setText(getLocationName(publicLeg.destination));
			} else {
				lineDestination.setText(null);  // don't hide for constraints
			}

			// Line bar
			int lineColor = getLineColor(publicLeg.line);
			fromCircle.setColorFilter(lineColor);
			lineBar.setColorFilter(lineColor);
			toCircle.setColorFilter(lineColor);

			// Stops
			if (publicLeg.intermediateStops != null && publicLeg.intermediateStops.size() > 0) {
				int numStops = publicLeg.intermediateStops.size();
				String text = stopsText.getContext().getResources().getQuantityString(R.plurals.stops, numStops, numStops);
				stopsText.setText(text);
				stopsText.setVisibility(VISIBLE);
				stopsButton.setVisibility(VISIBLE);
			} else {
				stopsText.setVisibility(GONE);
				stopsButton.setVisibility(GONE);
			}

			// show intermediate stops
			// TODO
//			bindStops(context, leg_holder, publicLeg.intermediateStops);

			// Optional message
			boolean hasText = false;
			if (!Strings.isNullOrEmpty(publicLeg.message)) {
				message.setVisibility(VISIBLE);
				message.setText(fromHtml(publicLeg.message));
				hasText = true;
			}
			if (publicLeg.line.message != null) {
				message.setVisibility(VISIBLE);
				message.setText(message.getText() + "\n" + fromHtml(publicLeg.line.message));
				hasText = true;
			}
			if (!hasText) message.setVisibility(GONE);
		}
		else if(leg instanceof Individual) {
			final Individual walkLeg = (Individual) leg;

			fromTime.setText(DateUtils.getTime(fromTime.getContext(), walkLeg.departureTime));
			toTime.setText(DateUtils.getTime(toTime.getContext(), walkLeg.arrivalTime));

			fromDelay.setVisibility(GONE);
			toDelay.setVisibility(GONE);

			lineView.setWalk();

			// line color
			fromCircle.setColorFilter(getColor(fromCircle.getContext(), R.color.walking));
			lineBar.setColorFilter(getColor(lineBar.getContext(), R.color.walking));
			toCircle.setColorFilter(getColor(toCircle.getContext(), R.color.walking));

			// show distance
			if(walkLeg.distance > 0) {
				lineDestination.setText(Integer.toString(walkLeg.distance) + " " + lineDestination.getContext().getString(R.string.meter));
			} else {
				lineDestination.setText(null);  // don't hide for constraints
			}

			message.setVisibility(GONE);

			stopsText.setVisibility(GONE);
			stopsButton.setVisibility(GONE);
		}
	}

	@ColorInt
	private int getLineColor(Line line) {
		if (line.style == null) return DEFAULT_LINE_COLOR;
		if (line.style.backgroundColor != 0) return line.style.backgroundColor;
		if (line.style.backgroundColor2 != 0) return line.style.backgroundColor2;
		if (line.style.foregroundColor != 0) return line.style.foregroundColor;
		if (line.style.borderColor != 0) return line.style.borderColor;
		return DEFAULT_LINE_COLOR;
	}

}
