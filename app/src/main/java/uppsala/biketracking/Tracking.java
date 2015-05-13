package uppsala.biketracking;

import android.content.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.support.v4.app.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

//import android.R;


public class Tracking extends FragmentActivity
{
	//private PendingIntent penInt;
	//private ActivityRecognitionApi recApi;
	//private FusedLocationProviderApi locPro;
	//private LocationRequest locReq;
	public static Tracking mainActivity = null;

	
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private Marker mMarker;
	private float mZoom;
	//private boolean mIsbound = false;
	//private Intent intent;
	//private GoogleApiClient mClient;
	//private TextView latitude, longtitude;
	private LinearLayout coordinates;
	private String aName = "NO ACTIVITY";
	private int aType = -1, aConfidence = 100;
	private Bitmap icon;
	private CollectedData data = null;
	//private AutoCompleteTextView autoCompView;
	//@Override
	//protected void onDestroy
	
	/*public void resetUpdate(){
		if(this.data != null){
			this.data.resetData();
		}
	}*/
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Intent i = new Intent(this, WakefulService.class);
		this.data = new CollectedData(this);
		if(WakefulService.mainService == null){
			i.setAction("SERVICE_START");
			this.startService(i);
		}
		//updating our titlebar
		final boolean customTitleSupported = this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		this.mZoom = 20;
        this.setContentView(R.layout.activity_tracking);
		
		if(customTitleSupported) {
			this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}
		
		/*this.autoCompView = (AutoCompleteTextView) this.findViewById(R.id.editloc);
        this.autoCompView.setAdapter(new PlacesAutoCompleteAdapter(this,
															  R.layout.list_item));*/
															  
		this.coordinates = (LinearLayout) this.findViewById(R.id.coordinates);
		this.icon = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);

		this.icon.setPixel(0, 0, Color.argb(Color.alpha(R.color.transparent), Color.red(R.color.transparent), Color.green(R.color.transparent), Color.blue(R.color.transparent)));
        this.setUpMapIfNeeded();
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
    }*/
/*
	@Override
	protected void onDestroy()
	{
		// TODO: Implement this method
		//mClient.disconnect();
		//penInt.cancel();
		super.onDestroy();
	}
	
	

	@Override
	protected void onStart()
	{
		// TODO: Implement this method
		super.onStart();
		mClient.connect();
		//while(!mClient.isConnected()){}
		
	}*/

    @Override
    protected void onResume() {
        super.onResume();
		this.mainActivity = this;
        this.setUpMapIfNeeded();
    }
	
	@Override
	protected void onPause(){
		super.onPause();
		this.mainActivity = null;
	}
	public void add(int sid, double latitude, double longitude, long time, float speed, float accuracy){
		if(this.data != null){
			this.data.add(sid, latitude, longitude, time, speed, accuracy);
		}
	}
	public void update(int type, String name, int confidence){
		if(!(type == this.aType && confidence == this.aConfidence)){
			this.aType = type; this.aName = name; this.aConfidence = confidence;
			this.updateMarkerTitle(this.aName+" ["+this.aConfidence+"%]");
		}
	}
	
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
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

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
		this.mMap.setMyLocationEnabled(true);
		this.mMap.animateCamera(CameraUpdateFactory.zoomTo(this.mZoom));
		this.mMap.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener(){
			@Override
			public void onCameraChange(CameraPosition pos){
				Tracking.this.mZoom = pos.zoom;
			}
		});
		
    	this.mMap.setOnMyLocationChangeListener( new GoogleMap.OnMyLocationChangeListener(){
			@Override
			public void onMyLocationChange(Location arg0){
				Tracking.this.changePosition(arg0.getLatitude(), arg0.getLongitude());
			}
		});
	}
	private void updateMarkerTitle(String title){
		if(this.mMarker!=null){
			this.mMarker.setTitle(title);
			this.mMarker.showInfoWindow();
		}
	}
	public void addMarker(float lat, float lon, String title){
		this.mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)).title(title));
	}
	public void changePosition(double latitude, double longitude){
		if(this.mMarker != null){
			this.mMarker.remove();
		}
		LatLng latLng = new LatLng(latitude, longitude);
		if(this.mMap != null){
			this.mMarker = this.mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon)));
			this.updateMarkerTitle(this.aName+" ["+this.aConfidence+"%]");
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
	/*private void requestPath(double fLatitude, double fLongitude, ){
		
	}*/
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
	}
}
