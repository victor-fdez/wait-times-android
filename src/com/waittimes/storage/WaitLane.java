package com.waittimes.storage;

import java.sql.Timestamp;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WaitLane")
public class WaitLane {
	
	@DatabaseField(id = true)
	private String id;
	
	@DatabaseField(canBeNull = false)
	private Timestamp lastUpdated;
	
	WaitLane(){
		
	}

}
