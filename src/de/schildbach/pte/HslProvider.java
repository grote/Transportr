/*
 * Copyright 2010-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.schildbach.pte;

import java.net.URLEncoder;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.google.common.base.Joiner;

import de.schildbach.pte.dto.Departure;
import de.schildbach.pte.dto.Line;
import de.schildbach.pte.dto.Location;
import de.schildbach.pte.dto.LocationType;
import de.schildbach.pte.dto.NearbyLocationsResult;
import de.schildbach.pte.dto.Point;
import de.schildbach.pte.dto.Product;
import de.schildbach.pte.dto.QueryDeparturesResult;
import de.schildbach.pte.dto.QueryTripsContext;
import de.schildbach.pte.dto.QueryTripsResult;
import de.schildbach.pte.dto.ResultHeader;
import de.schildbach.pte.dto.StationDepartures;
import de.schildbach.pte.dto.Stop;
import de.schildbach.pte.dto.SuggestLocationsResult;
import de.schildbach.pte.dto.SuggestedLocation;
import de.schildbach.pte.dto.Trip;
import de.schildbach.pte.dto.Style;

import de.schildbach.pte.exception.ParserException;

import de.schildbach.pte.util.HttpClient;
import de.schildbach.pte.util.XmlPullUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @author Mats Sjöberg <mats@sjoberg.fi>
 */
public class HslProvider extends AbstractNetworkProvider 
{
	private static final int COORD_MUL = 1000000;
	private static final String SERVER_PRODUCT = "hsl";
	private static final String SERVER_VERSION = "1_2_0";
	private static String API_BASE;
	private static final int EARLIER_TRIPS_MINUTE_OFFSET = 10;
	private static final int EARLIER_TRIPS_MINIMUM = 3;

	private final XmlPullParserFactory parserFactory;

	public HslProvider(String user, String pass)
	{
		super(NetworkId.HSL);
		API_BASE = String.format("http://api.reittiopas.fi/hsl/%s/?user=%s&pass=%s",
					 SERVER_VERSION, user, pass);
		try
		{
			parserFactory = XmlPullParserFactory.
				newInstance(System.getProperty(XmlPullParserFactory.PROPERTY_NAME), null);
		}
		catch (final XmlPullParserException x)
		{
			throw new RuntimeException(x);
		}
	}

	@Override
	protected boolean hasCapability(final Capability capability)
	{
		return true;
	}

	private StringBuilder apiUri(final String request) {
		StringBuilder ret = new StringBuilder(API_BASE);
		ret.append("&request=").append(request);
		ret.append("&epsg_out=wgs84");
		ret.append("&epsg_in=wgs84");
		ret.append("&format=xml");
		return ret;
	}

	private static double latLonToDouble(final int value)
	{
		return (double) value / COORD_MUL;
	}

	private Point coordStrToPoint(final String coordStr)
	{
		if (coordStr == null)
			return null;

		final String[] parts = coordStr.split(",");
		return new Point(Math.round(Float.parseFloat(parts[1]) * COORD_MUL),
				 Math.round(Float.parseFloat(parts[0]) * COORD_MUL));
	}

	private Point xmlCoordsToPoint(final XmlPullParser pp) 
		throws XmlPullParserException, IOException
	{
		XmlPullUtil.enter(pp, "coord");
		String x = xmlValueTag(pp, "x");
		String y = xmlValueTag(pp, "y");
		XmlPullUtil.skipExit(pp, "coord");
		return new Point(Math.round(Float.parseFloat(y) * COORD_MUL),
				 Math.round(Float.parseFloat(x) * COORD_MUL));
	}

	private void xmlSkipUntil(final XmlPullParser pp, final String tagName)
		throws XmlPullParserException, IOException
	{
		while (!XmlPullUtil.test(pp, tagName)) 
		{
			if (!pp.isEmptyElementTag())
			{
					XmlPullUtil.enter(pp);
					XmlPullUtil.skipExit(pp);
			}
			else
			{
					XmlPullUtil.next(pp);
		        }
		}
	}

	private String xmlValueTag(final XmlPullParser pp, final String tagName)
		throws XmlPullParserException, IOException
	{
		xmlSkipUntil(pp, tagName);
		return XmlPullUtil.valueTag(pp, tagName);
	}

