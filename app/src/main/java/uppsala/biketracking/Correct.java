package uppsala.biketracking;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Sergej on 19/12/2015.
 */

public class Correct implements Runnable
{
    //private static boolean correcting = false;
    private int ssid, spid = -1, counter;
    private double slat, slon, elat, elon;
    private float speed, accuracy;
    private long stime, etime;
    private final CollectedData raw;
    private static Context context = null;
    private String KEY;
    public Correct(Context c){
        raw = new CollectedData();
        context = c;
        KEY = null;
    }
    public static boolean correcting(){
        return context != null;
    }


    private void INIT_KEY(){
        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            KEY = bundle.getString(C.SNAP_API_TXT);
            //Log.i("uppsala.biketracking", KEY);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();  //Do something more useful here!
        }
    }

    private String THE_KEY(){
        if(KEY == null) {
            INIT_KEY();
        }
        return KEY;
    }
    private boolean push(String points){
        boolean result = false;
        if(THE_KEY() != null) {
            try {

                String output = C.EMPTY, line;
                URL url = new URL(C.CORRECT_URL + C.Q + C.path_TXT + C.EQ + points + C.AND + C.interpolate_TXT + C.EQ + C.false_TXT + C.AND + C.key_TXT + C.EQ + THE_KEY());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                //connection.connect();
                InputStream input = connection.getInputStream();
                //Log.i("uppsala.biketracking","points : "+points);
                //Log.i("uppsala.biketracking","input != null : "+(input != null));
                if(input != null) {
                    InputStreamReader reader = new InputStreamReader(input);
                    BufferedReader in = new BufferedReader(reader);
                    while ((line = in.readLine()) != null) {
                        output += line;
                    }

                    in.close();
                    //out.close();
                    //Log.i("uppsala.biketracking","!output.equals(C.JSON_EMPTY) : "+!output.equals(C.JSON_EMPTY));

                    JSONObject out = new JSONObject(output);
                    double lat, lon;
                    //JSONArray point;
                    //Log.i("uppsala.biketracking","output : " + output + "\nJSONString : " + out.toString());
                    if (out.has(C.snappedPoints_TXT)) {
                        JSONArray elems = out.getJSONArray(C.snappedPoints_TXT);

                        for (int i = 0; i < elems.length(); i++) {
                            JSONObject elem = elems.getJSONObject(i);
                            JSONObject obj = elem.getJSONObject(C.location_TXT);

                            lat = obj.getDouble(C.latitude_TXT);
                            lon = obj.getDouble(C.longitude_TXT);
                            Data point = raw
                                    .get(elem.getInt(C.originalIndex_TXT))
                                    .correct(lat, lon, raw.addRoadPlace(elem.getString(C.placeId_TXT), lat, lon));
                            if(spid == -1 || ssid != point.getSID() || spid != point.getPID()) {
                                if(spid != -1) {
                                    raw.addRange(ssid, slat, slon, elat, elon, stime, etime, ( speed/(float)counter ), ( accuracy/(float)counter ), spid);
                                }
                                ssid = point.getSID();
                                spid = point.getPID();
                                slat = point.getLatitude();
                                slon = point.getLongitude();
                                elat = slat;
                                elon = slon;
                                stime = point.getTime();
                                etime = stime;
                                speed = point.getSpeed();
                                accuracy = point.getAccuracy();
                                counter = 1;
                            } else {
                                elat = point.getLatitude();
                                elon = point.getLongitude();
                                etime = point.getTime();
                                speed += point.getSpeed();
                                accuracy += point.getAccuracy();
                                counter++;
                            }
                        }
                    }
                    if (!output.equals(C.JSON_EMPTY)) {
                        C.writeFile(C.CORRECTED_ROADS, raw.roadsToFileString(), false);
                        C.writeFile(C.CORRECTED_PLACES, raw.placesToFileString(), false);
                        C.writeFile(C.CORRECTED_RANGES, raw.rangesToFileString(), true);
                    } //else {
                     // RecordLocationSensor.resetSettings();
                    //}
                    raw.resetData();
                    result = true;
                }
            } catch (Exception e) {
                //Log.e("uppsala.biketracking", "EXCEPTION");
                e.printStackTrace();
            }
        }
        return result;
    }

    public void run() {
        boolean result = false;
        if (!Upload.uploading()){
            String input = C.EMPTY, line;
            raw.importRoadFile(C.CORRECTED_ROADS);
            raw.importPlaceFile(C.CORRECTED_PLACES);
            try {
                File updateLog = new File(C.getSaveDirectory(), C.TEMPORARY_DATA);
                while (updateLog.exists() && result) {

                    BufferedReader buf = new BufferedReader(new FileReader(updateLog));
                    String[] splitLine, splitSID, splitLatitude, splitLongitude, splitTime, splitSpeed, splitAccuracy;
                    while ((line = buf.readLine()) != null && result) {
                        if (!line.equals(C.EMPTY) && line.matches(C.MATCH_SYMBOLS)) {
                            splitLine = line.split("\\|");
                            splitSID = splitLine[0].split(C.SPACE);
                            splitLatitude = splitLine[1].split(C.SPACE);
                            splitLongitude = splitLine[2].split(C.SPACE);
                            splitTime = splitLine[3].split(C.SPACE);
                            splitSpeed = splitLine[4].split(C.SPACE);
                            splitAccuracy = splitLine[5].split(C.SPACE);
                            if (
                                    splitSID[0].equals(C.SID_TXT)
                                            && splitLatitude[0].equals(C.LAT_TXT)
                                            && splitLongitude[0].equals(C.LON_TXT)
                                            && splitTime[0].equals(C.TIME_TXT)
                                            && splitSpeed[0].equals(C.SPEED_TXT)
                                            && splitAccuracy[0].equals(C.ACCURACY_TXT)) {
                                raw.add(Integer.parseInt(splitSID[1]), Double.parseDouble(splitLatitude[1]), Double.parseDouble(splitLongitude[1]), Long.parseLong(splitTime[1]), Float.parseFloat(splitSpeed[1]), Float.parseFloat(splitAccuracy[1]));
                                input += ((!input.equals(C.EMPTY)) ? C.SPLIT : C.EMPTY) + splitLatitude[1] + C.COMMA + splitLongitude[1];
                            }
                        }

                        if (raw.size() > 99) {
                            result = push(input);
                            input = C.EMPTY;
                        }
                    }
                    buf.close();
                    if (!input.equals(C.EMPTY)) {
                        result = push(input);
                    }
                    if (result) {
                        updateLog.delete();
                    }
                    C.appendFileTo(C.WAITING_TEMPORARY_DATA, C.TEMPORARY_DATA);
                    if (result) {
                        updateLog = new File(C.getSaveDirectory(), C.TEMPORARY_DATA);
                    }
                }
                if (spid != -1) {
                    raw.addRange(ssid, slat, slon, elat, elon, stime, etime, (speed / (float) counter), (accuracy / (float) counter), spid);
                }
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        context.startService(new Intent(context, ApiService.class).setAction(C.CORRECT_TXT).putExtra(C.SUCCESS_TXT, result));
        context = null;
    }
}