/*
 *    Transportr
 *
 *    Copyright (c) 2013 - 2025 Torsten Grote
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_MAX;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.NotificationCompat.Builder;

public class AlertService extends Service implements LocationListener {
	private LocationManager mLocManager = null;
	private boolean isWatchdogRunning = false;
	private long lastLocationUpdate = 0;
	private NotificationManagerCompat mNotifManager = null;
	private static final int NOTIF_ID = 111;
	private static final String CHANNEL_ID = "alert";
	private Builder mNotifBuilder;
	private String destinationName;
	private String arrivalTime;
	private long arrivalTimeLong;
	private Location destination;
	private PendingIntent stopPendingIntent;
	private static final long ARRIVAL_THRESHOLD_METERS = 150;
	private static final long ARRIVAL_THRESHOLD_SEC = 30;
	private static final long WATCHDOG_INTERVAL_MS = 1_000; // 1 second
	private static final long LOCATION_INTERVAL_MS = 2_000; // 2 seconds
	private static final long LOCATION_TIMEOUT_MS = 10_000; // 10 seconds
	private static final String ACTION_STOP = "STOP";
	private final Handler handler = new Handler(Looper.getMainLooper());
	private final Runnable watchdogRunnable = new Runnable() {
		@Override
		public void run() {
			long now = System.currentTimeMillis();
			if (now - lastLocationUpdate > LOCATION_TIMEOUT_MS) {
				// No location update in the last 10 seconds → take action
				onLocationUpdateTimeout();
			}
			// Reschedule the check
			if (isWatchdogRunning) handler.postDelayed(this, WATCHDOG_INTERVAL_MS);
		}
	};

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mNotifManager = NotificationManagerCompat.from(getApplicationContext());
		NotificationChannelCompat notifChannel = new NotificationChannelCompat.Builder(CHANNEL_ID, IMPORTANCE_MAX).setName(getResources().getString(R.string.action_alert)).setVibrationEnabled(true).build();
		mNotifManager.createNotificationChannel(notifChannel);

		Intent stopIntent = new Intent(this, AlertService.class);
		stopIntent.setAction(ACTION_STOP);
		stopPendingIntent = PendingIntent.getService(
				this,
				0,
				stopIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
		);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			// Service restarted by system, but we lack destination data → stop.
			stopSelf();
			return START_NOT_STICKY;
		}
		if (!ACTION_STOP.equals(intent.getAction())) {
			handler.removeCallbacksAndMessages(null);
			arrivalTime = intent.getStringExtra("EXTRA_TIME_STR");
			arrivalTimeLong = intent.getLongExtra("EXTRA_TIME_LONG",0L);
			destinationName = intent.getStringExtra("EXTRA_LOCATION_NAME");

			double latitude = intent.getDoubleExtra("EXTRA_LATITUDE", 0.0);
			double longitude = intent.getDoubleExtra("EXTRA_LONGITUDE", 0.0);
			destination = new Location("manual");
			destination.setLatitude(latitude);
			destination.setLongitude(longitude);
			lastLocationUpdate = 0;
			showNotif();
			startGpsLocListener();
			isWatchdogRunning = true;
			handler.postDelayed(watchdogRunnable, WATCHDOG_INTERVAL_MS);
			return START_STICKY;
		} else {
			stopSelf();
			return START_NOT_STICKY;
		}
	}

	private void showNotif() {
		updateNotification(null, false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			startForeground(NOTIF_ID, mNotifBuilder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
		} else {
			startForeground(NOTIF_ID, mNotifBuilder.build());
		}

	}

	private void startGpsLocListener() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		mLocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL_MS, 0, this);
	}

	@SuppressLint("MissingPermission")
	private void stopGpsLocListener() {
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}
		mLocManager.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(@NonNull Location location) {
		lastLocationUpdate = System.currentTimeMillis();
		long timeToDestination = (arrivalTimeLong - System.currentTimeMillis()) / 1000;
		String timeString = (Math.abs(timeToDestination) > 60) ? getString(R.string.in_x_minutes, Math.round(timeToDestination / 60.0)) : getString(R.string.seconds, timeToDestination);
		long distanceToDestination = (long) destination.distanceTo(location);
		if (distanceToDestination > ARRIVAL_THRESHOLD_METERS){
			updateNotification(getString(R.string.meter, distanceToDestination) + " / " + timeString , false);
		} else {
			updateNotification(null, true);
			stopGpsLocListener();
			isWatchdogRunning = false;
			handler.postDelayed(this::stopSelf, 30000);
		}
	}

	private void onLocationUpdateTimeout() {
		long timeToDestination = (arrivalTimeLong - System.currentTimeMillis()) / 1000;
		String timeString = (Math.abs(timeToDestination) > 60) ? getString(R.string.in_x_minutes, Math.round(timeToDestination / 60.0)) : getString(R.string.seconds, timeToDestination);
		if (timeToDestination > ARRIVAL_THRESHOLD_SEC){
			updateNotification( timeString , false);
		} else {
			updateNotification(null, true);
			stopGpsLocListener();
			isWatchdogRunning = false;
			handler.postDelayed(this::stopSelf, 30000);
		}
	}

	private void updateNotification(@Nullable String contentText, boolean hasArrived) {
		mNotifBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
				.setSilent(!hasArrived)
				.setOnlyAlertOnce(!hasArrived) // or adjust as needed
				.setSmallIcon(R.drawable.ic_transportr)
				.setPriority(hasArrived ? NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_DEFAULT)  //ignored on Android 8+
				.setAutoCancel(false)
				.setOngoing(true)
				.addAction(R.drawable.ic_stop, getString(R.string.action_stop), stopPendingIntent)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setContentTitle((hasArrived ? "\ud83c\udfc1 " : "") + destinationName + " " + arrivalTime);  //Unicode Character "🏁" (U+1F3C1)

		if (contentText != null) {
			mNotifBuilder.setContentText(contentText);
		}

		mNotifManager.notify(NOTIF_ID, mNotifBuilder.build());
	}

	@Override
	public void onDestroy() {
		handler.removeCallbacks(watchdogRunnable);
		stopGpsLocListener();
		super.onDestroy();
	}
}