	private String locationToCoords(final Location loc)
	{
		return String.format("%2.6f,%2.6f", latLonToDouble(loc.lon), latLonToDouble(loc.lat));
	}

	private Location queryStop(final String stationId) throws IOException 
	{
		final StringBuilder uri = apiUri("stop");

		uri.append("&code=").append(stationId);
		uri.append(String.format("&dep_limit=1"));
		
		InputStream is = null;
		String firstChars = null;

		try
		{
			is = httpClient.getInputStream(uri.toString());
			firstChars = HttpClient.peekFirstChars(is);

			final XmlPullParser pp = parserFactory.newPullParser();
			pp.setInput(is, null);

			XmlPullUtil.enter(pp, "response"); 
			XmlPullUtil.enter(pp, "node");

			final String id = xmlValueTag(pp, "code");
			final String name = xmlValueTag(pp, "name_fi");
			final Point pt = coordStrToPoint(xmlValueTag(pp, "coords"));

			return new Location(LocationType.STATION, id, pt.lat, pt.lon, null, name);
		}
		catch (final XmlPullParserException x)
		{
			throw new ParserException("cannot parse xml: " + firstChars, x);
		}
		finally
		{
			if (is != null)
				is.close();
		}
	}
		
	// Determine stations near to given location. At least one of
	// stationId or lat/lon pair must be present.
	// NOTE: HSL returns only stops, not other locations, so "types" is not honoured.
	@Override
	public NearbyLocationsResult queryNearbyLocations(EnumSet<LocationType> types, Location location,
							  int maxDistance, int maxStations)
		throws IOException 
	{
		final StringBuilder uri = apiUri("stops_area");

		if (!location.hasLocation()) {
			if (location.type != LocationType.STATION)
				throw new IllegalArgumentException("cannot handle: " + location.type);
			if (!location.hasId())
				throw new IllegalArgumentException("at least one of stationId or lat/lon " +
								   "must be given");
			location = queryStop(location.id);
		}
		uri.append("&center_coordinate=").append(locationToCoords(location));
		uri.append(String.format("&limit=%d", maxStations));
		uri.append(String.format("&diameter=%d", maxDistance*2));

		InputStream is = null;
		String firstChars = null;

		try
		{
			is = httpClient.getInputStream(uri.toString());
			firstChars = HttpClient.peekFirstChars(is);

			final XmlPullParser pp = parserFactory.newPullParser();
			pp.setInput(is, null);

			final List<Location> stations = new ArrayList<>();
			final ResultHeader header = new ResultHeader(network, SERVER_PRODUCT);

			XmlPullUtil.enter(pp, "response");		

			while (XmlPullUtil.test(pp, "node")) 
			{
				XmlPullUtil.enter(pp, "node");

				final String id = xmlValueTag(pp, "code");
				final String name = xmlValueTag(pp, "name");
				final Point pt = coordStrToPoint(xmlValueTag(pp, "coords"));
				final String place = xmlValueTag(pp, "address");

				XmlPullUtil.skipExit(pp, "node");

				stations.add(new Location(LocationType.STATION, id, pt.lat, pt.lon, place, name));
			}

			return new NearbyLocationsResult(header, stations);
		}
		catch (final XmlPullParserException x)
		{
			throw new ParserException("cannot parse xml: " + firstChars, x);
		}
		finally
		{
			if (is != null)
				is.close();
		}
	}

	private Line newLine(String code, int type, String message) {
		String label = code.substring(1, 5).trim().replaceAll("^0+",""); 
		Product product = Product.BUS;
		char acode = code.substring(0, 1).charAt(0);

		int color = 0xFF193695;

		if (label.equals("1300M") || type == 6) {
			label = "Metro";
			product = Product.SUBWAY;
			color = 0xFFfb6500;
		} else if (label.equals("1019") || type == 7) {
			label = "Ferry";
			product = Product.FERRY;
		} else if (type == 2) {
			product = Product.TRAM;
			color = 0xFF00ab67;
		} else if (type == 12 || (type == 0 && acode == '3')) {
			product = Product.REGIONAL_TRAIN;
			color = 0xFF2cbe2c;
			if ((label.charAt(0) == '1' ||
			     label.charAt(0) == '2') && label.length() == 2)
				label = label.substring(1) + "-train";
		}

		Style style = new Style(color, Style.deriveForegroundColor(color));

		return new Line(code, network.toString(), product, label, style, message);
	}

