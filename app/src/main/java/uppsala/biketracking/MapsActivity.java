package uppsala.biketracking;

/*import android.content.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.support.v4.app.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//import android.R;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private static GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private Marker mMarker;
	private float mZoom;
	private LinearLayout coordinates;
	private String aName = C.NO_ACTIVITY_TXT;
	private Bitmap icon;
	//private CollectedData data;
    private BroadcastReceiver receiver;
    private IntentFilter broadcast_filter;
	private static boolean ar_remote = true;
	private static boolean rl_remote = false;
	//private AutoCompleteTextView autoCompView;

	public static boolean active(){
		return mMap != null;
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/******************/
        //recordLocation(true);
		/******************/

        final boolean customTitleSupported = this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        this.mZoom = 20;
        this.setContentView(R.layout.activity_maps);

        if (customTitleSupported) {
            this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
        }

        this.coordinates = (LinearLayout) this.findViewById(R.id.coordinates);
        this.icon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        this.icon.setPixel(0, 0, Color.argb(Color.alpha(R.color.transparent), Color.red(R.color.transparent), Color.green(R.color.transparent), Color.blue(R.color.transparent)));
        this.receiver = new BroadcastReceiver() {
            @Override
            public void onReceive (Context context, Intent intent){
                if (intent != null) {
                    switch (intent.getAction()) {
						case (C.UPLOAD_TXT):
							if(intent.getBooleanExtra(C.SUCCESS_TXT, false)) {
								Toast.makeText(context, C.SUCCESS_UPLOAD_TXT, Toast.LENGTH_LONG).show();
							} else {
								Toast.makeText(context, C.ERROR_UPLOAD_TXT, Toast.LENGTH_LONG).show();
							}
                            break;
                        case (C.CORRECT_TXT):
							if(intent.getBooleanExtra(C.SUCCESS_TXT, false)) {
								Toast.makeText(context, C.SUCCESS_CORRECT_TXT, Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(context, C.ERROR_CORRECT_TXT, Toast.LENGTH_SHORT).show();
							}
                            break;
                        case (C.FILE_ERROR_TXT):
                            Toast.makeText(context, C.ERROR_FILE_TXT, Toast.LENGTH_LONG).show();
                            break;

                        case (C.RECOGNITION_DATA_TXT):
                            MapsActivity.this.updateActivity(intent.getExtras().getString(C.ACTIVITY_NAME_TXT));
                            break;
						case (C.LOCATION_DATA_TXT) :
							Bundle extras = intent.getExtras();
							MapsActivity.this.changePosition(extras.getDouble(C.LAT_TXT), extras.getDouble(C.LON_TXT));
							break;
						case (C.GOOGLE_SERVICES_UNAVAILABLE_TXT) :
							GooglePlayServicesUtil.getErrorDialog(intent.getIntExtra(C.STATE_TXT, ConnectionResult.SERVICE_DISABLED), MapsActivity.this, ConnectionResult.RESOLUTION_REQUIRED).show();
							break;
						case (C.UPDATE_BUTTON_TXT) :
							MapsActivity.this.set_ba(intent.getIntExtra(C.BUTTON_TXT, 0));
							Toast.makeText(context, C.OK_TXT+33, Toast.LENGTH_SHORT).show();
                        default:
                            break;
                    }
                }
            }
        };
        broadcast_filter = new IntentFilter();
        broadcast_filter.addAction(C.UPDATE_BUTTON_TXT);
        broadcast_filter.addAction(C.UPLOAD_TXT);
        broadcast_filter.addAction(C.CORRECT_TXT);
        broadcast_filter.addAction(C.FILE_ERROR_TXT);
        broadcast_filter.addAction(C.RECOGNITION_DATA_TXT);
        broadcast_filter.addAction(C.LOCATION_DATA_TXT);
        broadcast_filter.addAction(C.GOOGLE_SERVICES_UNAVAILABLE_TXT);
    }

	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		waiting_for_map = false;
		// Check if we were successful in obtaining the map.
		if (mMap != null) {
			setUpMap();
		}
		else {
			Toast.makeText(getApplicationContext(), C.ERROR_MAP_TXT, Toast.LENGTH_LONG).show();
		}
	}

	public void set_ba(int id){
		boolean a;
		Button ar = (Button)this.findViewById(id);

		switch(id) {
			case R.id.ACTIVITY_RECOGNITION :
				a = !(ApiService.ar_is_active(getApplicationContext()));
				ar.setText(a ? C.Start_AR_TXT : C.Stop_AR_TXT);
				if(!ar.isEnabled()){
					ar.setEnabled(true);
				}
				break;
			case R.id.RECORD_LOCATION :
				a = !(ApiService.rl_is_active(getApplicationContext()));
				ar.setText(a ? C.Start_RL_TXT : C.Stop_RL_TXT);
				if(!ar.isEnabled()){
					ar.setEnabled(true);
				}
				break;
			case R.id.CORRECT :
				a = ApiService.do_correct() && !(Correct.correcting());
				ar.setText(a ? C.Correct_The_Data_TXT : Correct.correcting() ? C.Correct_ON_TXT : getResources().getString(R.string.no_correct));
				ar.setEnabled(a);
				break;
			case R.id.UPLOAD :
				a = ApiService.do_upload() && !(Upload.uploading());
				ar.setText(a ? C.Upload_The_Data_TXT : Upload.uploading() ? C.Upload_ON_TXT : getResources().getString(R.string.no_upload));
				ar.setEnabled(a);
				break;
			default : a = false; break;
		}
		ar.setTextColor(getResources().getColor(a ? R.color.green :
				((id == R.id.UPLOAD && (Upload.uploading() || !(ApiService.do_upload())))
						|| (id == R.id.CORRECT && (Correct.correcting() || !(ApiService.do_correct())))) ? R.color.white : R.color.red));

	}

	public void do_ar (View view) {
		view.setEnabled(false);
		activityRecognition(!(ApiService.ar_is_active(getApplicationContext())));
		Toast.makeText(this, C.OK_TXT+C.SPACE+12+ApiService.ar_is_active(getApplicationContext()), Toast.LENGTH_SHORT).show();
	}
	public void do_rl (View view){
		view.setEnabled(false);
		recordLocation(!(ApiService.rl_is_active(getApplicationContext())));
		Toast.makeText(this, C.OK_TXT+C.SPACE+22+ApiService.rl_is_active(getApplicationContext()), Toast.LENGTH_SHORT).show();
	}

	public void do_correct (View view){
		view.setEnabled(false);
		if (ApiService.do_correct() && !(Correct.correcting()) && NetworkStateNotifier.available()) {
			new Thread(new Correct(getApplicationContext())).start();
		} else if (!(NetworkStateNotifier.available())) {
			Toast.makeText(getApplicationContext(), C.CONNECTION_UNAVAILABLE_TXT, Toast.LENGTH_LONG).show();
		}
	}

	public void do_upload (View view){
		view.setEnabled(false);
		if (ApiService.do_upload() && !(Upload.uploading()) && NetworkStateNotifier.available()) {
			new Thread(new Upload(getApplicationContext())).start();
		} else if (!(NetworkStateNotifier.available())) {
			Toast.makeText(getApplicationContext(), C.CONNECTION_UNAVAILABLE_TXT, Toast.LENGTH_LONG).show();
		}
	}
	
	private void activityRecognition(boolean start){
		if(start != (ApiService.ar_is_active(getApplicationContext()))){
			ar_remote = start;
			this.startService((new Intent(this, ApiService.class).setAction(C.ACTIVITY_RECOGNITION_TXT).putExtra(C.START_TXT, start)));

			Toast.makeText(this, C.OK_TXT+C.SPACE+11, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, C.NOT_OK_TXT+C.SPACE+11, Toast.LENGTH_SHORT).show();
		}
	}

	private void locationUpdates(boolean start){
		if(start != (ApiService.lu_is_active())){
			this.startService(new Intent(this, ApiService.class).setAction(C.LOCATION_UPDATES_TXT).putExtra(C.START_TXT, start));
		}
	}

	private void recordLocation(boolean start){
		if(start != (ApiService.rl_is_active(getApplicationContext()))) {
			rl_remote = start;
			this.startService(new Intent(this, ApiService.class).setAction(C.RECORD_LOCATION_TXT).putExtra(C.START_TXT, start));
			Toast.makeText(this, C.OK_TXT+C.SPACE+21, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(this, C.NOT_OK_TXT+C.SPACE+21, Toast.LENGTH_SHORT).show();
		}
	}

	private void powerSavingModule(){
		startService(new Intent(this, PowerSavingModule.class).setAction(C.BATTERY_CHECK_TXT));
	}

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, broadcast_filter);
        setUpMapIfNeeded();
		if(!ApiService.active()){
			powerSavingModule();
		}
		locationUpdates(true);
		//Toast.makeText(this.getApplicationContext(), C.OK_TXT, Toast.LENGTH_SHORT).show();
		set_ba(R.id.ACTIVITY_RECOGNITION);
		set_ba(R.id.RECORD_LOCATION);
		set_ba(R.id.CORRECT);
		set_ba(R.id.UPLOAD);
    }
	
	@Override
	protected void onPause(){
		super.onPause();
        this.unregisterReceiver(this.receiver);
		mMap = null;
		locationUpdates(false);
	}

	public static boolean ar_remote(){ return ar_remote; }
	public static boolean rl_remote(){ return rl_remote; }

	private void updateActivity(String name){
		if(!this.aName.equals(name)){
			this.aName = name;
			this.updateMarkerTitle(this.aName);
		}
	}

	private boolean waiting_for_map = false;
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null && !waiting_for_map) {
			waiting_for_map = true;
            // Try to obtain the map from the SupportMapFragment.
			SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map);
			mapFragment.getMapAsync(this);
        }
    }
    private void setUpMap() {
		mMap.setMyLocationEnabled(true);
		mMap.animateCamera(CameraUpdateFactory.zoomTo(this.mZoom));
		mMap.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener(){
			@Override
			public void onCameraChange(CameraPosition pos){
				MapsActivity.this.mZoom = pos.zoom;
			}
		});
	}
	private void updateMarkerTitle(String title){
		if(this.mMarker!=null){
			this.mMarker.setTitle(title);
			this.mMarker.showInfoWindow();
		}
	}
	public void changePosition(double latitude, double longitude){
		if(mMarker != null){
			mMarker.remove();
		}
		LatLng latLng = new LatLng(latitude, longitude);
		if(mMap != null){
			mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon)));
			//this.mMarker.setVisible(true);
			updateActivity(aName);
			// Showing the current location in Google Map
			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			// Zoom in the Google Map
			mMap.animateCamera(CameraUpdateFactory.zoomTo(this.mZoom));
		}
		if(coordinates != null) {
			((TextView)coordinates.getChildAt(0)).setText(C.LAT_TXT+ C.SPACE+latitude);
			((TextView)coordinates.getChildAt(1)).setText(C.LON_TXT+ C.SPACE+longitude);
		}
	}
    /*private static final String TAG = "Tracking";
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyAfGNm4U1z8IbWbpE1rnyvFumLOg51eUws";
	public static ArrayList<String> autocomplete(String input) {

        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
			//Log.i(TAG, "I'm in");
            StringBuilder sb = new StringBuilder(PLACES_API_BASE
												 + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?sensor=false&key=" + API_KEY);
            // sb.append("&components=country:uk");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error processing Places API URL", e);
            return resultList;
		} catch (IOException e) {
            Log.e(TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
				//Log.i(TAG, "disconnecting, "+jsonResults.toString());
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString(
								   "description"));
            }

        } catch (JSONException e) {
            Log.e("Tracking", "Cannot process JSON results", e);
        }

        return resultList;
    }
    public void add(int sid, double latitude, double longitude, long time, float speed, float accuracy){
		if(this.data != null){
			this.data.add(sid, latitude, longitude, time, speed, accuracy);
		}
	}
	private void requestPath(double fLatitude, double fLongitude, ){
		
	}
	public void addMarker(float lat, float lon, String title){
		this.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(title));
	}
	private List<Polyline> points;
	private PolylineOptions lineOpt;
	private void addLinePoint(LatLng point){
		if(this.lineOpt == null){
			this.lineOpt = new PolylineOptions().width(5).color(Color.GREEN);
		}
		this.lineOpt.add(point);
	}
	private void addLine(){
		if(lineOpt != null){
			if(points == null){
				points = new ArrayList<Polyline>();
			}
			points.add(mMap.addPolyline(lineOpt));
			lineOpt = null;
		}
	}*/
}
