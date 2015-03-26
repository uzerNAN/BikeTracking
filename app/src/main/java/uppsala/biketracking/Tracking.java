package uppsala.biketracking;

import android.graphics.*;
import android.location.*;
import android.os.*;
import android.support.v4.app.*;
import android.view.animation.*;
import android.widget.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.graphics.Interpolator;


public class Tracking extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
	private Marker now;
	private float zoom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		zoom = 20;
        setContentView(R.layout.activity_tracking);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
				if(now != null){
					now.remove();
				}

				// Getting latitude of the current location
				//double latitude = location.getLatitude();

				// Getting longitude of the current location
				//double longitude = location.getLongitude();

				// Creating a LatLng object for the current location
				LatLng latLng = new LatLng(arg0.getLatitude(), arg0.getLongitude());
				now = mMap.addMarker(new MarkerOptions().position(latLng));
				
				// Showing the current location in Google Map
				mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

				// Zoom in the Google Map
				mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom));
				
				Toast.makeText(getApplicationContext(),"LATITUDE "+arg0.getLatitude()+" :: LONGTITUDE "+arg0.getLongitude()+" :: ZOOM "+zoom, Toast.LENGTH_SHORT).show();
			}
			
		});
	}
	
	
}
