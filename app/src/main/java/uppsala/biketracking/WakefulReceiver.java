package uppsala.biketracking;

import android.content.*;
import android.location.*;
import android.os.*;
import android.support.v4.content.*;
import android.util.*;
import android.widget.*;
import com.google.android.gms.location.*;

public class WakefulReceiver extends WakefulBroadcastReceiver
{
	private String aName = "";
	private int aConfidence = 0;
	private int aType = 0;
	private boolean startService = false;
	
	@Override
	public void onReceive(Context context, Intent intent){
		//Bundle bundle = intent.getExtras();
		
		/*Location loc;
		if (bundle != null) {
			loc = (Location)bundle.get(android.location.LocationManager.KEY_LOCATION_CHANGED);
		if(loc != null){
			Toast.makeText(context, "2 UPDATE | "+loc.toString(), Toast.LENGTH_LONG).show();
		}*/
		if(intent.getAction().equals("FILE_ERROR")){
			Toast.makeText(context, "UNABLE TO WRITE IN A FILE", Toast.LENGTH_LONG).show();
		}
		//if(intent.getAction().equals("SERVICE_DATA")){
			//Log.i("RECEIVER UPDATE", "GOT DATA ON RECEIVE");
			//if(Tracking.mainActivity!=null){
			/*send.putExtra("ACCURANCY", p1.getAccuracy());
			send.putExtra("LATITUDE", p1.getLatitude());
			send.putExtra("LONGITUDE", p1.getLongitude());
			send.putExtra("ALTITUDE", p1.getAltitude());
			send.putExtra("SPEED", p1.getSpeed());
			send.putExtra("TIME", p1.getTime());
			//send.putExtra("ACTIVITY", aType+" :: "+aName+" | "+aConfidence+"%");
			send.putExtra("ACTIVITY_TYPE", aType);
			send.putExtra("ACTIVITY_NAME", aName);
			send.putExtra("ACTIVITY_CONFIDENCE", aConfidence);*/
			//Toast.makeText(context, 
			//"GOT DATA",
			//intent.getStringExtra("LOCATION")+" | \r\n"+intent.getStringExtra("ACTIVITY"), 
			//Toast.LENGTH_SHORT).show();
			//}
		//}
		if(intent.getAction().equals("SERVICE_DATA") && Tracking.mainActivity != null){
			Bundle data = intent.getExtras();
			Tracking.mainActivity.update(data.getInt("ACTIVITY_TYPE"), data.getString("ACTIVITY_NAME"), data.getInt("ACTIVITY_CONFIDENCE"));
		}
		/*if (ActivityRecognitionResult.hasResult(intent) && 
			(WakefulService.mainService == null || 
			Tracking.mainActivity != null)){
				Log.i("RECEIVER UPDATE", "STILL UPDATING");
				Toast.makeText(context, "STILL UPDATING", Toast.LENGTH_SHORT).show();
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
						startService = true;
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
				//Toast.makeText(ActivityRecognitionIS.this,
				//Log.i(TAG, this.activityName+" :: "+this.confidence+" %  confidence");
				// Log.i(TAG, );
			//name = bundle.getString("name");
			//confidence = bundle.getInt("confidence");
			//type = bundle.getInt("type");
			//if (resultCode == RESULT_OK) {
			//Log.i("WakefulReceiver",type + " : '" + name + "' | " + confidence+"%");
			//Toast.makeText(context,
			
			if(WakefulService.mainService == null && startService){
				intent = new Intent(context, WakefulService.class);
				intent.setAction("SERVICE_START");
				//intent.putExtra("aType", aType);
				//intent.putExtra("aName", aName);
				//intent.putExtra("aConfidence", aConfidence);
				context.startService(intent);
				Toast.makeText(context, "Service Started", Toast.LENGTH_LONG).show();
				startService = false;
			}
			if(Tracking.mainActivity != null){
						 Tracking.mainActivity.update(aType, aName, aConfidence);
						
						 //Tracking.mainActivity.changePosition(loc.getLatitude(), loc.getLongitude());
			}
		}
		else{
			Log.i("RECEIVER UPDATE", "NOT UPDATING");
		}*/
			//} else {
			//	Toast.makeText(MainActivity.this, "Download failed",
			//				   Toast.LENGTH_LONG).show();
			//	textView.setText("Download failed");
			//}
		//}
		//else {
		//	Toast.makeText(context,
		//				   "NO ACTIVITY RESULT", Toast.LENGTH_LONG).show();
		//}
		//intent = new Intent(context, ActivityRecognitionIS.class);
		//intent.putExtra("name", name);
		//intent.putExtra("confidence", confidence);
		//intent.putExtra("type", type);
		//startWakefulService(context, intent);
	}
	
}