	// Get departures at a given station, probably live
	@Override
	public QueryDeparturesResult queryDepartures(String stationId, Date queryDate, int maxDepartures, 
						     boolean equivs)
	    throws IOException
	{
		final StringBuilder uri = apiUri("stop");

		uri.append("&code=").append(stationId);
		if (queryDate != null) {
		    uri.append("&date=").append(new SimpleDateFormat("yyyyMMdd").format(queryDate));
		    uri.append("&time=").append(new SimpleDateFormat("HHmm").format(queryDate));
		}
		uri.append(String.format("&dep_limit=%d", maxDepartures));

		InputStream is = null;
		String firstChars = null;

		try
		{
			is = httpClient.getInputStream(uri.toString());
			firstChars = HttpClient.peekFirstChars(is);

			final XmlPullParser pp = parserFactory.newPullParser();
			pp.setInput(is, null);

			XmlPullUtil.enter(pp, "response"); 
			XmlPullUtil.enter(pp, "node");

			// FIXME: id is never used!?
			final String id = xmlValueTag(pp, "code");
			final String name = xmlValueTag(pp, "name_fi");

			final Map<String, Line> lines = new HashMap<>();

			xmlSkipUntil(pp, "lines");
			XmlPullUtil.enter(pp, "lines");
			while (XmlPullUtil.test(pp, "node")) 
			{
				final String[] parts = XmlPullUtil.valueTag(pp, "node").split(":");
				lines.put(parts[0], newLine(parts[0], 0, parts[1]));
			}
			XmlPullUtil.skipExit(pp, "lines");
				
			final ResultHeader header = new ResultHeader(network, SERVER_PRODUCT);
			final QueryDeparturesResult result = new QueryDeparturesResult(header);

			xmlSkipUntil(pp, "departures");
			XmlPullUtil.enter(pp, "departures");

			final List<Departure> departures = new ArrayList<>(maxDepartures);
			while (XmlPullUtil.test(pp, "node")) 
			{
				XmlPullUtil.enter(pp, "node");
				final String code = xmlValueTag(pp, "code");
				final String time = xmlValueTag(pp, "time");
				final String date = xmlValueTag(pp, "date");
				XmlPullUtil.skipExit(pp, "node");

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
				Date depDate = sdf.parse(date + time, new ParsePosition(0));

				final Line line = lines.get(code);
				final Location destination = new Location(LocationType.ANY, line.message,
									  null, null);
				final Departure departure = new Departure(depDate, null, line, null,
									  destination, null, "");
				departures.add(departure);
			}

			Location station = new Location(LocationType.STATION, id, null, name);
			result.stationDepartures.add(new StationDepartures(station, departures, null));

			return result;

		}
		catch (final XmlPullParserException x)
		{
			throw new ParserException("cannot parse xml: " + firstChars, x);
		}
		finally
		{
			if (is != null)
				is.close();
		}
	}
	
