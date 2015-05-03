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
	
	@Override
	public void onReceive(Context context, Intent intent){
		if(intent != null){
		switch(intent.getAction()){
			case("DATA_UPLOADED"):
				Toast.makeText(context, "Data was successfully uploaded to server", Toast.LENGTH_LONG).show();
				if(Tracking.mainActivity != null){
					//Tracking.mainActivity.resetUpdate();
				}
				break;
			case("UPLOAD_ERROR"):
				Toast.makeText(context, "UNABLE TO UPLOAD DATA", Toast.LENGTH_LONG).show();
				break;
		
			case("FILE_ERROR"):
				Toast.makeText(context, "UNABLE TO WRITE IN A FILE", Toast.LENGTH_LONG).show();
				break;
		
			case("SERVICE_DATA"):
				if(Tracking.mainActivity != null){
					Bundle data = intent.getExtras();
					Tracking.mainActivity.update(data.getInt("ACTIVITY_TYPE"), data.getString("ACTIVITY_NAME"), data.getInt("ACTIVITY_CONFIDENCE"));
				}
				break;
			case("ADD_DATA"):
				if(Tracking.mainActivity != null){
					//Bundle data = intent.getExtras();
					//Tracking.mainActivity.add(data.getInt("SID"), data.getDouble("LATITUDE"), data.getDouble("LONGITUDE"), data.getLong("TIME"), data.getFloat("ACCURACY"));
				}
				break;
			default:
				break;
		}
		}
	}
	
}
