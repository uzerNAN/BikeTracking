package uppsala.biketracking;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;

public class ApiService extends IntentService {

	private static GoogleApiClient mClient = null;
	private static LocationListener location = null;
	private static boolean recognition = false, record = false;
	private static final String TAG = "uppsala.biketracking";

	public ApiService() {
		super(ApiService.class.getName());
	}

	public static boolean active(){
		return (mClient != null);
	}

	public static boolean ar_is_active(){
		return recognition;
	}

	public static boolean rl_is_active(){
		return record;
	}

	public static boolean lu_is_active(){
		return (location != null);
	}

	public static boolean do_upload(){
		return (new File(C.getSaveDirectory(), C.CORRECTED_DATA).exists());
	}

	public static boolean do_correct(){
		return (new File(C.getSaveDirectory(), C.TEMPORARY_DATA).exists());
	}

	@Override
	public void onCreate() {
		super.onCreate();
		checkGoogleClient();
	}

	private static void createLocationListener(final Context context){
		if (location == null) {
			location = new LocationListener() {
				private final Context c = context;
				@Override
				public void onLocationChanged(Location location) {
					if (location != null) {
						if (MapsActivity.active()) {
							c.sendBroadcast(
									new Intent()
											.setAction(C.LOCATION_DATA_TXT)
											.putExtra(C.LAT_TXT, location.getLatitude())
											.putExtra(C.LON_TXT, location.getLongitude()));
						}
					}
				}
			};
		}
	}


	@Override
	public void onHandleIntent(Intent intent) {
		if (intent != null) {
			switch (intent.getAction()) {
				case C.ACTIVITY_RECOGNITION_TXT:
					activityRecognition(intent.getBooleanExtra(C.START_TXT, false));
					break;
				case C.RECORD_LOCATION_TXT:
					recordLocation(intent.getBooleanExtra(C.START_TXT, false));
					break;
				case C.LOCATION_UPDATES_TXT:
					locationUpdates(intent.getBooleanExtra(C.START_TXT, false));
					break;
				case C.CORRECT_TXT:
					if(intent.getBooleanExtra(C.START_TXT, false)){
						startCorrect();
					} else {
						corrected(intent.getBooleanExtra(C.SUCCESS_TXT, false));
					}
					break;
				case C.UPLOAD_TXT:
					if(intent.getBooleanExtra(C.START_TXT, false)){
						startUpload();
					} else {
						uploaded(intent.getBooleanExtra(C.SUCCESS_TXT, false));
					}
					break;
				case C.CHECK_BATTERY_STATE_TXT:
					checkBatteryState();
					break;
				case C.NETWORK_NOTIFICATION_TXT:
					networkNotification();
					break;
				default:
					break;
			}
			check_if_needed();
		}
		//return START_STICKY_COMPATIBILITY;
	}

	private void checkGoogleClient(){
		if (mClient != null){
			if(!client(getApplicationContext()).isConnected() && !client(getApplicationContext()).isConnecting()) {
				client(getApplicationContext()).connect();
			}
		} else if (googlePlayServicesAvailable()) {
			connectGoogleApiClient(getApplicationContext());
		}
	}

	private boolean googlePlayServicesAvailable() {
		int state = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
		boolean available = (ConnectionResult.SUCCESS == state);
		if (!available && MapsActivity.active()) {
			sendBroadcast(new Intent().setAction(C.GOOGLE_SERVICES_UNAVAILABLE_TXT).putExtra(C.STATE_TXT, state));
		}
		return available;
	}

	private void networkNotification(){
		if (NetworkStateNotifier.available()) {
			startCorrect();
			startUpload();
		}
	}

	private static void buttonCheck(Context context, int id){
		if(MapsActivity.active()){
			context.sendBroadcast(new Intent().setAction(C.UPDATE_BUTTON_TXT).putExtra(C.BUTTON_TXT, id));
		}
	}

	private void activityRecognition(boolean start) {
		if (start) {
			if (!ar_is_active() && MapsActivity.ar_remote()) {
				if (client(getApplicationContext()).isConnected()) {
					updateActivityRecognition(getApplicationContext());
				} else {
					reconnectClient();
				}
			}
		} else {
			if (ar_is_active() && !MapsActivity.ar_remote()) {
				if (client(getApplicationContext()).isConnected()) {
					stopActivityRecognition(getApplicationContext());
					//if(ar_is_active(getApplicationContext())){
					//	mClient.disconnect();
					//	mClient = null;
					//	connectGoogleApiClient(getApplicationContext());
					//}
				}
				else {
					reconnectClient();
				}
			}
		}
		//buttonCheck(R.id.ACTIVITY_RECOGNITION);
	}

