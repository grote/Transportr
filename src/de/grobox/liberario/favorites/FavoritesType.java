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

package de.grobox.liberario.favorites;

enum FavoritesType {

	HOME(0),
	WORK(1),
	TRIP(2);

	private final int value;

	FavoritesType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	static FavoritesType fromValue(int value) {
		for (FavoritesType s : values())
			if (s.value == value) return s;
		throw new IllegalArgumentException();
	}

}
