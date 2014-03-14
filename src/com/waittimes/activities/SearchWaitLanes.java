package com.waittimes.activities;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.waittimes.R;
import com.waittimes.storage.DatabaseHelper;
import com.waittimes.storage.WaitLane;
import com.waittimes.ui.JSONWaitLanesSearchAdapter;
import com.waittimes.utilities.*;

import android.app.ActionBar;
import android.os.Bundle;
import android.widget.ListView;


public class SearchWaitLanes extends OrmLiteBaseActivity<DatabaseHelper> {
	public final static String tag = SearchWaitLanes.class.getName();
	public final static String ACTIVITY_TITLE = "Add/Remove Wait Lanes";
	private ListView list;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.search_wait_times);
		this.list = (ListView)this.findViewById(R.id.searchListView);
		WaitLane.setActivity(this);	
		ActionBar actionBar = this.getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(SearchWaitLanes.ACTIVITY_TITLE);
		JSONGetterTask task = new JSONGetterTask();
		try {
			task.execute(new URI("http://"+this.getString(R.string.domain)+"/WaitLanes/file/all/list.json"));
			JSONObject jsonObj = task.get();	
			JSONWaitLanesSearchAdapter adapter = new JSONWaitLanesSearchAdapter(this, jsonObj);
			this.list.setAdapter(adapter);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		super.onCreate(savedInstanceState);
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		WaitLane.setActivity(null);
		super.onStop();
	}

}