	/**
	 * Meant for auto-completion of location names, like in an {@link android.widget.AutoCompleteTextView}
	 * 
	 * @param constraint
	 *            input by user so far
	 * @return location suggestions
	 * @throws IOException
	 */
	@Override
	public SuggestLocationsResult suggestLocations(CharSequence constraint) throws IOException
	{
		final StringBuilder uri = apiUri("geocode");

		// Since HSL is picky about the input we clean out any
		// character that isn't alphabetic, numeral, -, ', /
		// or a space. Those should be all chars needed for a
		// name.
		String constraintStr = 
		    constraint.toString().replaceAll("[^\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}\\d-'/ ]", "");
		uri.append("&key=").append(URLEncoder.encode(constraintStr, "utf-8"));

		InputStream is = null;
		String firstChars = null;

		try
		{
			is = httpClient.getInputStream(uri.toString());
			firstChars = HttpClient.peekFirstChars(is);

			final ResultHeader header = new ResultHeader(network, SERVER_PRODUCT);
			final List<SuggestedLocation> locations = new ArrayList<>();

			if (firstChars.isEmpty())
			    return new SuggestLocationsResult(header, locations);

			final XmlPullParser pp = parserFactory.newPullParser();
			pp.setInput(is, null);


			XmlPullUtil.enter(pp, "response"); 

			int weight = 10000;

			while (XmlPullUtil.test(pp, "node")) 
			{
				XmlPullUtil.enter(pp, "node");
				final String locType = xmlValueTag(pp, "locType");
				String name = xmlValueTag(pp, "name");
				final Point pt = coordStrToPoint(xmlValueTag(pp, "coords"));

				LocationType type = LocationType.ANY;
				if (locType.equals("poi"))
					type = LocationType.POI;
				if (locType.equals("address"))
					type = LocationType.ADDRESS;
				if (locType.equals("stop"))
					type = LocationType.STATION;

				xmlSkipUntil(pp, "details");
				XmlPullUtil.enter(pp, "details");
				XmlPullUtil.optSkip(pp, "address");
				final String id = XmlPullUtil.optValueTag(pp, "code", null);
				final String shortCode = XmlPullUtil.optValueTag(pp, "shortCode", null);
				XmlPullUtil.skipExit(pp, "details");

				XmlPullUtil.skipExit(pp, "node");

				if (shortCode != null)
					name = name + " (" + shortCode + ")";

				locations.add(new SuggestedLocation(new Location(type, id, pt.lat, pt.lon,
										 null, name),
								    weight));
				weight -= 1;
			}

			return new SuggestLocationsResult(header, locations);
		}
		catch (final XmlPullParserException x)
		{
			throw new ParserException("cannot parse xml: " + firstChars, x);
		}
		finally
		{
			if (is != null)
				is.close();
		}	
	}

	public static class QueryTripsHslContext implements QueryTripsContext
	{
		public final String uri;
		public Date date;
		public Date prevDate = null;
		public Date nextDate = null;
		public final Location from;
		public final Location via;
		public final Location to;
		public List<Trip> trips;

		public QueryTripsHslContext(final String uri, final Location from, final Location via,
					    final Location to, final Date date)
		{
			this.uri = uri;
			this.from = from;
			this.via = via;
			this.to = to;
			this.date = date;
			this.trips = new ArrayList<>();
		}

		public boolean canQueryLater()
		{
			return true;
		}

		public boolean canQueryEarlier()
		{
			return true;
		}
	}


	// Query trips, asking for any ambiguousnesses
	// NOTE: HSL ignores accessibility
	@Override
	public QueryTripsResult queryTrips(Location from, Location via, Location to, Date date, boolean dep, 
				    Set<Product> products, Optimize optimize, WalkSpeed walkSpeed,
				    Accessibility accessibility, Set<Option> options) throws IOException 
	{
		final ResultHeader header = new ResultHeader(network, SERVER_PRODUCT);

		if (!from.isIdentified())
		{
			final List<Location> locations = suggestLocations(from.name).getLocations();
			if (locations.isEmpty())
				return new QueryTripsResult(header, QueryTripsResult.Status.NO_TRIPS);
			if (locations.size() > 1)
				return new QueryTripsResult(header, locations, null, null);
			from = locations.get(0);
		}

		if (via != null && !via.isIdentified())
		{
			final List<Location> locations = suggestLocations(via.name).getLocations();
			if (locations.isEmpty())
				return new QueryTripsResult(header, QueryTripsResult.Status.NO_TRIPS);
			if (locations.size() > 1)
				return new QueryTripsResult(header, null, locations, null);
			via = locations.get(0);
		}

		if (!to.isIdentified())
		{
			final List<Location> locations = suggestLocations(to.name).getLocations();
			if (locations.isEmpty())
				return new QueryTripsResult(header, QueryTripsResult.Status.NO_TRIPS);
			if (locations.size() > 1)
				return new QueryTripsResult(header, null, null, locations);
			to = locations.get(0);
		}

		final StringBuilder uri = apiUri("route");
		
		uri.append("&from=").append(locationToCoords(from));
		if (via != null)
			uri.append("&via=").append(locationToCoords(via));
		uri.append("&to=").append(locationToCoords(to));

		uri.append("&timetype=").append(dep ? "departure" : "arrival");

		if (walkSpeed != WalkSpeed.NORMAL)
			uri.append(String.format("&walk_speed=%d",
						 walkSpeed == WalkSpeed.SLOW ? 30 : 100));
		uri.append("&show=5");

		if (products.size() > 0) {
			List<String> tt = new ArrayList<>();
			if (products.contains(Product.HIGH_SPEED_TRAIN) ||
			    products.contains(Product.REGIONAL_TRAIN) ||
			    products.contains(Product.SUBURBAN_TRAIN))
				tt.add("train");
			if (products.contains(Product.SUBWAY))
				tt.add("metro");
			if (products.contains(Product.TRAM))
				tt.add("tram");
			if (products.contains(Product.BUS))
				tt.add("bus");
			if (products.contains(Product.FERRY))
				tt.add("ferry");

			if (tt.size() > 0)
				uri.append("&transport_types=").append(Joiner.on("|").join(tt));
		}
		
		QueryTripsHslContext context = new QueryTripsHslContext(uri.toString(), from, via, to, date);

		return queryHslTrips(from, via, to, context, date, true);
	}

