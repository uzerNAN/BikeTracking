package uppsala.biketracking;

import android.app.*;
import android.content.*;
import android.util.*;
import android.widget.*;
import com.google.android.gms.location.*;
import android.os.*;
import android.support.v4.app.*;
//import uppsala.biketracking.Tracking;

/**
 * Service that receives ActivityRecognition updates. It receives updates
 * in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIS extends IntentService
{
	//private NotificationManager mNM;
	//private int NOTIFICATION = R.string.local_service_started;
	//public class LocalBinder extends Binder {
	//	ActivityRecognitionIS getService() {
	//		return ActivityRecognitionIS.this;
	//	}
	//}
	
	//@Override
	//public int onStartCommand(Intent intent, int flags, int startId) {
		
	//	return START_STICKY;
	//}

	//@Override
	//public void onDestroy()
	//{
		// TODO: Implement this method
		//super.onDestroy();
		
	//}

	//@Override
	//public IBinder onBind(Intent p1)
	//{
		// TODO: Implement this method
	//	return null;
	//}


	protected static final String TAG = "activity-detection-intent-service";
	private int activityType = -1, confidence = 0;
	private String activityName = "";
	//..
	//@Override
	public ActivityRecognitionIS(){
		super(TAG);
	}
	
	@Override
	public void onCreate(){
		//mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		//showNotification();
		super.onCreate();
	}
	
	public int getType(){
		return this.activityType;
	}
	
	public String getName(){
		return this.activityName;
	}
	
	public int getConfidence(){
		return this.confidence;
	}
	
	//public int getSleepTime(){
	//	return this.sleepTime;
	//}
	
	/**
	 * Called when a new activity detection update is available.
	 */
	 
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "Entering onHandleIntent");
		//...
		// If the intent contains an update
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			this.publishResults("BOOT COMPLETED", 0, -1);
		}
		else if (ActivityRecognitionResult.hasResult(intent)){
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
		  // if( !(probableActivity.getType() == this.activityType  && 
		 //   probableActivity.getConfidence() == this.confidence )
		//	) {
			//this.sleepTime = intent.getIntExtra("time", 0);
			// Get the update
			//ActivityRecognitionResult result = 
			//	ActivityRecognitionResult.extractResult(intent);

			//DetectedActivity mostProbableActivity 
			//	= result.getMostProbableActivity();

			// Get the confidence % (probability)
			this.confidence = probableActivity.getConfidence();

			// Get the type 
			this.activityType = probableActivity.getType();
			
			//this.activityName = "";
			
			switch(activityType){
				case(DetectedActivity.IN_VEHICLE):
					this.activityName = "IN VEHICLE";
					break;
			 	case(DetectedActivity.ON_BICYCLE):
					this.activityName = "ON BICYCLE";
					break;
			 	case(DetectedActivity.ON_FOOT):
					this.activityName = "ON FOOT";
					break;
			 	case(DetectedActivity.STILL):
					this.activityName = "STILL";
					break;
			 	case(DetectedActivity.UNKNOWN):
					this.activityName = "UNKNOWN";
					break;
			 	case(DetectedActivity.TILTING):
					this.activityName = "TILTING";
					break;
				default:
					break;
			}
			//Toast.makeText(ActivityRecognitionIS.this,
			Log.i(TAG,
						   this.activityName+" :: "+this.confidence+" %  confidence");
			// Log.i(TAG, );
			// process
			//intent = new Intent(this, WakefulReceiver.class);
			//intent.putExtra("name", this.activityName);
			//intent.putExtra("confidence", this.confidence);
			//intent.putExtra("type", this.activityType);
			
			//try {
			//	Thread.sleep(this.sleepTime);
			//} catch (InterruptedException e) {
			//	e.printStackTrace();
			//}
			//finally {
			//WakefulReceiver.completeWakefulIntent(intent);
				this.publishResults(this.activityName, this.confidence, this.activityType);
			//}
			//}
		}
		/*else if(intent.getExtras() != null){
			//Log.i(TAG, "Else if");
			//if(!this.activityName.equals(intent.getExtras().getString("name"))
			//|| this.confidence != intent.getExtras().getInt("confidence")){
				
				//Log.i(TAG, "passed");
			 	//WakefulReceiver.completeWakefulIntent(intent);
				//this.publishResults(this.activityName, this.confidence, this.activityType);
			 
			//}
			//else {
			//	Log.i(TAG, "not passed");
				//WakefulReceiver.completeWakefulIntent(intent);
			//}
				
		 	//WakefulReceiver.completeWakefulIntent(intent);
		}
		else {
			Log.i(TAG, "Else");
			//WakefulReceiver.completeWakefulIntent(intent);
			this.publishResults(this.activityName, this.confidence, this.activityType);
			
		}*/
		//WakefulReceiver.completeWakefulIntent(intent);
		Log.i(TAG, "Leaving onHandleIntent");
	}
	private void publishResults(String name, int c, int type) {
		//if(Tracking.mainActivity != null){
		//	Tracking.mainActivity.update(" * FROM_SERVICE * "+name, type, c);
		//}
		//else{
		Log.i(TAG,
		//Toast.makeText(this, 
		"UPDATE : '"+ name+"' :: "+type+" | "+c+"%");//, Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(this.getApplicationContext(), WakefulReceiver.class);
		intent.putExtra("name", name);
		intent.putExtra("confidence", c);
		intent.putExtra("type", type);
		sendBroadcast(intent);
		//}
	}
}
