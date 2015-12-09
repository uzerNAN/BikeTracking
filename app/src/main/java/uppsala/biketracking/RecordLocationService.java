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
import com.google.android.gms.drive.internal.*;
//import java.net.*;
//import java.util.*;
//mport org.json.*;

public class RecordLocationService extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{

	//@Override
	
	//{
		// TODO: Implement this method
	//}
	public static final String settingsPath = "sdcard/recording_settings.txt";
	public static final String dataPath = "sdcard/collected_data.txt";
	
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
	//private PendingIntent penInt;
	private LocationRequest locReq;
	//private int updateTime = 15, cycle = 0;
	private Location lLoc;
	private GoogleApiClient mClient;
    private static RecordLocationService service = null;
	private int currSID;
	private long prvTime;
	

	protected synchronized void connectGoogleApiClient() {
		if(this.mClient == null){
			this.mClient = new GoogleApiClient.Builder(this)
			.addApi(LocationServices.API)
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
		this.connectGoogleApiClient();
	}
	
	public static boolean writeFile(String path, String line, boolean append){
		boolean write = false;
		File updateLog = new File(path);

		try{
			if(!updateLog.exists()){
				updateLog.createNewFile();
			}
			BufferedWriter buf = new BufferedWriter(new FileWriter(updateLog, append));
			if(append){
				buf.append(line);
			}
			else{
				buf.write(line);
			}
			buf.close();
			write = true;
		}
		catch(IOException e){
			e.printStackTrace();
		}

		return write;
	}
	
	private boolean loadSettings(){
		boolean load = false;
		File updateLog = new File(settingsPath);
		try{
			if(updateLog.exists()){
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitSID, splitTime;
				line = buf.readLine();
					if(line != null && line.matches("[A-Z0-9| ]*")){
						splitLine = line.split("\\|");
						splitSID = splitLine[0].split(" ");
						splitTime = splitLine[1].split(" ");
						if(splitSID[0].equals("SID")
						   && splitTime[0].equals("TIME")){
							this.currSID = Integer.parseInt(splitSID[1]);
							this.prvTime = Long.parseLong(splitTime[1]);
							load = true;
						}
						else{
							Log.i("ImportSettings","FILE LINE HAS DIFFERENT CONTENT");
						}
					}
					else{
						Log.i("ImportSettings","UNKNOWN LINE: "+line);
						
						
					}
				
				buf.close();
				
			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		Log.i("ImportSettings", "load="+load);
		return load;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(intent != null){
			if(intent.getAction().equals("START_RECORDING")){
				this.active = true;
				this.service = this;
				if(!loadSettings()){
					this.currSID = 0;
					this.prvTime = 0;
				}
				Log.i("RECORD LOCATION", "GOT START_RECORDING ON START");
			}
			else if(intent.getAction().equals( "STOP_RECORDING" )){
				this.active = false;
			}
		}
		return onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onConnected(Bundle p1){
		
		Location last = LocationServices.FusedLocationApi.getLastLocation(this.mClient);
		if(last!=null){
			onLocationChanged(last);
		}
		else{
			Log.i("FIRST LOCATION","LOCATION ACTIVITY ERROR");
		}
		
		createLocationRequest();
		startLocationUpdates();
	}
	
	
	protected void createLocationRequest() {
		locReq = new LocationRequest();
		locReq.setInterval(120000);
		locReq.setFastestInterval(10000);
		locReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	}
	
	private void startLocationUpdates(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mClient, this.locReq, this);
		}
	}
	
	private void stopLocationUpdates(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient, this);
		}
	}
	
	@Override
	public void onDestroy()
	{
		if(!this.active){
			this.writeFile(this.settingsPath, "SID "+this.currSID+"|TIME "+this.prvTime, false);
			this.stopLocationUpdates();
			this.mClient.disconnect();
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
		
		
			//int currSID = this.data().getLastSID();
			//long prvTime = this.data().getLastTime();
		if(this.lLoc == null
		|| (this.lLoc.getLatitude() != p1.getLatitude()
		&& this.lLoc.getLongitude() != p1.getLongitude())){
			if(this.longDiff(prvTime, 0) > 0 && this.longDiff(this.longDiff(p1.getTime(), prvTime), this.sessionTimeout) > 0){
				currSID++;
			}
			
			float speed = 0;
			if(this.lLoc != null && this.longDiff(prvTime, 0) > 0){ speed = 1000*p1.distanceTo(lLoc)/(p1.getTime()-prvTime); }
			else{ speed = p1.getSpeed(); }
			this.prvTime = p1.getTime();
			
			if(!writeFile(this.dataPath,
			  "SID "+currSID
			+"|LATITUDE "+p1.getLatitude()
			+"|LONGITUDE "+p1.getLongitude()
			+"|TIME "+p1.getTime()
			+"|SPEED "+speed
			+"|ACCURACY "+p1.getAccuracy()
			+"\r\n", true)){
				this.sendBroadcast(new Intent("FILE_PERMISSION").setAction("FILE_ERROR"));
			}
		}
		lLoc = p1;
	}

	public class MainBinder extends Binder {
		RecordLocationService getService(){
			return RecordLocationService.this;
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
