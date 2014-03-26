package com.waittimes;

import com.waittimes.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import android.location.Location;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.TextView;

@SuppressLint("SetJavaScriptEnabled")
public class LocationTest 
	extends FragmentActivity 
	implements 	ConnectionCallbacks, 
				OnConnectionFailedListener,
				LocationListener{
	
	//CLASS VARIABLES
	//class tag
	public static final String tag = "com.waittimes";
	//updates boolean string
	public static final String key_updates = "KEY_UPDATES_ON";
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
	// Define an object that holds accuracy and frequency parameters
 // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL =
            MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    //INSTANCE VARIABLES
    LocationRequest mLocationRequest;
    LocationClient mLocationClient;
    boolean mUpdatesRequested;
	@SuppressWarnings("unused")
	private Editor mEditor;
	private SharedPreferences mPrefs;
	private WebView mWebView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_test);
        
        //WebView setup on main activity
        mWebView = (WebView) findViewById(R.id.mainWebView);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        //Log.d(LocationTest.tag, ""+myWebView.getSettings().getJavaScriptEnabled());
        //Load web page on WebView
        mWebView.loadUrl("http://www.cerberu.com:8001/static/templates/current_location.html");
        
        //Setup location updates, and location client
        this.mLocationRequest = LocationRequest.create();
        this.mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.mLocationRequest.setInterval(LocationTest.UPDATE_INTERVAL);
        this.mLocationRequest.setFastestInterval(LocationTest.FASTEST_INTERVAL);
        this.mLocationClient = new LocationClient(this, this, this);
        //location updates are turned off
        this.mUpdatesRequested = true;
        
        //open shared preferences
        mPrefs = this.getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
    }
    
    protected void onStart()
    {
    	Log.d(LocationTest.tag, "onStart: location client is connected");
    	this.mLocationClient.connect();
    	super.onStart();
    }
    
    protected void onStop()
    {
    	// If the client is connected
        if (mLocationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            this.mLocationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
    	Log.d(LocationTest.tag, "onStop: location client is disconnected");
        this.mLocationClient.disconnect();
        super.onStop();
    }
    
    protected void onResume()
    {
    	 /*
         * Get any previous setting for location updates
         * Gets "false" if an error occurs
         */
        /*if (mPrefs.contains("KEY_UPDATES_ON")) {
            mUpdatesRequested =
                    mPrefs.getBoolean("KEY_UPDATES_ON", false);

        // Otherwise, turn off location updates
        } else {
            mEditor.putBoolean("KEY_UPDATES_ON", false);
            mEditor.commit();
        }*/
        super.onResume();
    }

    private boolean servicesConnected(){
    	int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    	if(ConnectionResult.SUCCESS == resultCode)
    	{
    		Log.d(LocationTest.tag, "google play services are available");
    		return true;
    	}
    	else
    	{
    		Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
    		if(dialog != null)
    		{
    			ErrorDialogFragment errorDialog = new ErrorDialogFragment();
    			errorDialog.setDialog(dialog);
    			errorDialog.show(this.getSupportFragmentManager(), LocationTest.tag);
    			
    		}
    		return false;
    	}
    }
    
    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.location_test, menu);
        return true;
    }

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle bundle) {
		// If already requested, start periodic updates
        if (mUpdatesRequested) {
        	Log.d(LocationTest.tag, "onConnected: resquested location updates");
            mLocationClient.requestLocationUpdates(mLocationRequest, this);
        }
	}

	@Override
	public void onDisconnected() {
		
	}

	@Override
	public void onLocationChanged(Location location) {
		TextView locationInText = (TextView) findViewById(R.id.textView);
		double lat = location.getLatitude();
		double lon = location.getLongitude();
		String stringLocation = "latitude: "+lat+"\n"+
								"longitude: "+lon;
		locationInText.setText(stringLocation);
		
		//call javascript function to update location
		String javascriptCommand = "javascript: updateLocationPoint("+lon+","+lat+");";
    	Log.d(LocationTest.tag, "onLocationChanged: "+javascriptCommand);
		this.mWebView.loadUrl(javascriptCommand);
	}
    
}
