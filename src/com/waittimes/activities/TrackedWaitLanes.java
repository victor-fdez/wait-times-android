package com.waittimes.activities;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.waittimes.storage.DatabaseHelper;
import com.waittimes.storage.WaitLane;
import com.waittimes.ui.WaitLanesTrackedListAdapter;
import com.waittimes.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

public class TrackedWaitLanes extends OrmLiteBaseActivity<DatabaseHelper> {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		WaitLane.setActivity(this);
		
		//setup gui for this activity, then
		this.setContentView(R.layout.tracked_wait_times);
		
		//set up adapter for list, need to call on create
		//before the helper method to get database helper
		//can be used
		super.onCreate(savedInstanceState);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		ListView trackedList = (ListView)this.findViewById(R.id.trackedListView);
		WaitLanesTrackedListAdapter waitLanesAdapter = new WaitLanesTrackedListAdapter(this);
		trackedList.setAdapter(waitLanesAdapter);
		super.onStart();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = this.getMenuInflater();
		inflater.inflate(R.menu.tracked_wait_times, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.search_wait_lanes:
				Log.d(TrackedWaitLanes.class.getName(), "onOptionsItemSelected() selected search wait lanes");
				Intent intent = new Intent(this, SearchWaitLanes.class);
				this.startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onStop();
	}

}
