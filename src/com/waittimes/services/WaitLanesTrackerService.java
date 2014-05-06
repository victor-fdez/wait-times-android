package com.waittimes.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class WaitLanesTrackerService extends Service {
	// Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    
	public class LocalBinder extends Binder {
        WaitLanesTrackerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return WaitLanesTrackerService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return this.mBinder;
	}

	
}
