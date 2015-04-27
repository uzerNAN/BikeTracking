package uppsala.biketracking;

import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.location.*;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.google.android.gms.common.*;
import com.google.android.gms.common.api.*;
import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.view.animation.Interpolator;
import java.util.*;

//import android.R;


public class Tracking extends FragmentActivity
{
	//private PendingIntent penInt;
	//private ActivityRecognitionApi recApi;
	//private FusedLocationProviderApi locPro;
	//private LocationRequest locReq;
	public static Tracking mainActivity = null;

	
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private Marker now;
	private float zoom;
	//private boolean mIsbound = false;
	//private Intent intent;
	//private GoogleApiClient mClient;
	//private TextView latitude, longtitude;
	private LinearLayout coordinates;
	private String aName = "NO ACTIVITY";
	private int aType = -1, aConfidence = 100;
	private Bitmap icon;
	private CollectedData data;
	
	//@Override
	//protected void onDestroy
	
	public void resetUpdate(){
		if(this.data != null){
			this.data.resetUpdate();
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//intent = new Intent(this.getApplicationContext(), WakefulReceiver.class);
		Intent i = new Intent(this, WakefulService.class);
		data = new CollectedData(this);
		i.setAction("SERVICE_START");
		startService(i);
		
		//updating our titlebar
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		//latitude = (TextView) findViewById(R.id.latitude);
		//longtitude = (TextView) findViewById(R.id.longtitude);
		
		//activity = new ActivityRecognitionIS();
		//startService(intent);
		//activity.
		//intent.putExtra("name", "FIRST PACKET");
		//intent.putExtra("confidence", 100);
		//intent.putExtra("type", 0);
		
		//super.onCreate(savedInstanceState);
		
		zoom = 20;
        setContentView(R.layout.activity_tracking);
		
		if(customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
		}
		//icon =  findViewById(R.drawable.icon_transparent);
		//BitmapDescriptor icon_desc = icon.getDrawingCache();
		coordinates = (LinearLayout) findViewById(R.id.coordinates);
		icon = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
		//a = Color.alpha(R.color.transparent);
		//r = Color.red(R.color.transparent);
		//g = Color.green(R.color.transparent);
		//b = Color.blue(R.color.transparent);

		icon.setPixel(0, 0, Color.argb(Color.alpha(R.color.transparent), Color.red(R.color.transparent), Color.green(R.color.transparent), Color.blue(R.color.transparent)));
		//((TextView)coordinates.getChildAt(0)).setTextColor(Color.WHITE);
		//((TextView)coordinates.getChildAt(0)).setBackgroundColor(Color.BLACK);
		//((TextView)coordinates.getChildAt(1)).setTextColor(Color.WHITE);
		//((TextView)coordinates.getChildAt(1)).setBackgroundColor(Color.BLACK);
        setUpMapIfNeeded();
		//sendBroadcast(intent);//new Intent(this, WakefulReceiver.class));
		
    }
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
		//registerReceiver(receiver, new IntentFilter("uppsala.biketracking"));
		//requestUpdates();
		mainActivity = this;
		//doBindService();
        setUpMapIfNeeded();
    }
	
	@Override
	protected void onPause(){
		//doUnbindService();
		//removeUpdates();
		super.onPause();
		mainActivity = null;
		//unregisterReceiver(receiver);
	}
	
	//CollectedData data == null;
	
