package org.herndl.parkinsonapp;

import org.herndl.parkinsonapp.medreminder.MedReminderEntity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class TaskHandler {

	private static TaskService mBoundService;
	private static Context mContext;
	private static boolean mIsBound;

	public TaskHandler(Context context) {
		mContext = context;
	}

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

	public void doBindService() {
		Log.v("TaskClient", "doBindService");
		mContext.bindService(new Intent(mContext, TaskService.class),
				mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	public void doUnbindService() {
		Log.v("TaskClient", "doUnbindService");
		if (mIsBound) {
			mContext.unbindService(mConnection);
			mIsBound = false;
		}
	}

	public static void setMedAlarm(MedReminderEntity med) {
		if (mIsBound)
			mBoundService.setMedAlarm(med);
		else
			Log.w("TaskHandler:setMedAlarm", "service not bound");
	}

	public static void cancelMedAlarm(MedReminderEntity med) {
		if (mIsBound)
			mBoundService.cancelMedAlarm(med);
		else
			Log.w("TaskHandler:setMedAlarm", "service not bound");
	}

}
