package de.grobox.liberario.data;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

abstract public class DbTest {

	protected Db db;

	@Before
	public void createDb() throws Exception {
		Context context = InstrumentationRegistry.getContext();
		db = Room.inMemoryDatabaseBuilder(context, Db.class).build();
	}

	@After
	public void closeDb() throws IOException {
		db.close();
	}

	/**
	 * Room calculates the LiveData's value lazily when there is an observer.
	 * This forces it to return the expected value right away.
	 */
	protected  <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
		final Object[] data = new Object[1];
		final CountDownLatch latch = new CountDownLatch(1);
		Observer<T> observer = new Observer<T>() {
			@Override
			public void onChanged(@Nullable T o) {
				data[0] = o;
				latch.countDown();
				liveData.removeObserver(this);
			}
		};
		liveData.observeForever(observer);
		latch.await(2, TimeUnit.SECONDS);
		//noinspection unchecked
		return (T) data[0];
	}

}
