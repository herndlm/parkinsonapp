package org.herndl.parkinsonapp;

import com.orm.SugarRecord;

public class MedReminderEntity extends SugarRecord {
	
	public String name;
	public Integer dose;
	public Integer remind_hour;
	public Integer remind_minute;
	
	public MedReminderEntity() {
	}

	public MedReminderEntity(String name, Integer dose, Integer remind_hour, Integer remind_minute) {
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

}
