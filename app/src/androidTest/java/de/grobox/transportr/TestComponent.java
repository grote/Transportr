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

package de.grobox.transportr;

import javax.inject.Singleton;

import dagger.Component;
import de.grobox.transportr.data.TestDbModule;
import de.grobox.transportr.map.MapActivityTest;
import de.grobox.transportr.networks.PickTransportNetworkActivityTest;
import de.grobox.transportr.trips.TripsTest;

@Singleton
@Component(modules = {TestModule.class, TestDbModule.class})
public interface TestComponent extends AppComponent {

	void inject(PickTransportNetworkActivityTest test);
	void inject(MapActivityTest test);
	void inject(TripsTest test);

}
