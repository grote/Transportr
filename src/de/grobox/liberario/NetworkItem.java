/*    Liberario
 *    Copyright (C) 2013 Torsten Grote
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

package de.grobox.liberario;

import de.schildbach.pte.NetworkId;

public class NetworkItem {
	public NetworkId id;
	public String name;
	public String description;
	public boolean beta;

	public NetworkItem(NetworkId id, String text) {
		this.id = id;
		this.name = text;
		this.description = "";
		this.beta = false;
	}

	public NetworkItem(NetworkId id, String text, boolean beta) {
		this.id = id;
		this.name = text;
		this.description = "";
		this.beta = beta;
	}

	public NetworkItem(NetworkId id, String text, String description) {
		this.id = id;
		this.name = text;
		this.description = description;
		this.beta = false;
	}

	public NetworkItem(NetworkId id, String text, String description, boolean beta) {
		this.id = id;
		this.name = text;
		this.description = description;
		this.beta = beta;
	}
}
