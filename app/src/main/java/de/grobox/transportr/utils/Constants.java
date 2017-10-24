package de.grobox.transportr.utils;

public interface Constants {

	// Serialization
	String DATE = "Date";
	String NOW = "now";
	String WRAP_LOCATION = "WrapLocation";
	String LOCATION = "Location";
	String FAV_TRIP_UID = "uid";
	String FROM = "from";
	String VIA = "via";
	String TO = "to";
	String IS_DEPARTURE = "isDeparture";
	String SEARCH = "search";

	// Request Codes
	int REQUEST_LOCATION_PERMISSION = 1;

	// Loaders
	int LOADER_DEPARTURES = 1;
	int LOADER_NEARBY_STATIONS = 2;
	int LOADER_TRIPS = 4;
	int LOADER_MORE_TRIPS = 5;

}