	public void update(int aType, String aName, int c){
		if(!(aType == this.aType && c == this.aConfidence)){
			this.aType = aType; this.aName = aName; this.aConfidence = c;
			//changePosition(now.getPosition().latitude, now.getPosition().longitude);
			updateTitle();
			
			//Toast.makeText(this.getApplicationContext(), aType+" : "+aName+" | "+c+"%", Toast.LENGTH_LONG).show();
		}
		if(!this.data.uploading() && !this.data.updateIsEmpty()){
			this.data.uploadData();
		}
		//else{
		//	Toast.makeText(this, "Unable to upload: uploading="+this.data.uploading()+", updateIsEmpty="+this.data.updateIsEmpty(), Toast.LENGTH_SHORT).show();
		//}
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
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
			else {
				Toast.makeText(getApplicationContext(),"Sorry! Unable to create maps", Toast.LENGTH_LONG).show();
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
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
		
		mMap.setMyLocationEnabled(true);
		
		mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
		
		mMap.setOnCameraChangeListener( new GoogleMap.OnCameraChangeListener(){
			
			@Override
			public void onCameraChange(CameraPosition pos){
				zoom = pos.zoom;
			}
			
		});
		
    	mMap.setOnMyLocationChangeListener( new GoogleMap.OnMyLocationChangeListener(){
			
			@Override
			public void onMyLocationChange(Location arg0){
				//mMap.get.addMarker( new MarkerOptions().position( new LatLng(arg0.getLatitude(), arg0.getLongitude())).title("YOU ARE HERE"));
				changePosition(arg0.getLatitude(), arg0.getLongitude());
				//Toast.makeText(getApplicationContext(),"LATITUDE "+arg0.getLatitude()+" :: LONGTITUDE "+arg0.getLongitude()+" \r\nZOOM "+zoom, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	private void updateTitle(){
		if(now!=null){
			now.setTitle(this.aName+" ["+aConfidence+"%]");
			now.showInfoWindow();
		}
	}
	
	/*public void animateMarker(final Marker marker, final LatLng toPosition,
							  final boolean hideMarker) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = mMap.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;
		final Interpolator interpolator = new LinearInterpolator();
		handler.post(new Runnable() {
				@Override
				public void run() {
					long elapsed = SystemClock.uptimeMillis() - start;
					float t = interpolator.getInterpolation((float) elapsed
															/ duration);
					double lng = t * toPosition.longitude + (1 - t)
						* startLatLng.longitude;
					double lat = t * toPosition.latitude + (1 - t)
						* startLatLng.latitude;
					marker.setPosition(new LatLng(lat, lng));
					if (t < 1.0) {
						// Post again 16ms later.
						handler.postDelayed(this, 16);
					} else {
						if (hideMarker) {
							marker.setVisible(false);
						} else {
							marker.setVisible(true);
						}
					}
				}
			});
	}*/
	
	//CollectedData data = null;
	
	public void changePosition(double latitude, double longitude){
		
		/*:::*/
		//if(data == null){
		//	data = new CollectedData();
		//}
		//if(data != null){
		//if(data.getTemp() != ""){
		//	Toast.makeText(this, data.getTemp(), Toast.LENGTH_LONG).show();
		//}
		//}
		/*:::*/
		
		if(now != null){
			//now.setDraggable(true);
			//animateMarker(now, new LatLng(latitude, longitude), true);
			/*while(latitude != lat && longitude != lon){
				if(latitude > lat){
					
				}
				else if(latitude < lat){
					lat 
				}
				if(longitude > lon){
					
				}
				else if(longitude < lon){
					
				}
				now.setPosition(new LatLng( latitude, longitude));
			}*/
			now.remove();
		}
		// Getting latitude of the current location
		//double latitude = location.getLatitude();

		// Getting longitude of the current location
		//double longitude = location.getLongitude();

		// Creating a LatLng object for the current location latitude

		LatLng latLng = new LatLng(latitude, longitude);
		//MarkerOptions marker = ;
		//marker.se
		//marker.icon(BitmapDescriptorFactory.fromResource(R. .house_flag))
		//	.anchor(0.0f, 1.0f);
		if(mMap != null){
			now = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(icon)));
			updateTitle();
			//now.showInfoWindow();
			// Showing the current location in Google Map
			mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

			// Zoom in the Google Map
			mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
		}
		if(coordinates != null) {
			((TextView) coordinates.getChildAt(0)).setText("LATITUDE "+latitude);
			((TextView) coordinates.getChildAt(1)).setText("LONGTITUDE "+longitude);
		}
		if(lineOp != null){
			if(lineOp.getPoints().size() >= lineSize){
				addLine();
			}
		}
		addLinePoint(latLng);
	}
	private List<Polyline> points;
	private PolylineOptions lineOp;
	private final static int lineSize = 5;
	private void addLinePoint(LatLng point){
		if(lineOp == null){
			lineOp = new PolylineOptions().width(5).color(Color.GREEN);
		}
		lineOp.add(point);
	}
	private void addLine(){
		if(lineOp != null){
			if(points == null){
				points = new ArrayList<Polyline>();
			}
			points.add(mMap.addPolyline(lineOp));
			lineOp = null;
		}
	}
	
	
}
