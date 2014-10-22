package org.herndl.parkinsonapp.med;

import java.util.Calendar;
import java.util.Comparator;

import com.orm.SugarRecord;

// DB persisted med reminder entity
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dose == null) ? 0 : dose.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((remind_hour == null) ? 0 : remind_hour.hashCode());
		result = prime * result
				+ ((remind_minute == null) ? 0 : remind_minute.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MedReminderEntity other = (MedReminderEntity) obj;
		if (dose == null) {
			if (other.dose != null)
				return false;
		} else if (!dose.equals(other.dose))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (remind_hour == null) {
			if (other.remind_hour != null)
				return false;
		} else if (!remind_hour.equals(other.remind_hour))
			return false;
		if (remind_minute == null) {
			if (other.remind_minute != null)
				return false;
		} else if (!remind_minute.equals(other.remind_minute))
			return false;
		return true;
	}

}
