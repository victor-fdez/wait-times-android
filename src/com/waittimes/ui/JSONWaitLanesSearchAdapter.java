package com.waittimes.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.waittimes.R;
import com.waittimes.utilities.BitmapGetTask;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class JSONWaitLanesSearchAdapter extends BaseAdapter {
	
	private JSONArray waitLanes;
	private LayoutInflater inflater;
	private String domain;
	public JSONWaitLanesSearchAdapter(Activity activity, JSONObject jsonObj) throws JSONException{
		this.waitLanes = jsonObj.getJSONArray("waitLanes");
		this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.domain = activity.getString(R.string.domain);
	}

	@Override
	public int getCount() {
		return waitLanes.length();
	}

	@Override
	public Object getItem(int position) {
		try {
			return waitLanes.get(position);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		try {
			return this.waitLanes.getJSONObject(position).getInt("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null)
			view = this.inflater.inflate(R.layout.search_wait_times_row, null);
		
		JSONObject waitLane = (JSONObject)this.getItem(position);
		//get waitlane
		Map<String, ImageView> imageViews = new HashMap<String, ImageView>();
		TextView waitLaneNameView = (TextView)view.findViewById(R.id.waitLaneNameTextView);
		imageViews.put("origin", (ImageView)view.findViewById(R.id.originCountryImageView));
		imageViews.put("destination", (ImageView)view.findViewById(R.id.destinationCountryImageView));

		try {
			waitLaneNameView.setText(waitLane.getString("name"));
			//set country images for waitlane
			for(Map.Entry<String, ImageView> entry: imageViews.entrySet()){
				URI flagURI = new URI("http://"+
						this.domain+
						waitLane.getJSONObject(entry.getKey()).getJSONObject("country").getString("flag"));
				//get bitmap and set it on the image view
				BitmapGetTask bitmapGetter = new BitmapGetTask();
				bitmapGetter.execute(flagURI);
				entry.getValue().setImageBitmap(bitmapGetter.get());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}	
		
		return view;
	}

}
