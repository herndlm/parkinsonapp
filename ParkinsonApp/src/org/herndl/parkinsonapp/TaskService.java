package org.herndl.parkinsonapp;

import java.util.List;

import org.herndl.parkinsonapp.med.MedReminderEntity;
import org.herndl.parkinsonapp.med.TaskMedAlarm;

import com.orm.SugarRecord;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

// the service which does the setting and unsetting of alarms
public class TaskService extends Service {

	// binder which receives interactions from clients
	private final IBinder mBinder = new ServiceBinder();

	// binder class for service client
	public class ServiceBinder extends Binder {
		TaskService getService() {
			return TaskService.this;
		}
	}

	// all alarms are set if the service is started manually (reboot)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.v("TaskService", "onStartCommand");
		setAllMedAlarms();
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.v("TaskService", "onBind");
		return mBinder;
	}

	public void setMedAlarm(MedReminderEntity med) {
		Log.v("TaskService", "setMedAlarm " + med);
		new TaskMedAlarm(this, med).run();
	}

	public void cancelMedAlarm(MedReminderEntity med) {
		Log.v("TaskService", "cancelMedAlarm " + med);
		TaskMedAlarm.cancel(this, med);
	}

	public void setAllMedAlarms() {
		Log.v("TaskService", "setAllAlarms");

		// query all med reminders from DB and set them
		List<MedReminderEntity> listMeds = SugarRecord
				.listAll(MedReminderEntity.class);
		for (MedReminderEntity med : listMeds) {
			setMedAlarm(med);
		}
	}

}
