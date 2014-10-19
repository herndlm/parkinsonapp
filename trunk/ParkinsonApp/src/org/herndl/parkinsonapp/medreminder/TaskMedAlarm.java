package org.herndl.parkinsonapp.medreminder;

import java.util.Calendar;

import org.herndl.parkinsonapp.TaskNotifyService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TaskMedAlarm implements Runnable {

	private static AlarmManager alarmMgr;
	private Context context;
	private MedReminderEntity med;

	public TaskMedAlarm(Context context, MedReminderEntity med) {
		this.context = context;
		TaskMedAlarm.alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		this.med = med;
	}

	// used to set and also delete intents
	public static PendingIntent createMedPendingIntent(Context context,
			MedReminderEntity med) {
		Intent intent = new Intent(context, TaskNotifyService.class);

		String notification_title = "Notification Title";

		// TODO build notification string
		intent.putExtra("notification_title", notification_title);
		intent.putExtra("notification_string", med.name);

		// create pending intent for service and remove old ones floating around
		return PendingIntent.getService(context, med.getIntId(), intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public static void cancel(Context context, MedReminderEntity med) {
		alarmMgr.cancel(createMedPendingIntent(context, med));
	}

	@Override
	public void run() {
		// create pending intent for service and remove old ones floating around
		PendingIntent alarmIntent = createMedPendingIntent(context, med);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, med.remind_hour);
		calendar.set(Calendar.MINUTE, med.remind_minute);
		calendar.set(Calendar.SECOND, 0);
		// fix immediately going of past alarms
		// by setting it for the next day
		if (calendar.before(Calendar.getInstance()))
			calendar.add(Calendar.DATE, 1);

		Log.v("TaskAlarm", "set to run at " + calendar.getTime());

		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
				alarmIntent);
	}

}
