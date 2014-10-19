package org.herndl.parkinsonapp;

import java.util.Calendar;
import java.util.Comparator;

import android.util.Log;

import com.orm.SugarRecord;

public class MedReminderEntity extends SugarRecord<MedReminderEntity> {

	public String name;
	public Integer dose;
	public Integer remind_hour;
	public Integer remind_minute;

	public MedReminderEntity() {
	}

	public MedReminderEntity(String name, Integer dose, Integer remind_hour,
			Integer remind_minute) {
		this.name = name;
		this.dose = dose;
		this.remind_hour = remind_hour;
		this.remind_minute = remind_minute;
	}

	@Override
	public String toString() {
		return "MedReminderEntity [name=" + name + ", dose=" + dose
				+ ", remind_hour=" + remind_hour + ", remind_minute="
				+ remind_minute + "]";
	}

	public int getIntId() {
		long id = getId();
		int intId = 0;
		if (id >= Integer.MIN_VALUE && id <= Integer.MAX_VALUE)
			intId = (int) id;
		else
			Log.w("MedReminderEntity",
					"Can't cast long to int without loosing information");
		return intId;

	}

	public static class medComparator implements Comparator<MedReminderEntity> {
		@Override
		public int compare(MedReminderEntity med1, MedReminderEntity med2) {
			Calendar calendar1 = Calendar.getInstance();
			calendar1.set(Calendar.HOUR_OF_DAY, med1.remind_hour);
			calendar1.set(Calendar.MINUTE, med1.remind_minute);
			calendar1.set(Calendar.SECOND, 0);

			Calendar calendar2 = Calendar.getInstance();
			calendar2.set(Calendar.HOUR_OF_DAY, med2.remind_hour);
			calendar2.set(Calendar.MINUTE, med2.remind_minute);
			calendar2.set(Calendar.SECOND, 0);

			return calendar1.compareTo(calendar2);
		}
	}

}
