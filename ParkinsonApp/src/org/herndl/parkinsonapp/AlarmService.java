package org.herndl.parkinsonapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * This {@code IntentService} does the app's actual work.
 * {@code SampleAlarmReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class AlarmService extends IntentService {
	
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	
	public AlarmService() {
        super("SchedulingService");
    }

	@Override
    protected void onHandleIntent(Intent intent) {
        
            sendNotification("yo");
            
         // start vibrating
    	    final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    	    // delay, vibrate, sleep
    	    long[] pattern = {0, 1000, 1000};
    	    //vibrator.vibrate(pattern, 0);
    		
    		// get default user ringtone and start playing
    		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    		if (alert == null) {
    		    // alert is null, using backup
    		    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    		    // I can't see this ever being null (as always have a default notification)
    		    // but just incase
    		    if (alert == null) {  
    		        // alert backup is null, using 2nd backup
    		        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);                
    		    }
    		}
    		final Ringtone r = RingtoneManager.getRingtone(this, alert);
    		//r.play(); // TODO this should play, just not while I'm developing..
    		
    		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
    		@SuppressWarnings("deprecation")
			final
			PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "My Tag");
    		wl.acquire();
    		
    		Log.v("AlarmService:onHandleIntent","go");
    		
    		// stop notifications after x seconds
    		new Handler().postDelayed(new Runnable() {
    	        @Override
    	        public void run() {
    	        	Log.v("AlarmService:onHandleIntent","stop");
    	        	// stop playing ringtone
    	            if (r.isPlaying())
    	                r.stop();
    	            // stop vibrator
    	            vibrator.cancel();
    	            // release power manager wake lock
    	            wl.release();
    	        }
    	    }, 5 * 1000);
            
         // Release the wake lock provided by the BroadcastReceiver.
            AlarmReceiver.completeWakefulIntent(intent);
            
    }
    
    // Post a notification indicating whether a doodle was found.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
               this.getSystemService(Context.NOTIFICATION_SERVICE);
    
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("yO man")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
	
	/*@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm);
		
		// start vibrating
	    final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    // delay, vibrate, sleep
	    long[] pattern = {0, 1000, 1000};
	    //vibrator.vibrate(pattern, 0);
		
		// get default user ringtone and start playing
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
		    // alert is null, using backup
		    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    // I can't see this ever being null (as always have a default notification)
		    // but just incase
		    if (alert == null) {  
		        // alert backup is null, using 2nd backup
		        alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);                
		    }
		}
		final Ringtone r = RingtoneManager.getRingtone(this, alert);
		//r.play(); // TODO this should play, just not while I'm developing..
		
		// stop notifications after x seconds
		final Handler handler = new Handler();
	    handler.postDelayed(new Runnable() {
	        @Override
	        public void run() {
	        	// stop playing ringtone
	            if (r.isPlaying())
	                r.stop();
	            // stop vibrator
	            vibrator.cancel();
	        }
	    }, 5 * 1000);
	}*/
}
