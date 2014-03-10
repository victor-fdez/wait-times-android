package com.waittimes.activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.waittimes.R;
import com.waittimes.ui.JSONWaitLanesSearchAdapter;
import com.waittimes.utilities.json.*;

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
		String domain = this.getString(R.string.domain);
		ActionBar actionBar = this.getActionBar();
		actionBar.setTitle(SearchWaitLanes.ACTIVITY_TITLE);
		JSONGetTask task = new JSONGetTask();
		try {
			task.execute(new URI("http://"+domain+":8001/WaitLanes/file/all/list.json"));
			JSONObject jsonObj = task.get();
			JSONWaitLanesSearchAdapter adapter = new JSONWaitLanesSearchAdapter(this, jsonObj);
			this.list.setAdapter(adapter);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		super.onCreate(savedInstanceState);
	}

}
