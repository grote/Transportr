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
