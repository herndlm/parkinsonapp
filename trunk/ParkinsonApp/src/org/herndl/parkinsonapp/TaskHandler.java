package org.herndl.parkinsonapp;

import org.herndl.parkinsonapp.med.MedReminderEntity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/*
 * TaskHandler which wraps service methodes and does service binding
 */
public class TaskHandler {

	private static TaskService mBoundService;
	private static Context mContext;
	private static boolean mIsBound;

	public TaskHandler(Context context) {
		mContext = context;
	}

	// connect to TaskService and set all alarms
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.v("TaskClient:ServiceConnection", "onServiceConnected");
			mBoundService = ((TaskService.ServiceBinder) service).getService();
			mBoundService.setAllMedAlarms();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBoundService = null;
		}
	};

	// bind to TaskService
	public void doBindService() {
		Log.v("TaskClient", "doBindService");
		mContext.bindService(new Intent(mContext, TaskService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	// unbind from TaskService
	public void doUnbindService() {
		Log.v("TaskClient", "doUnbindService");
		if (mIsBound) {
			mContext.unbindService(mConnection);
			mIsBound = false;
		}
	}

	// service wrapper for setting a med reminder alarm
	public static void setMedAlarm(MedReminderEntity med) {
		if (mIsBound)
			mBoundService.setMedAlarm(med);
		else
			Log.w("TaskHandler:setMedAlarm", "service not bound");
	}

	// service wrapper for cancel a med reminder alarm
	public static void cancelMedAlarm(MedReminderEntity med) {
		if (mIsBound)
			mBoundService.cancelMedAlarm(med);
		else
			Log.w("TaskHandler:setMedAlarm", "service not bound");
	}

}
