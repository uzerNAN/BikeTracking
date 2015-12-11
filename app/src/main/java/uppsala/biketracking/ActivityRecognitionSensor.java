package uppsala.biketracking;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.io.File;

public class ActivityRecognitionSensor extends IntentService
{
	private CollectedData data = null;

	public ActivityRecognitionSensor(){
		super(ActivityRecognitionSensor.class.getName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (ActivityRecognitionResult.hasResult(intent)){
			DetectedActivity probableActivity = ActivityRecognitionResult.extractResult(intent).getMostProbableActivity();
			int type = probableActivity.getType();
			if(MainActivity.active){
				this.sendBroadcast(new Intent()
						.setAction("RECOGNITION_DATA")
						.putExtra("ACTIVITY_NAME", this.getActivityName(type)));
			}

			switch(type){
				case DetectedActivity.ON_BICYCLE :
					if(!ApiService.rl_is_active) {
						startRecordLocation();
					}
					break;
				default :
					if(ApiService.rl_is_active){
						this.stopRecordLocation();
					}

					if(ApiService.do_upload){
						if(((new File(Constants.DATA_PATH)).length() != 0)
								&&  ApiService.isOnline(this.getApplicationContext()) ){
							this.data().uploadData(getApplicationContext());
						}
						this.data().resetData();
						this.data().finalization();
						this.data = null;
					}
					break;
			}
		}
	}
	
	private void startRecordLocation(){
		this.startService(new Intent(this, ApiService.class).setAction("START_RL"));
	}

	private void stopRecordLocation(){
		this.startService(new Intent(this, ApiService.class).setAction("STOP_RL"));
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

	private CollectedData data(){
		if(this.data == null){
			this.data = new CollectedData(this.getApplicationContext());
		}
		if(this.data.isEmpty()){
			this.data.importFile(Constants.DATA_PATH);
		}
		return this.data;
	}
}
