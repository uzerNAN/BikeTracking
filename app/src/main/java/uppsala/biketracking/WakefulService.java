package uppsala.biketracking;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.util.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import java.io.*;

public class WakefulService extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

	//@Override
	
	//{
		// TODO: Implement this method
	//}
	public static final String filePath = "sdcard/collected_data.txt";
	@Override
	public void onConnectionSuspended(int p1)
	{
		// TODO: Implement this method
	}

	@Override
	public void onConnectionFailed(ConnectionResult p1)
	{
		// TODO: Implement this method
	}

	public static WakefulService mainService = null;
	private PendingIntent penInt;
	private Intent startInt;
	//private Context context;
	//private ActivityRecognitionApi recApi;
	//private FusedLocationProviderApi locPro;
	private LocationRequest locReq;
	private int aType = -1, aConfidence = 100, time = 15, count = 0, prvSID = -1;
	//private double prvLatitude, prvLongitude;
	private long prvTime = 0;
	private String aName = "NO ACTIVITY";
	private Location lLoc;
	private boolean destroy = false, record = false;
	private GoogleApiClient mClient;
	//private int test = 10;

	public void successfullyUploaded(){
		sendBroadcast(new Intent().setAction("DATA_UPLOADED"));
		this.addUpdate("UPLOADED");
	}
	public void failedToUpload(){
		sendBroadcast(new Intent().setAction("UPLOAD_ERROR"));
	}
	
	
	private boolean addUpdate(String text){
		boolean add = true;
		File updateLog = new File(filePath);
		if(!updateLog.exists()){
			try{
				updateLog.createNewFile();
			}
			catch(IOException e){
				add = false;
				e.printStackTrace();
			}
		}
		if(add){
		try{
			BufferedWriter buf = new BufferedWriter(new FileWriter(updateLog, true));
			buf.append(text);
			buf.newLine();
			buf.close();
		}
		catch(IOException e){
			add = false;
			e.printStackTrace();
		}
		}
		return add;
	}
	
	protected synchronized void buildGoogleApiClient() {
		mClient = new GoogleApiClient.Builder(this)
			.addApi(LocationServices.API)
			.addApi(ActivityRecognition.API)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
		//intent = new Intent(this.getApplicationContext(), WakefulReceiver.class);
		buildGoogleApiClient();
		
	}
	
	
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		mainService = this;
		// TODO: Implement this method
		if(intent.getAction().equals("SERVICE_START")){
			startInt = intent;
			//Log.i("SERVICE START", "GOT SERVICE_START ON START");
			//sendBroadcast(new Intent("SERVICE_BROADCAST").setAction("SERVICE_DATA"));
		}
		if(intent.getAction().equals("SERVICE_STOP")){
			destroy = true;
			onDestroy();
		}
		super.onStart(intent, startId);
		mClient.connect();
		
	}
	
	/*@Override
	protected void onStop(){
		Log.i("SERVICE UPDATE", "ON STOP");
		// TODO: Implement this method
		if(destroy){
			removeUpdates();
			penInt.cancel();
			mClient.disconnect();
			//WakefulReceiver.completeWakefulIntent(startInt);
			mainService = null;
			super.onStop();
		}
	}*/
	
	@Override
	public void onConnected(Bundle p1){
		Intent i = new Intent(this, WakefulService.class);
		i.setAction("ACTIVITY_DATA");
		penInt = PendingIntent.getService(this, 7422, i, PendingIntent.FLAG_UPDATE_CURRENT);
		locReq = LocationRequest.create();
		locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locReq.setFastestInterval(3000);
		locReq.setInterval(10000);
		requestRecognitionUpdates(time);
		//mainService = this;
	}
	
	private void requestRecognitionUpdates(int sec){
		if(mClient.isConnected()){

			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient, sec*1000, penInt)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							Log.i("SERVICE UPDATE REQUEST", "Successfully registered activity");
						} else {
							Log.i("SERVICE UPDATE REQUEST", "Failed to register activity");
						}
					}
				});
		}
	}
	
	private void requestLocationUpdates(){
		if(mClient.isConnected()){
			LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locReq, this)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							Log.i("SERVICE UPDATE REQUEST", "Successfully registered location");
						} else {
							Log.i("SERVICE UPDATE REQUEST", "Failed to register updates");
						}
					}
				});
		}
	}

	private void removeRecognitionUpdates(){
		if(mClient.isConnected()){
			
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mClient, penInt)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							Log.i("SERVICE UPDATE REMOVE", "Successfully removed activity updates");
						} else {
							Log.i("SERVICE UPDATE REMOVE", "Failed to remove activity updates");
						}
					}
				});
			
		}
	}
	
	private void removeLocationUpdates(){
		if(mClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(mClient, penInt)
				.setResultCallback(new ResultCallback<Status>() {
					@Override
					public void onResult(Status status) {
						if (status.isSuccess()) {
							Log.i("SERVICE UPDATE REMOVE", "Successfully removed location updates");
						} else {
							Log.i("SERVICE UPDATE REMOVE", "Failed to remove location updates");
						}
					}
				});
		}
	}
	
	@Override
	public void onDestroy()
	{
		//Log.i("SERVICE UPDATE", "ON DESTROY");
		// TODO: Implement this method
		if(destroy){
			removeRecognitionUpdates();
			removeLocationUpdates();
			penInt.cancel();
			mClient.disconnect();
			//WakefulReceiver.completeWakefulIntent(startInt);
			mainService = null;
			super.onDestroy();
		}
	}

	//private Intent send;
	private final long sessionTimeout = 600000;//ms = 10 min
	@Override
	public void onLocationChanged(Location p1)
	{
		lLoc = p1;
		if(record){
			//String update = "";
			if(this.prvTime!=0 && (p1.getTime()-this.prvTime)>this.sessionTimeout){
				this.prvSID++;
			}
			if(!addUpdate("SID "+this.prvSID
			+"|LATITUDE "+p1.getLatitude()
			+"|LONGITUDE "+p1.getLongitude()
			+"|TIME "+p1.getTime())){
			 sendBroadcast(new Intent("FILE_PERMISSION").setAction("FILE_ERROR"));
		 }
		}
		
		// TODO: Implement this method
		/*if(test > 0){
			test--;
		}
		else{
			destroy = true;
			onDestroy();
		}*/
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		// TODO: Implement this method
		
		//Log.i("SERVICE UPDATE", "ON HANDLE INTENT");
		//record = false;
		if (ActivityRecognitionResult.hasResult(intent)){
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
			// if( !(probableActivity.getType() == this.activityType  && 
			//   probableActivity.getConfidence() == this.confidence )
			//	) {
			//this.sleepTime = intent.getIntExtra("time", 0);
			// Get the update
			//ActivityRecognitionResult result = 
			//	ActivityRecognitionResult.extractResult(intent);

			//DetectedActivity mostProbableActivity 
			//	= result.getMostProbableActivity();pp

			// Get the confidence % (probability)
			aConfidence = probableActivity.getConfidence();

			// Get the type 
			aType = probableActivity.getType();

			//this.activityName = "";

			switch(aType){
				case(DetectedActivity.IN_VEHICLE):
					aName = "IN VEHICLE";
					break;
				case(DetectedActivity.ON_BICYCLE):
					aName = "ON BICYCLE";
					//record = true;
					break;
				case(DetectedActivity.ON_FOOT):
					aName = "ON FOOT";
					break;
				case(DetectedActivity.STILL):
					aName = "STILL";
					break;
				case(DetectedActivity.UNKNOWN):
					aName = "UNKNOWN";
					break;
				case(DetectedActivity.TILTING):
					aName = "TILTING";
					break;
				default:
					break;
			}
			
			if(Tracking.mainActivity != null){
				sendBroadcast(new Intent("SERVICE_UPDATE")
							  .setAction("SERVICE_DATA")
							  .putExtra("ACTIVITY_TYPE", aType)
							  .putExtra("ACTIVITY_NAME", aName)
							  .putExtra("ACTIVITY_CONFIDENCE", aConfidence));
			}
			
			if(aType == DetectedActivity.ON_BICYCLE && !record){
				record = true;
				//removeRecognitionUpdates();
				requestLocationUpdates();
				if(time > 15){
					removeRecognitionUpdates();
					time = 15;
					count = 0;
					requestRecognitionUpdates(time);
				}
			}
			
			if(record && aType != DetectedActivity.ON_BICYCLE){
				record = false;
				removeLocationUpdates();
			}
			
			if(count > 5 && !record && aConfidence == 100){
				if(time<30){
					removeRecognitionUpdates();
					time+=5; count--;
					requestRecognitionUpdates(time);
				}
			}
			else if(count<=5){
				count++;
			}
		}
		
		return super.onStartCommand(intent, flags, startId);
	
	}


	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return null;
	}

	
}
