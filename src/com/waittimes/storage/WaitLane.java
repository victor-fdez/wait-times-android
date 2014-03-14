package com.waittimes.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "WaitLane")
public class WaitLane {
	private static final String DIRECTORY_NAME = "WaitLanes";
	
	@DatabaseField(id = true)
	private String id;
	@DatabaseField(canBeNull = false)
	private Timestamp lastUpdated;
	private JSONObject jsonRepresentation;
	
	WaitLane(){
		
	}
		
	public WaitLane(JSONObject waitLaneJSON) throws JSONException{
		this.jsonRepresentation = waitLaneJSON;
		try {
			this.id = this.jsonRepresentation.getString("id");
			String lastUpdatedString = this.jsonRepresentation.getString("last_updated");
			lastUpdatedString = lastUpdatedString.replace("T", " ");
			lastUpdatedString = lastUpdatedString.replace("Z", " ");
			this.lastUpdated = Timestamp.valueOf(lastUpdatedString);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}
	/**
	 * store
	 * 
	 * Creates a WaitLane row in the database, and if not already there creates a directory for
	 * the given WaitLane in the applications private directory.
	 * 
	 * 	/WaitLane/x/info.json
	 * 
	 * Where x is the integer id of the WaitLane.
	 * 
	 * @param dao
	 */
	public boolean store(OrmLiteBaseActivity<DatabaseHelper> activity){
	
		if(activity == null){
			return false;
		}
		
		RuntimeExceptionDao<WaitLane, Integer> dao = activity.getHelper().getWaitLaneDataDAO();
		//create database entry
		dao.createOrUpdate(this);
		//create filesystem directories
		File mainDirectory = activity.getDir(WaitLane.DIRECTORY_NAME, Context.MODE_PRIVATE);
		File waitLaneJSONFile = new File(mainDirectory.getAbsoluteFile()+
										File.separator+
										this.id+
										File.separator+
										"info.json");
		if(!waitLaneJSONFile.getParentFile().exists()){
			waitLaneJSONFile.getParentFile().mkdir();
		}
		boolean problem = false;
		try {
			if(!waitLaneJSONFile.exists()){
				waitLaneJSONFile.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			problem = true;
		}
		if(problem)
			return false;
		//store JSON file for the given wait lane
		BufferedOutputStream bos = null;
		problem = false;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(waitLaneJSONFile));
			bos.write(this.jsonRepresentation.toString().getBytes());
			bos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			problem = true;
		} catch (IOException e) {
			e.printStackTrace();
			problem = true;
		} finally {
			if(bos != null){
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
					problem = true;
				}
			}
		}
		if(problem)
			return false;
		return true;
	}
	/**
	 * removeAllFiles
	 * 
	 * Removes all files and directories for the this waitlane
	 */
	private void removeAllFiles(OrmLiteBaseActivity<DatabaseHelper> activity){
		if(activity == null){
			return;
		}
		
		File mainDirectory = activity.getDir(WaitLane.DIRECTORY_NAME, Context.MODE_PRIVATE);
		File waitLaneJSONFile = new File(mainDirectory.getAbsoluteFile()+
				File.separator+
				this.id+
				File.separator);
		if(waitLaneJSONFile.exists()){
			for(File file: waitLaneJSONFile.listFiles()){
				Log.i(WaitLane.class.getName(),"deleted ["+file.getAbsolutePath()+"]");
				file.delete();
			}
			Log.i(WaitLane.class.getName(),"deleted ["+waitLaneJSONFile.getAbsolutePath()+"]");
			waitLaneJSONFile.delete();
		}
	}
}
