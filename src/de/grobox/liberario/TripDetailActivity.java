package de.grobox.liberario;

import java.util.Date;
import java.util.List;

import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Trip.Individual;
import de.schildbach.pte.dto.Trip.Leg;
import de.schildbach.pte.dto.Trip.Public;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class TripDetailActivity extends Activity {
	private TableLayout view;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trip_details);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		view = (TableLayout) findViewById(R.id.trip_details);

		Intent intent = getIntent();

		Trip trip = (Trip) intent.getSerializableExtra("de.schildbach.pte.dto.Trip");
		addHeader(trip);
		addLegs(trip.legs);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();

				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// TODO remove deprecated code
	@SuppressWarnings("deprecation")
	private void addHeader(Trip trip) {
		Date d = trip.getFirstDepartureTime();

		((TextView) findViewById(R.id.tripDetailsDurationView)).setText(DateUtils.getDuration(trip.getFirstDepartureTime(), trip.getLastArrivalTime()));
		((TextView) findViewById(R.id.tripDetailsDateView)).setText(DateUtils.formatDate(getBaseContext(), d.getYear()+1900, d.getMonth(), d.getDate()));
	}

	private void addLegs(List<Leg> legs) {
		int i = 1;
		TableRow legViewOld = (TableRow) LayoutInflater.from(this).inflate(R.layout.trip_details_row, null);
		TableRow legViewNew;

		// for each leg
		for(final Leg leg : legs) {
			legViewNew = (TableRow) LayoutInflater.from(this).inflate(R.layout.trip_details_row, null);

			TextView dDepartureViewNew = ((TextView) legViewNew.findViewById(R.id.dDepartureView));

			// only for the first leg
			if(i == 1) {
				// hide arrival time for start location of trip
				((LinearLayout) legViewOld.findViewById(R.id.dArrivialLinearLayout)).setVisibility(View.GONE);
				// only add old view the first time, because it isn't old there
				view.addView(legViewOld);
			}
			// only for the last leg
			if(i >= legs.size()) {
				// span last row 
				TableRow.LayoutParams params = (TableRow.LayoutParams) dDepartureViewNew.getLayoutParams();
				params.span = 3;
				dDepartureViewNew.setLayoutParams(params);

				// hide stuff for last stop (destination)
				((LinearLayout) legViewNew.findViewById(R.id.dDepartureLinearLayout)).setVisibility(View.GONE);
				((TextView) legViewNew.findViewById(R.id.dDestinationView)).setVisibility(View.GONE);
				((TextView) legViewNew.findViewById(R.id.dLineView)).setVisibility(View.GONE);
				((TextView) legViewNew.findViewById(R.id.dMessageView)).setVisibility(View.GONE);
			}

			if(leg instanceof Trip.Public) {
				Public public_line = ((Public) leg);
				((TextView) legViewOld.findViewById(R.id.dDepartureTimeView)).setText(DateUtils.getTime(public_line.departureStop.getDepartureTime()));
				// TODO public_line.getDepartureDelay()
				((TextView) legViewOld.findViewById(R.id.dDepartureView)).setText(public_line.departureStop.location.name);
				((TextView) legViewOld.findViewById(R.id.dLineView)).setText(public_line.line.label.substring(1, public_line.line.label.length()));
				if(public_line.destination != null) {
					((TextView) legViewOld.findViewById(R.id.dDestinationView)).setText(public_line.destination.name);
				}
				((TextView) legViewNew.findViewById(R.id.dArrivalTimeView)).setText(DateUtils.getTime(public_line.arrivalStop.getArrivalTime()));
				// TODO public_line.getArrivalDelay()
				dDepartureViewNew.setText(public_line.arrivalStop.location.name);
				if(public_line.message == null) {
					((TextView) legViewOld.findViewById(R.id.dMessageView)).setVisibility(View.GONE);
				} else {
					((TextView) legViewOld.findViewById(R.id.dMessageView)).setVisibility(View.VISIBLE);
					((TextView) legViewOld.findViewById(R.id.dMessageView)).setText(public_line.message);
				}
				// get and add intermediate stops
				view.addView(getStops(public_line.intermediateStops));

				// make intermediate stops fold out and in on click
				legViewOld.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						ViewGroup parent = ((ViewGroup) view.getParent());
						View v = parent.getChildAt(parent.indexOfChild(view) + 1);
						if(v != null) {
							if(v.getVisibility() == View.GONE) {
								v.setVisibility(View.VISIBLE);
							}
							else if(v.getVisibility() == View.VISIBLE) {
								v.setVisibility(View.GONE);
							}
						}
					}

				});
			}
			else if(leg instanceof Trip.Individual) {
				Individual individual = (Trip.Individual) leg;

				((TextView) legViewOld.findViewById(R.id.dDepartureView)).setText(individual.departure.name);
				((TextView) legViewOld.findViewById(R.id.dDepartureTimeView)).setText(DateUtils.getTime(leg.departureTime));
				((TextView) legViewOld.findViewById(R.id.dLineView)).setText("W");
				((TextView) legViewOld.findViewById(R.id.dDestinationView)).setText(Integer.toString(individual.min) + " min " + Integer.toString(individual.distance) + " m");
				((TextView) legViewNew.findViewById(R.id.dArrivalTimeView)).setText(DateUtils.getTime(leg.arrivalTime));
				((TextView) legViewNew.findViewById(R.id.dDestinationView)).setText(individual.arrival.name);
				dDepartureViewNew.setText(individual.arrival.name);
			}

			view.addView(legViewNew);

			// save new leg view for next run of the loop
			legViewOld = legViewNew;
			i += 1;
		}
	}

	private TableLayout getStops(List<Stop> stops) {
		TableLayout stopsView = new TableLayout(this);
		stopsView.setVisibility(View.GONE);

		if(stops != null) {
			for(final Stop stop : stops) {
				TableRow stopView = (TableRow) LayoutInflater.from(this).inflate(R.layout.stop, null);
				Date arrivalTime = stop.getArrivalTime();
				Date departureTime = stop.getDepartureTime();

				if(arrivalTime != null) {
					((TextView) stopView.findViewById(R.id.sArrivalTimeView)).setText(DateUtils.getTime(arrivalTime));
				}
				else {
					((TextView) stopView.findViewById(R.id.sArrivalTimeView)).setVisibility(View.GONE);
				}
				if(departureTime != null) {
					((TextView) stopView.findViewById(R.id.sDepartureTimeView)).setText(DateUtils.getTime(departureTime));
				}
				else {
					((TextView) stopView.findViewById(R.id.sDepartureTimeView)).setVisibility(View.GONE);
				}
				((TextView) stopView.findViewById(R.id.sLocationView)).setText(stop.location.name);

				stopsView.addView(stopView);
			}
		}

		return stopsView;
	}

}
