package com.waittimes.ui;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.waittimes.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class JSONWaitLanesSearchAdapter extends BaseAdapter {
	
	private JSONArray waitLanes;
	private LayoutInflater inflater;
	public JSONWaitLanesSearchAdapter(Activity activity, JSONObject jsonObj) throws JSONException{
		this.waitLanes = jsonObj.getJSONArray("waitLanes");
		this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		//get lane  
		TextView waitLaneName = (TextView)view.findViewById(R.id.waitLaneNameTextView);
		
		//set values
		try {
			waitLaneName.setText(waitLane.getString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}	
		
		return view;
	}

}
