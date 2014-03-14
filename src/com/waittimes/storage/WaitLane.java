package com.waittimes.storage;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

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
	private static OrmLiteBaseActivity<DatabaseHelper> activity = null;
	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private Date lastUpdated;
	private JSONObject jsonRepresentation;
	
	WaitLane(){
		Log.i(WaitLane.class.getName(), "created new WaitLane object [()]");
		Log.i(WaitLane.class.getName(), "\twith id=["+this.id+"] lastUpdated=["+this.lastUpdated+"]");
		if(WaitLane.activity == null){
			return;
		}
		//get wait lane json stored in file system
		this.jsonRepresentation = this.getJSONInfo();
		
	}
		
	public WaitLane(JSONObject waitLaneJSON) throws JSONException{
		Log.i(WaitLane.class.getName(), "created new WaitLane object [(JSONObject)]");
		this.jsonRepresentation = waitLaneJSON;
		try {
			this.id = this.jsonRepresentation.getString("id");
			String lastUpdatedString = this.jsonRepresentation.getString("last_updated");
			lastUpdatedString = lastUpdatedString.replace("T", " ");
			lastUpdatedString = lastUpdatedString.replace("Z", " ");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.US);
			this.lastUpdated = dateFormat.parse(lastUpdatedString);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ParseException e) {
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
	public boolean store(){
	
		if(WaitLane.activity == null){
			return false;
		}
		
		RuntimeExceptionDao<WaitLane, String> dao = WaitLane.activity.getHelper().getWaitLaneDataDAO();
		//create database entry
		dao.createOrUpdate(this);
		//create filesystem directories
		File mainDirectory = WaitLane.activity.getDir(WaitLane.DIRECTORY_NAME, Context.MODE_PRIVATE);
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
	 * 
	 * @return
	 */
	public boolean remove(){
		
		if(WaitLane.activity == null){
			return false;
		}
		boolean removed = true;
		RuntimeExceptionDao<WaitLane, String> dao = WaitLane.activity.getHelper().getWaitLaneDataDAO();
		
		//remove wait lane from database
		if(dao.delete(this) != 1){
			removed = false;
		}
		
		//remove all files for this wait lane
		this.removeAllFiles();
		
		return removed;
	}
	/**
	 * removeAllFiles
	 * 
	 * Removes all files and directories for the this waitlane
	 */
	private void removeAllFiles(){
		if(WaitLane.activity == null){
			return;
		}
		
		File mainDirectory = WaitLane.activity.getDir(WaitLane.DIRECTORY_NAME, Context.MODE_PRIVATE);
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
	
	public JSONObject getJSONInfo(){
		JSONObject JSONInfo = null;
		try {
			FileInputStream fileInfo = WaitLane.activity.openFileInput("");
			JSONInfo = new JSONObject(fileInfo.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return JSONInfo;
	}
	/**
	 * STATIC METHODS
	 */
	
	/**
	 * Check weather the waitlane with the given id exists.
	 * 
	 * @return
	 */
	public static boolean exists(JSONObject waitLaneJSON) {
		boolean exists = false;
		//check that an activity has been set
		if(WaitLane.activity == null){
			return exists;
		}
		RuntimeExceptionDao<WaitLane, String> dao = WaitLane.activity.getHelper().getWaitLaneDataDAO();
		try {
			WaitLane waitLane = dao.queryForId(waitLaneJSON.getString("id"));
			if(waitLane != null){
				exists = true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return exists;
	}

	public static OrmLiteBaseActivity<DatabaseHelper> getActivity() {
		return WaitLane.activity;
	}

	public static void setActivity(OrmLiteBaseActivity<DatabaseHelper> activity) {
		WaitLane.activity = activity;
	}
}
