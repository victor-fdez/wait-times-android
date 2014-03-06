package com.example.waittimes.utilities.json;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.waittimes.LocationTest;

import android.os.AsyncTask;
import android.util.Log;

public class JSONGetTask extends AsyncTask<URI, Integer, JSONObject> {
	
	@Override
	
	protected JSONObject doInBackground(URI... uris) {
		//assume there is only one url for now
		//setup GET request to get document
		HttpClient client = new DefaultHttpClient();
		URI uri = uris[0];
		HttpGet request = new HttpGet(uri);
		try{
			//send request
			HttpResponse response = client.execute(request);
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() == HttpStatus.SC_OK){
				//get input reader and parse JSON from response object
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				response.getEntity().writeTo(outputStream);
				outputStream.close();
				Log.d(LocationTest.tag, "succesfully downloaded json file with URI="+uri);
				//get JSON object
				JSONObject json = new JSONObject(outputStream.toString());
				Log.d(LocationTest.tag, "succesfully parsed json object");
				return json;
			}
			else{
				response.getEntity().getContent().close();
				throw new IOException(status.getReasonPhrase());
			}
		}catch(ClientProtocolException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		// TODO Auto-generated method stub
		super.onProgressUpdate(values);
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
	}
}
