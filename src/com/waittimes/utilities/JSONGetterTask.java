package com.waittimes.utilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class JSONGetterTask extends AsyncTask<URI, Integer, JSONObject>{
	public static String tag = JSONGetterTask.class.getName();
	
	@Override
	protected JSONObject doInBackground(URI... uris) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		JSONObject json = null;
		try {
			HttpEntity entity = new HTTPEntityGetter().getItNow(uris);
			entity.writeTo(outputStream);
			outputStream.close();
			json = new JSONObject(outputStream.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Log.d(JSONGetterTask.tag, "succesfully parsed json object");
		return json;
	}
}
