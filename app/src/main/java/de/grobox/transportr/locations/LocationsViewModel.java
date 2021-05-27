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

package de.grobox.transportr.locations;

import androidx.lifecycle.LiveData;

import java.util.List;

import de.grobox.transportr.TransportrApplication;
import de.grobox.transportr.data.locations.FavoriteLocation;
import de.grobox.transportr.data.locations.HomeLocation;
import de.grobox.transportr.data.locations.LocationRepository;
import de.grobox.transportr.data.locations.WorkLocation;
import de.grobox.transportr.networks.TransportNetworkManager;
import de.grobox.transportr.networks.TransportNetworkViewModel;

public abstract class LocationsViewModel extends TransportNetworkViewModel {

	private final LocationRepository locationRepository;

	private final LiveData<HomeLocation> home;
	private final LiveData<WorkLocation> work;
	private final LiveData<List<FavoriteLocation>> locations;

	protected LocationsViewModel(TransportrApplication application, TransportNetworkManager transportNetworkManager, LocationRepository locationRepository) {
		super(application, transportNetworkManager);
		this.locationRepository = locationRepository;
		this.home = locationRepository.getHomeLocation();
		this.work = locationRepository.getWorkLocation();
		this.locations = locationRepository.getFavoriteLocations();
	}

	public void setHome(WrapLocation wrapLocation) {
		locationRepository.setHomeLocation(wrapLocation);
	}

	public LiveData<HomeLocation> getHome() {
		return home;
	}

	public void setWork(WrapLocation wrapLocation) {
		locationRepository.setWorkLocation(wrapLocation);
	}

	public LiveData<WorkLocation> getWork() {
		return work;
	}

	public LiveData<List<FavoriteLocation>> getLocations() {
		return locations;
	}

}
