package uppsala.biketracking;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.util.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.common.*;
import com.google.android.gms.location.*;
import java.io.*;

public class ActivityRecognitionService extends IntentService
{
	//private long last_time = 0;
	private CollectedData data = null;
	
	
	//private int currSID = 0;
	//private long prvTime = 0;
	
	//private LocationRequest locReq = null;
	
	private void startLocationRecording(){
		if(!RecordLocationService.active){
			//service.
			this.startService(new Intent(this, RecordLocationService.class).setAction("START_RECORDING"));
		}
	}

	private void stopLocationRecording(){
		if(RecordLocationService.active){
			//service.
			this.stopService(new Intent(this, RecordLocationService.class).setAction("STOP_RECORDING"));
			//LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient,this);
		}
	}
	
	@Override
	public void onCreate()
	{
		// TODO: Implement this method
		super.onCreate();
		this.connectGoogleApiClient();
	}
	
	
	
	@Override
	public void onDestroy()
	{
		if(!this.active){
			this.stopRecognitionUpdates();
			this.stopLocationRecording();
			this.penInt.cancel();
			this.mClient.disconnect();
			this.data().finalization();
			//this.active = false;
			this.service = null;
			super.onDestroy();
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

	public static boolean isOnline(Context context){
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNI = cm.getActiveNetworkInfo();
		return activeNI != null && activeNI.isConnected();
	}
	
	private CollectedData data(){
		if(this.data == null){
			this.data = new CollectedData(this.getApplicationContext());
		}
		if(this.data.isEmpty()){
			this.data.importFile(RecordLocationService.dataPath);
		}
		return this.data;
	}
	
	private boolean do_upload = false;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		if(intent != null){
		if (ActivityRecognitionResult.hasResult(intent)){
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
			//int confidence = probableActivity.getConfidence();
			int type = probableActivity.getType();


			if(MainActivity.active){
				this.sendBroadcast(new Intent()
							  .setAction("SERVICE_DATA")
							  .putExtra("ACTIVITY_NAME", this.getActivityName(type)));
							  //.putExtra("ACTIVITY_CONFIDENCE", confidence));
			}

			switch(type){
			case DetectedActivity.ON_BICYCLE :
				if(!RecordLocationService.active){
					//this.record = true;
					this.startLocationRecording();
					Log.i("ACTIVITY RECOGNITION","START LOCATION RECORDING");
				}
				break;
			/*if(this.updateTime > 15){
				this.removeRecognitionUpdates();
				this.updateTime = 15;
				this.cycle = 0;
				this.requestRecognitionUpdates(this.updateTime);
			}*/
			default :
				if(RecordLocationService.active){
					
					if(!this.data().isEmpty()){
						this.do_upload = true;
					}
					this.stopLocationRecording();
				}
				
				if(this.do_upload){
					if(!this.data().isEmpty() 
					&&  this.isOnline(this.getApplicationContext()) ){
						this.data().uploadData();
					}
					this.data().resetData();
				}
				break;
			}
				String action = intent.getAction();
				if(action != null){
				switch(action){
				case "START_ACTIVITY_RECOGNITION" :
					this.active = true;
					this.service = this;
					Log.i("ACTIVITY RECOGNITION","GOT INTENT ON START");
					break;
				case "STOP_ACTIVITY_RECOGNITION" :
					this.active = false;
					break;
				default :
					break;
				}
				}
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private int updateTime = 15;
	
	@Override
	public void onConnected(Bundle p1){
		this.penInt = PendingIntent.getService(this, 0, new Intent(this, ActivityRecognitionService.class), PendingIntent.FLAG_UPDATE_CURRENT);
		//this.locReq = new LocationRequest();
		//this.locReq.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
		//this.locReq.setFastestInterval(5000);
		//this.locReq.setInterval(10000);
		this.startRecognitionUpdates(this.updateTime);
	}
	
	
	public class MainBinder extends Binder {
		ActivityRecognitionService getService(){
			return ActivityRecognitionService.this;
		}
	}
	
	@Override
	public IBinder onBind(Intent p1)
	{
		return this.binder;
	}
	private final IBinder binder = new MainBinder();
	
	
}
