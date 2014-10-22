package org.herndl.parkinsonapp.med;

import java.util.Calendar;

import org.herndl.parkinsonapp.R;
import org.herndl.parkinsonapp.TaskNotifyService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// this class runs in a thread and creates the intent and notification data in an alarm for the TaskNotifyService
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

	// used to set and also delete intents and their notification alarms
	public static PendingIntent createMedPendingIntent(Context context,
			MedReminderEntity med) {
		Intent intent = new Intent(context, TaskNotifyService.class);

		// build notification data and put it into intent
		intent.putExtra("notification_id", med.hashCode());
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

		// create new pending intent for service which cancels old ones
		return PendingIntent.getService(context, med.hashCode(), intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
	}

	// helper method which gets the corresponding Calendar object to an med
	// reminder object
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

	// helper method which cancels an already set notification alarm by
	// creating the same intent as before from the med reminder object
	public static void cancel(Context context, MedReminderEntity med) {
		alarmMgr.cancel(createMedPendingIntent(context, med));
	}

	// creates an alarm for one med reminder object which has been set in the
	// constructor, the current default interval is a day, this could be made
	// user settable via GUI
	@Override
	public void run() {
		PendingIntent alarmIntent = createMedPendingIntent(context, med);
		Calendar calendar = getCalendar(med);

		Log.v("TaskMedAlarm", "set to run at " + calendar.getTime());
		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
				calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY,
				alarmIntent);
	}

}
