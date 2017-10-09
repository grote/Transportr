package de.grobox.liberario.data;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public class DbTest {

	protected Db db;

	@Before
	public void createDb() {
		Context context = InstrumentationRegistry.getTargetContext();
		db = Room.inMemoryDatabaseBuilder(context, Db.class).build();
	}

	@After
	public void closeDb() throws IOException {
		db.close();
	}

}
