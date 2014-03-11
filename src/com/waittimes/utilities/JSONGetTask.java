package com.waittimes.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JSONGetTask extends HTTPEntityAsyncTask {
	public static String tag = JSONGetTask.class.getName();
	public JSONObject getItNow(URI... uris){
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JSONObject json = null;
		try {
			this.get().writeTo(outputStream);
			outputStream.close();
			json = new JSONObject(outputStream.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d(JSONGetTask.tag, "succesfully parsed json object");
		return json;
	}
}
