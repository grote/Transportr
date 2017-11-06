package de.grobox.transportr;

import javax.inject.Singleton;

import dagger.Component;
import de.grobox.transportr.networks.PickTransportNetworkActivityTest;
import de.grobox.transportr.trips.TripsTest;

@Singleton
@Component(modules = TestModule.class)
public interface TestComponent extends AppComponent {

	void inject(PickTransportNetworkActivityTest test);

	void inject(TripsTest test);

}
