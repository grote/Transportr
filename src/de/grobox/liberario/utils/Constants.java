package de.grobox.liberario.utils;

public interface Constants {

	// Serialization
	String DATE = "Date";
	String NOW = "now";
	String WRAP_LOCATION = "WrapLocation";
	String LOCATION = "Location";
	String FROM = "from";
	String VIA = "via";
	String TO = "to";
	String IS_DEPARTURE = "isDeparture";
	String SEARCH = "search";

	// Request Codes
	int REQUEST_NETWORK_PROVIDER_CHANGE = 1;

	// Loaders
	int LOADER_AUTO_COMPLETE = 0;
	int LOADER_DEPARTURES = 1;
	int LOADER_NEARBY_STATIONS = 2;
	int LOADER_FAVORITES = 3;
	int LOADER_TRIPS = 4;

}
