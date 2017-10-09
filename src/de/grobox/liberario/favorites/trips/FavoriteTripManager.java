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

package de.grobox.liberario.favorites.trips;

import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;

import de.grobox.liberario.data.searches.SearchesRepository;
import de.grobox.liberario.locations.WrapLocation;

@ParametersAreNonnullByDefault
public class FavoriteTripManager {

	private final SearchesRepository searchesRepository;

	@Inject
	public FavoriteTripManager(SearchesRepository searchesRepository) {
		this.searchesRepository = searchesRepository;
	}

	@UiThread
	public void addTrip(long uid, WrapLocation from, @Nullable WrapLocation via, WrapLocation to) {
		FavoriteTripItem item = new FavoriteTripItem(uid, from, via, to);
		searchesRepository.storeSearch(item);
	}

}
