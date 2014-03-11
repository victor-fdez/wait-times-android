package com.waittimes.utilities;

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class HTTPEntityAsyncTask extends AsyncTask<URI, Integer, HttpEntity>{
	public static String tag = HTTPEntityAsyncTask.class.getName();
	@Override
	protected HttpEntity doInBackground(URI... uris) {
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
				Log.d(HTTPEntityAsyncTask.tag, "succesfully downloaded http object="+uri);
				return response.getEntity();
			}
			else{
				response.getEntity().getContent().close();
				throw new IOException(status.getReasonPhrase());
			}
		}catch(ClientProtocolException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
