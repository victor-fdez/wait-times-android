package com.waittimes.storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.waittimes.R;
import com.waittimes.utilities.BitmapGetterTask;

@DatabaseTable(tableName = "WaitLane")
public class WaitLane {

	public static final String[] ORIGIN_PICTURE_DIR = {"origin","country", "flag"};
	public static final String[] DESTIN_PICTURE_DIR = {"destination", "country", "flag"};
	public static final String[] MODEL_DIR = {"files", "model"};
	public static final String[] BOUNDARY_DIR = {"files", "boundary"};
	public static final String[] EXITS_DIR = {"files", "exits"};
	public static final String[] ENTRIES_DIR = {"files", "entries"};

	private static final String DIRECTORY_NAME = "WaitLanes";
	private static OrmLiteBaseActivity<DatabaseHelper> activity = null;
	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private Date lastUpdated;
	private JSONObject jsonRepresentation;
	
	
	WaitLane(){
		super();
		Log.i(WaitLane.class.getName(), "created new WaitLane object [()]");
		Log.i(WaitLane.class.getName(), "\twith id=["+this.id+"] lastUpdated=["+this.lastUpdated+"]");
		if(WaitLane.activity == null){
			return;
		}
		//get wait lane json stored in file system
		//this.jsonRepresentation = this.getJSONInfo();
		
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
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the lastUpdated
	 */
	public Date getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * @param lastUpdated the lastUpdated to set
	 */
	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		String name = null;
		if(this.jsonRepresentation == null){
			this.jsonRepresentation = this.getJSONInfo();
		}
		try {
			name = this.jsonRepresentation.getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return name;
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
	 * remove locally all of the information stored for this instance of
	 * a wait lane.
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
	/**
	 * Returns a JSON Object representing the locally stored file
	 * in the application directory.
	 * 
	 * @return
	 */
	public JSONObject getJSONInfo(){
		JSONObject JSONInfo = null;
		BufferedReader r = null;
		try {
			//get info.json to a JSONObject
			r = new BufferedReader(new FileReader(this.getWaitLaneFile("info.json")));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
			    total.append(line);
			}
			JSONInfo = new JSONObject(total.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(r != null){
					r.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return JSONInfo;
	}
	/**
	 * Returns a file relative to this wait lane within
	 * the application directory.
	 * 
	 * @return
	 */
	private File getWaitLaneFile(String filePath){
		File waitTimesDirectory = WaitLane.activity.getDir(WaitLane.DIRECTORY_NAME, Context.MODE_PRIVATE);
		File waitLaneFile = new File(waitTimesDirectory.getAbsoluteFile()+
				File.separator+
				this.id+
				File.separator+
				filePath);
		return waitLaneFile;
	}
	/**
	 * Get bitmap for the given directory passed, these are defined as
	 * static arrays for a wait lane.
	 * 
	 * @param directory
	 * @return
	 */
	public Bitmap getBitmapProperty(String[] directory){
		String uriDirectory = this.getStringPrefixJSONObjects(directory);
		URI uri = null;
		try {
			uri = new URI("http://"+WaitLane.activity.getString(R.string.domain)+uriDirectory);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		//return a null if not uri is found
		if(uri == null){
			return null;
		}
		return this.getBitmapAtURI(uri);
	}
	/**
	 * Gets the bitmap at the provided URI if it is possible
	 * else it returns null.
	 * 
	 * @param uri
	 * @return
	 */
	private Bitmap getBitmapAtURI(URI uri){
		BitmapGetterTask bitmapGetter = new BitmapGetterTask();
		bitmapGetter.execute(uri);
		Bitmap bitmap = null;
		try {
			bitmap = bitmapGetter.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
	/**
	 * Gets a string given that all objects before the string in JSON
	 * representation of this wait lane are JSON obejcts.
	 * 
	 * @param directory
	 * @return
	 */
	private String getStringPrefixJSONObjects(String[] directory){
		JSONObject jsonObject = this.jsonRepresentation;
		String string = null;
		for(String dir: directory){
			//get object if it is an object
			try {
				jsonObject = jsonObject.getJSONObject(dir);
				continue;
			} catch (JSONException e) {}
		
			//if it is not an object then get the string
			try {
				string = jsonObject.getString(dir);
				break;
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		}
		return string;
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
	/**
	 * Returns a list of all the wait lanes in the local database
	 * 
	 * @return
	 */
	public static List<WaitLane> getAllTrackedWaitLanes(){
		if(WaitLane.activity == null){
			Log.e(WaitLane.class.getName(), "getAllTrackedLanes() could not find activity set");
			return null;
		}
		RuntimeExceptionDao<WaitLane, String> dao = WaitLane.activity.getHelper().getWaitLaneDataDAO();
		Log.d(WaitLane.class.getName(), "getAllTrackedLanes() dao is "+dao);
		return dao.queryForAll();
	}
	
	public static OrmLiteBaseActivity<DatabaseHelper> getActivity() {
		return WaitLane.activity;
	}

	public static void setActivity(OrmLiteBaseActivity<DatabaseHelper> activity) {
		WaitLane.activity = activity;
	}
}
