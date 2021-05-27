/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2021 Torsten Grote
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

package de.grobox.transportr.utils;

public interface Constants {

	// Serialization
	String DATE = "Date";
	String DEPARTURE = "departure";
	String WRAP_LOCATION = "WrapLocation";
	String LOCATION = "Location";
	String FAV_TRIP_UID = "uid";
	String FROM = "from";
	String VIA = "via";
	String TO = "to";
	String EXPANDED = "expanded";

	// Request Codes
	int REQUEST_LOCATION_PERMISSION = 1;

	// Loaders
	int LOADER_DEPARTURES = 1;
	int LOADER_NEARBY_STATIONS = 2;

}
