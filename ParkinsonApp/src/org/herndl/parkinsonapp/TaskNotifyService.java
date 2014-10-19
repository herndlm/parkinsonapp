package org.herndl.parkinsonapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TaskNotifyService extends Service {

	public class ServiceBinder extends Binder {
		TaskNotifyService getService() {
			return TaskNotifyService.this;
		}
	}

	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;

	@Override
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("TaskNotifiyService", "onStartCommand");

		showNotification(intent.getStringExtra("notification_title"),
				intent.getStringExtra("notification_string"));

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new ServiceBinder();

	private void showNotification(String notification_title,
			String notification_string) {
		Log.v("TaskNotifiyService:showNotification", "title: "
				+ notification_title + ", string: " + notification_string);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(notification_title)
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText(notification_string))
				.setContentText(notification_string);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

		final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		// delay, vibrate, sleep
		long[] pattern = { 0, 1000, 1000 };
		vibrator.vibrate(pattern, 0);

		// get default user ringtone and start playing
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			// alert is null, using backup
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			// I can't see this ever being null (as always have a default
			// notification)
			// but just incase
			if (alert == null) {
				// alert backup is null, using 2nd backup
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			}
		}
		final Ringtone r = RingtoneManager.getRingtone(this, alert);
		if (r != null)
			r.play();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		@SuppressWarnings("deprecation")
		final PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
		wl.acquire();

		// stop notifications after x seconds
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// stop playing ringtone
				if (r != null && r.isPlaying())
					r.stop();
				// stop vibrator
				vibrator.cancel();
				// release power manager wake lock
				wl.release();
				// stop notification service
				stopSelf();
			}
		}, 3 * 1000);

	}

}
