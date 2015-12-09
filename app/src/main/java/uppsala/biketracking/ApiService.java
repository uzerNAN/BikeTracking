package uppsala.biketracking;

import android.app.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;

public class ApiService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
	
	private GoogleApiClient mClient = null;
	private PendingIntent penInt = null;
	private static ApiService service = null;
	public static boolean ar_is_active = false;
	public static boolean rl_is_active = false;
	public static boolean lu_is_active = false;
	public static boolean is_active = false;
	
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

	protected synchronized void connectGoogleApiClient() {
		if(this.mClient == null){
			this.mClient = new GoogleApiClient.Builder(this)
				.addApi(ActivityRecognition.API)
				.addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.build();
		}
		if(!this.mClient.isConnected() || !this.mClient.isConnecting()){
			this.mClient.connect();
		}
	}

	private void startRecognitionUpdates(int sec){
		if(this.mClient.isConnected()){
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(this.mClient, sec*1000, this.penInt);
		}
	}

	

	private void stopRecognitionUpdates(){
		if(this.mClient.isConnected()){
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(this.mClient, this.penInt);
		}
	}

	private void startLocationRecording(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.requestLocationUpdates(this.mClient, this.locReq, RecordLocationService.class);
		}
	}

	private void stopLocationRecording(){
		if(this.mClient.isConnected()){
			LocationServices.FusedLocationApi.removeLocationUpdates(this.mClient, RecordLocationService);
		}
	}
	
	public static void successfullyUploaded(){
		service.do_upload = false;
		if(MainActivity.active){
			service.sendBroadcast(new Intent().setAction("DATA_UPLOADED"));
		}
		RecordLocationService.writeFile(RecordLocationService.dataPath, "", false);
		RecordLocationService.writeFile(RecordLocationService.settingsPath, "", false);
	}

	public static void failedToUpload(){
		if(MainActivity.active){
		    service.sendBroadcast(new Intent().setAction("UPLOAD_ERROR"));
		}
	}
}
