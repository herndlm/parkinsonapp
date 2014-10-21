package org.herndl.parkinsonapp.track;

import java.util.Calendar;

import org.herndl.parkinsonapp.med.MedReminderEntity;

import com.orm.SugarRecord;

public class TrackerEntity extends SugarRecord<MedReminderEntity> {

	public String type;
	public String name;
	public int intValue;
	public String stringValue;
	public Calendar calendar;

	public TrackerEntity() {
	}

	public TrackerEntity(String type, String name, int intValue,
			String stringValue, Calendar calendar) {
		super();
		this.type = type;
		this.name = name;
		this.intValue = intValue;
		this.stringValue = stringValue;
		this.calendar = calendar;
	}

	@Override
	public String toString() {
		return "TrackerEntity [type=" + type + ", name=" + name + ", intValue="
				+ intValue + ", stringValue=" + stringValue + ", calendar="
				+ calendar + "]";
	}

}
