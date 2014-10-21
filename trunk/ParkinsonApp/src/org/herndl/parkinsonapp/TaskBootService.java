package org.herndl.parkinsonapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * this class handles starting the TaskService after a reboot
 */
public class TaskBootService extends BroadcastReceiver {

	// start TaskService on boot, needs special permissions and configuration in
	// the application manifest to receive the intent
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TaskBootService", "onReceive");
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Log.v("TaskBootService", "BOOT_COMPLETED");
			context.startService(new Intent(context, TaskService.class));
		}

	}

}
