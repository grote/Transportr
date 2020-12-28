/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2020 Torsten Grote
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

package de.grobox.transportr.locations

import de.grobox.transportr.networks.TransportNetwork

//TODO - Make robust to handle other address formats.
class LocationFormatChanger(private val format: TransportNetwork.LocationFormat = TransportNetwork.LocationFormat.USE_DEFAULT){

    fun formatLocationString(unformattedLocation: String) : String {
        if (format == TransportNetwork.LocationFormat.STREET_NAME_FIRST) return formatStreetNameFirst(unformattedLocation);
        else if (format == TransportNetwork.LocationFormat.STREET_NUMBER_FIRST) return formatStreetNumberFirst(unformattedLocation);
        return unformattedLocation;
    }

    @ExperimentalUnsignedTypes
    private fun formatStreetNameFirst(locationName : String) : String{
        //Tokenize the address string on spaces
        val tokens: Array<String> = locationName.split(" ").toTypedArray()

        //If the final token is a number we assume it is in the correct format
        if (tokens[tokens.size - 1].toUIntOrNull(10) != null) return locationName

        val str = StringBuilder()

        //Keep appending tokens until you get to the very last token that you already inserted
        for (i in 1 until tokens.size - 2) {
            str.append(tokens[i])
            str.append(" ")
        }

        //Append the number to the front of the new string
        str.append(tokens[tokens.size - 1])

        return str.toString().trim { it <= ' ' }
    }

    //Converts an address of the form (Pennsylvania Avenue, 1600) into 1600 Pennsylvania Avenue
    @ExperimentalUnsignedTypes
    private fun formatStreetNumberFirst(locationName: String) : String{

        //Tokenize the address string on spaces
        val tokens: Array<String> = locationName.split(" ").toTypedArray()

        //If the final token isn't a number we assume it's in the correct format
        if (tokens[tokens.size - 1].toUIntOrNull(10) == null) return locationName

        val str = StringBuilder()

        //Append the number to the front of the new string
        str.append(tokens[tokens.size - 1])
        str.append(" ")

        //Keep appending tokens until you get to the very last token that you already inserted
        for (i in 0 until tokens.size - 1) {
            str.append(tokens[i])
            str.append(" ")
        }

        return str.toString().trim { it <= ' ' }
    }
}