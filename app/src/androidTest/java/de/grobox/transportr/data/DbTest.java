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

package de.grobox.transportr.data;

import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.room.Room;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

abstract public class DbTest {

	@Rule
	public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

	protected Db db;

	@Before
	public void createDb() throws Exception {
		Context context = getApplicationContext();
		db = Room.inMemoryDatabaseBuilder(context, Db.class).build();
	}

	@After
	public void closeDb() throws IOException {
		// observing live data sometimes still tries to access the DB after this was called
		// not closing the in-memory DB for tests only seems to be safe enough
//		db.close();
	}

	/**
	 * Room calculates the LiveData's value lazily when there is an observer.
	 * This forces it to return the expected value right away.
	 */
	public static <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
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
