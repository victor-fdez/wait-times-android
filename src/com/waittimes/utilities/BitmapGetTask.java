package com.waittimes.utilities;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapGetTask extends HTTPEntityAsyncTask {
	
	public Bitmap getItNow(URI uri){
		Bitmap map = null;
		this.execute(uri);
		try {
			byte[] bytes = EntityUtils.toByteArray(this.get());
			map = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		return map;
	}
}
