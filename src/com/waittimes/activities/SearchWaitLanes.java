package com.waittimes.activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.waittimes.R;
import com.waittimes.ui.JSONWaitLanesSearchAdapter;
import com.waittimes.utilities.*;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;


public class SearchWaitLanes extends Activity {
	public final static String tag = SearchWaitLanes.class.getName();
	public final static String ACTIVITY_TITLE = "Add Wait Lanes";
	private ListView list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.search_wait_times);
		this.list = (ListView)this.findViewById(R.id.searchListView);
		ActionBar actionBar = this.getActionBar();
		actionBar.setTitle(SearchWaitLanes.ACTIVITY_TITLE);
		JSONGetTask task = new JSONGetTask();
		try {
			JSONObject jsonObj = task.getItNow(new URI("http://"+this.getString(R.string.domain)+"/WaitLanes/file/all/list.json"));	
			JSONWaitLanesSearchAdapter adapter = new JSONWaitLanesSearchAdapter(this, jsonObj);
			this.list.setAdapter(adapter);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		super.onCreate(savedInstanceState);
	}

}
