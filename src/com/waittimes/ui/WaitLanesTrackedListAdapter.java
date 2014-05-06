package com.waittimes.ui;

import java.util.List;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.waittimes.R;
import com.waittimes.activities.DetailWaitLanes;
import com.waittimes.storage.DatabaseHelper;
import com.waittimes.storage.WaitLane;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WaitLanesTrackedListAdapter extends BaseAdapter implements OnItemClickListener {
	
	private List<WaitLane> waitLanes;
	private LayoutInflater inflater;
	public WaitLanesTrackedListAdapter(OrmLiteBaseActivity<DatabaseHelper> activity){
		this.waitLanes = WaitLane.getAllTrackedWaitLanes();
		this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return waitLanes.size();
	}

	@Override
	public Object getItem(int position) {
		return waitLanes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Long.parseLong(waitLanes.get(position).getId());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null)
			view = this.inflater.inflate(R.layout.tracked_wait_times_row, null);
		
		WaitLane waitLane =	this.waitLanes.get(position);
		//get waitlane
		TextView waitLaneNameView = (TextView)view.findViewById(R.id.waitLaneNameTextView);
		TextView waitLaneTimeTextView = (TextView)view.findViewById(R.id.waitTimeTextView);
		TextView waitLaneTimeInterpretationTextView = (TextView)view.findViewById(R.id.waitTimeInterpretationTextView);
		ImageView originImageView = (ImageView)view.findViewById(R.id.originCountryImageView);
		ImageView destinationImageView = (ImageView)view.findViewById(R.id.destinationCountryImageView);

		//set text for each row
		waitLaneNameView.setText(waitLane.getName());
		waitLaneTimeTextView.setText("wait time: 0 mins");
		waitLaneTimeInterpretationTextView.setText("no traffic");
		
		//set country images for waitlane
		originImageView.setImageBitmap(waitLane.getBitmapOriginFlag());
		destinationImageView.setImageBitmap(waitLane.getBitmapDestinationFlag());
	
		//setup view tag
		view.setTag(waitLane);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
		Log.d(WaitLanesTrackedListAdapter.class.getCanonicalName(),"clicked another row");
		Intent intent = new Intent(WaitLane.getActivity(), DetailWaitLanes.class);
		WaitLane waitLane = (WaitLane)view.getTag();
		intent.putExtra("id", waitLane.getId());
		WaitLane.getActivity().startActivity(intent);
		
	}
	

}
