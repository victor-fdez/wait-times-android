package com.waittimes.storage;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.waittimes.R;
import com.waittimes.utilities.BitmapGetterTask;
import com.waittimes.utilities.HTTPEntityGetter;
import com.waittimes.utilities.InputStreamTask;
import com.waittimes.utilities.JSONGetterTask;

@DatabaseTable(tableName = "WaitLane")
public class WaitLane {
	@DatabaseField(id = true)
	private String id;
	@DatabaseField
	private Date lastUpdated;
	
	private JSONObject jsonRepresentation;
	private static int  jsonLocator = 0,
			localLocator = 1,
			isGlobal = 2,
			fileType = 3;
	private static String GlobalFile = "global",
							InstanceFile = "instance",
							jsonType = "json",
							bitmapType = "bitmap",
							GeoModelsFolder = "models/", 
							PicturesFolder = "pictures/";

	private static Map<String, String[]> dirs = new HashMap<String, String[]>();
	static {
			dirs.put("info", new String[]{null, "info.json", InstanceFile, jsonType});
			dirs.put("model", new String[]{"files,model", GeoModelsFolder+"model.json", InstanceFile, jsonType});
			dirs.put("boundary", new String[]{"files,boundary", GeoModelsFolder+"boundary.json", InstanceFile, jsonType});
			dirs.put("exists", new String[]{"files,exists", GeoModelsFolder+"exists.json", InstanceFile, jsonType});
			dirs.put("entries", new String[]{"files,entries", GeoModelsFolder+"entries.json", InstanceFile, jsonType});
			dirs.put("originPicture", new String[]{"origin,country,flag", PicturesFolder+"%.gif", GlobalFile, bitmapType});
			dirs.put("destinationPicture", new String[]{"destination,country,flag", PicturesFolder+"%.gif", GlobalFile, bitmapType});
	}

	private static final String INSTANCE_DIR = "WaitLanes";
	private static final String GLOBAL_DIR = "global";
	private static OrmLiteBaseActivity<DatabaseHelper> activity = null;
	
	
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
			this.id = this.getJsonRepresentation().getString("id");
			String lastUpdatedString = this.getJsonRepresentation().getString("last_updated");
			lastUpdatedString = lastUpdatedString.replace("T", " ");
			lastUpdatedString = lastUpdatedString.replace("Z", " ");
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ", Locale.US);
			this.lastUpdated = dateFormat.parse(lastUpdatedString);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
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
	 * @return the jsonRepresentation
	 * @throws FileNotFoundException 
	 */
	public JSONObject getJsonRepresentation() throws FileNotFoundException {
		if(this.jsonRepresentation == null){
			this.jsonRepresentation = this.getJSONInfo();
		}
		return this.jsonRepresentation;
	}

