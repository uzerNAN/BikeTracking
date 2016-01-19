package uppsala.biketracking;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class ActivityRecognitionSensor extends IntentService
{
	public ActivityRecognitionSensor(){
		super(ActivityRecognitionSensor.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)){
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
			int type = probableActivity.getType();
			if(MapsActivity.active()){
				this.sendBroadcast(new Intent()
						.setAction(C.RECOGNITION_DATA_TXT)
						.putExtra(C.ACTIVITY_NAME_TXT, C.getActivityName(type)));
			}

			switch(type){
				case DetectedActivity.ON_BICYCLE :
					if(!ApiService.rl_is_active()) {
						startRecordLocation(true);
					}
					break;
				default :
					if (ApiService.rl_is_active() && !MapsActivity.rl_remote()) {
						startRecordLocation(false);
					}
					break;
			}
		}
	}
	
	private void startRecordLocation(boolean start){
		this.startService(new Intent(getApplicationContext(), ApiService.class).setAction(C.RECORD_LOCATION_TXT).putExtra(C.START_TXT, start));
	}

}
