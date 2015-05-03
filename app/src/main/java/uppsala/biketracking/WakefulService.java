package uppsala.biketracking;

import android.app.*;
import android.content.*;
import android.location.*;
import android.net.*;
import android.os.*;
import android.util.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import java.io.*;
import java.util.*;

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
	//private Intent startInt;
	//private Context context;
	//private ActivityRecognitionApi recApi;
	//private FusedLocationProviderApi locPro;
	private LocationRequest locReq;
	private int aType = -1, aConfidence = 100, time = 15, count = 0, prvSID = -1;
	//private double prvLatitude, prvLongitude;
	private long prvTime = 0;
	private String aName = "NO ACTIVITY";
	private Location lLoc;
	private boolean destroy = false, record = false, test = false;
	private GoogleApiClient mClient;
	private CollectedData data = null;
	private ResultCallback<Status> status;
	//public final static String no_need_for_upload = "UPLOADED";
	//private int test = 10;

	public void successfullyUploaded(){
		if(Tracking.mainActivity != null){
			this.sendBroadcast(new Intent().setAction("DATA_UPLOADED"));
		}
		//this.addUpdate(this.no_need_for_upload);
		this.clearCollectedFile();
		this.data().resetData();
		this.prvSID = this.data().getLastSID(); this.prvTime = this.data().getLastTime();
		
	}
	public void failedToUpload(){
		if(Tracking.mainActivity != null){
			this.sendBroadcast(new Intent().setAction("UPLOAD_ERROR"));
		}
		//this.data().resetData();
	}
	
	private boolean clearCollectedFile(){
		boolean clear = true;
		File clearLog = new File(this.filePath);
		if(!clearLog.exists()){
			try{
				clearLog.createNewFile();
			}
			catch(IOException e){
				//add = false;
				e.printStackTrace();
			}
		}
		else{
			try{
				FileWriter buf = new FileWriter(clearLog);
				buf.write("");
				//buf.newLine();
				buf.close();
			}
			catch(IOException e){
				clear = false;
				e.printStackTrace();
			}
		}
		return clear;
	}
	public static boolean isOnline(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNI = cm.getActiveNetworkInfo();
		return activeNI != null && activeNI.isConnected();
	}
	private boolean addUpdate(String text){
		boolean add = true;
		File updateLog = new File(this.filePath);
		
		//	try{
				
		//	}
		//	catch(IOException e){
		//		add = false;
		//		e.printStackTrace();
		//	}
		//}
		//if(add){
		try{
			if(!updateLog.exists()){
					updateLog.createNewFile();
			}
			BufferedWriter buf = new BufferedWriter(new FileWriter(updateLog, true));
			buf.append(text);
			buf.newLine();
			buf.close();
		}
		catch(IOException e){
			add = false;
			e.printStackTrace();
		}
		//}
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
		this.buildGoogleApiClient();
	}
	
	private CollectedData data(){
		if(this.data == null){
			this.data = new CollectedData(this.getApplicationContext());
		}
		else if(this.data.dataIsEmpty()){
			this.data.importFile(this.filePath);
		}
		return this.data;
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		if(intent != null){
			this.mainService = this;
			// TODO: Implement this method
			if(intent.getAction().equals("SERVICE_START")){
				//this.startInt = intent;
				//this.data = new CollectedData(this.getApplicationContext());
				if(this.data == null){
					this.prvSID = this.data().getLastSID();
					this.prvTime = this.data().getLastTime();
				}
				//Log.i("SERVICE START", "GOT SERVICE_START ON START");
				//sendBroadcast(new Intent("SERVICE_BROADCAST").setAction("SERVICE_DATA"));
			}
			if(intent.getAction().equals("SERVICE_STOP")){
				this.destroy = true;
				this.onDestroy();
			}
			super.onStart(intent, startId);
			if(!this.mClient.isConnected()){
				this.mClient.connect();
			}
		}
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
		this.status = new ResultCallback<Status>() {
			@Override
			public void onResult(Status status) {
				if (status.isSuccess()) {
					Log.i("SERVICE UPDATE REQUEST", "Successfully registered/removed updates");
				} else {
					Log.i("SERVICE UPDATE REQUEST", "Failed to register/remove updates");
				}
			}
		};
		i.setAction("ACTIVITY_DATA");
		this.penInt = PendingIntent.getService(this, 7422, i, PendingIntent.FLAG_UPDATE_CURRENT);
		this.locReq = LocationRequest.create();
		this.locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		this.locReq.setFastestInterval(3000);
		this.locReq.setInterval(10000);
		this.requestRecognitionUpdates(time);
		//mainService = this;
	}
	
	private void requestRecognitionUpdates(int sec){
		if(this.mClient.isConnected() && !this.test){
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(this.mClient, sec*1000, this.penInt)
				.setResultCallback(this.status);
		}
	}
	
	private void requestLocationUpdates(){
		if(this.mClient.isConnected() && !this.test){
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mClient, this.locReq, this)
				.setResultCallback(this.status);
		}
	}

	private void removeRecognitionUpdates(){
		if(this.mClient.isConnected() && !this.test){
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(this.mClient, this.penInt)
				.setResultCallback(this.status);
		}
	}
	
	private void removeLocationUpdates(){
		if(this.mClient.isConnected() && !this.test){
			LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient, this.penInt)
				.setResultCallback(this.status);
		}
	}
	
	@Override
	public void onDestroy()
	{
		//Log.i("SERVICE UPDATE", "ON DESTROY");
		// TODO: Implement this method
		if(this.destroy){
			this.removeRecognitionUpdates();
			this.removeLocationUpdates();
			this.penInt.cancel();
			this.mClient.disconnect();
			this.data().finalization();
			//WakefulReceiver.completeWakefulIntent(startInt);
			this.mainService = null;
			super.onDestroy();
		}
	}

	//private Intent send;
	public final static long sessionTimeout = 600000;//ms = 10 min
	@Override
	public void onLocationChanged(Location p1)
	{
		lLoc = p1;
		if(record){
			//String update = "";
			
			if(Long.compare(this.prvTime, 0) > 0 && Long.compare((p1.getTime()-this.prvTime), this.sessionTimeout) > 0){
				this.prvSID++;
			}
			//else if(this.prvTime == 0){
				
			//}
			if(!addUpdate("SID "+this.prvSID
			+"|LATITUDE "+p1.getLatitude()
			+"|LONGITUDE "+p1.getLongitude()
			+"|TIME "+p1.getTime()
			+"|ACCURACY "+p1.getAccuracy())){
				this.sendBroadcast(new Intent("FILE_PERMISSION").setAction("FILE_ERROR"));
			}
			else{
				this.data().add(this.prvSID, p1.getLatitude(), p1.getLongitude(), p1.getTime(), p1.getAccuracy());
				this.prvTime = p1.getTime();
			}
			
			/*if(Tracking.mainActivity != null){
				sendBroadcast(new Intent()
							  .setAction("ADD_DATA")
							  .putExtra("SID", this.prvSID)
							  .putExtra("LATITUDE", p1.getLatitude())
							  .putExtra("LONGITUDE", p1.getLongitude())
							  .putExtra("TIME", p1.getTime())
							  .putExtra("ACCURACY", p1.getAccuracy()));
			}*/
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
			Bundle b = intent.getExtras();
			Set<String> kset = b.keySet();
			for(String key : kset){
				Log.i("ACTIVITY INTENT", "Key : "+key+" , Object.toString : "+b.get(key).toString());
			}
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
			
			this.aConfidence = probableActivity.getConfidence();

			// Get the type 
			this.aType = probableActivity.getType();

			//this.activityName = "";

			switch(this.aType){
				case(DetectedActivity.IN_VEHICLE):
					this.aName = "IN VEHICLE";
					break;
				case(DetectedActivity.ON_BICYCLE):
					this.aName = "ON BICYCLE";
					//record = true;
					break;
				case(DetectedActivity.ON_FOOT):
					this.aName = "ON FOOT";
					break;
				case(DetectedActivity.STILL):
					this.aName = "STILL";
					break;
				case(DetectedActivity.UNKNOWN):
					this.aName = "UNKNOWN";
					break;
				case(DetectedActivity.TILTING):
					this.aName = "TILTING";
					break;
				default:
					this.aName = "NO ACTIVITY";
					break;
			}
			
			if(Tracking.mainActivity != null){
				this.sendBroadcast(new Intent("SERVICE_UPDATE")
							  .setAction("SERVICE_DATA")
							  .putExtra("ACTIVITY_TYPE", this.aType)
							  .putExtra("ACTIVITY_NAME", this.aName)
							  .putExtra("ACTIVITY_CONFIDENCE", this.aConfidence));
			}
			
			if(this.aType == DetectedActivity.ON_BICYCLE && !this.record){
				this.record = true;
				//removeRecognitionUpdates();
				this.requestLocationUpdates();
				if(this.time > 15){
					this.removeRecognitionUpdates();
					this.time = 15;
					this.count = 0;
					this.requestRecognitionUpdates(time);
				}
			}
			
			if(this.record && this.aType != DetectedActivity.ON_BICYCLE){
				this.record = false;
				this.removeLocationUpdates();
			}
			
			if(this.count > 5 && !this.record){
				if(this.time<60){
					this.removeRecognitionUpdates();
					this.time+=5; this.count=0;
					this.requestRecognitionUpdates(this.time);
				}
				if(!this.data().uploadData() && !this.data().dataIsEmpty()){
					this.data().resetData();
				}
			}
			else if(this.count<=5){
				this.count++;
			}
			//this.data().uploadData();
		}
		
		return super.onStartCommand(intent, flags, startId);
	
	}

	public class WakefulBinder extends Binder {
		WakefulService getService(){
			return WakefulService.this;
		}
	}
	@Override
	public IBinder onBind(Intent p1)
	{
		// TODO: Implement this method
		return this.binder;
	}
	private final IBinder binder = new WakefulBinder();

	public boolean testRun(){
		boolean run = true;
		Intent i = new Intent()
			.putExtra("com.google.android.location.internal.EXTRA_RELEASE_VERSION", 2014)
			.putExtra("com.google.android.location.internal.EXTRA_ACTIVITY_RESULT",new ActivityRecognitionResult(new DetectedActivity(DetectedActivity.ON_FOOT, 100), System.currentTimeMillis(), System.currentTimeMillis()));
		//probableActivities=[DetectedActivity [type=3, confidence=54], DetectedActivity [type=4, confidence=31], DetectedActivity [type=0, confidence=15]], timeMillis=1430593549589, elapsedRealtimeMillis=82875494]
		this.removeLocationUpdates();
		this.removeRecognitionUpdates();
		this.count = 0;
		this.time = 15;
		this.test = true;
		this.data().resetData();
		//TO-DO: WRITE TESTS FOR FUNCTIONS
		return run;
	}
	
}