	/**
	 * @param jsonRepresentation the jsonRepresentation to set
	 */
	public void setJsonRepresentation(JSONObject jsonRepresentation) {
		this.jsonRepresentation = jsonRepresentation;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		String name = null;
		try {
			name = this.getJsonRepresentation().getString("name");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		File mainDirectory = WaitLane.activity.getDir(WaitLane.INSTANCE_DIR, Context.MODE_PRIVATE);
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
	 * removes all files for this wait lane using depth first
	 * search removal.
	 * 
	 */
	private void removeAllFiles(){
		if(WaitLane.activity == null){
			return;
		}
		File mainDirectory = WaitLane.activity.getDir(WaitLane.INSTANCE_DIR, Context.MODE_PRIVATE);
		File waitLaneFolder = new File(mainDirectory.getAbsoluteFile()+
				File.separator+
				this.id+
				File.separator);
		this.removeDirectory(waitLaneFolder);
	}
	/**
	 * Depth first search removal of all files under a given
	 * directory.
	 * 
	 * @param directory
	 */
	private void removeDirectory(File directory){
		Stack<File> stack = new Stack<File>();
		stack.push(directory);
		while(!stack.isEmpty()){
			File file = stack.peek();
			if(file.isDirectory()){
				File[] childrenFiles = file.listFiles();
				if(childrenFiles.length == 0){
					file.delete();
					stack.pop();
				}else{
					for(File childFile : childrenFiles){
						stack.push(childFile);
					}
				}
			}else{
				file.delete();
				stack.pop();
			}
		}
	}
	/**
	 * Returns a JSON Object representing the locally stored file
	 * in the application directory.
	 * 
	 * @return
	 * @throws FileNotFoundException 
	 */
	public JSONObject getJSONInfo() throws FileNotFoundException{
		return this.getJSONObjectResource("info");
	}
	/**
	 * Gets bitmap for origin flag of this wait lane.
	 * 
	 * @return
	 */
	public Bitmap getBitmapOriginFlag(){
		return this.getBitmapProperty("originPicture");
	}
	/**
	 * Gets bitmap for destination flag of this wait lane.
	 * 
	 * @return
	 */
	public Bitmap getBitmapDestinationFlag(){
		return this.getBitmapProperty("destinationPicture");
	}
	/**
	 * Get bitmap for the given directory passed, these are defined as
	 * static arrays for a wait lane.
	 * 
	 * @param string
	 * @return
	 */
	private static Map<String, String> CountryCodesMap = new HashMap<String, String>();
	static {
			CountryCodesMap.put("originPicture", "origin,country,code");
			CountryCodesMap.put("destinationPicture", "destination,country,code");
	}
	private Bitmap getBitmapProperty(String resourceName){
		//this.getWaitLaneFileInputStream(resourceName, )
		Bitmap map = null;
		String code = this.getStringPrefixJSONObjects(WaitLane.CountryCodesMap.get(resourceName));
		try {
			InputStream stream = this.getWaitLaneFileInputStream(resourceName, new String[][]{new String[]{code}, null});
			map = BitmapFactory.decodeStream(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * Gets the JSONObject for the specified resource name.
	 * 
	 * @param resourceName
	 * @return
	 * @throws FileNotFoundException
	 */
	private JSONObject getJSONObjectResource(String resourceName) throws FileNotFoundException{
		InputStream stream = this.getWaitLaneFileInputStream(resourceName, null);
		JSONObject object = null;
		try {
			object = new JSONObject(IOUtils.toString(stream));
			stream.close();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return object;
	}
	/**
	 * get an input stream for the given resource
	 * 
	 * @param resourceName
	 * @param holders
	 * @return
	 * @throws FileNotFoundException
	 */
	private InputStream getWaitLaneFileInputStream(String resourceName, String[][] holders) throws FileNotFoundException{
		File file = null;
		if(holders == null){
			file = this.getWaitLaneFile(resourceName, null, null);
		}else{
			file = this.getWaitLaneFile(resourceName, holders[0], holders[1]);
		}
		return new FileInputStream(file);
	}
	/**
	 * Returns a file relative to this wait lane within
	 * the application directory.
	 * 
	 * @return
	 */
	private File getWaitLaneFile(String resourceName, String[] localPlaceHolders, String[] remotePlaceHolders){
		String localLocator = this.getLocalLocator(resourceName, localPlaceHolders);
		File localFile = new File(localLocator);
		//TODO: add caching capability here by checking
		//time at which remote wait lane was created 
		if(!localFile.exists()){
			String remoteLocator = this.getRemoteLocator(resourceName, remotePlaceHolders);
			this.getRemoteResource(remoteLocator, localLocator);
		}
		File waitLaneFile = new File(localLocator);
		return waitLaneFile;
	}
	/**
	 * Get remote resource and store in a file.
	 * 
	 * @param remoteLocator
	 * @param localLocator
	 * @param fileType
	 * @return
	 */
	private void getRemoteResource(String remoteLocator, String localLocator) {
		File localFile = new File(localLocator);
		if (localFile.getParentFile().exists() || localFile.getParentFile().mkdirs()){
	        try
	        {
	            localFile.createNewFile();
	        }
	        catch(IOException ioe)
	        {
	            ioe.printStackTrace();
	            return;
	        }
		} else {
			Log.d(WaitLane.class.getCanonicalName(), "created file or non-existent parent directories");
		}
		try{
			//TODO: make this only return an input stream object
			/*if(fileType.equals("json")){
				//get json object and write it to local file
				JSONGetterTask task = new JSONGetterTask();
				task.execute(new URI(remoteLocator));
				JSONObject object = task.get();
				writer.write(object.toString().getBytes());
				resource = object;
			}else if(fileType.equals("bitmap")){
				//get bitmap of image and write it to local file
				BitmapGetterTask task = new BitmapGetterTask();
				task.execute(new URI(remoteLocator));
				Bitmap bitmap = task.get();
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, writer);
				resource = bitmap;
			}*/
			InputStreamTask inputTask = new InputStreamTask();
			inputTask.execute(new URI(remoteLocator));
			InputStream stream = inputTask.get();
			FileUtils.copyInputStreamToFile(stream, localFile);
			
		}catch(ExecutionException e){
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.d(WaitLane.class.getCanonicalName(), "downloaded resource -> ("+fileType+")"+remoteLocator);
		Log.d(WaitLane.class.getCanonicalName(), "to "+localLocator);
	}

	/**
	 * Get the remote uri string representation of the given
	 * resource.
	 * 
	 * @param resourceName
	 * @return
	 */
	private String getRemoteLocator(String resourceName, String[] placeHolders) {
		StringBuilder stringBld = new StringBuilder();
		stringBld.append("http://"+WaitLane.activity.getString(R.string.domain));
		//append remote path
		if(WaitLane.dirs.get(resourceName)[WaitLane.jsonLocator] != null){
				 stringBld.append(this.getStringPrefixJSONObjects(WaitLane.dirs.get(resourceName)[WaitLane.jsonLocator]));
		}else{
				 stringBld.append("file/"+this.id+"/info.json");
		}
		return this.replaceWithPlaceHolders(stringBld.toString(), placeHolders);
	}
	/**
	 * Get a string representing the file absolute location where
	 * the resource should be located. This file path is formed based
	 * on the static configuration written on the top of the class.
	 * If the final path contains % marks they will be replaced with
	 * the given place holders in order.
	 * 
	 * @param resourceName
	 * @param placeHolders
	 * @return
	 */
	private String getLocalLocator(String resourceName, String[] placeHolders) {
		StringBuilder stringBld = new StringBuilder();	
		Map<String, String[]> dir = WaitLane.dirs;
		dir.get("hello");
		if(WaitLane.dirs.get(resourceName)[WaitLane.isGlobal].equals("global")){
			File waitTimesDirectory = WaitLane.activity.getDir(WaitLane.GLOBAL_DIR, Context.MODE_PRIVATE);
			stringBld.append(waitTimesDirectory.getAbsoluteFile())
					 .append(File.separator)
					 .append(WaitLane.dirs.get(resourceName)[WaitLane.localLocator]);
		}else{
			File waitTimesDirectory = WaitLane.activity.getDir(WaitLane.INSTANCE_DIR, Context.MODE_PRIVATE);
			stringBld.append(waitTimesDirectory.getAbsoluteFile())
					 .append(File.separator)
					 .append(this.id)
					 .append(File.separator)
					 .append(WaitLane.dirs.get(resourceName)[WaitLane.localLocator]);
		}
		String locator = this.replaceWithPlaceHolders(stringBld.toString(), placeHolders);
		return locator;
	}
	/**
	 * places each of the places holders in order in the
	 * location within the string were there is a "%".
	 * eg. 
	 * 
	 * "xasdf%assfd", {"FF"} => "xasdfFFassfd"
	 * 
	 * @param str
	 * @param placeHolders
	 * @return
	 */
	private String replaceWithPlaceHolders(String str, String[] placeHolders){
		StringBuilder stringBld = new StringBuilder();
		int placeHolderIndex = 0;
		do{
			int index = str.indexOf("%");
			if(index > -1){
				stringBld.append(str.substring(0, index))
					     .append(placeHolders[placeHolderIndex]);
				str = str.substring(index+1);
				placeHolderIndex++;
			}else{
				stringBld.append(str);
				break;
			}
		}while(true);
		return stringBld.toString();
	}

	/**
	 * Gets a string given that all objects before the string in JSON
	 * representation of this wait lane are JSON objects.
	 * 
	 * @param locator
	 * @return
	 */
	private String getStringPrefixJSONObjects(String locator){
		String[] locatorArray = this.locatorSplitter(locator);
		String string = null;
		JSONObject jsonObject = null;
		try {
			jsonObject = this.getJsonRepresentation();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		for(String dir: locatorArray){
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
	private String[] locatorSplitter(String locator){
		String[] locatorArray = locator.split(",");
		return locatorArray;
	}
	/**
	 * STATIC METHODS
	 */
	/**
	 * get the wait lane with the specified id, it can only
	 * get a wait lane that is local
	 * 
	 * @param id
	 * @return
	 */
	public static WaitLane getWaitLaneWithID(String id){
		RuntimeExceptionDao<WaitLane, String> dao = WaitLane.activity.getHelper().getWaitLaneDataDAO();
		WaitLane waitLane = dao.queryForId(id);
		return waitLane;
	}
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
