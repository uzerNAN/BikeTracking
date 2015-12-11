package uppsala.biketracking;


import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ApiService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	
	private GoogleApiClient mClient = null;
	private LocationRequest record_req = null;
	private LocationListener location = null;
	private LocationRequest location_req = null;
	//private static ApiService service = null;
	public static boolean ar_is_active = false;
	public static boolean rl_is_active = false;
	public static boolean lu_is_active = false;
	public static boolean active = false;
	public static boolean do_upload = false;
	public static int rl_sid = 0;
	public static double rl_latitude = 0;
	public static double rl_longitude = 0;
	public static long rl_time = 0;

	@Override
	public void onCreate(){
		super.onCreate();
		loadSettings(Constants.SETTINGS_PATH);
		connectGoogleApiClient();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i("ApiService", "BATTERY_HIGH " + PowerSavingModule.battery_high + " | BATTERY_LOW " + PowerSavingModule.battery_low);
		if(!PowerSavingModule.battery_low || MainActivity.active) {
			if (intent != null) {
				String action = intent.getAction();
				switch (action) {
					case "LAST_DATA":
						Bundle extras = intent.getExtras();
						rl_sid = extras.getInt("SID");
						rl_latitude = extras.getDouble("LATITUDE");
						rl_longitude = extras.getDouble("LONGITUDE");
						rl_time = extras.getLong("TIME");
						break;
					case "START_AR":
						if (!ar_is_active) {
							if (this.mClient.isConnected()) {
								activateActivityRecognition();
							}
							ar_is_active = true;
						}
						break;
					case "START_RL":
						if (!rl_is_active) {
							if (this.mClient.isConnected()) {
								activateRecordLocation();
							}
							rl_is_active = true;
							updateActivityRecognition();
						}
						break;
					case "START_LU":
						if (!lu_is_active) {
							if (this.mClient.isConnected()) {
								activateLocationUpdates();
							}
							lu_is_active = true;
							updateActivityRecognition();
						}
						break;
					case "STOP_AR":
						if (ar_is_active) {
							stopActivityRecognition();
							ar_is_active = false;
						}
						break;
					case "STOP_RL":
						if (rl_is_active) {
							stopRecordLocation();
							rl_is_active = false;
							updateActivityRecognition();
							do_upload = ((new File(Constants.DATA_PATH)).length() != 0);
						}
						break;
					case "STOP_LU":
						if (lu_is_active) {
							stopLocationUpdates();
							lu_is_active = false;
							updateActivityRecognition();
						}
						break;
					case "DATA_RESULT":
						data_uploaded(intent.getBooleanExtra("SUCCESS", false));
						break;
					default:
						break;
				}
				check_if_needed(startId);
			}
		}
		else {
			stopSelf(startId);
		}
		return START_STICKY_COMPATIBILITY;
	}

	private void check_if_needed(int startId){
		if(!ar_is_active && !lu_is_active && !rl_is_active){
			stopSelf(startId);
		}
	}

	private void updateActivityRecognition(){
		if (!lu_is_active && !rl_is_active) {
			stopActivityRecognition();
			startActivityRecognition(Constants.AR_TIME);
		}
		else if (!(lu_is_active && rl_is_active)){
			stopActivityRecognition();
			startActivityRecognition(Constants.AR_FASTEST_TIME);
		}
	}

	private void activateActivityRecognition(){
		if (!lu_is_active && !rl_is_active) {
			startActivityRecognition(Constants.AR_TIME);
		}
		else if (!(lu_is_active && rl_is_active)){
			startActivityRecognition(Constants.AR_FASTEST_TIME);
		}
	}

	private void activateLocationUpdates(){
		createLocationRequest(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		startLocationUpdates();
	}

	private void activateRecordLocation(){
		int priority;
		if (PowerSavingModule.battery_high) {
			priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
		}
		else {
			priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
		}
		createRecordRequest(priority);
		startRecordLocation();
	}

	public static boolean isOnline(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNI = cm.getActiveNetworkInfo();
		return activeNI != null && activeNI.isConnected();
	}

	private void reconnectClient(){
		if(!this.mClient.isConnected() && !this.mClient.isConnecting()){
			this.mClient.connect();
		}
	}

	@Override
	public void onConnectionSuspended(int p1)
	{
		reconnectClient();
		// TODO: Implement this method
	}

	@Override
	public void onConnectionFailed(ConnectionResult p1)
	{
		reconnectClient();
		// TODO: Implement this method
	}

	protected void createLocationRequest(int priority) {
		location_req = new LocationRequest();
		location_req.setInterval(Constants.RL_TIME);
		location_req.setFastestInterval(Constants.RL_FASTEST_TIME);
		location_req.setPriority(priority);
		if(location == null){
			location = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {
					if(location != null){
						if(MainActivity.active) {
							sendBroadcast(
									new Intent()
											.setAction("LOCATION_DATA")
											.putExtra("LATITUDE", location.getLatitude())
											.putExtra("LONGITUDE", location.getLongitude()));
						}
						else {
							stopLocationUpdates();
						}
					}
				}
			};
		}
	}
	protected void createRecordRequest(int priority) {
		record_req = new LocationRequest();
		record_req.setInterval(Constants.LU_TIME);
		record_req.setFastestInterval(Constants.LU_FASTEST_TIME);
		record_req.setPriority(priority);
	}

	public void onConnected(Bundle p1){
		if(lu_is_active){
			this.activateLocationUpdates();
		}
		if(rl_is_active){
			this.activateRecordLocation();
		}
		if(ar_is_active) {
			this.activateActivityRecognition();
		}
	}

	protected synchronized void connectGoogleApiClient() {
		if(this.mClient == null){
			this.mClient = new GoogleApiClient.Builder(this)
				.addApi(ActivityRecognition.API)
				.addApi(LocationServices.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		}
		reconnectClient();
	}

	private void startActivityRecognition(int time){
		ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(this.mClient, time, PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionSensor.class), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void stopActivityRecognition(){
		if(this.mClient.isConnected()){
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(this.mClient, PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionSensor.class), PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}

	private void startLocationUpdates(){
		LocationServices.FusedLocationApi.requestLocationUpdates(this.mClient, this.location_req, location);// this.location);
	}

	private void stopLocationUpdates(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient, this.location);
		}
		this.location = null;
	}

	private void startRecordLocation(){
		LocationServices.FusedLocationApi.requestLocationUpdates(this.mClient, this.record_req, PendingIntent.getService(this, 0, new Intent(this, RecordLocationSensor.class), PendingIntent.FLAG_UPDATE_CURRENT));
	}

	private void stopRecordLocation(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient, PendingIntent.getService(this, 0, new Intent(this, RecordLocationSensor.class), PendingIntent.FLAG_UPDATE_CURRENT));
		}
	}
	
	private void data_uploaded(boolean success){
		if(success) {
			do_upload = false;
			if (MainActivity.active) {
				sendBroadcast(new Intent().setAction("DATA_UPLOADED"));
			}
			rl_sid = 0;
			rl_latitude = 0;
			rl_longitude = 0;
			rl_time = 0;
			Constants.writeFile(Constants.DATA_PATH, "", false);
			Constants.writeFile(Constants.SETTINGS_PATH, "", false);
		}
		else{
			if(MainActivity.active){
				sendBroadcast(new Intent().setAction("UPLOAD_ERROR"));
			}
		}
	}

	private boolean loadSettings(String path){
		boolean load = false;
		File updateLog = new File(path);
		try{
			if(updateLog.exists()){
				BufferedReader buf = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitSID, splitLat, splitLon, splitTime;
				line = buf.readLine();
				if(line != null && line.matches("[A-Z0-9.| ]*")){
					splitLine = line.split("\\|");
					splitSID = splitLine[0].split(" ");
					splitLat = splitLine[1].split(" ");
					splitLon = splitLine[2].split(" ");
					splitTime = splitLine[3].split(" ");
					if(splitSID[0].equals("SID")
							&& splitLat[0].equals("LATITUDE")
							&& splitLon[0].equals("LONGITUDE")
							&& splitTime[0].equals("TIME")){
						rl_sid = Integer.parseInt(splitSID[1]);
						rl_latitude = Double.parseDouble(splitLat[1]);
						rl_longitude = Double.parseDouble(splitLon[1]);
						rl_time = Long.parseLong(splitTime[1]);
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
	public void onDestroy(){
		Constants.writeFile(Constants.SETTINGS_PATH, "SID "+rl_sid+"|LATITUDE "+rl_latitude+"|LONGITUDE "+rl_longitude+"|TIME "+rl_time, false);
		if(mClient.isConnected()) {
			if (ar_is_active) {
				stopActivityRecognition();
			}
			if(lu_is_active){
				stopLocationUpdates();
			}
			if(rl_is_active){
				stopRecordLocation();
			}
			mClient.disconnect();
		}
		super.onDestroy();
	}

	public class MainBinder extends Binder {
		ApiService getService(){
			return ApiService.this;
		}
	}

	@Override
	public IBinder onBind(Intent p1)
	{
		return this.binder;
	}
	private final IBinder binder = new MainBinder();
}
