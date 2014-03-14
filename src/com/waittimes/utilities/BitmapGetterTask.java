package com.waittimes.utilities;

import java.io.IOException;
import java.net.URI;

import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class BitmapGetterTask extends AsyncTask<URI, Integer, Bitmap> {
	
	@Override
	protected Bitmap doInBackground(URI... uris) {
		Bitmap map = null;
		try {
			byte[] bytes = EntityUtils.toByteArray(new HTTPEntityGetter().getItNow(uris));
			map = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
}
