package org.herndl.parkinsonapp;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class AlarmReceiver extends WakefulBroadcastReceiver {

	// The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("AlarmReceiver", "onReceive");
		/*Toast.makeText(context, "Don't panik but your time is up!!!!.",
        Toast.LENGTH_LONG).show();*/
	    
		/*Intent intentAlarm = new Intent(context, AlarmActivity.class);
		// TODO send info to activity
		// intent.putExtra(EXTRA_MESSAGE, message);
		intentAlarm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intentAlarm);*/
		
		// BEGIN_INCLUDE(alarm_onreceive)
        /* 
         * If your receiver intent includes extras that need to be passed along to the
         * service, use setComponent() to indicate that the service should handle the
         * receiver's intent. For example:
         * 
         * ComponentName comp = new ComponentName(context.getPackageName(), 
         *      MyService.class.getName());
         *
         * // This intent passed in this call will include the wake lock extra as well as 
         * // the receiver intent contents.
         * startWakefulService(context, (intent.setComponent(comp)));
         * 
         * In this example, we simply create a new intent to deliver to the service.
         * This intent holds an extra identifying the wake lock.
         */
        Intent service = new Intent(context, AlarmService.class);
        
        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, service);
		
		/*NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(context)
		        .setSmallIcon(R.drawable.ic_launcher)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
		
		PendingIntent resultPendingIntent = PendingIntent.getActivity(
			    context,
			    0,
			    new Intent(), // add this
			    PendingIntent.FLAG_UPDATE_CURRENT);
		
		mBuilder.setContentIntent(resultPendingIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(0, mBuilder.build()); // TODO use better notification ID?*/
	}
	
	public void setAlarm(Context context) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // Set the alarm's trigger time to 8:30 a.m.
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 35);
 
        
        // Set the alarm to fire at approximately 8:30 a.m., according to the device's
        // clock, and to repeat once a day.
        //alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,  
        //        calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (3 * 1000)/*calendar.getTimeInMillis()*/, alarmIntent);
		
		Toast.makeText(context, "Alarm set for 08:30",
		        Toast.LENGTH_LONG).show();
        
        // Enable {@code SampleBootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);           
    }
	
	public void cancelAlarm(Context context) {
        // If the alarm has been set, cancel it.
        if (alarmMgr!= null) {
            alarmMgr.cancel(alarmIntent);
        }
        
        // Disable {@code SampleBootReceiver} so that it doesn't automatically restart the 
        // alarm when the device is rebooted.
       ComponentName receiver = new ComponentName(context, AlarmBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
	

}
