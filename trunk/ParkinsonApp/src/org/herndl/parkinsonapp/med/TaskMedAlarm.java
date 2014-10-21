package org.herndl.parkinsonapp.med;

import java.util.Calendar;

import org.herndl.parkinsonapp.R;
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
		Log.v("TaskMedAlarm", "constructor");
		this.context = context;
		TaskMedAlarm.alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		this.med = med;
	}

	// used to set and also delete intents
	public static PendingIntent createMedPendingIntent(Context context,
			MedReminderEntity med) {
		Intent intent = new Intent(context, TaskNotifyService.class);

		// build notification string
		intent.putExtra("notification_title",
				context.getString(R.string.notification_title));
		if (med.dose == 1)
			intent.putExtra("notification_string", String.format(context
					.getString(R.string.med_reminder_notification_singular),
					med.name));
		else
			intent.putExtra("notification_string", String.format(context
					.getString(R.string.med_reminder_notification_plural),
					med.dose, med.name));
		intent.putExtra("tracker_type", "med");
		intent.putExtra("tracker_name", med.name);
		intent.putExtra("tracker_intValue", med.dose);

		// create pending intent for service and remove old ones floating around
		return PendingIntent.getService(context, med.hashCode(), intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
	}

	public static Calendar getCalendar(MedReminderEntity med) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, med.remind_hour);
		calendar.set(Calendar.MINUTE, med.remind_minute);
		calendar.set(Calendar.SECOND, 0);
		// fix immediately going of past alarms
		// by setting it for the next day
		if (calendar.before(Calendar.getInstance()))
			calendar.add(Calendar.DATE, 1);
		return calendar;
	}

	public static void cancel(Context context, MedReminderEntity med) {
		alarmMgr.cancel(createMedPendingIntent(context, med));
	}

	@Override
	public void run() {
		// create pending intent for service and remove old ones floating around
		PendingIntent alarmIntent = createMedPendingIntent(context, med);

		Calendar calendar = getCalendar(med);

		Log.v("TaskMedAlarm", "set to run at " + calendar.getTime());
		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
				alarmIntent);
	}

}
