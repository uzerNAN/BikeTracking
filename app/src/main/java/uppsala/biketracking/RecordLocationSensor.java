package uppsala.biketracking;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
//import java.net.*;
//import java.util.*;
//mport org.json.*;

public class RecordLocationSensor extends IntentService
{
	public RecordLocationSensor(){
		super(RecordLocationSensor.class.getName());
	}

	@Override
	public void onHandleIntent(Intent intent)
	{
		int currSID = ApiService.rl_sid;
		Location p1 = LocationResult.extractResult(intent).getLastLocation();
		if(p1 != null && ApiService.rl_latitude != p1.getLatitude()
		&& ApiService.rl_longitude != p1.getLongitude()){

			if(Constants.long_diff(ApiService.rl_time, 0) > 0 && Constants.long_diff(Constants.long_diff(p1.getTime(), ApiService.rl_time), Constants.SESSION_TIMEOUT) > 0){
				currSID++;
			}

			float speed;
			if(Constants.long_diff(ApiService.rl_time, 0) > 0){
				Location loc = new Location("");
				loc.setLatitude(ApiService.rl_latitude);
				loc.setLongitude(ApiService.rl_longitude);
				speed = 1000*p1.distanceTo(loc)/(p1.getTime()-ApiService.rl_time); }
			else{ speed = p1.getSpeed(); }

			if(!Constants.writeFile(Constants.DATA_PATH,
					"SID " + currSID
							+ "|LATITUDE " + p1.getLatitude()
							+ "|LONGITUDE " + p1.getLongitude()
							+ "|TIME " + p1.getTime()
							+ "|SPEED " + speed
							+ "|ACCURACY " + p1.getAccuracy()
							+ "\r\n", true)){
				this.sendBroadcast(new Intent("FILE_PERMISSION").setAction("FILE_ERROR"));
			}
			else{
				this.startService(new Intent(this, ApiService.class)
						.setAction("LAST_DATA")
						.putExtra("SID", currSID)
						.putExtra("LATITUDE", p1.getLatitude())
						.putExtra("LONGITUDE", p1.getLongitude())
						.putExtra("TIME", p1.getTime()));
			}
		}
	}


}
