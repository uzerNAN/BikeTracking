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
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

//import android.R;


public class MainActivity extends FragmentActivity
{
	public static boolean active = false;

	
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private Marker mMarker;
	private float mZoom;
	private LinearLayout coordinates;
	private String aName = "NO ACTIVITY";
	private Bitmap icon;
	//private CollectedData data;
    private BroadcastReceiver receiver;
    private IntentFilter broadcast_filter;
	//private AutoCompleteTextView autoCompView;

	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		/******************/
        startRecordLocation();
		/******************/

        final boolean customTitleSupported = this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

        this.mZoom = 20;
        this.setContentView(R.layout.activity_tracking);

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
                        case ("DATA_UPLOADED"):
                            Toast.makeText(context, "Data was successfully uploaded to server", Toast.LENGTH_LONG).show();
                            break;
							
                        case ("UPLOAD_ERROR"):
                            Toast.makeText(context, "UNABLE TO UPLOAD DATA", Toast.LENGTH_LONG).show();
                            break;

                        case ("FILE_ERROR"):
                            Toast.makeText(context, "UNABLE TO WRITE IN A FILE", Toast.LENGTH_LONG).show();
                            break;

                        case ("RECOGNITION_DATA"):
                            MainActivity.this.updateActivity(intent.getExtras().getString("ACTIVITY_NAME"));
                            break;
						case ("LOCATION_DATA") :
							Bundle extras = intent.getExtras();
							MainActivity.this.changePosition(extras.getDouble("LATITUDE"), extras.getDouble("LONGITUDE"));
							break;
                        default:
                            break;
                    }
                }
            }
        };
        this.broadcast_filter = new IntentFilter();
        this.broadcast_filter.addAction("DATA_UPLOADED");
        this.broadcast_filter.addAction("UPLOAD_ERROR");
        this.broadcast_filter.addAction("FILE_ERROR");
        this.broadcast_filter.addAction("RECOGNITION_DATA");
        this.broadcast_filter.addAction("LOCATION_DATA");
		if(!ApiService.ar_is_active){
			startActivityRecognition();
		}
    }

	@Override
	protected void onStop()
	{
		// TODO: Implement this method
		super.onStop();
	}
	
	private void startActivityRecognition(){
		if(!ApiService.ar_is_active){
			this.startService((new Intent(this, ApiService.class).setAction("START_AR")));
		}
	}
	
	private void stopActivityRecognition(){
		if(ApiService.ar_is_active){
			this.startService((new Intent(this, ApiService.class).setAction("STOP_AR")));
		}
	}

	private void startLocationUpdates(){
		if(!ApiService.lu_is_active){
			this.startService(new Intent(this, ApiService.class).setAction("START_LU"));
		}
	}

	private void stopLocationUpdates(){
		if(ApiService.lu_is_active){
			this.startService((new Intent(this, ApiService.class).setAction("STOP_LU")));
		}
	}

	private void startRecordLocation(){
		if(!ApiService.rl_is_active){
			this.startService(new Intent(this, ApiService.class).setAction("START_RL"));
		}
	}

	private void stopRecordLocation(){
		if(ApiService.rl_is_active){
			this.startService((new Intent(this, ApiService.class).setAction("STOP_RL")));
		}
	}

    @Override
    protected void onResume() {
        super.onResume();
        this.registerReceiver(this.receiver, this.broadcast_filter);
		active = true;
        this.setUpMapIfNeeded();
		this.startLocationUpdates();
    }
	
	@Override
	protected void onPause(){
		super.onPause();
        this.unregisterReceiver(this.receiver);
		active = false;
		this.stopLocationUpdates();
	}
	
	public void updateActivity(String name){
		if(!this.aName.equals(name)){
			this.aName = name;
			this.updateMarkerTitle("["+this.aName+"]");
		}
	}

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (this.mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            this.mMap = ((SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (this.mMap != null) {
                this.setUpMap();
            }
			else {
				Toast.makeText(this.getApplicationContext(),"Sorry! Unable to create maps", Toast.LENGTH_LONG).show();
			}
        }
    }
    private void setUpMap() {
		this.mMap.setMyLocationEnabled(true);
		this.mMap.animateCamera(CameraUpdateFactory.zoomTo(this.mZoom));
		this.mMap.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener(){
			@Override
			public void onCameraChange(CameraPosition pos){
				MainActivity.this.mZoom = pos.zoom;
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
		if(this.mMarker != null){
			this.mMarker.remove();
		}
		LatLng latLng = new LatLng(latitude, longitude);
		if(this.mMap != null){
			this.mMarker = this.mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon)));
			//this.mMarker.setVisible(true);
			this.updateActivity(this.aName);
			// Showing the current location in Google Map
			this.mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
			// Zoom in the Google Map
			this.mMap.animateCamera(CameraUpdateFactory.zoomTo(this.mZoom));
		}
		if(this.coordinates != null) {
			((TextView) this.coordinates.getChildAt(0)).setText("LATITUDE "+latitude);
			((TextView) this.coordinates.getChildAt(1)).setText("LONGITUDE "+longitude);
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