	private void recordLocation(boolean start) {
		if (start) {
			if (!rl_is_active()) {
				if (client(getApplicationContext()).isConnected()) {
					updateRecordLocation(getApplicationContext());
					if (!lu_is_active()) {
						updateActivityRecognition(getApplicationContext());
					}
				} else {
					reconnectClient();
				}
			}
		} else {
			if (rl_is_active()) {
				if (client(getApplicationContext()).isConnected()) {
					stopRecordLocation(getApplicationContext());
					if (!lu_is_active()) {
						updateActivityRecognition(getApplicationContext());
					}
					//if(rl_is_active(getApplicationContext())){
					//	mClient.disconnect();
					//	mClient = null;
					//	connectGoogleApiClient(getApplicationContext());
					//}
				} else {
					reconnectClient();
				}
				RecordLocationSensor.flush_buffer();
				String fileTo;
				if(Correct.correcting()){
					fileTo = C.WAITING_TEMPORARY_DATA;
				} else {
					fileTo = C.TEMPORARY_DATA;
				}
				C.appendFileTo(C.RAW_DATA, fileTo);
				if(do_correct() && !Correct.correcting()) {
					startCorrect();
				}
			}
		}
		//buttonCheck(R.id.RECORD_LOCATION);
	}

	private void locationUpdates(boolean start) {
		if (start) {
			if (!lu_is_active()) {
				if (client(getApplicationContext()).isConnected()) {
					startLocationUpdates(LocationRequest.PRIORITY_HIGH_ACCURACY, getApplicationContext());
					if(!rl_is_active()) {
						updateActivityRecognition(getApplicationContext());
					}
				} else {
					reconnectClient();
				}
			}
		} else {
			if (lu_is_active()) {
				if (client(getApplicationContext()).isConnected()) {
					stopLocationUpdates();
					if (!rl_is_active()) {
						updateActivityRecognition(getApplicationContext());
					}
				} else {
					reconnectClient();
				}
			}
		}
	}

	private static GoogleApiClient client(Context context){
		if(mClient == null){
			connectGoogleApiClient(context);
		}
		return mClient;
	}

	private void checkBatteryState() {
		if(!active()){
			if(client(getApplicationContext()).isConnected()){
				updateActivityRecognition(getApplicationContext());
			} else if (MapsActivity.active() || MapsActivity.ar_remote() || MapsActivity.rl_remote()) {
				mClient.connect();
			}
		} else if (rl_is_active()) {
			stopRecordLocation(getApplicationContext());
			updateRecordLocation(getApplicationContext());
		}
	}

	private void check_if_needed() {
		if (!ar_is_active() && !lu_is_active() && !rl_is_active() || PowerSavingModule.battery_low() && !MapsActivity.rl_remote() && !MapsActivity.ar_remote()) {
			if (client(getApplicationContext()).isConnected()) {
				if (ar_is_active()) {
					stopActivityRecognition(getApplicationContext());
				}
				if (lu_is_active()) {
					stopLocationUpdates();
				}
				if (rl_is_active()) {
					stopRecordLocation(getApplicationContext());
				}
				client(getApplicationContext()).disconnect();
				mClient = null;
			}
		}
	}

	private static void updateActivityRecognition(Context context) {
		if (!(lu_is_active() && rl_is_active()) && MapsActivity.ar_remote()) {
			if (ar_is_active()) {
				stopActivityRecognition(context);
			}
			int time;
			if (!lu_is_active() && !rl_is_active()) {
				time = C.AR_TIME;
			} else {
				time = C.AR_FASTEST_TIME;
			}
			startActivityRecognition(time, context);
		}
	}

	private static void updateRecordLocation(Context context) {
		int priority;
		if (PowerSavingModule.battery_high()) {
			priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
		} else {
			priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
		}
		startRecordLocation(priority, context);
	}

	private static void reconnectClient() {
		if (mClient != null){
			if(!mClient.isConnected() && !mClient.isConnecting()) {
				mClient.connect();
			}
		}
	}

