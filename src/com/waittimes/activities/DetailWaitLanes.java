package com.waittimes.activities;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.waittimes.LocationTest;
import com.waittimes.R;
import com.waittimes.storage.DatabaseHelper;
import com.waittimes.storage.WaitLane;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;


public class DetailWaitLanes 
	extends OrmLiteBaseActivity<DatabaseHelper> 
	implements LocationListener,
			   ConnectionCallbacks, 
			   OnConnectionFailedListener{
	
	public final static String tag = DetailWaitLanes.class.getName();
	public final static String ACTIVITY_TITLE = "Wait Lane Information";
	private WaitLane waitLane = null;
	private WebView webView;
	private LocationRequest locationRequest;
	private LocationClient locationClient;
	private boolean updatesRequest;
	private boolean finishedLoading;
	private JavascriptWebInterface javascriptInterface;
	
	/**
	 * @param finishedLoading the finishedLoading to set
	 */
	public void setFinishedLoading(boolean finishedLoading) {
		this.finishedLoading = finishedLoading;
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		this.setContentView(R.layout.detail_wait_lane);
		super.onCreate(savedInstanceState);
		WaitLane.setActivity(this);
		
		this.updatesRequest = true;
		this.finishedLoading = false;
		//get wait lane
		String id = this.getIntent().getExtras().getString("id");
		Log.d(DetailWaitLanes.class.getName(), "onCreate() showing activity detail of wait lane "+id);
		this.waitLane = WaitLane.getWaitLaneWithID(id);
		
		//setup action bar
		ActionBar actionBar = this.getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setTitle(this.waitLane.getName());
		
		//setup UI
		webView = (WebView)this.findViewById(R.id.web_view);
		webView.setWebChromeClient(new WebChromeClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.clearCache(true);
        
        WebSettings webSettings = webView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        
        this.javascriptInterface = new JavascriptWebInterface(this);
        webView.addJavascriptInterface(this.javascriptInterface, "Android");
        webView.loadUrl("http://"+this.getString(R.string.domain)+"/WaitLanes/app/geojson");
        
        this.locationRequest = LocationRequest.create();
        this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        this.locationRequest.setInterval(3000);
        this.locationRequest.setFastestInterval(1000);
        this.locationClient = new LocationClient(this, this, this);
        
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(LocationTest.tag, "onStart: connected to location updates");
		this.locationClient.connect();
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		if(this.finishedLoading){
			double lat = location.getLatitude();
			double lon = location.getLongitude();
			//Log.d(DetailWaitLanes.class.getName(), "getting location at "+lon+","+
			//this.javascriptInterface.showMessage("getting location at "+lon+","+lat+".");
			this.webView.loadUrl("javascript: updateLocation("+lon+","+
																lat+");");
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnected(Bundle arg0) {
		if(this.updatesRequest){
			this.locationClient.requestLocationUpdates(this.locationRequest, this);
		}
		
	}

	@Override
	public void onDisconnected() {
		// TODO Auto-generated method stub
		
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// If the client is connected
        if (locationClient.isConnected()) {
            /*
             * Remove location updates for a listener.
             * The current Activity is the listener, so
             * the argument is "this".
             */
            this.locationClient.removeLocationUpdates(this);
        }
        /*
         * After disconnect() is called, the client is
         * considered "dead".
         */
    	Log.d(LocationTest.tag, "onStop: location client is disconnected");
        this.locationClient.disconnect();
		super.onStop();
	}

	private class JavascriptWebInterface{
		
		private DetailWaitLanes activity = null;
		
		JavascriptWebInterface(DetailWaitLanes activity){
			this.activity = activity;
		}
		
		@JavascriptInterface
		public String getDomains(){
			return this.activity.getString(R.string.domain);
		}
		
		@JavascriptInterface
		public void finishedLoadingLayers(String id){
			//start anything that is supposed to run when
			//the page is loaded.
			Log.d(DetailWaitLanes.class.getName(), "JavascriptWebInterface.finishedLoadingLayers() "+id);
			if(id.equals("map")){	
				this.activity.webView.post(
						new RunnableLoadURL(this.activity.webView, "javascript: loadWaitLane("+activity.waitLane.getId()+");")
				);
			}
			this.activity.setFinishedLoading(true);
			//begin tracking gps location
		}
		
		@JavascriptInterface
		public void appendMessage(String message){

				TextView textView = (TextView) this.activity.findViewById(R.id.web_view_debug_text_view);
				textView.post(
						new RunnableChangeText(textView, message, false)
				);
		}
		
		@JavascriptInterface
		public void showMessage(String message){
				
				TextView textView = (TextView) this.activity.findViewById(R.id.web_view_debug_text_view);
				textView.post(
						new RunnableChangeText(textView, message, true)
				);
		}
		
	}
	private class RunnableLoadURL implements Runnable{
		
		private WebView webView;
		private String url;

		RunnableLoadURL(WebView webView, String url){
			this.webView = webView;
			this.url = url;
		}

		@Override
		public void run() {
			this.webView.loadUrl(this.url);
		}
	}
	
	private class RunnableChangeText implements Runnable{
		private TextView view;
		private String message;
		private boolean setText;
		RunnableChangeText(TextView view, String message, boolean setText){
			this.view = view;
			this.message = message;
			this.setText = setText;
		}
		@Override
		public void run() {
			if(this.setText){
				this.view.setText(this.message);
			}else{
				this.view.append(this.message);
			}
		}
	
	}
}
