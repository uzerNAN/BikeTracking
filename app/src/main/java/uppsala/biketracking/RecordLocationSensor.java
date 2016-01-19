package uppsala.biketracking;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
//import java.net.*;
//import java.util.*;
//mport org.json.*;

public class RecordLocationSensor extends IntentService
{
	public RecordLocationSensor(){
		super(RecordLocationSensor.class.getName());
	}

	private static Data[] buffer;
	private static int buf_i;
	private static int last_flush;

	static{
		buffer = new Data[10];
		buf_i = 0;
		last_flush = 0;
		for(int i = 0; i < buffer.length; i++){
			buffer[i] = new Data();
		}
		loadSettings();
	}

	private static int last_buf_i(){
		return ( ( buf_i == 0 ) ? last_flush : ( buf_i - 1 ) ) ;
	}

	private static void loadSettings(){
		//boolean load = false;
		File updateLog = new File(C.getSaveDirectory(), C.SETTINGS);
		try{
			if(updateLog.exists()){
				BufferedReader buff = new BufferedReader(new FileReader(updateLog));
				String line;
				String[] splitLine, splitSID, splitLat, splitLon, splitTime;
				line = buff.readLine();
				if(line != null && line.matches(C.MATCH_SYMBOLS)){
					splitLine = line.split("\\|");
					splitSID = splitLine[0].split(C.SPACE);
					splitLat = splitLine[1].split(C.SPACE);
					splitLon = splitLine[2].split(C.SPACE);
					splitTime = splitLine[3].split(C.SPACE);
					if(splitSID[0].equals(C.SID_TXT)
							&& splitLat[0].equals(C.LAT_TXT)
							&& splitLon[0].equals(C.LON_TXT)
							&& splitTime[0].equals(C.TIME_TXT)){
						Data buf = buffer[last_buf_i()];
						buf.set(
								Integer.parseInt(splitSID[1]) ,
								Double.parseDouble(splitLat[1]) ,
								Double.parseDouble(splitLon[1]) ,
								Long.parseLong(splitTime[1]) , 0 , 0, C.EMPTY
						);
						//load = true;
					}
					else{
						Log.i(C.ImportSettings_TXT, C.UNKNOWN_CONTENT_TXT);
					}
				}
				else{
					Log.i(C.ImportSettings_TXT, C.UNKNOWN_LINE_TXT + C.COLON + C.SPACE + line);
				}

				buff.close();

			}
		}
		catch(ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		//Log.i(C.ImportSettings_TXT, "load="+load);
		//return load;
	}

	public static void saveSettings(){
		Data buf = buffer[last_buf_i()];
		C.writeFile(
				C.SETTINGS,
				C.SID_TXT + C.SPACE + buf.getSID() + C.SPLIT
						+ C.LAT_TXT + C.SPACE + buf.getLatitude() + C.SPLIT
						+ C.LON_TXT + C.SPACE + buf.getLongitude() + C.SPLIT
						+ C.TIME_TXT + C.SPACE + buf.getTime(),
				false);
	}

	public static void resetSettings(){
		buf_i = 0;
		last_flush = 0;
		buffer[0].set(0,0,0,0,0,0, C.EMPTY);
		saveSettings();
	}

	@Override
	public void onHandleIntent(Intent intent)
	{
		if (LocationResult.hasResult(intent)) {
			Location p1 = LocationResult.extractResult(intent).getLastLocation();
			if (p1 != null && different_location(p1.getLatitude(), p1.getLongitude())) {
				int sid = get_sid(p1.getTime());
				float speed = get_speed(p1);

				if (buf_i == buffer.length) {
					flush_buffer();
				}
				buffer[buf_i].set(
						sid,
						p1.getLatitude(),
						p1.getLongitude(),
						p1.getTime(),
						speed,
						p1.getAccuracy(), C.EMPTY
				);
				buf_i++;
			}
		}
	}

	public static void flush_buffer(){
		if(buf_i > 0) {
			String flush = C.EMPTY;
			for (int i = 0; i < buf_i; i++) {
				if(buffer[i].getTime() != 0) {
					flush += (buffer[i].toFileString() + C.NEW_LINE);
				}
			}
			C.writeFile(C.RAW_DATA, flush, true);
			last_flush = buf_i-1;
			buf_i = 0;
		}
	}
	private boolean different_location(double latitude, double longitude){
		Data buf = buffer[last_buf_i()];
		return !( Double.compare(buf.getLatitude(), latitude) == 0 && Double.compare(buf.getLongitude(), longitude) == 0);
	}

	private boolean session_timeout(long time){
		long last_time = buffer[last_buf_i()].getTime();
		return ( C.long_diff(last_time, 0) > 0 && C.long_diff(C.long_diff(time, last_time), C.SESSION_TIMEOUT) > 0 );
	}

	private int get_sid(long time){
		int sid = buffer[last_buf_i()].getSID();
		return ( session_timeout(time) ) ? sid + 1 : sid ;
	}

	private float get_speed(Location p1){
		float speed;
		if(!session_timeout(p1.getTime())){
			Location loc = new Location(C.EMPTY);
			Data buf = buffer[last_buf_i()];
			loc.setLatitude(buf.getLatitude());
			loc.setLongitude(buf.getLongitude());
			speed = 1000*p1.distanceTo(loc)/ C.long_diff(p1.getTime(), buf.getTime());
		}
		else{ speed = p1.getSpeed(); }
		return speed;
	}


}
