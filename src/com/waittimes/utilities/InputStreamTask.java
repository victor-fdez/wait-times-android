package com.waittimes.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class InputStreamTask extends AsyncTask<URI, Integer, InputStream> {
	
	@Override
	protected InputStream doInBackground(URI... uris) {
		
		InputStream stream = null;
		try {
			stream = new HTTPEntityGetter().getItNow(uris).getContent();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}
}