	private static synchronized void connectGoogleApiClient(final Context context) {
		if (mClient == null && context != null) {
			mClient = new GoogleApiClient.Builder(context)
					.addApi(ActivityRecognition.API)
					.addApi(LocationServices.API)
					.addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

						public void onConnected(Bundle p1) {
							if (MapsActivity.active()) {
								ApiService.startLocationUpdates(LocationRequest.PRIORITY_HIGH_ACCURACY, context);
							}
							if (MapsActivity.rl_remote()) {
								ApiService.updateRecordLocation(context);
							} else {
								buttonCheck(context, R.id.RECORD_LOCATION);
							}
							if (MapsActivity.ar_remote()) {
								ApiService.updateActivityRecognition(context);
							} else {
								buttonCheck(context, R.id.ACTIVITY_RECOGNITION);
							}
						}

						@Override
						public void onConnectionSuspended(int p1) {
							ApiService.reconnectClient();
							// TODO: Implement this method
						}
					})
					.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
						@Override
						public void onConnectionFailed(ConnectionResult p1) {
							ApiService.reconnectClient();
							// TODO: Implement this method
						}
					})
					.build();
		}
		reconnectClient();
	}

	private static void startActivityRecognition(int time, final Context context) {
		if (client(context).isConnected()) {
			ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient, time, PendingIntent.getService(context, C.AR_ID, new Intent(context, ActivityRecognitionSensor.class), PendingIntent.FLAG_UPDATE_CURRENT));
			recognition = true;
			buttonCheck(context, R.id.ACTIVITY_RECOGNITION);
		}
	}

	private static void stopActivityRecognition(final Context context) {
		if (client(context).isConnected()) {
			ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(mClient, PendingIntent.getService(context, C.AR_ID, new Intent(context, ActivityRecognitionSensor.class), PendingIntent.FLAG_NO_CREATE));
			recognition = false;
			buttonCheck(context, R.id.ACTIVITY_RECOGNITION);
		}
	}

	private static void startLocationUpdates(int priority, Context context) {
		if (client(context).isConnected()) {
			createLocationListener(context);
			LocationRequest location_req = new LocationRequest();
			location_req.setInterval(C.RL_TIME);
			location_req.setFastestInterval(C.RL_FASTEST_TIME);
			location_req.setPriority(priority);

			LocationServices.FusedLocationApi.requestLocationUpdates(mClient, location_req, location);// this.location);
		}
	}

	private static void stopLocationUpdates() {
		if (mClient != null && mClient.isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mClient, location);
			RecordLocationSensor.saveSettings();
			location = null;
		}
	}

	private static void startRecordLocation(int priority, final Context context) {
		if (client(context).isConnected()) {
			LocationRequest record_req = new LocationRequest();
			record_req.setInterval(C.LU_TIME);
			record_req.setFastestInterval(C.LU_FASTEST_TIME);
			record_req.setPriority(priority);
			LocationServices.FusedLocationApi.requestLocationUpdates(mClient, record_req, PendingIntent.getService(context, C.RL_ID, new Intent(context, RecordLocationSensor.class), PendingIntent.FLAG_UPDATE_CURRENT));
			record = true;
			buttonCheck(context, R.id.RECORD_LOCATION);
		}
	}

	private static void stopRecordLocation(final Context context) {
		if (client(context).isConnected()) {
			LocationServices.FusedLocationApi.removeLocationUpdates(mClient, PendingIntent.getService(context, C.RL_ID, new Intent(context, RecordLocationSensor.class), PendingIntent.FLAG_NO_CREATE));
			record = false;
			buttonCheck(context, R.id.RECORD_LOCATION);
		}
	}


	private void corrected(boolean success) {
		if (MapsActivity.active()) {
			sendBroadcast(new Intent().setAction(C.CORRECT_TXT).putExtra(C.SUCCESS_TXT, success));
		}
		if(success) {
			startUpload();
		}
	}

	private void startCorrect(){
		if (do_correct() && !Correct.correcting() && NetworkStateNotifier.available()) {
			new Thread(new Correct(getApplicationContext())).start();
			buttonCheck(getApplicationContext(), R.id.CORRECT);
		}
	}

	private void startUpload(){
		if(!rl_is_active() && !do_correct() && do_upload() && !Upload.uploading() && NetworkStateNotifier.available()){
			new Thread(new Upload(getApplicationContext())).start();
			buttonCheck(getApplicationContext(), R.id.UPLOAD);
		}
	}

	private void uploaded(boolean success){
		if (MapsActivity.active()) {
			sendBroadcast(new Intent().setAction(C.UPLOAD_TXT).putExtra(C.SUCCESS_TXT, success));
		}
	}
}
