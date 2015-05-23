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
//import java.net.*;
//import java.util.*;
//mport org.json.*;

public class MainService extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
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

	public static boolean active = false;
	private PendingIntent penInt;
	private LocationRequest locReq;
	private int updateTime = 15, cycle = 0;
	private Location lLoc;
	private boolean destroy = false, record = false;
	private GoogleApiClient mClient;
	private CollectedData data = null;
    private static MainService service = null;

	public static void successfullyUploaded(){
		if(MainActivity.active){
		    MainService.service.sendBroadcast(new Intent().setAction("DATA_UPLOADED"));
		}
		service.clearCollectedFile();
		service.data().resetData();
		
	}

	public static void failedToUpload(){
		if(MainActivity.active){
		    MainService.service.sendBroadcast(new Intent().setAction("UPLOAD_ERROR"));
		}
	}
	
	private boolean clearCollectedFile(){
		boolean clear = true;
		File clearLog = new File(this.filePath);
		if(clearLog.exists()){
			try{
				FileWriter buf = new FileWriter(clearLog);
				buf.write("");
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

		return add;
	}
	
	protected synchronized void buildGoogleApiClient() {
		if(this.mClient == null){
			this.mClient = new GoogleApiClient.Builder(this)
			.addApi(LocationServices.API)
			.addApi(ActivityRecognition.API)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		}
		if(!this.mClient.isConnected() || !this.mClient.isConnecting()){
			this.mClient.connect();
		}
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
		this.buildGoogleApiClient();
	}
	
	private CollectedData data(){
		if(this.data == null){
			this.data = new CollectedData(this.getApplicationContext());
		}
		if(this.data.dataIsEmpty()){
			this.data.importFile(this.filePath);
		}
		return this.data;
	}
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		if(intent != null){

			if(intent.getAction().equals("SERVICE_START")){
			    this.active = true;
				this.service = this;
				Log.i("SERVICE START", "GOT SERVICE_START ON START");
			}
			/*if(intent.getAction().equals("SERVICE_STOP")){
				this.destroy = true;
				this.onDestroy();
			}*/
			super.onStart(intent, startId);
		}
	}
	@Override
	public void onConnected(Bundle p1){
		Intent i = new Intent(this, MainService.class);
		i.setAction("ACTIVITY_DATA");
		this.penInt = PendingIntent.getService(this, 7422, i, PendingIntent.FLAG_UPDATE_CURRENT);
		this.locReq = LocationRequest.create();
		this.locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		this.locReq.setFastestInterval(3000);
		this.locReq.setInterval(10000);
		this.requestRecognitionUpdates(this.updateTime);
	}
	
	private void requestRecognitionUpdates(int sec){
		if(this.mClient.isConnected()){
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(this.mClient, sec*1000, this.penInt);
		}
	}
	
	private void requestLocationUpdates(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mClient, this.locReq, this);
		}
	}

	private void removeRecognitionUpdates(){
		if(this.mClient.isConnected()){
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(this.mClient, this.penInt);
		}
	}
	
	private void removeLocationUpdates(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient, this.penInt);
		}
	}
	
	@Override
	public void onDestroy()
	{
		if(this.destroy){
			this.removeRecognitionUpdates();
			this.removeLocationUpdates();
			this.penInt.cancel();
			this.mClient.disconnect();
			this.data().finalization();
			this.active = false;
			this.service = null;
			super.onDestroy();
		}
	}
    private long longDiff(long l1, long l2){
        return (l1-l2);
    }
	//private Intent send;
	public final static long sessionTimeout = 600000;//ms = 10 min
	@Override
	public void onLocationChanged(Location p1)
	{
		
		if(record){
			int currSID = this.data().getLastSID();
			long prvTime = this.data().getLastTime();
			if(this.longDiff(prvTime, 0) > 0 && this.longDiff(this.longDiff(p1.getTime(), prvTime), this.sessionTimeout) > 0){
				currSID++;
			}
			
			float speed = 0;
			if(this.lLoc != null && this.longDiff(prvTime, 0) > 0 && currSID == this.data().getLastSID()){ speed = 1000*p1.distanceTo(lLoc)/(p1.getTime()-prvTime); }
							 else{ speed = p1.getSpeed(); }
			
			if(!addUpdate(
			  "SID "+currSID
			+"|LATITUDE "+p1.getLatitude()
			+"|LONGITUDE "+p1.getLongitude()
			+"|TIME "+p1.getTime()
			+"|SPEED "+speed
			+"|ACCURACY "+p1.getAccuracy())){
				this.sendBroadcast(new Intent("FILE_PERMISSION").setAction("FILE_ERROR"));
			}
			else{
				this.data().add(currSID, p1.getLatitude(), p1.getLongitude(), p1.getTime(), speed, p1.getAccuracy());
			}
			
			lLoc = p1;
		}
	}
	private String getActivityName(int type){
        String name;
        switch(type){
            case(DetectedActivity.IN_VEHICLE):
                name = "IN VEHICLE";
                break;
            case(DetectedActivity.ON_BICYCLE):
                name = "ON BICYCLE";
                break;
            case(DetectedActivity.ON_FOOT):
                name = "ON FOOT";
                break;
            case(DetectedActivity.STILL):
                name = "STILL";
                break;
            case(DetectedActivity.UNKNOWN):
                name = "UNKNOWN";
                break;
            case(DetectedActivity.TILTING):
                name = "TILTING";
                break;
            default:
                name = "NO ACTIVITY";
                break;
        }
        return name;
    }
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if (ActivityRecognitionResult.hasResult(intent)){
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
			int confidence = probableActivity.getConfidence();
			int type = probableActivity.getType();

			
			if(MainActivity.active){
				this.sendBroadcast(new Intent("SERVICE_UPDATE")
							  .setAction("SERVICE_DATA")
							  .putExtra("ACTIVITY_NAME", this.getActivityName(type))
							  .putExtra("ACTIVITY_CONFIDENCE", confidence));
			}
			
			if(type == DetectedActivity.ON_BICYCLE && !this.record){
				this.record = true;
				this.requestLocationUpdates();
				if(this.updateTime > 15){
					this.removeRecognitionUpdates();
					this.updateTime = 15;
					this.cycle = 0;
					this.requestRecognitionUpdates(this.updateTime);
				}
			}
			else if(this.record && type != DetectedActivity.ON_BICYCLE){
				this.record = false;
				this.removeLocationUpdates();
			}
			
			if(!this.record){
				if(this.cycle > 5){
					if(this.updateTime<60){
						this.removeRecognitionUpdates();
						this.updateTime+=5; this.cycle=0;
						this.requestRecognitionUpdates(this.updateTime);
					}
					if(!this.data().uploadData() && !this.data().dataIsEmpty()){
						this.data().resetData();
					}
				}
				else if(this.cycle<=5){
					this.cycle++;
				}
			}
		}
        if(intent != null){
            /*if(intent.getAction().equals("SERVICE_START")){
                this.active = true;
                Log.i("SERVICE START", "GOT SERVICE_START ON START");
            }
            else*/ if(intent.getAction().equals("SERVICE_STOP")){
                this.destroy = true;
                this.onDestroy();
            }
        }

		return super.onStartCommand(intent, flags, startId);
	}

	public class MainBinder extends Binder {
		MainService getService(){
			return MainService.this;
		}
	}
	@Override
	public IBinder onBind(Intent p1)
	{
		return this.binder;
	}
	private final IBinder binder = new MainBinder();

	/*public boolean testRun(){
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
		//TO-DO: WRITE TESTS FOR FUNCTIONS
		return run;
	}*/
	
}
