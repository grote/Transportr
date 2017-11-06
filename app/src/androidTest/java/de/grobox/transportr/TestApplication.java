package de.grobox.transportr;


public class TestApplication extends TransportrApplication {

	@Override
	protected AppComponent createComponent() {
		return DaggerTestComponent.builder().testModule(new TestModule(this)).build();
	}

}