	/**
	 * Query more trips (e.g. earlier or later)
	 * 
	 * @param contextObj
	 *            context to query more trips from
	 * @param later
	 *            {@code true} for get next trips, {@code false} for get previous trips
	 * @return result object that contains possible trips
	 * @throws IOException
	 */
	@Override
	public QueryTripsResult queryMoreTrips(QueryTripsContext contextObj, boolean later) throws IOException 
	{
		final QueryTripsHslContext context = (QueryTripsHslContext)contextObj;
		Date date = context.nextDate;
		int context_trips = context.trips.size();

		int tries = 0;
		QueryTripsResult result;
		do {
			// if we are fetching earlier trips, we have
			// to do a hack to search backwards in small 5
			// minute steps
			if (!later) {
				final Calendar cal = new GregorianCalendar(timeZone);
				cal.setTime(context.prevDate);
				cal.add(Calendar.MINUTE, -EARLIER_TRIPS_MINUTE_OFFSET);
				date = cal.getTime();
			}

			result = queryHslTrips(context.from, context.via, context.to, context, date, later);

			tries += 1;
			
			// keep trying if we are fetching earlier
			// trips and the list of trips hasn't grown enough
		} while (!later && (result.trips.size() - context_trips < EARLIER_TRIPS_MINIMUM) && tries < 10);

		return result;
	}

