package org.herndl.parkinsonapp;

import java.util.Calendar;

import org.herndl.parkinsonapp.track.TrackerEntity;

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
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

/*
 * this short running service is called via a set alarm and creates visual and audible notifications
 */
public class TaskNotifyService extends Service {

	public class ServiceBinder extends Binder {
		TaskNotifyService getService() {
			return TaskNotifyService.this;
		}
	}

	// vibrate pattern { delay, onTime, offTime }
	private static final long[] vibratePattern = { 0, 1000, 1000 };
	// after this time the notification will be silented
	private static final int notifyMaxSeconds = 60;
	// ringtone types which will be used in that order in case it is missing
	private static final int[] ringtoneTypes = { RingtoneManager.TYPE_ALARM,
			RingtoneManager.TYPE_NOTIFICATION, RingtoneManager.TYPE_RINGTONE };
	// wakelock tag for the system
	private static final String wakelockTag = "org.herndl.parkinsonapp.TaskNotifiyService";

	private static NotificationManager notificationManager;
	private static Ringtone ringtone = null;
	private static WakeLock wakeLock = null;
	private static Vibrator vibrator = null;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("TaskNotifiyService", "onStartCommand");

		doTrack(intent.getStringExtra("tracker_type"),
				intent.getStringExtra("tracker_name"),
				intent.getIntExtra("tracker_intValue", 0),
				intent.getStringExtra("tracker_stringValue"));

		showNotification(intent.getIntExtra("notification_id", 0),
				intent.getStringExtra("notification_title"),
				intent.getStringExtra("notification_string"));

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IBinder mBinder = new ServiceBinder();

	private void showNotification(int notification_id,
			String notification_title, String notification_string) {

		Log.v("TaskNotifiyService", "showNotification");

		startNotification(this, notification_id, notification_title,
				notification_string);

		// stop ringtone and vibrator after notifyMaxSeconds seconds
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				// stop ringtone and vibrator
				stopNotification();
				// stop notification service
				stopSelf();
			}
		}, notifyMaxSeconds * 1000);

		// release power manager wake lock
		if (wakeLock != null)
			wakeLock.release();
	}

	// do a track action with the data got from the alarm intent
	// a TrackerEntity is created and saved to the DB here
	private void doTrack(String tracker_type, String tracker_name,
			int tracker_intValue, String tracker_stringValue) {

		TrackerEntity trackerEntity = new TrackerEntity(tracker_type,
				tracker_name, tracker_intValue, tracker_stringValue,
				Calendar.getInstance());
		trackerEntity.save();

		Log.v("TaskNotifyService", "doTrack " + trackerEntity);
	}

	// starts the notifications
	@SuppressWarnings("deprecation")
	public static void startNotification(Context context, int notification_id,
			String notification_title, String notification_string) {

		// wake the screen
		PowerManager powerManager = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK
						| PowerManager.ACQUIRE_CAUSES_WAKEUP, wakelockTag);
		wakeLock.acquire();

		// build notification and start showing it
		PendingIntent contentIntent = PendingIntent.getActivity(context,
				notification_id, new Intent(context, MainActivity.class),
				PendingIntent.FLAG_CANCEL_CURRENT);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(
				context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setAutoCancel(true)
				.setContentTitle(notification_title)
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText(notification_string))
				.setContentText(notification_string);

		builder.setContentIntent(contentIntent);
		notificationManager = (NotificationManager) context
				.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.notify(notification_id, builder.build());

		// vibrate in the specified pattern
		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null)
			vibrator.vibrate(vibratePattern, 0);

		// get best matching user ringtone and start playing
		Uri alert = null;
		for (int type : ringtoneTypes) {
			alert = RingtoneManager.getDefaultUri(type);
			if (alert != null) {
				ringtone = RingtoneManager.getRingtone(context, alert);
				if (ringtone != null)
					ringtone.play();
				break;
			}
		}
	}

	public static void stopNotification() {
		// stop playing ringtone
		if (ringtone != null && ringtone.isPlaying())
			ringtone.stop();
		// stop vibrator
		if (vibrator != null)
			vibrator.cancel();
	}
}
