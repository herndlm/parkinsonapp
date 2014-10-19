package org.herndl.parkinsonapp;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class AlarmHandler {
	
	private AlarmManager alarmMgr;
	private PendingIntent alarmIntent;

	public AlarmHandler(Context context) {
		alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		Log.d("AlarmHandler", "AlarmHandler");
		
		// Set the alarm to start at 8:30 a.m.
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 36);
		
		alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (3 * 1000)/*calendar.getTimeInMillis()*/, alarmIntent);
		
		Toast.makeText(context, "Alarm set in 3s",
		        Toast.LENGTH_LONG).show();
		
		// With setInexactRepeating(), you have to use one of the AlarmManager interval
		// constants--in this case, AlarmManager.INTERVAL_DAY.
		//alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
		//        AlarmManager.INTERVAL_DAY, alarmIntent);
	}
}