	private QueryTripsResult queryHslTrips(final Location from, final Location via, final Location to,
					       QueryTripsHslContext context, Date date, boolean later) 
	    throws IOException 
	{
		final StringBuilder uri = new StringBuilder(context.uri);

		uri.append("&date=").append(new SimpleDateFormat("yyyyMMdd").format(date));
		uri.append("&time=").append(new SimpleDateFormat("HHmm").format(date));

		InputStream is = null;
		String firstChars = null;

		context.date = date;

		try
		{
			is = httpClient.getInputStream(uri.toString());
			firstChars = HttpClient.peekFirstChars(is);

			final XmlPullParser pp = parserFactory.newPullParser();
			pp.setInput(is, null);

			XmlPullUtil.enter(pp, "response"); 

			final ResultHeader header = new ResultHeader(network, SERVER_PRODUCT);

			final List<Trip> trips = new ArrayList<>(context.trips);

			// we use this for quick checking if trip already exists
			Set<String> tripSet = new HashSet<>();
			for (Trip t : trips) tripSet.add(t.getId());
			
			int insert = later ? trips.size() : 0;

			while (XmlPullUtil.test(pp, "node")) 
			{
				XmlPullUtil.enter(pp, "node");

				XmlPullUtil.enter(pp, "node");

				List<Trip.Leg> legs = new ArrayList<>();

				xmlSkipUntil(pp, "legs");
				XmlPullUtil.enter(pp, "legs");
				int numTransfers = 0;

				while (XmlPullUtil.test(pp, "node")) {
					XmlPullUtil.enter(pp, "node");

					int distance = Integer.parseInt(xmlValueTag(pp, "length"));
					String legType = xmlValueTag(pp, "type");
					String lineCode = XmlPullUtil.optValueTag(pp, "code", null);

					List<Point> path = new ArrayList<>();

					Location departure = null;
					Date departureTime = null;
					Stop departureStop = null;

					Location arrival = null;
					Date arrivalTime = null;

					LinkedList<Stop> stops = new LinkedList<>();
					
					xmlSkipUntil(pp, "locs");
					XmlPullUtil.enter(pp, "locs");
					while (XmlPullUtil.test(pp, "node")) {
						XmlPullUtil.enter(pp, "node");
						Point pt = xmlCoordsToPoint(pp);
						
						String arrTime = xmlValueTag(pp, "arrTime");
						String depTime = xmlValueTag(pp, "depTime");
						String name = XmlPullUtil.optValueTag(pp, "name", null);
						String code = XmlPullUtil.optValueTag(pp, "code", null);
						// FIXME: shortCode is never used
						String shortCode = XmlPullUtil.optValueTag(pp, "shortCode", null);
						String stopAddress = 
						    XmlPullUtil.optValueTag(pp, "stopAddress", null);

						if (name == null) {
							name = (path.size() == 0 && from != null &&
								from.name != null) ? from.name : "";
						}
							
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
						Date arrDate = sdf.parse(arrTime, new ParsePosition(0));
						Date depDate = //arrTime.equals(depTime) ? null :
						    sdf.parse(depTime, new ParsePosition(0));

						LocationType type = LocationType.ANY;
						if (code != null)
							type = LocationType.STATION;
						Location loc = new Location(type, code, pt.lat, pt.lon,
									    stopAddress, name);

						if (path.size() == 0) {
							departure = loc;
							departureTime = depDate;
							if (type == LocationType.STATION)
								departureStop = new Stop(loc, true, departureTime, 
											 null, null, null);
							
						} else {
							arrival = loc;
							arrivalTime = arrDate;
							if (type == LocationType.STATION) {
								stops.add(new Stop(loc, arrDate, null,
										   depDate, null));
							}
						}

						path.add(pt);
						XmlPullUtil.skipExit(pp, "node");
					}
					XmlPullUtil.skipExit(pp, "locs");
					XmlPullUtil.skipExit(pp, "node");

					if (legType.equals("walk")) {
						// ugly hack to set the name of the last arrival
						if (arrival.name.isEmpty()) {
							arrival = new Location(arrival.type, arrival.id, 
									       arrival.lat, arrival.lon,
									       arrival.place, to.name);
						}

						legs.add(new Trip.Individual(Trip.Individual.Type.WALK,
									     departure, departureTime,
									     arrival, arrivalTime, path,
									     distance));
					} else {
						Stop arrivalStop = null;
						if (stops.size() > 0) {
							Stop last = stops.getLast();
							arrivalStop = new Stop(last.location, false, 
									       last.plannedArrivalTime,
									       null, null, null);
							stops.removeLast();
						}

						Line line = null;
						if (lineCode != null) {
							line = newLine(lineCode, Integer.parseInt(legType), null);
						}
						
						legs.add(new Trip.Public(line, null, departureStop,
									 arrivalStop, stops,
									 path, null));
						numTransfers++;
					}
				}
				XmlPullUtil.skipExit(pp, "legs");
				XmlPullUtil.skipExit(pp, "node");
				XmlPullUtil.skipExit(pp, "node");

				Trip t = new Trip(null, from, to, legs, null, null, numTransfers-1);
				if (!tripSet.contains(t.getId())) {
					trips.add(insert++, t);
					tripSet.add(t.getId());
				}
			}

			Date lastDate = trips.get(trips.size()-1).legs.get(0).getDepartureTime();
			if (context.nextDate == null || lastDate.after(context.nextDate))
				context.nextDate = lastDate;
			if (context.prevDate == null || date.before(context.prevDate))
				context.prevDate = date;
			context.trips = trips;
			return new QueryTripsResult(header, uri.toString(), from, via, to, context, trips);
		}
		catch (final XmlPullParserException x)
		{
			throw new ParserException("cannot parse xml: " + firstChars, x);
		}
		finally
		{
			if (is != null)
				is.close();
		}	
	}

}
