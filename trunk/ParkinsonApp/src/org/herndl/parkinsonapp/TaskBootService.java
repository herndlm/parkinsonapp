package org.herndl.parkinsonapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TaskBootService extends BroadcastReceiver {

	// start TaskService on boot
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.v("TaskBootService", "onReceive");
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			Log.v("TaskBootService", "BOOT_COMPLETED");
			context.startService(new Intent(context, TaskService.class));
		}

	}

}
